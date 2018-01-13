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

package org.apache.servicecomb.serviceregistry.consumer;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.cache.InstanceCache;
import org.apache.servicecomb.serviceregistry.version.VersionRule;
import org.apache.servicecomb.serviceregistry.version.VersionRuleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicroserviceVersionRule {
  private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceVersionRule.class);

  private final String appId;

  private final String microserviceName;

  private final VersionRule versionRule;

  private MicroserviceVersion latestVersion;

  // key is microserviceId
  private Map<String, MicroserviceVersion> versions = new ConcurrentHashMap<>();

  // key is instanceId
  private Map<String, MicroserviceInstance> instances = Collections.emptyMap();

  private InstanceCache instanceCache;

  private VersionedCache versionedCache;

  public MicroserviceVersionRule(String appId, String microserviceName, String strVersionRule) {
    this.appId = appId;
    this.microserviceName = microserviceName;
    this.versionRule = VersionRuleUtils.getOrCreate(strVersionRule);

    resetInstanceCache();
  }

  private void resetInstanceCache() {
    instanceCache = new InstanceCache(appId, microserviceName, versionRule.getVersionRule(), instances);
    versionedCache = new VersionedCache()
        .name(versionRule.getVersionRule())
        .autoCacheVersion()
        .data(instances);
  }

  public void addMicroserviceVersion(MicroserviceVersion microserviceVersion) {
    if (!versionRule.isAccept(microserviceVersion.getVersion())) {
      return;
    }

    versions.put(microserviceVersion.getMicroservice().getServiceId(), microserviceVersion);
    resetLatestVersion();

    LOGGER.info("add microserviceVersion, appId={}, microserviceName={}, version={}, versionRule={}.",
        microserviceVersion.getMicroservice().getAppId(),
        microserviceVersion.getMicroservice().getServiceName(),
        microserviceVersion.getVersion().getVersion(),
        versionRule.getVersionRule());
  }

  public void deleteMicroserviceVersion(MicroserviceVersion microserviceVersion) {
    if (!versionRule.isAccept(microserviceVersion.getVersion())) {
      return;
    }

    if (versions.remove(microserviceVersion.getMicroservice().getServiceId()) == null) {
      return;
    }

    resetLatestVersion();
    LOGGER.info("delete microserviceVersion, appId={}, microserviceName={}, version={}, versionRule={}.",
        microserviceVersion.getMicroservice().getAppId(),
        microserviceVersion.getMicroservice().getServiceName(),
        microserviceVersion.getVersion().getVersion(),
        versionRule.getVersionRule());
  }

  protected void resetLatestVersion() {
    latestVersion = null;
    if (versions.isEmpty()) {
      return;
    }

    latestVersion = versions.values().stream().sorted((v1, v2) -> {
      return v2.version.compareTo(v1.version);
    }).findFirst().get();
  }

  public VersionRule getVersionRule() {
    return versionRule;
  }

  @SuppressWarnings("unchecked")
  public <T extends MicroserviceVersion> T getLatestMicroserviceVersion() {
    return (T) latestVersion;
  }

  public Map<String, MicroserviceInstance> getInstances() {
    return instances;
  }

  public InstanceCache getInstanceCache() {
    return instanceCache;
  }

  public VersionedCache getVersionedCache() {
    return versionedCache;
  }

  public void setInstances(Collection<MicroserviceInstance> newInstances) {
    if (newInstances == null) {
      return;
    }

    Map<String, MicroserviceInstance> tmpInstances = newInstances.stream().filter(instance -> {
      MicroserviceVersion microserviceVersion = versions.get(instance.getServiceId());
      boolean isMatch = microserviceVersion != null
          && versionRule.isMatch(microserviceVersion.getVersion(), latestVersion.getVersion());
      if (isMatch) {
        LOGGER.info("set instances, appId={}, microserviceName={}, versionRule={}, instanceId={}, endpoints={}.",
            appId,
            microserviceName,
            versionRule.getVersionRule(),
            instance.getInstanceId(),
            instance.getEndpoints());
      }
      return isMatch;
    }).collect(Collectors.toMap(MicroserviceInstance::getInstanceId, Function.identity()));
    instances = Collections.unmodifiableMap(tmpInstances);

    resetInstanceCache();
  }
}
