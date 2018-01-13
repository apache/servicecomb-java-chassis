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

package org.apache.servicecomb.serviceregistry.cache;

import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.serviceregistry.consumer.AppManager;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersionRule;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.junit.Assert;
import org.junit.Test;

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

  @Test
  public void getOrCreateVersionedCache(@Mocked AppManager appManager,
      @Mocked MicroserviceVersionRule microserviceVersionRule,
      @Mocked VersionedCache versionedCache) {
    InstanceCacheManagerNew mgr = new InstanceCacheManagerNew(appManager);
    String appId = "app";
    String microserviceName = "ms";
    String versionRule = DefinitionConst.VERSION_RULE_ALL;
    new Expectations() {
      {
        appManager.getOrCreateMicroserviceVersionRule(appId, microserviceName, versionRule);
        result = microserviceVersionRule;
        microserviceVersionRule.getVersionedCache();
        result = versionedCache;
      }
    };

    Assert.assertSame(versionedCache, mgr.getOrCreateVersionedCache(appId, microserviceName, versionRule));
  }
}
