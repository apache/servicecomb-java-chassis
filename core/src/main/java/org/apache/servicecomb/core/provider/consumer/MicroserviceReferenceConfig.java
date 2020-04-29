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
package org.apache.servicecomb.core.provider.consumer;

import java.util.Objects;
import java.util.function.Supplier;

import org.apache.servicecomb.core.definition.ConsumerMicroserviceVersionsMeta;
import org.apache.servicecomb.core.definition.CoreMetaUtils;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.serviceregistry.DiscoveryManager;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersion;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersionRule;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersions;
import org.apache.servicecomb.serviceregistry.consumer.StaticMicroserviceVersions;

/**
 * <pre>
 *   when the list data changed, MicroserviceReferenceConfig should rebuild:
 *     1.versionRule
 *     2.latestMicroserviceMeta
 *     3.microservice deleted
 * </pre>
 */
public class MicroserviceReferenceConfig {
  private final ConsumerMicroserviceVersionsMeta microserviceVersionsMeta;

  private final MicroserviceVersionRule microserviceVersionRule;

  private final MicroserviceVersion latestMicroserviceVersion;

  private final MicroserviceMeta latestMicroserviceMeta;

  private final String versionRule;

  // return true means changed
  private final Supplier<Boolean> versionRuleChangedChecker;

  public MicroserviceReferenceConfig(ConsumerMicroserviceVersionsMeta microserviceVersionsMeta, String versionRule) {
    this.microserviceVersionsMeta = microserviceVersionsMeta;

    this.versionRule =
        versionRule != null ? versionRule : microserviceVersionsMeta.getMicroserviceConfig().getVersionRule();
    this.versionRuleChangedChecker = this.versionRule != null ? this::checkByConfig : this::notChange;

    microserviceVersionRule = microserviceVersionsMeta.getMicroserviceVersions()
        .getOrCreateMicroserviceVersionRule(this.versionRule);
    latestMicroserviceVersion = microserviceVersionRule.getLatestMicroserviceVersion();
    latestMicroserviceMeta =
        latestMicroserviceVersion != null ? CoreMetaUtils.getMicroserviceMeta(latestMicroserviceVersion) : null;
  }

  private Boolean notChange() {
    return false;
  }

  private Boolean checkByConfig() {
    return !Objects.equals(versionRule, microserviceVersionsMeta.getMicroserviceConfig().getVersionRule());
  }

  public MicroserviceMeta getLatestMicroserviceMeta() {
    if (latestMicroserviceVersion == null) {
      throw new IllegalStateException(
          String.format(
              "Probably invoke a service before it is registered, or no instance found for it, appId=%s, name=%s, versionRule=%s.",
              microserviceVersionRule.getAppId(),
              microserviceVersionRule.getMicroserviceName(),
              versionRule));
    }

    return latestMicroserviceMeta;
  }

  public ReferenceConfig createReferenceConfig(OperationMeta operationMeta) {
    return createReferenceConfig(null, operationMeta);
  }

  public ReferenceConfig createReferenceConfig(String transport, OperationMeta operationMeta) {
    if (transport == null) {
      transport = operationMeta.getConfig().getTransport();
    }
    final ReferenceConfig referenceConfig = new ReferenceConfig(transport, versionRule);
    mark3rdPartyService(operationMeta, referenceConfig);
    return referenceConfig;
  }

  private void mark3rdPartyService(OperationMeta operationMeta, ReferenceConfig referenceConfig) {
    final MicroserviceVersions microserviceVersions = DiscoveryManager.INSTANCE
        .getOrCreateMicroserviceVersions(
            operationMeta.getMicroserviceMeta().getAppId(),
            operationMeta.getMicroserviceName());
    referenceConfig.setThirdPartyService(microserviceVersions instanceof StaticMicroserviceVersions);
  }

  public boolean isExpired() {
    // 1.microservice deleted
    // 2.latest version changed
    // 3.versionRule configuration changed
    return microserviceVersionsMeta.getMicroserviceVersions().isWaitingDelete() ||
        latestMicroserviceVersion != microserviceVersionRule.getLatestMicroserviceVersion() ||
        versionRuleChangedChecker.get();
  }
}
