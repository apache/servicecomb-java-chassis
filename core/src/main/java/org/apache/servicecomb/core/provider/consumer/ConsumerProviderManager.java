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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.ConsumerProvider;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.serviceregistry.consumer.AppManager;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersionRule;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;

@Component
public class ConsumerProviderManager {
  @Autowired(required = false)
  private List<ConsumerProvider> consumerProviderList = Collections.emptyList();

  private AppManager appManager;

  // key为微服务名
  private Map<String, ReferenceConfig> referenceConfigMap = new ConcurrentHashMapEx<>();

  public void setAppManager(AppManager appManager) {
    this.appManager = appManager;
  }

  public void init() throws Exception {
    for (ConsumerProvider provider : consumerProviderList) {
      provider.init();
    }
  }

  public ReferenceConfig createReferenceConfig(String microserviceName, String versionRule, String transport) {
    ReferenceConfig referenceConfig = new ReferenceConfig(appManager, microserviceName, versionRule, transport);

    MicroserviceVersionRule microserviceVersionRule = referenceConfig.getMicroserviceVersionRule();
    if (microserviceVersionRule.getLatestMicroserviceVersion() == null) {
      throw new IllegalStateException(
          String.format(
              "Probably invoke a service before it is registered, or no instance found for it, appId=%s, name=%s",
              microserviceVersionRule.getAppId(),
              microserviceVersionRule.getMicroserviceName()));
    }

    return referenceConfig;
  }

  public ReferenceConfig createReferenceConfig(String microserviceName) {
    String key = "servicecomb.references." + microserviceName;

    String defaultVersionRule = DynamicPropertyFactory.getInstance()
        .getStringProperty("servicecomb.references.version-rule", DefinitionConst.VERSION_RULE_ALL)
        .get();
    String versionRule = DynamicPropertyFactory.getInstance()
        .getStringProperty(key + ".version-rule", defaultVersionRule)
        .get();

    String defaultTransport = DynamicPropertyFactory.getInstance()
        .getStringProperty("servicecomb.references.transport", Const.ANY_TRANSPORT)
        .get();
    String transport = DynamicPropertyFactory.getInstance()
        .getStringProperty(key + ".transport", defaultTransport)
        .get();

    return createReferenceConfig(microserviceName, versionRule, transport);
  }

  public ReferenceConfig getReferenceConfig(String microserviceName) {
    return referenceConfigMap.computeIfAbsent(microserviceName, this::createReferenceConfig);
  }

  // 只用于测试场景
  public ReferenceConfig setTransport(String microserviceName, String transport) {
    ReferenceConfig config = getReferenceConfig(microserviceName);
    config.setTransport(transport);

    return config;
  }
}
