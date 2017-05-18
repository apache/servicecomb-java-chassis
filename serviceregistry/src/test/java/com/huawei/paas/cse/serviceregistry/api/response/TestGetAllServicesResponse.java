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

package com.huawei.paas.cse.serviceregistry.api.response;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.huawei.paas.cse.serviceregistry.api.registry.Microservice;

/**
 * @author 
 * @since Mar 14, 2017
 * @see 
 */
public class TestGetAllServicesResponse {

    GetAllServicesResponse oGetAllServicesResponse = null;

    Microservice oMockMicroservice = null;

    /**
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        oGetAllServicesResponse = new GetAllServicesResponse();
        oMockMicroservice = Mockito.mock(Microservice.class);
    }

    /**
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        oGetAllServicesResponse = null;
    }

    /**
     * Test Un-Initialized Values
     */
    @Test
    public void testDefaultValues() {
        Assert.assertNull(oGetAllServicesResponse.getServices());
    }

    /**
     * Test InitializedValues
     */
    @Test
    public void testIntializedValues() {
        initFields(); //Initialize the Object
        List<Microservice> list = oGetAllServicesResponse.getServices();
        Assert.assertEquals(oMockMicroservice, list.get(0));
    }

    /**
     * Initialize the Values
     */
    private void initFields() {
        List<Microservice> list = new ArrayList<>();
        list.add(oMockMicroservice);
        oGetAllServicesResponse.setServices(list);
    }

}
