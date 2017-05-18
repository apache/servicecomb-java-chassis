/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.paas.cse.serviceregistry.api.registry;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author  
 * @since Mar 13, 2017
 * @see 
 */
public class TestMicroServiceInstance {

    MicroserviceInstance oMicroserviceInstance = null;

    Map<String, String> oMapProperties = null;

    List<String> oListEndpoints = null;

    HealthCheck oMockHealthCheck = null;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        oMicroserviceInstance = new MicroserviceInstance();
        oMapProperties = new HashMap<>();
        oListEndpoints = new ArrayList<>();
        oMockHealthCheck = Mockito.mock(HealthCheck.class);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        oMicroserviceInstance = null;
        oMapProperties = null;
        oListEndpoints = null;
        oMockHealthCheck = null;
    }

    /**
     * Test Un-Initialized Values
     */
    @Test
    public void testDefaultValues() {
        Assert.assertNull(oMicroserviceInstance.getHostName());
        Assert.assertNull(oMicroserviceInstance.getInstanceId());
        Assert.assertNull(oMicroserviceInstance.getServiceId());
        Assert.assertEquals(0, oMicroserviceInstance.getProperties().size());
        Assert.assertEquals(0, oMicroserviceInstance.getEndpoints().size());
        Assert.assertNull(oMicroserviceInstance.getHealthCheck());
        Assert.assertNull(oMicroserviceInstance.getStage());
        Assert.assertEquals(MicroserviceInstanceStatus.UP, oMicroserviceInstance.getStatus());

    }

    /**
     * Test InitializedValues
     */
    @Test
    public void testIntializedValues() {
        initMicroserviceInstance(); //Initialize the Object
        Assert.assertEquals("testHostName", oMicroserviceInstance.getHostName());
        Assert.assertEquals("testInstanceID", oMicroserviceInstance.getInstanceId());
        Assert.assertEquals(1, oMicroserviceInstance.getEndpoints().size());
        Assert.assertEquals("testServiceID", oMicroserviceInstance.getServiceId());
        Assert.assertEquals(oMockHealthCheck, oMicroserviceInstance.getHealthCheck());
        Assert.assertEquals(MicroserviceInstanceStatus.DOWN, oMicroserviceInstance.getStatus());
        Assert.assertEquals("Test", oMicroserviceInstance.getStage());
        Assert.assertEquals("china", oMicroserviceInstance.getProperties().get("region"));

    }

    /**
     * Initialize the Values
     */
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

}
