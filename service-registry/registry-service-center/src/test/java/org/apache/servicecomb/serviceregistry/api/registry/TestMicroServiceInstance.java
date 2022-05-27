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

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.registry.api.registry.HealthCheck;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;

import mockit.Deencapsulation;

public class TestMicroServiceInstance {

  MicroserviceInstance oMicroserviceInstance = null;

  Map<String, String> oMapProperties = null;

  List<String> oListEndpoints = null;

  HealthCheck oMockHealthCheck = null;

  @AfterClass
  public static void classTeardown() {
    Deencapsulation.setField(ConfigurationManager.class, "instance", null);
    Deencapsulation.setField(ConfigurationManager.class, "customConfigurationInstalled", false);
    Deencapsulation.setField(DynamicPropertyFactory.class, "config", null);
    RegistryUtils.setServiceRegistry(null);
  }

  @Before
  public void setUp() throws Exception {
    oMicroserviceInstance = new MicroserviceInstance();
    oMapProperties = new HashMap<>();
    oListEndpoints = new ArrayList<>();
    oMockHealthCheck = Mockito.mock(HealthCheck.class);
  }

  @After
  public void tearDown() throws Exception {
    oMicroserviceInstance = null;
    oMapProperties = null;
    oListEndpoints = null;
    oMockHealthCheck = null;
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testDefaultValues() {
    Assertions.assertNull(oMicroserviceInstance.getHostName());
    Assertions.assertNull(oMicroserviceInstance.getInstanceId());
    Assertions.assertNull(oMicroserviceInstance.getServiceId());
    Assertions.assertEquals(0, oMicroserviceInstance.getProperties().size());
    Assertions.assertEquals(0, oMicroserviceInstance.getEndpoints().size());
    Assertions.assertNull(oMicroserviceInstance.getHealthCheck());
    Assertions.assertNull(oMicroserviceInstance.getStage());
    Assertions.assertEquals(MicroserviceInstanceStatus.UP, oMicroserviceInstance.getStatus());
    Assertions.assertEquals("instanceId=null;serviceId=null;status=UP;endpoints=[]", oMicroserviceInstance.toString());
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testInitializedValues() {
    initMicroserviceInstance(); //Initialize the Object
    Assertions.assertEquals("testHostName", oMicroserviceInstance.getHostName());
    Assertions.assertEquals("testInstanceID", oMicroserviceInstance.getInstanceId());
    Assertions.assertEquals(1, oMicroserviceInstance.getEndpoints().size());
    Assertions.assertEquals("testServiceID", oMicroserviceInstance.getServiceId());
    Assertions.assertEquals(oMockHealthCheck, oMicroserviceInstance.getHealthCheck());
    Assertions.assertEquals(MicroserviceInstanceStatus.DOWN, oMicroserviceInstance.getStatus());
    Assertions.assertEquals("Test", oMicroserviceInstance.getStage());
    Assertions.assertEquals("china", oMicroserviceInstance.getProperties().get("region"));

    Assertions.assertEquals(oMicroserviceInstance, oMicroserviceInstance);
    MicroserviceInstance other = new MicroserviceInstance();
    other.setInstanceId("testInstanceIDOther");
    MicroserviceInstance same = new MicroserviceInstance();
    same.setInstanceId("testInstanceID");
    Assertions.assertNotEquals(oMicroserviceInstance, other);
    Assertions.assertNotEquals(oMicroserviceInstance.hashCode(), other.hashCode());
    Assertions.assertEquals(oMicroserviceInstance, same);
    Assertions.assertEquals(oMicroserviceInstance.hashCode(), same.hashCode());
    Assertions.assertEquals("instanceId=testInstanceID;serviceId=testServiceID;status=DOWN;endpoints=[testEndpoints]",
        oMicroserviceInstance.toString());
  }

  @SuppressWarnings("deprecation")
  private void initMicroserviceInstance() {
    oMicroserviceInstance.setHostName("testHostName");
    oMicroserviceInstance.setInstanceId("testInstanceID");
    oMicroserviceInstance.setStage("Test");
    oMicroserviceInstance.setServiceId("testServiceID");
    oMicroserviceInstance.setStatus(MicroserviceInstanceStatus.DOWN);
    oMapProperties.put("region", "china");
    oListEndpoints.add("testEndpoints");
    oMicroserviceInstance.setProperties(oMapProperties);
    oMicroserviceInstance.setEndpoints(oListEndpoints);
    oMicroserviceInstance.setHealthCheck(oMockHealthCheck);
  }

  @Test
  public void testCreateMicroserviceInstanceFromFile() {
    AbstractConfiguration config = ConfigUtil.createLocalConfig();
    ConcurrentCompositeConfiguration configuration = new ConcurrentCompositeConfiguration();
    configuration.addConfiguration(config);
    ConfigurationManager.install(configuration);
    MicroserviceInstance instance = MicroserviceInstance.createFromDefinition(config);
    Assertions.assertEquals(instance.getDataCenterInfo().getName(), "myDC");
    Assertions.assertEquals(instance.getDataCenterInfo().getRegion(), "my-Region");
    Assertions.assertEquals(instance.getDataCenterInfo().getAvailableZone(), "my-Zone");
  }
}
