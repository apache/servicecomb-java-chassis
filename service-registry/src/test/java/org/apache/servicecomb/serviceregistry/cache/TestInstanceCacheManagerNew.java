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

import org.apache.servicecomb.serviceregistry.MockMicroserviceVersions;
import org.apache.servicecomb.serviceregistry.consumer.AppManager;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceManager;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.junit.Assert;
import org.junit.Test;

public class TestInstanceCacheManagerNew {
  MockMicroserviceVersions mockMicroserviceVersions = new MockMicroserviceVersions();

  AppManager appManager = mockMicroserviceVersions.getAppManager();

  MicroserviceManager microserviceManager = appManager
      .getOrCreateMicroserviceManager(mockMicroserviceVersions.getAppId());

  InstanceCacheManagerNew mgr = new InstanceCacheManagerNew(mockMicroserviceVersions.getAppManager());

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
