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
package org.apache.servicecomb.serviceregistry.api;

import org.apache.servicecomb.config.ConfigUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestConst {
  @Before
  public void setUp() {
    System.setProperty("servicecomb.service.registry.api.version", "v4");
    System.setProperty("servicecomb.config.client.domainName", "testtenane");
    ConfigUtil.createLocalConfig();
  }

  @After
  public void tearDown() {
    System.getProperties().remove("servicecomb.service.registry.api.version");
    System.getProperties().remove("servicecomb.config.client.domainName");
  }

  @Test
  public void testV4URL() {

    // this test case depends on class loading and java property may initialized after class loading, so we make to run in all cases.
    if (Const.REGISTRY_API.CURRENT_VERSION.equals(Const.REGISTRY_API.VERSION_V3)) {
      Assert.assertEquals("/registry/v3/microservices", Const.REGISTRY_API.MICROSERVICE_OPERATION_ALL);
      Assert.assertEquals("/registry/v3/microservices/%s", Const.REGISTRY_API.MICROSERVICE_OPERATION_ONE);
      Assert.assertEquals("/registry/v3/microservices/%s/instances",
          Const.REGISTRY_API.MICROSERVICE_INSTANCE_OPERATION_ALL);
      Assert.assertEquals("/registry/v3/microservices/%s/instances/%s",
          Const.REGISTRY_API.MICROSERVICE_INSTANCE_OPERATION_ONE);
      Assert.assertEquals("/registry/v3/existence", Const.REGISTRY_API.MICROSERVICE_EXISTENCE);
      Assert.assertEquals("/registry/v3/microservices/%s/schemas/%s", Const.REGISTRY_API.MICROSERVICE_SCHEMA);
      Assert.assertEquals("/registry/v3/microservices/%s/instances/%s/heartbeat",
          Const.REGISTRY_API.MICROSERVICE_HEARTBEAT);
      Assert.assertEquals("/registry/v3/microservices/%s/watcher", Const.REGISTRY_API.MICROSERVICE_WATCH);
      Assert.assertEquals("/registry/v3/instances", Const.REGISTRY_API.MICROSERVICE_INSTANCES);
      Assert.assertEquals("/registry/v3/microservices/%s/properties",
          Const.REGISTRY_API.MICROSERVICE_PROPERTIES);
      Assert.assertEquals("/registry/v3/microservices/%s/instances/%s/properties",
          Const.REGISTRY_API.MICROSERVICE_INSTANCE_PROPERTIES);
    } else {
      String prefix = "/v4/" + Const.REGISTRY_API.DOMAIN_NAME + "/registry/";
      Assert.assertEquals(prefix + "microservices", Const.REGISTRY_API.MICROSERVICE_OPERATION_ALL);
      Assert.assertEquals(prefix + "microservices/%s", Const.REGISTRY_API.MICROSERVICE_OPERATION_ONE);
      Assert.assertEquals(prefix + "microservices/%s/instances",
          Const.REGISTRY_API.MICROSERVICE_INSTANCE_OPERATION_ALL);
      Assert.assertEquals(prefix + "microservices/%s/instances/%s",
          Const.REGISTRY_API.MICROSERVICE_INSTANCE_OPERATION_ONE);
      Assert.assertEquals(prefix + "existence", Const.REGISTRY_API.MICROSERVICE_EXISTENCE);
      Assert.assertEquals(prefix + "microservices/%s/schemas/%s",
          Const.REGISTRY_API.MICROSERVICE_SCHEMA);
      Assert.assertEquals(prefix + "microservices/%s/instances/%s/heartbeat",
          Const.REGISTRY_API.MICROSERVICE_HEARTBEAT);
      Assert.assertEquals(prefix + "microservices/%s/watcher", Const.REGISTRY_API.MICROSERVICE_WATCH);
      Assert.assertEquals(prefix + "instances", Const.REGISTRY_API.MICROSERVICE_INSTANCES);
      Assert.assertEquals(prefix + "microservices/%s/properties",
          Const.REGISTRY_API.MICROSERVICE_PROPERTIES);
      Assert.assertEquals(prefix + "microservices/%s/instances/%s/properties",
          Const.REGISTRY_API.MICROSERVICE_INSTANCE_PROPERTIES);
    }
  }
}
