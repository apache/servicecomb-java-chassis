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
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.foundation.common.utils.StringBuilderUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Framework;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.cache.InstanceCache;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.apache.servicecomb.serviceregistry.version.VersionRule;
import org.apache.servicecomb.serviceregistry.version.VersionRuleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicroserviceVersionRule {
  private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceVersionRule.class);

  private final String appId;

  private final String microserviceName;

  private final VersionRule versionRule;

  // wrap variable data to make them atomic
  private MicroserviceVersionRuleData data;

  public MicroserviceVersionRule(String appId, String microserviceName, String strVersionRule) {
    this.appId = appId;
    this.microserviceName = microserviceName;
    this.versionRule = VersionRuleUtils.getOrCreate(strVersionRule);

    data = createEmptyData();
  }

  public String getAppId() {
    return appId;
  }

  public String getMicroserviceName() {
    return microserviceName;
  }

  public VersionRule getVersionRule() {
    return versionRule;
  }

  public <T extends MicroserviceVersion> T getLatestMicroserviceVersion() {
    return data.getLatestMicroserviceVersion();
  }

  public Map<String, MicroserviceInstance> getInstances() {
    return data.instances;
  }

  public InstanceCache getInstanceCache() {
    return data.instanceCache;
  }

  public VersionedCache getVersionedCache() {
    return data.versionedCache;
  }

  public MicroserviceVersionRuleData getData() {
    return data;
  }

  /**
   *
   * @param allVersions all versions even not belongs to this rule
   * @param allInstances all instances even not belongs to this rule, related MicroserviceVersion always exists
   */
  public void update(Map<String, MicroserviceVersion> allVersions, Collection<MicroserviceInstance> allInstances) {
    if (allInstances == null) {
      allInstances = Collections.emptyList();
    }

    if (DefinitionConst.VERSION_RULE_LATEST.equals(versionRule.getVersionRule())) {
      data = createDataByLatestRule(allVersions, allInstances);
    } else {
      data = createDataByOtherRule(allVersions, allInstances);
    }

    printData(data, allVersions.size(), allInstances.size());
  }

  private void printData(MicroserviceVersionRuleData data, int inputVersionCount, int inputInstanceCount) {
    String latestVersion = data.latestVersion == null ? null : data.latestVersion.getVersion().getVersion();
    if (data.getInstances().isEmpty()) {
      LOGGER.info(
          "update instances to be empty caused by version rule, appId={}, microserviceName={}, versionRule={}"
              + ", latestVersion={}, inputVersionCount={}, inputInstanceCount={}",
          appId, microserviceName, versionRule.getVersionRule(),
          latestVersion, inputVersionCount, inputInstanceCount);
      return;
    }

    StringBuilder sb = new StringBuilder();
    StringBuilderUtils
        .appendLine(sb,
            "update instances, appId=%s, microserviceName=%s, versionRule=%s"
                + ", latestVersion=%s, inputVersionCount=%s, inputInstanceCount=%s",
            appId, microserviceName, versionRule.getVersionRule(),
            latestVersion, inputVersionCount, inputInstanceCount);

    int idx = 0;
    for (MicroserviceInstance instance : data.getInstances().values()) {
      MicroserviceVersion microserviceVersion = data.versions.get(instance.getServiceId());
      Microservice microservice = microserviceVersion.getMicroservice();
      Framework framework = microservice.getFramework();
      String frameworkName = framework == null ? "unknown" : framework.getName();
      String frameworkVersion = framework == null ? "unknown" : framework.getVersion();

      StringBuilderUtils.appendLine(sb,
          "  %d.instanceId=%s, status=%s, version=%s, endpoints=%s, environment=%s, framework.name=%s, framework.version=%s",
          idx,
          instance.getInstanceId(), instance.getStatus(), microserviceVersion.getVersion(), instance.getEndpoints(),
          microservice.getEnvironment(), frameworkName, frameworkVersion);
      idx++;
    }

    LOGGER.info(StringBuilderUtils.deleteLast(sb, 1).toString());
  }

  private void initData(MicroserviceVersionRuleData data) {
    if (data.latestVersion == null) {
      data.latestVersion = findLatest(data.versions, data.instances.values());
    }

    data.instanceCache = new InstanceCache(appId, microserviceName, versionRule.getVersionRule(), data.instances);
    data.versionedCache = new VersionedCache()
        .name(versionRule.getVersionRule())
        .autoCacheVersion()
        .data(data.instances);
  }

  private MicroserviceVersionRuleData createEmptyData() {
    MicroserviceVersionRuleData data = new MicroserviceVersionRuleData();
    data.versions = Collections.emptyMap();
    data.instances = Collections.emptyMap();
    initData(data);
    return data;
  }

  private MicroserviceVersion findLatest(Map<String, MicroserviceVersion> allVersions,
      Collection<MicroserviceInstance> allInstances) {
    if (allInstances.isEmpty()) {
      return allVersions.isEmpty() ? null : allVersions.values().stream()
          .max(Comparator.comparing(MicroserviceVersion::getVersion))
          .get();
    }

    MicroserviceVersion latestVersion = null;
    for (MicroserviceInstance instance : allInstances) {
      // never to be null
      MicroserviceVersion version = allVersions.get(instance.getServiceId());
      if (latestVersion == null || latestVersion.version.compareTo(version.version) < 0) {
        latestVersion = version;
      }
    }
    return latestVersion;
  }

  private MicroserviceVersionRuleData createDataByLatestRule(Map<String, MicroserviceVersion> allVersions,
      Collection<MicroserviceInstance> allInstances) {
    // find latest
    MicroserviceVersion latestVersion = findLatest(allVersions, allInstances);
    if (latestVersion == null) {
      return createEmptyData();
    }

    MicroserviceVersionRuleData data = new MicroserviceVersionRuleData();

    String serviceId = latestVersion.getMicroservice().getServiceId();
    data.latestVersion = latestVersion;
    data.versions = Collections.singletonMap(serviceId, latestVersion);
    data.instances = allInstances.stream()
        .filter(instance -> instance.getServiceId().equals(serviceId))
        .collect(Collectors.toMap(MicroserviceInstance::getInstanceId, Function.identity()));
    data.instances = Collections.unmodifiableMap(data.instances);

    initData(data);

    return data;
  }

  private MicroserviceVersionRuleData createDataByOtherRule(Map<String, MicroserviceVersion> allVersions,
      Collection<MicroserviceInstance> allInstances) {
    MicroserviceVersionRuleData data = new MicroserviceVersionRuleData();

    // filter versions
    data.versions = allVersions.values().stream()
        .filter(v -> versionRule.isAccept(v.version))
        .collect(Collectors.toMap(MicroserviceVersion::getMicroserviceId, Function.identity()));
    data.versions = Collections.unmodifiableMap(data.versions);
    if (data.versions.isEmpty()) {
      return createEmptyData();
    }

    // filter instances
    data.instances = allInstances.stream()
        .filter(instance -> data.versions.containsKey(instance.getServiceId()))
        .collect(Collectors.toMap(MicroserviceInstance::getInstanceId, Function.identity()));
    data.instances = Collections.unmodifiableMap(data.instances);

    initData(data);

    return data;
  }
}
