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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
    Assert.assertNull(oMicroservice.getAppId());
    Assert.assertNull(oMicroservice.getDescription());
    Assert.assertNull(oMicroservice.getLevel());
    Assert.assertEquals(0, oMicroservice.getProperties().size());
    Assert.assertEquals(0, oMicroservice.getSchemas().size());
    Assert.assertNull(oMicroservice.getServiceId());
    Assert.assertNull(oMicroservice.getServiceName());
    Assert.assertEquals(MicroserviceInstanceStatus.UP.toString(), oMicroservice.getStatus());
    Assert.assertNull(oMicroservice.getVersion());
    Assert.assertEquals(0, oMicroservice.getPaths().size());
    Assert.assertNull(oMicroservice.getFramework());
  }

  @Test
  public void testInitializedValues() {
    initMicroservice(); //Initialize the Object
    Assert.assertEquals("testAppID", oMicroservice.getAppId());
    Assert.assertEquals("This is the test", oMicroservice.getDescription());
    Assert.assertEquals("INFO", oMicroservice.getLevel());
    Assert.assertEquals("testServiceID", oMicroservice.getServiceId());
    Assert.assertEquals("testServiceName", oMicroservice.getServiceName());
    Assert.assertEquals(MicroserviceInstanceStatus.DOWN.toString(), oMicroservice.getStatus());
    Assert.assertEquals("1.0.0", oMicroservice.getVersion());
    Assert.assertEquals("fakeProxy", oMicroservice.getProperties().get("proxy"));
    Assert.assertEquals(1, oMicroservice.getSchemas().size());
    Assert.assertEquals(1, oMicroservice.getPaths().size());
    Assert.assertEquals("JAVA-CHASSIS", oMicroservice.getFramework().getName());
    Assert.assertEquals("x.x.x", oMicroservice.getFramework().getVersion());
    Assert.assertEquals("SDK", oMicroservice.getRegisterBy());
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
  }
}
