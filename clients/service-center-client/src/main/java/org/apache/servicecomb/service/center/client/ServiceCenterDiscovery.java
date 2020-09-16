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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicecomb.http.client.task.AbstractTask;
import org.apache.servicecomb.http.client.task.Task;
import org.apache.servicecomb.service.center.client.DiscoveryEvents.InstanceChangedEvent;
import org.apache.servicecomb.service.center.client.model.FindMicroserviceInstancesResponse;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

public class ServiceCenterDiscovery extends AbstractTask {
  private static final String ALL_VERSION = "0+";

  private static final long POLL_INTERVAL = 15000;

  private boolean started = false;

  class SubscriptionKey {
    final String appId;

    final String serviceName;

    SubscriptionKey(String appId, String serviceName) {
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

  class SubscriptionValue {
    String revision;

    List<MicroserviceInstance> instancesCache;
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCenterRegistration.class);

  private final ServiceCenterClient serviceCenterClient;

  private final EventBus eventBus;

  private Microservice myself;

  private final Map<SubscriptionKey, SubscriptionValue> instancesCache = new ConcurrentHashMap<>();

  public ServiceCenterDiscovery(ServiceCenterClient serviceCenterClient, EventBus eventBus) {
    super("service-center-discovery-task");
    this.serviceCenterClient = serviceCenterClient;
    this.eventBus = eventBus;
  }

  public void updateMySelf(Microservice myself) {
    this.myself = myself;
  }

  public void startDiscovery() {
    if (!started) {
      started = true;
      startTask(new PullInstanceTask());
    }
  }

  public void register(Microservice microservice) {
    SubscriptionKey subscriptionKey = new SubscriptionKey(microservice.getAppId(), microservice.getServiceName());
    this.instancesCache.computeIfAbsent(subscriptionKey, (key) -> new SubscriptionValue());
  }

  class PullInstanceTask implements Task {
    @Override
    public void execute() {
      instancesCache.forEach((k, v) -> {
        try {
          FindMicroserviceInstancesResponse instancesResponse = serviceCenterClient
              .findMicroserviceInstance(myself.getServiceId(), k.appId, k.serviceName, ALL_VERSION, v.revision);
          if (instancesResponse.isModified()) {
            // java chassis 实现了空实例保护，这里暂时不实现。
            LOGGER.info("Instance changed event, "
                    + "current: revision={}, instances={}; "
                    + "origin: revision={}, instances={}; "
                    + "appId={}, serviceName={}",
                instancesResponse.getRevision(),
                instanceToString(instancesResponse.getMicroserviceInstancesResponse().getInstances()),
                v.revision,
                instanceToString(v.instancesCache),
                k.appId,
                k.serviceName
            );
            v.instancesCache = instancesResponse.getMicroserviceInstancesResponse().getInstances();
            v.revision = instancesResponse.getRevision();
            eventBus.post(new InstanceChangedEvent(k.appId, k.serviceName,
                v.instancesCache));
          }
        } catch (Exception e) {
          LOGGER.error("find service instance failed.", e);
        }
      });

      startTask(new BackOffSleepTask(POLL_INTERVAL, new PullInstanceTask()));
    }

    private String instanceToString(List<MicroserviceInstance> instances) {
      if (instances == null) {
        return "";
      }

      StringBuilder sb = new StringBuilder();
      for (MicroserviceInstance instance : instances) {
        for (String endpoint : instance.getEndpoints()) {
          sb.append(endpoint.length() > 64 ? endpoint.substring(0, 64) : endpoint);
          sb.append("|");
        }
      }
      sb.append("#");
      return sb.toString();
    }
  }
}
