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
package org.apache.servicecomb.zeroconfig.client;

import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.serviceregistry.api.response.HeartbeatResponse;
import org.apache.servicecomb.serviceregistry.client.http.MicroserviceInstances;
import org.apache.servicecomb.zeroconfig.server.ServerMicroserviceInstance;
import org.mockito.Mock;

import static org.apache.servicecomb.zeroconfig.ZeroConfigRegistryConstants.INSTANCE_ID;
import static org.apache.servicecomb.zeroconfig.ZeroConfigRegistryConstants.INSTANCE_HEARTBEAT_RESPONSE_MESSAGE_OK;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.zeroconfig.server.ZeroConfigRegistryService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TestZeroConfigRegistryClientImpl {

  ZeroConfigRegistryClientImpl target;

  @Mock
  ZeroConfigRegistryService zeroConfigRegistryService;

  @Mock
  MulticastSocket multicastSocket;

  @Mock
  RestTemplate restTemplate;

  // testing data
  String selfServiceId = "123";
  String selfInstanceId = "instanceId";
  String otherServiceId = "456";
  String appId = "appId";
  String serviceName = "serviceName";
  String version = "0.0.0.1";
  String status = "UP";
  String host = "host";
  String schemaId1 = "schemaId1";
  String schemaContent1 = "schemaContent1";
  String newSchemaId = "newSchemaId";
  String newSchemaContent = "newSchemaContent";
  String nonExistSchemaId = "nonExistSchemaId";
  String endpoint1 = "endpoint1";
  String strVersionRule = "0.0.0.0+";

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    target = new ZeroConfigRegistryClientImpl(zeroConfigRegistryService, multicastSocket,
        restTemplate);
  }

  private Microservice prepareService(String serviceId, boolean withId) {
    Microservice microservice = new Microservice();
    if (withId) {
      microservice.setServiceId(serviceId);
    }
    microservice.setServiceName(serviceName);
    microservice.setAppId(appId);
    microservice.setVersion(version);
    microservice.setStatus(status);
    List<String> schemas = new ArrayList<>();
    schemas.add(schemaId1);
    microservice.setSchemas(schemas);
    microservice.addSchema(schemaId1, schemaContent1);
    return microservice;
  }

  private MicroserviceInstance prepareInstance(String instanceId, boolean withId) {
    MicroserviceInstance instance = new MicroserviceInstance();
    if (withId) {
      instance.setInstanceId(instanceId);
    }
    instance.setServiceId(selfServiceId);
    List<String> endpointList = new ArrayList<>();
    endpointList.add(endpoint1);
    instance.setEndpoints(endpointList);
    instance.setStatus(MicroserviceInstanceStatus.UP);
    instance.setHostName(host);
    return instance;
  }

  private ServerMicroserviceInstance prepareServerServiceInstance(boolean withEndpoint) {
    ServerMicroserviceInstance serverServiceInstance = new ServerMicroserviceInstance();
    serverServiceInstance.setServiceId(otherServiceId);
    serverServiceInstance.setInstanceId(selfInstanceId);
    serverServiceInstance.setServiceName(serviceName);
    serverServiceInstance.setAppId(appId);
    serverServiceInstance.setVersion(version);
    serverServiceInstance.setStatus(status);
    List<String> schemas = new ArrayList<>();
    schemas.add(schemaId1);
    serverServiceInstance.setSchemas(schemas);
    if (withEndpoint) {
      List<String> endpointList = new ArrayList<>();
      endpointList.add(endpoint1);
      serverServiceInstance.setEndpoints(endpointList);
    }
    serverServiceInstance.setHostName(host);
    return serverServiceInstance;
  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void test_registerMicroservice_withID_shouldReturnSameID() {
    Microservice selfService = prepareService(selfServiceId, true);
    ClientUtil.setMicroserviceSelf(selfService);

    String returnedServiceId = target.registerMicroservice(selfService);

    Assert.assertEquals(selfServiceId, returnedServiceId);
  }

  @Test
  public void test_registerMicroservice_withoutID_shouldReturnGeneratedID() {
    Microservice serviceWithoutID = prepareService(selfServiceId, false);
    ClientUtil.setMicroserviceSelf(serviceWithoutID);

    String returnedServiceId = target.registerMicroservice(serviceWithoutID);

    Assert.assertEquals(ClientUtil.generateServiceId(serviceWithoutID), returnedServiceId);
  }

  @Test
  public void test_getMicroservice_forItself_shouldReturnItself_And_NotCallZeroConfigRegistryService() {
    ClientUtil.setMicroserviceSelf(prepareService(selfServiceId, true));

    Microservice microservice = target.getMicroservice(selfServiceId);

    Assert
        .assertEquals(microservice.getServiceId(), ClientUtil.getMicroserviceSelf().getServiceId());
    verifyZeroInteractions(zeroConfigRegistryService);
  }

  @Test
  public void test_getMicroservice_forItself_shouldReturnOtherService_And_CallZeroConfigRegistryService() {
    ClientUtil.setMicroserviceSelf(prepareService(selfServiceId, true));

    when(zeroConfigRegistryService.getMicroservice(otherServiceId))
        .thenReturn(prepareServerServiceInstance(true));

    Microservice returnedMicroservice = target.getMicroservice(otherServiceId);

    Assert.assertEquals(otherServiceId, returnedMicroservice.getServiceId());
    verify(zeroConfigRegistryService, times(1)).getMicroservice(otherServiceId);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_isSchemaExist_whenServiceIdIsNull_shouldThrowIllegalArgumentException() {
    ClientUtil.setMicroserviceSelf(prepareService(selfServiceId, false));

    target.isSchemaExist(selfServiceId, schemaId1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_isSchemaExist_whenServiceIdIsNotItselfId_shouldThrowIllegalArgumentException() {
    ClientUtil.setMicroserviceSelf(prepareService(otherServiceId, true));

    target.isSchemaExist(selfServiceId, schemaId1);
  }

  @Test
  public void test_isSchemaExist_whenServiceIdIsItselfId_shouldReturnTrue() {
    ClientUtil.setMicroserviceSelf(prepareService(selfServiceId, true));

    boolean returnedResult = target.isSchemaExist(selfServiceId, schemaId1);
    Assert.assertTrue(returnedResult);
  }

  @Test
  public void test_isSchemaExist_whenSchemaNotExist_shouldReturnFalse() {
    ClientUtil.setMicroserviceSelf(prepareService(selfServiceId, true));

    boolean returnedResult = target.isSchemaExist(selfServiceId, nonExistSchemaId);
    Assert.assertFalse(returnedResult);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_registerSchema_whenServiceIdIsNull_shouldThrowIllegalArgumentException() {
    ClientUtil.setMicroserviceSelf(prepareService(selfServiceId, false));

    target.registerSchema(selfServiceId, schemaId1, schemaContent1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_registerSchema_whenServiceIdIsNotItSelfId_shouldThrowIllegalArgumentException() {
    ClientUtil.setMicroserviceSelf(prepareService(selfServiceId, true));

    target.registerSchema(otherServiceId, schemaId1, schemaContent1);
  }

  @Test
  public void test_registerSchema_whenReigsterForItself_shouldSuceed() {
    ClientUtil.setMicroserviceSelf(prepareService(selfServiceId, true));

    boolean returnedResult = target.registerSchema(selfServiceId, newSchemaId, newSchemaContent);
    Assert.assertTrue(returnedResult);
    Assert.assertEquals(newSchemaContent,
        ClientUtil.getMicroserviceSelf().getSchemaMap().computeIfPresent(newSchemaId, (k, v) -> {
          return v;
        }));
  }

  @Test
  public void test_getSchema_whenForSelfMicroservice_shouldSuceed() {
    ClientUtil.setMicroserviceSelf(prepareService(selfServiceId, true));

    String returnedSchemaContent = target.getSchema(selfServiceId, schemaId1);
    Assert.assertEquals(schemaContent1, returnedSchemaContent);
  }

  @Test
  public void test_getSchema_whenForSelfMicroservice_shouldNotCallZeroConfigRegistryServiceAndSucceed() {
    ClientUtil.setMicroserviceSelf(prepareService(selfServiceId, true));

    String returnedSchemaContent = target.getSchema(selfServiceId, schemaId1);

    Assert.assertEquals(schemaContent1, returnedSchemaContent);
    verifyZeroInteractions(zeroConfigRegistryService);
  }

  @Test
  public void test_getSchema_whenForOtherMicroservice_shouldCallZeroConfigRegistryService() {
    ClientUtil.setMicroserviceSelf(prepareService(selfServiceId, true));
    when(zeroConfigRegistryService.getMicroservice(otherServiceId))
        .thenReturn(prepareServerServiceInstance(true));
    String schemaContentEndpoint = endpoint1 + "/schemaEndpoint/schemas?schemaId=" + schemaId1;
    when(restTemplate.getForObject(schemaContentEndpoint, String.class)).thenReturn(schemaContent1);

    String returnedSchemaContent = target.getSchema(otherServiceId, schemaId1);

    Assert.assertEquals(schemaContent1, returnedSchemaContent);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_getSchema_whenProviderEndpointIsNull_shouldThrowIllegalArgumentException() {
    ClientUtil.setMicroserviceSelf(prepareService(selfServiceId, true));
    when(zeroConfigRegistryService.getMicroservice(otherServiceId))
        .thenReturn(prepareServerServiceInstance(false));

    target.getSchema(otherServiceId, schemaId1);

    verifyZeroInteractions(restTemplate);
  }

  @Test
  public void test_registerMicroserviceInstance_withNullInstanceId_shouldGenerateInstanceId_and_succeed()
      throws IOException {
    ClientUtil.setMicroserviceSelf(prepareService(selfServiceId, true));
    MicroserviceInstance instanceWithoutId = prepareInstance(selfInstanceId, false);
    when(zeroConfigRegistryService.getMicroservice(otherServiceId))
        .thenReturn(prepareServerServiceInstance(false));
    doNothing().when(multicastSocket).send(anyObject());

    String instanceId = target.registerMicroserviceInstance(instanceWithoutId);

    verify(multicastSocket, times(1)).send(anyObject());
    Assert.assertNotNull(instanceId);
    Assert.assertNotNull(ClientUtil.getServiceInstanceMapForHeartbeat());
    Assert
        .assertEquals(instanceId, ClientUtil.getServiceInstanceMapForHeartbeat().get(INSTANCE_ID));
  }

  @Test
  public void test_unregisterMicroserviceInstance_whenInstanceNotExist_shoulReturnFalse()
      throws IOException {
    when(zeroConfigRegistryService.findServiceInstance(selfServiceId, selfInstanceId))
        .thenReturn(Optional.empty());

    boolean returnedResult = target.unregisterMicroserviceInstance(selfServiceId, selfInstanceId);

    Assert.assertFalse(returnedResult);
    verify(multicastSocket, times(0)).send(anyObject());

  }

  @Test
  public void test_unregisterMicroserviceInstance_withNullInstanceId_shouldGenerateInstanceId_and_succeed()
      throws IOException {
    when(zeroConfigRegistryService.findServiceInstance(selfServiceId, selfInstanceId))
        .thenReturn(Optional.of(prepareServerServiceInstance(true)));
    doNothing().when(multicastSocket).send(anyObject());

    boolean returnedResult = target.unregisterMicroserviceInstance(selfServiceId, selfInstanceId);

    Assert.assertTrue(returnedResult);
    verify(multicastSocket, times(1)).send(anyObject());
  }

  @Test
  public void test_heartbeat_forUnhealthyInstance_shouldFail() {
    when(zeroConfigRegistryService.heartbeat(selfServiceId, selfInstanceId)).thenReturn(false);

    HeartbeatResponse returnedResult = target.heartbeat(selfServiceId, selfInstanceId);
    String returnedMessage = returnedResult.getMessage();
    boolean returnedStatusResult = returnedResult.isOk();

    Assert.assertNull(returnedMessage);
    Assert.assertFalse(returnedStatusResult);
  }

  @Test
  public void test_heartbeat_forHealthyInstance_shouldSucceed() {
    when(zeroConfigRegistryService.heartbeat(selfServiceId, selfInstanceId)).thenReturn(true);

    HeartbeatResponse returnedResult = target.heartbeat(selfServiceId, selfInstanceId);
    String returnedMessage = returnedResult.getMessage();
    boolean returnedStatusResult = returnedResult.isOk();

    Assert.assertEquals(INSTANCE_HEARTBEAT_RESPONSE_MESSAGE_OK, returnedMessage);
    Assert.assertTrue(returnedStatusResult);
  }

  @Test
  public void test_findServiceInstances_forNonExistInstance_shouldReturnEmptyResult() {
    List<ServerMicroserviceInstance> serverMicroserviceInstancesList = new ArrayList<>();
    when(zeroConfigRegistryService.findServiceInstances(selfServiceId, selfInstanceId))
        .thenReturn(serverMicroserviceInstancesList);

    MicroserviceInstances returnedResult = target
        .findServiceInstances("", appId, serviceName, strVersionRule, "");

    Assert.assertEquals(0, returnedResult.getInstancesResponse().getInstances().size());
  }

  @Test
  public void test_findServiceInstances_forExistInstance_shouldReturnInstances() {
    List<ServerMicroserviceInstance> serverMicroserviceInstancesList = new ArrayList<>();
    serverMicroserviceInstancesList.add(prepareServerServiceInstance(true));
    when(zeroConfigRegistryService.findServiceInstances(appId, serviceName))
        .thenReturn(serverMicroserviceInstancesList);

    MicroserviceInstances returnedResult = target
        .findServiceInstances("", appId, serviceName, strVersionRule, "");
    List<MicroserviceInstance> returnedInstanceList = returnedResult.getInstancesResponse()
        .getInstances();

    Assert.assertEquals(1, returnedInstanceList.size());
    Assert.assertEquals(selfInstanceId, returnedInstanceList.get(0).getInstanceId());
  }


  @Test
  public void test_findServiceInstance_forNonExistInstance_shouldReturnNull() {
    when(zeroConfigRegistryService.findServiceInstance(selfServiceId, selfInstanceId))
        .thenReturn(Optional.empty());

    MicroserviceInstance returnedResult = target.findServiceInstance(selfServiceId, selfInstanceId);

    Assert.assertNull(returnedResult);
  }

  @Test
  public void test_findServiceInstance_forExistInstance_shouldReturnInstance() {
    when(zeroConfigRegistryService.findServiceInstance(otherServiceId, selfInstanceId))
        .thenReturn(Optional.of(prepareServerServiceInstance(true)));

    MicroserviceInstance returnedResult = target
        .findServiceInstance(otherServiceId, selfInstanceId);

    Assert.assertNotNull(returnedResult);
    Assert.assertEquals(otherServiceId, returnedResult.getServiceId());
    Assert.assertEquals(selfInstanceId, returnedResult.getInstanceId());
  }

}

