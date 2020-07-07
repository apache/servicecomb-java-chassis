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
package org.apache.servicecomb.zeroconfig.server;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestZeroConfigRegistryService {

  ZeroConfigRegistryService target;

  // testing data
  String serviceId = "123";
  String instanceId = "instanceId";
  String instanceId1 = "instanceId1";
  String otherServiceId = "456";
  String appId = "appId";
  String serviceName = "serviceName";
  String otherServiceName = "otherServiceName";
  String version = "0.0.0.1";
  String status = "UP";
  String host = "host";
  String schemaId1 = "schemaId1";
  String endpoint1 = "endpoint1";

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    target = new ZeroConfigRegistryService();
  }

  private ServerMicroserviceInstance prepareServerServiceInstance(boolean withServiceId, String serviceInstanceId) {
    ServerMicroserviceInstance serverServiceInstance = new ServerMicroserviceInstance();
    if (withServiceId){
      serverServiceInstance.setServiceId(serviceId);
    }
    serverServiceInstance.setInstanceId(serviceInstanceId);
    serverServiceInstance.setServiceName(serviceName);
    serverServiceInstance.setAppId(appId);
    serverServiceInstance.setVersion(version);
    serverServiceInstance.setStatus(status);
    List<String> schemas = new ArrayList<>();
    schemas.add(schemaId1);
    serverServiceInstance.setSchemas(schemas);
    List<String> endpointList = new ArrayList<>();
    endpointList.add(endpoint1);
    serverServiceInstance.setEndpoints(endpointList);
    serverServiceInstance.setHostName(host);
    return serverServiceInstance;
  }

  private Map<String, Map<String, ServerMicroserviceInstance>> prepareEmptyServiceInstanceMap() {
    Map<String, Map<String, ServerMicroserviceInstance>> map = new ConcurrentHashMapEx<>();
    return map;
  }

  private Map<String, Map<String, ServerMicroserviceInstance>> prepareServiceInstanceMap(
      boolean multipleInstances) {
    Map<String, Map<String, ServerMicroserviceInstance>> map = new ConcurrentHashMapEx<>();
    Map<String, ServerMicroserviceInstance> instanceIdMap = new ConcurrentHashMapEx<>();
    instanceIdMap.put(instanceId, prepareServerServiceInstance(true, instanceId));
    if (multipleInstances) {
      instanceIdMap.put(instanceId1, prepareServerServiceInstance(true, instanceId1));
    }
    map.put(serviceId, instanceIdMap);
    return map;
  }

  private Map<String, Map<String, ServerMicroserviceInstance>> prepareServiceInstanceMapWihtoutInstance() {
    Map<String, Map<String, ServerMicroserviceInstance>> map = new ConcurrentHashMapEx<>();
    map.put(serviceId, new ConcurrentHashMapEx<>());
    return map;
  }


  @Test(expected = IllegalArgumentException.class)
  public void test_registerMicroserviceInstance_whenServiceIdIsNull_shouldThrowIllegalArgumentException() {
    target.registerMicroserviceInstance(prepareServerServiceInstance(false, instanceId));
  }

  @Test
  public void test_registerMicroserviceInstance_whenInstanceNotExist_shouldRegisterSuccessfully() {
    ServerUtil.microserviceInstanceMap = prepareEmptyServiceInstanceMap();

    target.registerMicroserviceInstance(prepareServerServiceInstance(true, instanceId));

    Assert.assertTrue(ServerUtil.microserviceInstanceMap.containsKey(serviceId));
    Assert.assertTrue(ServerUtil.microserviceInstanceMap.get(serviceId).containsKey(instanceId));
  }

  @Test
  public void test_registerMicroserviceInstance_whenInstanceExist_shouldRegisterSuccessfully() {
    ServerUtil.microserviceInstanceMap = prepareServiceInstanceMap(false);

    target.registerMicroserviceInstance(prepareServerServiceInstance(true, instanceId));

    Assert.assertTrue(ServerUtil.microserviceInstanceMap.containsKey(serviceId));
    Assert.assertTrue(ServerUtil.microserviceInstanceMap.get(serviceId).containsKey(instanceId));
    Assert.assertEquals(1, ServerUtil.microserviceInstanceMap.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_unregisterMicroserviceInstance_whenServiceIdIsNull_shouldThrowIllegalArgumentException() {
    target.unregisterMicroserviceInstance(prepareServerServiceInstance(false, instanceId));
  }

  @Test
  public void test_unregisterMicroserviceInstance_withServiceIdAndInstanceId_and_multipleInstance_shouldRemoveInstance() {
    ServerUtil.microserviceInstanceMap = prepareServiceInstanceMap(true);

    target.unregisterMicroserviceInstance(prepareServerServiceInstance(true, instanceId));

    Assert.assertTrue(ServerUtil.microserviceInstanceMap.containsKey(serviceId));
    Assert.assertTrue(ServerUtil.microserviceInstanceMap.get(serviceId).containsKey(instanceId1));
    Assert.assertFalse(ServerUtil.microserviceInstanceMap.get(serviceId).containsKey(instanceId));
  }

  @Test
  public void test_unregisterMicroserviceInstance_withServiceIdAndInstanceId_and_singleInstance_shouldRemoveService() {
    ServerUtil.microserviceInstanceMap = prepareServiceInstanceMap(false);

    target.unregisterMicroserviceInstance(prepareServerServiceInstance(true, instanceId));

    Assert.assertFalse(ServerUtil.microserviceInstanceMap.containsKey(serviceId));
  }

  @Test
  public void test_findServiceInstance_whenInstanceExist_shouldSucceed() {
    ServerUtil.microserviceInstanceMap = prepareServiceInstanceMap(false);

    ServerMicroserviceInstance returnedResult = target.findServiceInstance(serviceId, instanceId);

    Assert.assertNotNull(returnedResult);
  }

  @Test
  public void test_findServiceInstance_whenInstanceNotExist_shouldReturnEmptyOptionalObject() {
    ServerUtil.microserviceInstanceMap = prepareServiceInstanceMap(false);

    ServerMicroserviceInstance returnedResult = target.findServiceInstance(serviceId, instanceId1);

    Assert.assertNull(returnedResult);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_getMicroserviceInstance_whenServiceHasNoInstance_shouldThrowIllegalArgumentException() {
    ServerUtil.microserviceInstanceMap = prepareServiceInstanceMapWihtoutInstance();

    target.getMicroserviceInstance(serviceId, serviceId);
  }

  @Test
  public void test_getMicroserviceInstance_whenServiceHasInstance_shouldReturnAllInstances() {
    ServerUtil.microserviceInstanceMap = prepareServiceInstanceMap(true);

    List<ServerMicroserviceInstance> returnedResult = target
        .getMicroserviceInstance(serviceId, serviceId);

    Assert.assertNotNull(returnedResult);
    Assert.assertEquals(2, returnedResult.size());
  }

  @Test
  public void test_heartbeat_whenInstanceExist_shouldReturnTrue() {
    ServerUtil.microserviceInstanceMap = prepareServiceInstanceMap(false);

    boolean returnedResult = target.heartbeat(serviceId, instanceId);

    Assert.assertTrue(returnedResult);
  }

  @Test
  public void test_heartbeat_whenInstanceNotExist_shouldReturnFalse() {
    ServerUtil.microserviceInstanceMap = prepareServiceInstanceMap(false);

    boolean returnedResult = target.heartbeat(serviceId, instanceId1);

    Assert.assertFalse(returnedResult);
  }

  @Test
  public void test_getMicroservice_whenServiceExist_shouldReturnService() {
    ServerUtil.microserviceInstanceMap = prepareServiceInstanceMap(true);

    ServerMicroserviceInstance returnedResult = target.getMicroservice(serviceId);

    Assert.assertNotNull(returnedResult);
    Assert.assertEquals(serviceId, returnedResult.getServiceId());
  }

  @Test
  public void test_getMicroservice_whenServiceNotExist_shouldReturnNull() {
    ServerUtil.microserviceInstanceMap = prepareServiceInstanceMap(true);

    ServerMicroserviceInstance returnedResult = target.getMicroservice(otherServiceId);

    Assert.assertNull(returnedResult);
  }

  @Test
  public void test_findServiceInstances_whenInstanceExist_shouldReturnInstanceList() {
    ServerUtil.microserviceInstanceMap = prepareServiceInstanceMap(true);

    List<ServerMicroserviceInstance> returnedResult = target
        .findServiceInstances(appId, serviceName);

    Assert.assertTrue(!returnedResult.isEmpty());
    Assert.assertEquals(2, returnedResult.size());
  }

  @Test
  public void test_findServiceInstances_whenNoInstanceExist_shouldReturnEmptyInstanceList() {
    ServerUtil.microserviceInstanceMap = prepareServiceInstanceMap(true);

    List<ServerMicroserviceInstance> returnedResult = target
        .findServiceInstances(appId, otherServiceName);

    Assert.assertTrue(returnedResult.isEmpty());
  }

}
