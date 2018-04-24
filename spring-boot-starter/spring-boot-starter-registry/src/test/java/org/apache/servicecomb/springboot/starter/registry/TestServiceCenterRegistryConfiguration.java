
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
package org.apache.servicecomb.springboot.starter.registry;

import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import mockit.Expectations;

public class TestServiceCenterRegistryConfiguration {

  @Test
  public void testInitRegistry() {
    MicroserviceInstance microserviceInstance = Mockito.mock(MicroserviceInstance.class);
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.init();
        RegistryUtils.run();
        RegistryUtils.getMicroserviceInstance();
        result = microserviceInstance;

      }
    };
    System.setProperty("cse.rest.address", "127.0.0.1:8081");
    new ServiceCenterRegistryConfiguration();
    Assert.assertEquals("rest://127.0.0.1:8081", RegistryUtils.getPublishAddress("rest", "127.0.0.1:8081"));
  }

  @Test
  public void testInitRegistryException() {
    MicroserviceInstance microserviceInstance = Mockito.mock(MicroserviceInstance.class);
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getMicroserviceInstance();
        result = microserviceInstance;
      }
    };
    RegistryIntializer.initRegistry();
    Assert.assertEquals(0, RegistryUtils.getMicroserviceInstance().getEndpoints().size());
  }
}
