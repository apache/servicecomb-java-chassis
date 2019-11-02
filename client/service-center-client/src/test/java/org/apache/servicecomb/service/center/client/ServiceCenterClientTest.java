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

package org.apache.servicecomb.service.center.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.servicecomb.service.center.client.http.HttpResponse;
import org.apache.servicecomb.service.center.client.model.HeartbeatsRequest;
import org.apache.servicecomb.service.center.client.model.InstancesRequest;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstancesResponse;
import org.apache.servicecomb.service.center.client.model.MicroservicesResponse;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by   on 2019/10/17.
 */
public class ServiceCenterClientTest {

    @Test
    public void TestGetServiceCenterInstances() throws IOException {

        ServiceCenterRawClient serviceCenterRawClient = Mockito.mock(ServiceCenterRawClient.class);

        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatusCode(200);
        httpResponse.setMessage("ok");
        String responseString = "{\n"
                + "  \"instances\": [\n"
                + "    {\n"
                + "      \"instanceId\": \"111111\",\n"
                + "      \"serviceId\": \"222222\",\n"
                + "      \"version\": \"1.0\",\n"
                + "      \"hostName\": \"Test\",\n"
                + "      \"endpoints\": [\n"
                + "        \"string\"\n"
                + "      ],\n"
                + "      \"status\": \"UP\",\n"
                + "      \"properties\": {\n"
                + "        \"additionalProp1\": \"string\",\n"
                + "        \"additionalProp2\": \"string\",\n"
                + "        \"additionalProp3\": \"string\"\n"
                + "      },\n"
                + "      \"healthCheck\": {\n"
                + "        \"mode\": \"push\",\n"
                + "        \"port\": \"0\",\n"
                + "        \"interval\": \"0\",\n"
                + "        \"times\": \"0\"\n"
                + "      },\n"
                + "      \"dataCenterInfo\": {\n"
                + "        \"name\": \"string\",\n"
                + "        \"region\": \"string\",\n"
                + "        \"availableZone\": \"string\"\n"
                + "      },\n"
                + "      \"timestamp\": \"333333\",\n"
                + "      \"modTimestamp\": \"4444444\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";

        httpResponse.setContent(responseString);

        Mockito.when(serviceCenterRawClient.getHttpRequest("/registry/health", null, null)).thenReturn(httpResponse);

        ServiceCenterClient serviceCenterClient = new ServiceCenterClient(serviceCenterRawClient);
        MicroserviceInstancesResponse serviceCenterInstances = serviceCenterClient.getServiceCenterInstances();

        Assert.assertNotNull(serviceCenterInstances);
        Assert.assertEquals(1, serviceCenterInstances.getInstances().size());
        Assert.assertEquals("111111", serviceCenterInstances.getInstances().get(0).getInstanceId());
        Assert.assertEquals("222222", serviceCenterInstances.getInstances().get(0).getServiceId());
    }

    @Test
    public void TestRegistryService() throws IOException {

        ServiceCenterRawClient serviceCenterRawClient = Mockito.mock(ServiceCenterRawClient.class);

        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatusCode(200);
        httpResponse.setMessage("ok");
        httpResponse.setContent("{\"serviceId\": \"111111\"}");

        Microservice microservice = new Microservice();
        microservice.setServiceName("Test");
        microservice.setServiceId("111111");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);

        Mockito.when(serviceCenterRawClient
                .postHttpRequest("/registry/microservices", null, objectMapper.writeValueAsString(microservice)))
                .thenReturn(httpResponse);

        ServiceCenterClient serviceCenterClient = new ServiceCenterClient(serviceCenterRawClient);
        String actualResponse = serviceCenterClient.registerMicroservice(microservice);

