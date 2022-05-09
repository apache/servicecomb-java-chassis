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

package org.apache.servicecomb.serviceregistry.api.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.registry.api.registry.BasePath;
import org.apache.servicecomb.registry.api.registry.Framework;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstanceStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class TestMicroService {

  Microservice oMicroservice = null;

  Map<String, String> oMapProperties = null;

  List<String> oListSchemas = null;

  @Before
  public void setUp() throws Exception {
    oMicroservice = new Microservice();
    oMapProperties = new HashMap<>();
    oListSchemas = new ArrayList<>();
  }

  @After
  public void tearDown() throws Exception {
    oMicroservice = null;
    oMapProperties = null;
    oListSchemas = null;
  }

  @Test
  public void testDefaultValues() {
    Assertions.assertNull(oMicroservice.getAppId());
    Assertions.assertNull(oMicroservice.getDescription());
    Assertions.assertNull(oMicroservice.getLevel());
    Assertions.assertEquals(0, oMicroservice.getProperties().size());
    Assertions.assertEquals(0, oMicroservice.getSchemas().size());
    Assertions.assertNull(oMicroservice.getServiceId());
    Assertions.assertNull(oMicroservice.getServiceName());
    Assertions.assertEquals(MicroserviceInstanceStatus.UP.toString(), oMicroservice.getStatus());
    Assertions.assertNull(oMicroservice.getVersion());
    Assertions.assertEquals(0, oMicroservice.getPaths().size());
    Assertions.assertNull(oMicroservice.getFramework());
    Assertions.assertNull(oMicroservice.getEnvironment());
  }

  @Test
  public void testInitializedValues() {
    initMicroservice(); //Initialize the Object
    Assertions.assertEquals("testAppID", oMicroservice.getAppId());
    Assertions.assertEquals("This is the test", oMicroservice.getDescription());
    Assertions.assertEquals("INFO", oMicroservice.getLevel());
    Assertions.assertEquals("testServiceID", oMicroservice.getServiceId());
    Assertions.assertEquals("testServiceName", oMicroservice.getServiceName());
    Assertions.assertEquals(MicroserviceInstanceStatus.DOWN.toString(), oMicroservice.getStatus());
    Assertions.assertEquals("1.0.0", oMicroservice.getVersion());
    Assertions.assertEquals("fakeProxy", oMicroservice.getProperties().get("proxy"));
    Assertions.assertEquals(1, oMicroservice.getSchemas().size());
    Assertions.assertEquals(1, oMicroservice.getPaths().size());
    Assertions.assertEquals("JAVA-CHASSIS", oMicroservice.getFramework().getName());
    Assertions.assertEquals("x.x.x", oMicroservice.getFramework().getVersion());
    Assertions.assertEquals("SDK", oMicroservice.getRegisterBy());
    Assertions.assertEquals("development", oMicroservice.getEnvironment());
  }

  private void initMicroservice() {
    oMicroservice.setAppId("testAppID");
    oMicroservice.setDescription("This is the test");
    oMicroservice.setLevel("INFO");
    oMicroservice.setServiceId("testServiceID");
    oMicroservice.setServiceName("testServiceName");
    oMicroservice.setStatus(MicroserviceInstanceStatus.DOWN.toString());
    oMicroservice.setVersion("1.0.0");
    oMapProperties.put("proxy", "fakeProxy");
    oListSchemas.add("testSchemas");
    oMicroservice.setProperties(oMapProperties);
    oMicroservice.setSchemas(oListSchemas);
    oMicroservice.getPaths().add(new BasePath());
    Framework framework = new Framework();
    framework.setName("JAVA-CHASSIS");
    framework.setVersion("x.x.x");
    oMicroservice.setFramework(framework);
    oMicroservice.setRegisterBy("SDK");
    oMicroservice.setEnvironment("development");
  }
}
