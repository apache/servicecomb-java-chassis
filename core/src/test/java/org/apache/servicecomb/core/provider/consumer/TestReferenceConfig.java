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

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.MicroserviceVersionMeta;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.consumer.AppManager;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersionRule;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.eventbus.EventBus;

import mockit.Expectations;
import mockit.Mocked;

public class TestReferenceConfig {
  @Test
  public void constructNoParam(@Mocked MicroserviceMeta microserviceMeta,
      @Mocked MicroserviceVersionMeta microserviceVersionMeta,
      @Mocked MicroserviceVersionRule microserviceVersionRule) {
    new Expectations() {
      {
        microserviceVersionMeta.getMicroserviceMeta();
        result = microserviceMeta;
        microserviceVersionRule.getLatestMicroserviceVersion();
        result = microserviceVersionMeta;
      }
    };
    String transport = Const.ANY_TRANSPORT;

    ReferenceConfig referenceConfig = new ReferenceConfig();
    referenceConfig.setMicroserviceVersionRule(microserviceVersionRule);
    referenceConfig.setTransport(transport);

    Assert.assertSame(microserviceMeta, referenceConfig.getMicroserviceMeta());
    Assert.assertSame(microserviceVersionRule, referenceConfig.getMicroserviceVersionRule());
    Assert.assertSame(transport, referenceConfig.getTransport());
  }

  @Test
  public void constructWithParam(@Mocked AppManager appManager,
      @Mocked MicroserviceMeta microserviceMeta,
      @Mocked MicroserviceVersionRule microserviceVersionRule,
      @Mocked MicroserviceVersionMeta microserviceVersionMeta) {
    String microserviceName = "ms";
    String transport = Const.ANY_TRANSPORT;
    new Expectations() {
      {
        appManager.getOrCreateMicroserviceVersionRule(anyString, anyString, anyString);
        result = microserviceVersionRule;
        microserviceVersionMeta.getMicroserviceMeta();
        result = microserviceMeta;
        microserviceVersionRule.getLatestMicroserviceVersion();
        result = microserviceVersionMeta;
      }
    };

    ReferenceConfig referenceConfig = new ReferenceConfig(appManager, microserviceName,
        DefinitionConst.VERSION_RULE_LATEST,
        transport);
    Assert.assertSame(microserviceMeta, referenceConfig.getMicroserviceMeta());
    Assert.assertSame(microserviceVersionRule, referenceConfig.getMicroserviceVersionRule());
    Assert.assertSame(transport, referenceConfig.getTransport());
  }

  @Test
  public void unifyVersionRule(@Mocked MicroserviceVersionMeta microserviceVersionMeta) {
    String microserviceName = "app:ms";
    String transport = Const.ANY_TRANSPORT;

    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.findServiceInstances(anyString, anyString, anyString, anyString);
        result = Collections.emptyList();
      }
    };
    AppManager appManager = new AppManager(new EventBus());

    ReferenceConfig referenceConfig = new ReferenceConfig(appManager, microserviceName,
        "0+",
        transport);
    Assert.assertEquals("0.0.0.0+", referenceConfig.getVersionRule());

    referenceConfig = new ReferenceConfig(appManager, microserviceName,
        "1.0.0+",
        transport);
    Assert.assertEquals("1.0.0.0+", referenceConfig.getVersionRule());
  }
}
