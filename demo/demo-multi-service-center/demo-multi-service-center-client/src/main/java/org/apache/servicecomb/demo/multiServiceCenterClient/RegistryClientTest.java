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

package org.apache.servicecomb.demo.multiServiceCenterClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.foundation.common.event.SimpleEventBus;
import org.apache.servicecomb.http.client.auth.DefaultRequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpConfiguration.SSLProperties;
import org.apache.servicecomb.service.center.client.AddressManager;
import org.apache.servicecomb.service.center.client.DiscoveryEvents.InstanceChangedEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents;
import org.apache.servicecomb.service.center.client.RegistrationEvents.HeartBeatEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.MicroserviceInstanceRegistrationEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.MicroserviceRegistrationEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.SchemaRegistrationEvent;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterDiscovery;
import org.apache.servicecomb.service.center.client.ServiceCenterDiscovery.SubscriptionKey;
import org.apache.servicecomb.service.center.client.ServiceCenterRegistration;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.SchemaInfo;
import org.springframework.stereotype.Component;

import com.google.common.base.Charsets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.hash.Hashing;

@Component
public class RegistryClientTest implements CategorizedTestCase {
  private List<RegistrationEvents> events = new ArrayList<>();

  private CountDownLatch registrationCounter = new CountDownLatch(1);

  private CountDownLatch discoveryCounter = new CountDownLatch(1);

  private List<MicroserviceInstance> instances;

  // auto test only tests 'hasRegistered=false', can run this client many times to test 'hasRegistered=true'
  private boolean hasRegistered = true;

  @Override
  public void testRestTransport() throws Exception {
    AddressManager addressManager = new AddressManager("default", Arrays.asList("http://127.0.0.1:30100"));
    SSLProperties sslProperties = new SSLProperties();
    sslProperties.setEnabled(false);
    ServiceCenterClient serviceCenterClient = new ServiceCenterClient(addressManager, sslProperties,
        new DefaultRequestAuthHeaderProvider(), "default", null);
    EventBus eventBus = new SimpleEventBus();
    ServiceCenterRegistration serviceCenterRegistration = new ServiceCenterRegistration(serviceCenterClient, eventBus);
    Microservice microservice = new Microservice();
    microservice.setAppId("app_registry");
    microservice.setServiceName("name_registry");
    microservice.setVersion("1.0.0");
    microservice.setEnvironment("development");
    List<String> schemas = new ArrayList<>();
    schemas.add("SchemaA");
    schemas.add("SchemaB");
    microservice.setSchemas(schemas);
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    microserviceInstance.setHostName("host_registry");
    List<String> endpoints = new ArrayList<>();
    endpoints.add("rest://127.0.0.1/");
    microserviceInstance.setEndpoints(endpoints);
    List<SchemaInfo> schemaInfos = new ArrayList<>();
    SchemaInfo schemaA = new SchemaInfo();
    schemaA.setSchemaId("SchemaA");
    schemaA.setSchema("schema contents in any format");
    schemaA.setSummary(
        Hashing.sha256().newHasher().putString("schema contents in any format".toString(), Charsets.UTF_8).hash()
            .toString());
    schemaInfos.add(schemaA);
    SchemaInfo schemaB = new SchemaInfo();
    schemaB.setSchemaId("SchemaA");
    schemaB.setSchema("schema contents in any format");
    schemaB.setSummary(
        Hashing.sha256().newHasher().putString("schema contents in any format".toString(), Charsets.UTF_8).hash()
            .toString());
    schemaInfos.add(schemaB);
    serviceCenterRegistration.setMicroservice(microservice);
    serviceCenterRegistration.setMicroserviceInstance(microserviceInstance);
    serviceCenterRegistration.setSchemaInfos(schemaInfos);
    eventBus.register(this);

    serviceCenterRegistration.startRegistration();
    registrationCounter.await(30000, TimeUnit.MILLISECONDS);
    if (hasRegistered) {
      TestMgr.check(events.size() >= 3, true);
      TestMgr.check(events.get(0).isSuccess(), true);
      TestMgr.check(events.get(0) instanceof MicroserviceRegistrationEvent, true);
      TestMgr.check(events.get(1).isSuccess(), true);
      TestMgr.check(events.get(1) instanceof MicroserviceInstanceRegistrationEvent, true);
      TestMgr.check(events.get(2).isSuccess(), true);
      TestMgr.check(events.get(2) instanceof HeartBeatEvent, true);
    } else {
      TestMgr.check(events.size() >= 4, true);
      TestMgr.check(events.get(0).isSuccess(), true);
      TestMgr.check(events.get(0) instanceof MicroserviceRegistrationEvent, true);
      TestMgr.check(events.get(1).isSuccess(), true);
      TestMgr.check(events.get(1) instanceof SchemaRegistrationEvent, true);
      TestMgr.check(events.get(2).isSuccess(), true);
      TestMgr.check(events.get(2) instanceof MicroserviceInstanceRegistrationEvent, true);
      TestMgr.check(events.get(3).isSuccess(), true);
      TestMgr.check(events.get(3) instanceof HeartBeatEvent, true);
    }

    ServiceCenterDiscovery discovery = new ServiceCenterDiscovery(serviceCenterClient, eventBus);
    discovery.updateMyselfServiceId(microservice.getServiceId());
    discovery.startDiscovery();
    discovery.register(new SubscriptionKey(microservice.getAppId(), microservice.getServiceName()));
    discoveryCounter.await(30000, TimeUnit.MILLISECONDS);
    TestMgr.check(instances != null, true);
    TestMgr.check(instances.size(), 1);
    discovery.stop();
    serviceCenterRegistration.stop();
    serviceCenterClient.deleteMicroserviceInstance(microservice.getServiceId(), microserviceInstance.getInstanceId());
  }

  @Subscribe
  public void onMicroserviceRegistrationEvent(MicroserviceRegistrationEvent event) {
    events.add(event);
  }

  @Subscribe
  public void onSchemaRegistrationEvent(SchemaRegistrationEvent event) {
    events.add(event);
    hasRegistered = false;
  }

  @Subscribe
  public void onMicroserviceInstanceRegistrationEvent(MicroserviceInstanceRegistrationEvent event) {
    events.add(event);
  }

  @Subscribe
  public void onHeartBeatEvent(HeartBeatEvent event) {
    events.add(event);
    registrationCounter.countDown();
  }

  @Subscribe
  public void onInstanceChangedEvent(InstanceChangedEvent event) {
    instances = event.getInstances();
    discoveryCounter.countDown();
  }
}
