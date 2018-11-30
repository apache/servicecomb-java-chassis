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

package org.apache.servicecomb.serviceregistry.client;

import java.io.InputStream;
import java.util.List;

import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.registry.ServiceCenterInfo;
import org.apache.servicecomb.serviceregistry.api.response.GetSchemaResponse;
import org.apache.servicecomb.serviceregistry.client.http.Holder;
import org.apache.servicecomb.serviceregistry.client.http.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class LocalServiceRegistryClientImplTest {
  LocalServiceRegistryClientImpl registryClient;

  String appId = "appId";

  String microserviceName = "ms";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void loadRegistryFile() {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    InputStream is = loader.getResourceAsStream("registry.yaml");
    registryClient = new LocalServiceRegistryClientImpl(is);
  }

  @Test
  public void testLoadRegistryFile() {
    Assert.assertNotNull(registryClient);
    Assert.assertThat(registryClient.getAllMicroservices().size(), Is.is(1));
    List<MicroserviceInstance> m =
        registryClient.findServiceInstance("", "myapp", "springmvctest", DefinitionConst.VERSION_RULE_ALL);
    Assert.assertEquals(1, m.size());

    MicroserviceInstances microserviceInstances =
        registryClient.findServiceInstances("", "myapp", "springmvctest", DefinitionConst.VERSION_RULE_ALL, null);
    List<MicroserviceInstance> mi = microserviceInstances.getInstancesResponse().getInstances();
    Assert.assertEquals(1, mi.size());
  }

  private Microservice mockRegisterMicroservice(String appId, String name, String version) {
    Microservice microservice = new Microservice();
    microservice.setAppId(appId);
    microservice.setServiceName(name);
    microservice.setVersion(version);

    String serviceId = registryClient.registerMicroservice(microservice);
    microservice.setServiceId(serviceId);
    return microservice;
  }

  @Test
  public void getMicroserviceId_appNotMatch() {
    mockRegisterMicroservice("otherApp", microserviceName, "1.0.0");

    Assert.assertNull(registryClient.getMicroserviceId(appId, microserviceName, "1.0.0", ""));
  }

  @Test
  public void getMicroserviceId_nameNotMatch() {
    mockRegisterMicroservice(appId, "otherName", "1.0.0");

    Assert.assertNull(registryClient.getMicroserviceId(appId, microserviceName, "1.0.0", ""));
  }

  @Test
  public void getMicroserviceId_versionNotMatch() {
    mockRegisterMicroservice(appId, microserviceName, "1.0.0");
    Assert.assertNull(registryClient.getMicroserviceId(appId, microserviceName, "2.0.0", ""));
  }

  @Test
  public void getMicroserviceId_latest() {
    Microservice v2 = mockRegisterMicroservice(appId, microserviceName, "2.0.0");
    mockRegisterMicroservice(appId, microserviceName, "1.0.0");

    String serviceId =
        registryClient.getMicroserviceId(appId, microserviceName, DefinitionConst.VERSION_RULE_LATEST, "");
    Assert.assertEquals(v2.getServiceId(), serviceId);
  }

  @Test
  public void getMicroserviceId_fixVersion() {
    Microservice v1 = mockRegisterMicroservice(appId, microserviceName, "1.0.0");
    mockRegisterMicroservice(appId, microserviceName, "2.0.0");

    String serviceId = registryClient.getMicroserviceId(appId, microserviceName, "1.0.0", "");
    Assert.assertEquals(v1.getServiceId(), serviceId);
  }

  @Test
  public void findServiceInstance_noInstances() {
    List<MicroserviceInstance> result =
        registryClient.findServiceInstance("self", appId, microserviceName, DefinitionConst.VERSION_RULE_ALL);

    Assert.assertThat(result, Matchers.nullValue());

    MicroserviceInstances microserviceInstances =
        registryClient.findServiceInstances("self", appId, microserviceName, DefinitionConst.VERSION_RULE_ALL, null);
    Assert.assertThat(microserviceInstances.isMicroserviceNotExist(), Matchers.is(true));
    Assert.assertThat(microserviceInstances.getInstancesResponse(), Matchers.nullValue());
  }

  @Test
  public void findServiceInstance_twoSelectOne() {
    Microservice v1 = mockRegisterMicroservice(appId, microserviceName, "1.0.0");
    mockRegisterMicroservice(appId, microserviceName, "2.0.0");

    MicroserviceInstance instance = new MicroserviceInstance();
    instance.setInstanceId("testid");
    instance.setServiceId(v1.getServiceId());
    registryClient.registerMicroserviceInstance(instance);

    List<MicroserviceInstance> result =
        registryClient.findServiceInstance("self", appId, microserviceName, "1.0.0");

    Assert.assertThat(result, Matchers.contains(instance));

    MicroserviceInstances microserviceInstances =
        registryClient.findServiceInstances("self", appId, microserviceName, "1.0.0", "0");
    List<MicroserviceInstance> results = microserviceInstances.getInstancesResponse().getInstances();

    Assert.assertThat(results, Matchers.contains(instance));
  }

  @Test
  public void registerSchema_microserviceNotExist() {
    mockRegisterMicroservice(appId, microserviceName, "1.0.0");

    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage(Matchers.is("Invalid serviceId, serviceId=notExist"));

    registryClient.registerSchema("notExist", "sid", "content");
  }

  @Test
  public void registerSchema_normal() {
    Microservice v1 = mockRegisterMicroservice(appId, microserviceName, "1.0.0");

    Assert.assertTrue(registryClient.registerSchema(v1.getServiceId(), "sid", "content"));
  }

  @Test
  public void testFindServiceInstance() {
    Microservice microservice = mockRegisterMicroservice(appId, microserviceName, "1.0.0");
    MicroserviceInstance instance = new MicroserviceInstance();
    instance.setServiceId(microservice.getServiceId());
    String instanceId = registryClient.registerMicroserviceInstance(instance);
    Assert.assertNotNull(registryClient.findServiceInstance(microservice.getServiceId(), instanceId));
  }

  @Test
  public void testGetServiceCenterInfo() {
    ServiceCenterInfo serviceCenterInfo = registryClient.getServiceCenterInfo();
    Assert.assertEquals("1.0.0", serviceCenterInfo.getVersion());
  }

  @Test
  public void testGetSchemas() {
    Holder<List<GetSchemaResponse>> schemasHolder = registryClient.getSchemas("001");
    Assert.assertEquals(200, schemasHolder.getStatusCode());
    Assert.assertTrue(schemasHolder.getValue().isEmpty());
  }

  @Test
  public void testLoadSchemaIdsFromRegistryFile() {
    Microservice microservice = registryClient.getMicroservice("001");
    Assert.assertThat(microservice.getSchemas().size(), Is.is(1));
    Assert.assertTrue(microservice.getSchemas().contains("springmvcHello"));
  }
}

