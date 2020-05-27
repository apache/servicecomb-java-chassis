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

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.registry.cache.InstanceCacheManagerNew;
import org.apache.servicecomb.serviceregistry.MockMicroserviceVersions;
import org.apache.servicecomb.registry.consumer.AppManager;
import org.apache.servicecomb.registry.consumer.MicroserviceManager;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestInstanceCacheManagerNew {
  MockMicroserviceVersions mockMicroserviceVersions;

  AppManager appManager;

  MicroserviceManager microserviceManager;

  InstanceCacheManagerNew mgr;

  @Before
  public void setup() {
    ConfigUtil.installDynamicConfig();
    mockMicroserviceVersions = new MockMicroserviceVersions();
    appManager = mockMicroserviceVersions.getAppManager();
    microserviceManager = appManager
        .getOrCreateMicroserviceManager(mockMicroserviceVersions.getAppId());
    mgr = new InstanceCacheManagerNew(mockMicroserviceVersions.getAppManager());
  }

  @AfterClass
  public static void classTeardown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void getOrCreate() {
    mockMicroserviceVersions.update_all();
    microserviceManager.getVersionsByName()
        .put(mockMicroserviceVersions.getMicroserviceName(), mockMicroserviceVersions);

    Assert.assertEquals(8,
        mgr
            .getOrCreate(mockMicroserviceVersions.getAppId(),
                mockMicroserviceVersions.getMicroserviceName(),
                DefinitionConst.VERSION_RULE_ALL)
            .getInstanceMap().size());
  }

  @Test
  public void getOrCreateVersionedCache() {
    mockMicroserviceVersions.update_all();
    microserviceManager.getVersionsByName()
        .put(mockMicroserviceVersions.getMicroserviceName(), mockMicroserviceVersions);

    Assert.assertEquals(8,
        mgr
            .getOrCreateVersionedCache(mockMicroserviceVersions.getAppId(),
                mockMicroserviceVersions.getMicroserviceName(),
                DefinitionConst.VERSION_RULE_ALL)
            .mapData().size());
  }
}
