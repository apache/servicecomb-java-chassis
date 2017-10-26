/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.serviceregistry.cache;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.serviceregistry.consumer.AppManager;
import io.servicecomb.serviceregistry.consumer.MicroserviceVersionRule;
import io.servicecomb.serviceregistry.definition.DefinitionConst;
import mockit.Expectations;
import mockit.Mocked;

public class TestInstanceCacheManagerNew {
  @Test
  public void getOrCreate(@Mocked AppManager appManager, @Mocked MicroserviceVersionRule microserviceVersionRule,
      @Mocked InstanceCache instanceCache) {
    InstanceCacheManagerNew mgr = new InstanceCacheManagerNew(appManager);
    String appId = "app";
    String microserviceName = "ms";
    String versionRule = DefinitionConst.VERSION_RULE_ALL;
    new Expectations() {
      {
        appManager.getOrCreateMicroserviceVersionRule(appId, microserviceName, versionRule);
        result = microserviceVersionRule;
        microserviceVersionRule.getInstanceCache();
        result = instanceCache;
      }
    };

    Assert.assertSame(instanceCache, mgr.getOrCreate(appId, microserviceName, versionRule));
  }
}