        Assert.assertNotNull(actualResponse);
        Assert.assertEquals("{\"serviceId\": \"111111\"}", actualResponse);
    }

    @Test
    public void TestGetServiceMessage() throws IOException {

        ServiceCenterRawClient serviceCenterRawClient = Mockito.mock(ServiceCenterRawClient.class);

        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatusCode(200);
        httpResponse.setMessage("ok");
        String responseString = "{\n"
                + "  \"service\": {\n"
                + "      \"serviceId\": \"111111\",\n"
                + "      \"environment\": \"string\",\n"
                + "      \"appId\": \"string\",\n"
                + "      \"serviceName\": \"string\",\n"
                + "      \"version\": \"string\",\n"
                + "      \"description\": \"string\",\n"
                + "      \"level\": \"string\",\n"
                + "      \"registerBy\": \"string\",\n"
                + "      \"schemas\": [\n"
                + "        \"string\"\n"
                + "      ],\n"
                + "      \"status\": \"UP\",\n"
                + "      \"timestamp\": \"string\",\n"
                + "      \"modTimestamp\": \"string\",\n"
                + "      \"framework\": {\n"
                + "        \"name\": \"string\",\n"
                + "        \"version\": \"string\"\n"
                + "      },\n"
                + "      \"paths\": [\n"
                + "        {\n"
                + "          \"Path\": \"string\",\n"
                + "          \"Property\": {\n"
                + "            \"additionalProp1\": \"string\",\n"
                + "            \"additionalProp2\": \"string\",\n"
                + "            \"additionalProp3\": \"string\"\n"
                + "          }\n"
                + "        }\n"
                + "      ],\n"
                + "      \"properties\": {\n"
                + "        \"additionalProp1\": \"string\",\n"
                + "        \"additionalProp2\": \"string\",\n"
                + "        \"additionalProp3\": \"string\"\n"
                + "      }\n"
                + "    }\n"
                + "}";

        httpResponse.setContent(responseString);

        Mockito.when(serviceCenterRawClient.getHttpRequest("/registry/microservices/111111", null, null))
                .thenReturn(httpResponse);

        ServiceCenterClient serviceCenterClient = new ServiceCenterClient(serviceCenterRawClient);
        Microservice microservices = serviceCenterClient.getMicroserviceByServiceId("111111");

        Assert.assertNotNull(microservices);
        Assert.assertEquals("111111", microservices.getServiceId());
    }

    @Test
    public void TestGetServiceList() throws IOException {

        ServiceCenterRawClient serviceCenterRawClient = Mockito.mock(ServiceCenterRawClient.class);

        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatusCode(200);
        httpResponse.setMessage("ok");

        MicroservicesResponse microservicesResponse = new MicroservicesResponse();
        List<Microservice> microserviceList = new ArrayList<Microservice>();
        microserviceList.add(new Microservice("Test1"));
        microserviceList.add(new Microservice("Test2"));
        microserviceList.add(new Microservice("Test3"));
        microservicesResponse.setServices(microserviceList);
        ObjectMapper mapper = new ObjectMapper();
        httpResponse.setContent(mapper.writeValueAsString(microservicesResponse));

        Mockito.when(serviceCenterRawClient.getHttpRequest(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(httpResponse);

        ServiceCenterClient serviceCenterClient = new ServiceCenterClient(serviceCenterRawClient);
        MicroservicesResponse actualMicroservicesResponse = serviceCenterClient.getMicroserviceList();

        Assert.assertNotNull(actualMicroservicesResponse);
        Assert.assertEquals(3, actualMicroservicesResponse.getServices().size());
        Assert.assertEquals("Test1", actualMicroservicesResponse.getServices().get(0).getServiceName());
    }

    @Test
    public void TestQueryServiceId() throws IOException {

        ServiceCenterRawClient serviceCenterRawClient = Mockito.mock(ServiceCenterRawClient.class);

        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatusCode(200);
        httpResponse.setMessage("ok");
        httpResponse.setContent("{\"serviceId\": \"111111\"}");

        Mockito.when(serviceCenterRawClient.getHttpRequest(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(httpResponse);

        ServiceCenterClient serviceCenterClient = new ServiceCenterClient(serviceCenterRawClient);
        Microservice microservice = new Microservice("Test111");
        String actualServiceId = serviceCenterClient.queryServiceId(microservice);

        Assert.assertNotNull(actualServiceId);
        Assert.assertEquals("{\"serviceId\": \"111111\"}", actualServiceId);
    }

    @Test
    public void TestRegisterServiceInstance() throws IOException {

        ServiceCenterRawClient serviceCenterRawClient = Mockito.mock(ServiceCenterRawClient.class);

        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatusCode(200);
        httpResponse.setMessage("ok");
        httpResponse.setContent("{\"instanceId\": \"111111\"}");

        MicroserviceInstance instance = new MicroserviceInstance();
        instance.setInstanceId("111111");
        instance.setServiceId("222222");
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE,true);

        Mockito.when(serviceCenterRawClient.postHttpRequest("/registry/microservices/222222/instances", null,
                mapper.writeValueAsString(instance)))
                .thenReturn(httpResponse);

        ServiceCenterClient serviceCenterClient = new ServiceCenterClient(serviceCenterRawClient);
        String actualResponse = serviceCenterClient.registerMicroserviceInstance(instance, "222222");

        Assert.assertNotNull(actualResponse);
        Assert.assertEquals("{\"instanceId\": \"111111\"}", actualResponse);
    }

    @Test
    public void TestDeleteServiceInstance() throws IOException {

        ServiceCenterRawClient serviceCenterRawClient = Mockito.mock(ServiceCenterRawClient.class);

        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatusCode(200);
        httpResponse.setMessage("ok");
        httpResponse.setContent("Delete OK");

        Mockito.when(serviceCenterRawClient.deleteHttpRequest(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(httpResponse);

        ServiceCenterClient serviceCenterClient = new ServiceCenterClient(serviceCenterRawClient);
        String result = serviceCenterClient.deleteMicroserviceInstance("111", "222");

        Assert.assertNotNull(result);
        Assert.assertEquals("Delete OK", result);
    }

    @Test
    public void TestGetServiceInstanceList() throws IOException {

        ServiceCenterRawClient serviceCenterRawClient = Mockito.mock(ServiceCenterRawClient.class);

        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatusCode(200);
        httpResponse.setMessage("ok");
        String responseString = "{\n"
                + "  \"instances\": [\n"
                + "    {\n"
                + "      \"instanceId\": \"111111\",\n"
                + "      \"serviceId\": \"222222\",\n"
                + "      \"version\": \"1.0\",\n"
                + "      \"hostName\": \"Test\",\n"
                + "      \"endpoints\": [\n"
                + "        \"string\"\n"
                + "      ],\n"
                + "      \"status\": \"UP\",\n"
                + "      \"timestamp\": \"333333\",\n"
                + "      \"modTimestamp\": \"4444444\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";

        httpResponse.setContent(responseString);

        Mockito.when(serviceCenterRawClient.getHttpRequest("/registry/microservices/222222/instances", null, null))
                .thenReturn(httpResponse);

        ServiceCenterClient serviceCenterClient = new ServiceCenterClient(serviceCenterRawClient);
        MicroserviceInstancesResponse serviceCenterInstances = serviceCenterClient
                .getMicroserviceInstanceList("222222");

        Assert.assertNotNull(serviceCenterInstances);
        Assert.assertEquals(1, serviceCenterInstances.getInstances().size());
        Assert.assertEquals("111111", serviceCenterInstances.getInstances().get(0).getInstanceId());
        Assert.assertEquals("222222", serviceCenterInstances.getInstances().get(0).getServiceId());
    }

    @Test
    public void TestGetServiceInstanceMessage() throws IOException {

        ServiceCenterRawClient serviceCenterRawClient = Mockito.mock(ServiceCenterRawClient.class);

        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatusCode(200);
        httpResponse.setMessage("ok");
        String responseString = "{\n"
                + "  \"instance\": {\n"
                + "      \"instanceId\": \"111\",\n"
                + "      \"serviceId\": \"222\",\n"
                + "      \"version\": \"1.0\",\n"
                + "      \"hostName\": \"Test\",\n"
                + "      \"endpoints\": [\n"
                + "        \"string\"\n"
                + "      ],\n"
                + "      \"status\": \"UP\",\n"
                + "      \"properties\": {\n"
                + "        \"additionalProp1\": \"string\",\n"
                + "        \"additionalProp2\": \"string\",\n"
                + "        \"additionalProp3\": \"string\"\n"
                + "      },\n"
                + "      \"healthCheck\": {\n"
                + "        \"mode\": \"push\",\n"
                + "        \"port\": \"0\",\n"
                + "        \"interval\": \"0\",\n"
                + "        \"times\": \"0\"\n"
                + "      },\n"
                + "      \"dataCenterInfo\": {\n"
                + "        \"name\": \"string\",\n"
                + "        \"region\": \"string\",\n"
                + "        \"availableZone\": \"string\"\n"
                + "      },\n"
                + "      \"timestamp\": \"333333\",\n"
                + "      \"modTimestamp\": \"4444444\"\n"
                + "    }\n"
                + "}";

        httpResponse.setContent(responseString);

        Mockito.when(serviceCenterRawClient.getHttpRequest(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(httpResponse);

        ServiceCenterClient serviceCenterClient = new ServiceCenterClient(serviceCenterRawClient);
        MicroserviceInstance responseInstance = serviceCenterClient
                .getMicroserviceInstance("111", "222");

        Assert.assertNotNull(responseInstance);
        Assert.assertEquals("111", responseInstance.getInstanceId());
        Assert.assertEquals("Test", responseInstance.getHostName());
    }

    @Test
    public void TestSendHeartBeats() throws IOException {

        ServiceCenterRawClient serviceCenterRawClient = Mockito.mock(ServiceCenterRawClient.class);

        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatusCode(200);
        httpResponse.setMessage("ok");
        httpResponse.setContent("Send OK");

        HeartbeatsRequest heartbeatsRequest = new HeartbeatsRequest("001", "1001");
        heartbeatsRequest.addInstances(new InstancesRequest("002", "1002"));
        ObjectMapper mapper = new ObjectMapper();

        Mockito
                .when(serviceCenterRawClient.putHttpRequest("/registry/microservices/111/instances/222/heartbeat", null, null))
                .thenReturn(httpResponse);
        Mockito.when(serviceCenterRawClient
                .putHttpRequest("/registry/heartbeats", null, mapper.writeValueAsString(heartbeatsRequest)))
                .thenReturn(httpResponse);

        ServiceCenterClient serviceCenterClient = new ServiceCenterClient(serviceCenterRawClient);
        String actualResponses = serviceCenterClient.sendHeartBeats(heartbeatsRequest);

        Assert.assertNotNull(actualResponses);
        Assert.assertEquals("Send OK", actualResponses);
    }

    @Test
    public void TestUpdateServicesInstanceStatus() throws IOException {

        ServiceCenterRawClient serviceCenterRawClient = Mockito.mock(ServiceCenterRawClient.class);

        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatusCode(200);
        httpResponse.setMessage("ok");
        httpResponse.setContent("Update OK");

        Mockito.when(serviceCenterRawClient.putHttpRequest(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(httpResponse);

        ServiceCenterClient serviceCenterClient = new ServiceCenterClient(serviceCenterRawClient);
        String actualResponse = serviceCenterClient
                .updateMicroservicesInstanceStatus("111", "222", MicroserviceInstanceStatus.UP);

        Assert.assertNotNull(actualResponse);
        Assert.assertEquals("Update OK", actualResponse);
    }
}
