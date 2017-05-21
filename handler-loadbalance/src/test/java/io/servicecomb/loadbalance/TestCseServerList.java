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

package io.servicecomb.loadbalance;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.servicecomb.core.endpoint.AbstractEndpointsCache;
import com.huawei.paas.cse.serviceregistry.RegistryUtils;
import com.huawei.paas.cse.serviceregistry.api.registry.Microservice;
import com.huawei.paas.cse.serviceregistry.cache.InstanceCache;
import com.huawei.paas.cse.serviceregistry.cache.InstanceCacheManager;

import mockit.Mock;
import mockit.MockUp;

public class TestCseServerList {

    private CseServerList instance = null;

    private void mockTestCases() {
        final Microservice microService = new Microservice();
        microService.setAppId("appId");
        new MockUp<RegistryUtils>() {
            @Mock
            public Microservice getMicroservice() {
                return microService;
            }

            @Mock
            private Microservice createMicroserviceFromDefinition() {
                return microService;
            }
        };

        new MockUp<InstanceCacheManager>() {
            @Mock
            public InstanceCache getOrCreate(String appId, String microserviceName, String microserviceVersionRule) {
                return null;
            }

        };

    }

    @Before
    public void setUp() throws Exception {
        mockTestCases();
        instance = new CseServerList("appId", "microserviceName", "microserviceVersionRule", "transportName");
    }

    @After
    public void tearDown() throws Exception {
        instance = null;
    }

    @Test
    public void testCseServerList() {
        Assert.assertNotNull(instance);

    }

    @SuppressWarnings("rawtypes")
    @Test
    public <ENDPOINT> void testGetInitListServers() {

        @SuppressWarnings("unused")
        ServerListCache serverListCache = new ServerListCache("test", "test", "test", "test");

        List<ENDPOINT> endpoints = new ArrayList<>();

        ENDPOINT e = null;
        endpoints.add(e);

        new MockUp<AbstractEndpointsCache>() {

            @Mock
            public List<ENDPOINT> getLatestEndpoints() {
                return endpoints;
            }
        };

        instance.getInitialListOfServers();
        assertNotNull(instance.getInitialListOfServers());

    }

    @SuppressWarnings("rawtypes")
    @Test
    public <ENDPOINT> void testGetUpdatedListOfServers() {

        @SuppressWarnings("unused")
        ServerListCache serverListCache = new ServerListCache("test", "test", "test", "test");

        List<ENDPOINT> endpoints = new ArrayList<>();

        ENDPOINT e = null;
        endpoints.add(e);

        new MockUp<AbstractEndpointsCache>() {

            @Mock
            public List<ENDPOINT> getLatestEndpoints() {
                return endpoints;
            }
        };

        instance.getUpdatedListOfServers();
        assertNotNull(instance.getUpdatedListOfServers());
    }
}
