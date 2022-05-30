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

package org.apache.servicecomb.serviceregistry;

import java.util.Collections;

import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCache.MicroserviceCacheStatus;
import org.apache.servicecomb.serviceregistry.registry.cache.MockedMicroserviceCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegistryUtilsTest {
  @Test
  public void convertCacheToMicroserviceInstances() {
    MockedMicroserviceCache microserviceCache = new MockedMicroserviceCache();
    microserviceCache.setStatus(MicroserviceCacheStatus.CLIENT_ERROR);
    MicroserviceInstances microserviceInstances = RegistryUtils
        .convertCacheToMicroserviceInstances(microserviceCache);
    Assertions.assertNull(microserviceInstances);

    microserviceCache = new MockedMicroserviceCache();
    microserviceCache.setStatus(MicroserviceCacheStatus.SETTING_CACHE_ERROR);
    microserviceInstances = RegistryUtils.convertCacheToMicroserviceInstances(microserviceCache);
    Assertions.assertNull(microserviceInstances);

    microserviceCache = new MockedMicroserviceCache();
    microserviceCache.setStatus(MicroserviceCacheStatus.INIT);
    microserviceInstances = RegistryUtils.convertCacheToMicroserviceInstances(microserviceCache);
    Assertions.assertNull(microserviceInstances);

    microserviceCache = new MockedMicroserviceCache();
    microserviceCache.setStatus(MicroserviceCacheStatus.SERVICE_NOT_FOUND);
    microserviceInstances = RegistryUtils.convertCacheToMicroserviceInstances(microserviceCache);
    Assertions.assertTrue(microserviceInstances.isMicroserviceNotExist());
    Assertions.assertFalse(microserviceInstances.isNeedRefresh());
    Assertions.assertEquals("", microserviceInstances.getRevision());
    Assertions.assertNull(microserviceInstances.getInstancesResponse());

    microserviceCache = new MockedMicroserviceCache();
    microserviceCache.setStatus(MicroserviceCacheStatus.REFRESHED);
    microserviceCache.setRevisionId("0166f3c18702617d5e55cf911e4e412cc8760dab");
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    microserviceCache.setInstances(Collections.singletonList(microserviceInstance));
    microserviceInstances = RegistryUtils.convertCacheToMicroserviceInstances(microserviceCache);
    Assertions.assertFalse(microserviceInstances.isMicroserviceNotExist());
    Assertions.assertTrue(microserviceInstances.isNeedRefresh());
    Assertions.assertEquals("0166f3c18702617d5e55cf911e4e412cc8760dab", microserviceInstances.getRevision());
    Assertions.assertEquals(1, microserviceInstances.getInstancesResponse().getInstances().size());
    Assertions.assertSame(microserviceInstance, microserviceInstances.getInstancesResponse().getInstances().get(0));

    microserviceCache = new MockedMicroserviceCache();
    microserviceCache.setStatus(MicroserviceCacheStatus.NO_CHANGE);
    microserviceCache.setRevisionId("0166f3c18702617d5e55cf911e4e412cc8760dab");
    microserviceInstances = RegistryUtils.convertCacheToMicroserviceInstances(microserviceCache);
    Assertions.assertFalse(microserviceInstances.isMicroserviceNotExist());
    Assertions.assertFalse(microserviceInstances.isNeedRefresh());
    Assertions.assertEquals("0166f3c18702617d5e55cf911e4e412cc8760dab", microserviceInstances.getRevision());
    Assertions.assertNull(microserviceInstances.getInstancesResponse());
  }
}
