/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.service.center.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicecomb.http.client.task.AbstractTask;
import org.apache.servicecomb.http.client.task.Task;
import org.apache.servicecomb.service.center.client.DiscoveryEvents.InstanceChangedEvent;
import org.apache.servicecomb.service.center.client.DiscoveryEvents.PullInstanceEvent;
import org.apache.servicecomb.service.center.client.model.FindMicroserviceInstancesResponse;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class ServiceCenterDiscovery extends AbstractTask {

  public static final int MAX_INTERVAL = 600000;

  public static final int MIN_INTERVAL = 1000;

  private static final String ALL_VERSION = "0+";

  private static volatile boolean pullInstanceTaskOnceInProgress = false;

  public static class SubscriptionKey {
    final String appId;

    final String serviceName;

    public SubscriptionKey(String appId, String serviceName) {
      this.appId = appId;
      this.serviceName = serviceName;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      SubscriptionKey that = (SubscriptionKey) o;
      return appId.equals(that.appId) &&
          serviceName.equals(that.serviceName);
    }

    @Override
    public int hashCode() {
      return Objects.hash(appId, serviceName);
    }
  }

  public static class SubscriptionValue {
    String revision;

    List<MicroserviceInstance> instancesCache;
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCenterDiscovery.class);

  private final ServiceCenterClient serviceCenterClient;

  private final EventBus eventBus;

  private String myselfServiceId;

  private final Map<SubscriptionKey, SubscriptionValue> instancesCache = new ConcurrentHashMap<>();

  private final Map<String, Microservice> microserviceCache = new ConcurrentHashMap<>();

  private long pollInterval = 15000;

  private boolean started = false;

  private final Object lock = new Object();

  public ServiceCenterDiscovery(ServiceCenterClient serviceCenterClient, EventBus eventBus) {
    super("service-center-discovery-task");
    this.serviceCenterClient = serviceCenterClient;
    this.eventBus = eventBus;
    this.eventBus.register(this);
  }

  public ServiceCenterDiscovery setPollInterval(long interval) {
    if (interval > MAX_INTERVAL || interval < MIN_INTERVAL) {
      return this;
    }
    this.pollInterval = interval;
    return this;
  }

  public void updateMyselfServiceId(String myselfServiceId) {
    this.myselfServiceId = myselfServiceId;
  }

  public void startDiscovery() {
    if (!started) {
      started = true;
      startTask(new PullInstanceTask());
    }
  }

  public void registerIfNotPresent(SubscriptionKey subscriptionKey) {
    if (this.instancesCache.get(subscriptionKey) == null) {
      synchronized (lock) {
        if (this.instancesCache.get(subscriptionKey) == null) {
          SubscriptionValue value = new SubscriptionValue();
          pullInstance(subscriptionKey, value, false);
          this.instancesCache.put(subscriptionKey, value);
        }
      }
    }
  }

  public List<MicroserviceInstance> getInstanceCache(SubscriptionKey key) {
    return this.instancesCache.get(key).instancesCache;
  }

  @Subscribe
  public void onPullInstanceEvent(PullInstanceEvent event) {
    // to avoid too many pulls queued.
    if (pullInstanceTaskOnceInProgress) {
      return;
    }
    pullInstanceTaskOnceInProgress = true;
    startTask(new PullInstanceOnceTask());
  }

  private List<SubscriptionKey> pullInstance(SubscriptionKey k, SubscriptionValue v, boolean sendChangedEvent) {
    if (myselfServiceId == null) {
      // registration not ready
      return Collections.emptyList();
    }

    List<SubscriptionKey> failedKeys = new ArrayList<>();
    try {
      FindMicroserviceInstancesResponse instancesResponse = serviceCenterClient
          .findMicroserviceInstance(myselfServiceId, k.appId, k.serviceName, ALL_VERSION, v.revision);
      if (instancesResponse.isModified()) {
        List<MicroserviceInstance> instances = instancesResponse.getMicroserviceInstancesResponse().getInstances()
            == null ? Collections.emptyList() : instancesResponse.getMicroserviceInstancesResponse().getInstances();
        setMicroserviceInfo(instances);
        LOGGER.info("Instance changed event, "
                + "current: revision={}, instances={}; "
                + "origin: revision={}, instances={}; "
                + "appId={}, serviceName={}",
            instancesResponse.getRevision(),
            instanceToString(instances),
            v.revision,
            instanceToString(v.instancesCache),
            k.appId,
            k.serviceName
        );
        v.instancesCache = instances;
        v.revision = instancesResponse.getRevision();
        if (sendChangedEvent) {
          eventBus.post(new InstanceChangedEvent(k.appId, k.serviceName,
              v.instancesCache));
        }
      }
    } catch (Exception e) {
      LOGGER.error("find service {}#{} instance failed.", k.appId, k.serviceName, e);
      if (!(e.getCause() instanceof IOException)) {
        // for IOException, do not remove cache, or when service center
        // not available, invocation between microservices will fail.
        failedKeys.add(k);
      }
    }
    return failedKeys;
  }

  private void setMicroserviceInfo(List<MicroserviceInstance> instances) {
    instances.forEach(instance -> {
      Microservice microservice = microserviceCache
          .computeIfAbsent(instance.getServiceId(), id -> {
            try {
              return serviceCenterClient.getMicroserviceByServiceId(id);
            } catch (Exception e) {
              LOGGER.error("Find microservice by id={} failed", id, e);
              throw e;
            }
          });
      instance.setMicroservice(microservice);
    });
  }

  class PullInstanceTask implements Task {
    @Override
    public void execute() {
      pullAllInstance();

      startTask(new BackOffSleepTask(pollInterval, new PullInstanceTask()));
    }
  }

  class PullInstanceOnceTask implements Task {
    @Override
    public void execute() {
      try {
        pullAllInstance();
      } finally {
        pullInstanceTaskOnceInProgress = false;
      }
    }
  }

  private synchronized void pullAllInstance() {
    List<SubscriptionKey> failedInstances = new ArrayList<>();
    instancesCache.forEach((k, v) -> failedInstances.addAll(pullInstance(k, v, true)));
    if (failedInstances.isEmpty()) {
      return;
    }
    failedInstances.forEach(instancesCache::remove);
    failedInstances.clear();
  }

  private static String instanceToString(List<MicroserviceInstance> instances) {
    if (instances == null) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    for (MicroserviceInstance instance : instances) {
      for (String endpoint : instance.getEndpoints()) {
        sb.append(endpoint.length() > 64 ? endpoint.substring(0, 64) : endpoint);
        sb.append("|");
      }
      sb.append(instance.getServiceName());
      sb.append("|");
    }
    sb.append("#");
    return sb.toString();
  }
}
