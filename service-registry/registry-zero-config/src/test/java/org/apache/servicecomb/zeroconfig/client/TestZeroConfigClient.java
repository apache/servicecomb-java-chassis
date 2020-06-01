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

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.zeroconfig.ZeroConfigRegistration;
import org.apache.servicecomb.zeroconfig.server.ServerMicroserviceInstance;
import org.apache.servicecomb.zeroconfig.server.ZeroConfigRegistryService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

public class TestZeroConfigClient {

  ZeroConfigClient target;

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
  String endpoint1 = "endpoint1";
  String strVersionRule = "0.0.0.0+";

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    target = ZeroConfigClient.INSTANCE.initZeroConfigClientWithMocked(zeroConfigRegistryService, multicastSocket, restTemplate);

    prepareSelfMicroserviceAndInstance();
  }

  private void prepareSelfMicroserviceAndInstance() {
    // Microservice
    Microservice microservice = new Microservice();
    microservice.setServiceId(selfServiceId);
    microservice.setServiceName(serviceName);
    microservice.setAppId(appId);
    microservice.setVersion(version);
    microservice.setStatus(status);
    List<String> schemas = new ArrayList<>();
    schemas.add(schemaId1);
    microservice.setSchemas(schemas);
    microservice.addSchema(schemaId1, schemaContent1);

    // MicroserviceInstance
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    microserviceInstance.setServiceId(selfServiceId);
    microserviceInstance.setInstanceId(selfInstanceId);
    List<String> endpointList = new ArrayList<>();
    endpointList.add(endpoint1);
    microserviceInstance.setEndpoints(endpointList);
    microserviceInstance.setStatus(MicroserviceInstanceStatus.UP);
    microserviceInstance.setHostName(host);

    microservice.setInstance(microserviceInstance);
    ZeroConfigRegistration.INSTANCE.setSelfMicroservice(microservice);
    ZeroConfigRegistration.INSTANCE.setSelfMicroserviceInstance(microserviceInstance);
  }

  private ServerMicroserviceInstance prepareServerServiceInstance(boolean withEndpoint) {
    ServerMicroserviceInstance serverServiceInstance = new ServerMicroserviceInstance();
    serverServiceInstance.setServiceId(otherServiceId);
    serverServiceInstance.setInstanceId(selfInstanceId);
    serverServiceInstance.setServiceName(serviceName);
    serverServiceInstance.setAppId(appId);
    serverServiceInstance.setVersion(version);
    serverServiceInstance.setStatus(status);
    serverServiceInstance.setHostName(host);

    List<String> schemas = new ArrayList<>();
    schemas.add(schemaId1);
    serverServiceInstance.setSchemas(schemas);

    if (withEndpoint) {
      List<String> endpointList = new ArrayList<>();
      endpointList.add(endpoint1);
      serverServiceInstance.setEndpoints(endpointList);
    }

    return serverServiceInstance;
  }

  // test register method
  @Test
  public void test_register_withCorrectData_RegisterShouldSucceed() {
    boolean returnedResult = target.register();

    Assert.assertTrue(returnedResult);
  }

  @Test
  public void test_register_MulticastThrowException_RegisterShouldFail() throws IOException {
    doThrow(IOException.class).when(multicastSocket).send(anyObject());

    boolean returnedResult = target.register();

    Assert.assertFalse(returnedResult);
  }

  // test unregister method
  @Test
  public void test_unregister_withCorrectData_UnregisterShouldSucceed() {
    when(zeroConfigRegistryService.findServiceInstance(selfServiceId, selfInstanceId))
        .thenReturn(Optional.of(prepareServerServiceInstance(true)));

    boolean returnedResult = target.unregister();

    Assert.assertTrue(returnedResult);
  }

  @Test
  public void test_unregister_withWrongData_UnregisterShouldFail() {
    when(zeroConfigRegistryService.findServiceInstance(selfServiceId, selfInstanceId))
        .thenReturn(Optional.empty());

    boolean returnedResult = target.unregister();

    Assert.assertFalse(returnedResult);
  }

  @Test
  public void test_unregister_MulticastThrowException_UnregisterShouldFail() throws IOException {
    when(zeroConfigRegistryService.findServiceInstance(selfServiceId, selfInstanceId))
        .thenReturn(Optional.of(prepareServerServiceInstance(true)));
    doThrow(IOException.class).when(multicastSocket).send(anyObject());

    boolean returnedResult = target.unregister();

    Assert.assertFalse(returnedResult);
  }

  // test getMicroservice method
  @Test
  public void test_getMicroservice_forItself_shouldReturnItself_And_NotCallZeroConfigRegistryService() {
    Microservice returnedResult = target.getMicroservice(selfServiceId);

    Assert
        .assertEquals(ZeroConfigRegistration.INSTANCE.getSelfMicroservice().getServiceId(),
            returnedResult.getServiceId());
    verifyZeroInteractions(zeroConfigRegistryService);
  }

  @Test
  public void test_getMicroservice_forOtherService_shouldCallZeroConfigRegistryService() {
    when(zeroConfigRegistryService.getMicroservice(otherServiceId))
        .thenReturn(prepareServerServiceInstance(true));

    Microservice returnedResult = target.getMicroservice(otherServiceId);

    Assert.assertEquals(otherServiceId, returnedResult.getServiceId());
    verify(zeroConfigRegistryService, times(1)).getMicroservice(otherServiceId);
  }

  // test getSchema method
  @Test
  public void test_getSchema_forSelfMicroservice_shouldNotCallZeroConfigRegistryService_And_Succeed() {
    String returnedResult = target.getSchema(selfServiceId, schemaId1);
    Assert.assertEquals(schemaContent1, returnedResult);
    verifyZeroInteractions(zeroConfigRegistryService);
  }

  @Test
  public void test_getSchema_forOtherMicroservice_shouldCallZeroConfigRegistryService_And_Succeed() {
    when(zeroConfigRegistryService.getMicroservice(otherServiceId))
        .thenReturn(prepareServerServiceInstance(true));
    String schemaContentEndpoint = endpoint1 + "/schemaEndpoint/schemas?schemaId=" + schemaId1;
    when(restTemplate.getForObject(schemaContentEndpoint, String.class)).thenReturn(schemaContent1);

    String returnedResult = target.getSchema(otherServiceId, schemaId1);

    Assert.assertEquals(schemaContent1, returnedResult);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_getSchema_whenProviderEndpointIsNull_shouldThrowIllegalArgumentException() {
    when(zeroConfigRegistryService.getMicroservice(otherServiceId))
        .thenReturn(prepareServerServiceInstance(false));

    target.getSchema(otherServiceId, schemaId1);

    verifyZeroInteractions(restTemplate);
  }

  // test findMicroserviceInstance method
  @Test
  public void test_findMicroserviceInstance_forNonExistInstance_shouldReturnNull() {
    when(zeroConfigRegistryService.findServiceInstance(selfServiceId, selfInstanceId))
        .thenReturn(Optional.empty());

    MicroserviceInstance returnedResult = target
        .findMicroserviceInstance(selfServiceId, selfInstanceId);

    Assert.assertNull(returnedResult);
  }

  @Test
  public void test_findMicroServiceInstance_forExistInstance_shouldReturnInstance() {
    when(zeroConfigRegistryService.findServiceInstance(otherServiceId, selfInstanceId))
        .thenReturn(Optional.of(prepareServerServiceInstance(true)));

    MicroserviceInstance returnedResult = target
        .findMicroserviceInstance(otherServiceId, selfInstanceId);

    Assert.assertNotNull(returnedResult);
    Assert.assertEquals(otherServiceId, returnedResult.getServiceId());
    Assert.assertEquals(selfInstanceId, returnedResult.getInstanceId());
  }

  @Test
  public void test_findServiceInstances_forNonExistInstance_shouldReturnEmptyResult() {
    List<ServerMicroserviceInstance> serverMicroserviceInstancesList = new ArrayList<>();
    when(zeroConfigRegistryService.findServiceInstances(selfServiceId, selfInstanceId))
        .thenReturn(serverMicroserviceInstancesList);

    MicroserviceInstances returnedResult = target
        .findServiceInstances(appId, serviceName, strVersionRule);

    Assert.assertEquals(0, returnedResult.getInstancesResponse().getInstances().size());
  }

  @Test
  public void test_findServiceInstances_forExistInstance_shouldReturnInstances() {
    List<ServerMicroserviceInstance> serverMicroserviceInstancesList = new ArrayList<>();
    serverMicroserviceInstancesList.add(prepareServerServiceInstance(true));
    when(zeroConfigRegistryService.findServiceInstances(appId, serviceName))
        .thenReturn(serverMicroserviceInstancesList);

    MicroserviceInstances returnedResult = target
        .findServiceInstances(appId, serviceName, strVersionRule);
    List<MicroserviceInstance> returnedInstanceList = returnedResult.getInstancesResponse()
        .getInstances();

    Assert.assertEquals(1, returnedInstanceList.size());
    Assert.assertEquals(selfInstanceId, returnedInstanceList.get(0).getInstanceId());
  }
}

