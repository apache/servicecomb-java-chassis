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

package org.apache.servicecomb.registry.lightweight;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.foundation.test.scaffolding.time.MockTicker;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.lightweight.model.Microservice;
import org.apache.servicecomb.registry.lightweight.store.InstanceStore;
import org.apache.servicecomb.registry.lightweight.store.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;

import com.google.common.eventbus.EventBus;

import io.vertx.core.json.Json;

public class StoreServiceTest {
  static Endpoint endpoint = Mockito.mock(Endpoint.class);

  static class MockRegisterRequest extends RegisterRequest {
    @Override
    public Endpoint selectFirstEndpoint() {
      return endpoint;
    }
  }

  static ConfigurableEnvironment environment = Mockito.mock(ConfigurableEnvironment.class);

  Self self;

  EventBus eventBus = new EventBus();

  MockTicker ticker = new MockTicker();

  Store store = new Store().setTicker(ticker);

  DiscoveryClient discoveryClient = Mockito.mock(DiscoveryClient.class);

  StoreService service = new StoreService(eventBus, store, discoveryClient);

  @BeforeEach
  void setUp() {
    MutablePropertySources mutablePropertySources = new MutablePropertySources();
    EnumerablePropertySource<?> propertySource = Mockito.mock(EnumerablePropertySource.class);
    mutablePropertySources.addLast(propertySource);
    Mockito.when(environment.getPropertySources()).thenReturn(mutablePropertySources);
    Mockito.when(propertySource.getPropertyNames()).thenReturn(new String[] {});
    Mockito.when(environment.getProperty("servicecomb.service.application")).thenReturn("app");
    Mockito.when(environment.getProperty("servicecomb.service.name")).thenReturn("svc");
    Mockito.when(environment.getProperty("servicecomb.service.version")).thenReturn("1.0.0.0");
    Mockito.when(environment.getProperty("servicecomb.instance.initialStatus")).thenReturn("UP");

    self = new Self() {
      @Override
      protected RegisterRequest createRegisterRequest() {
        return new MockRegisterRequest();
      }
    }
        .init(environment)
        .addSchema("schema-1", "s1")
        .addEndpoint("rest://1.1.1.1:80");

    Mockito.when(discoveryClient.getInfoAsync(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(CompletableFuture.completedFuture(self.getMicroserviceInfo()));
    Mockito.when(discoveryClient.getInstanceAsync(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(CompletableFuture.completedFuture(self.getInstance()));
  }

  @Test
  void should_register_microservice_and_instance_when_both_not_exist() {
    RegisterRequest request = self.buildRegisterRequest();
    InstanceStore instanceStore = service.register(request);

    assertThat(store.findMicroserviceStore(self.getServiceId()).getMicroservice())
        .isSameAs(self.getMicroservice());
    assertThat(instanceStore.getInstance()).isSameAs(self.getInstance());
    assertThat(self.getInstance().getStatus()).isEqualTo(MicroserviceInstanceStatus.UP);
  }

  @Test
  void should_register_instance_when_microservice_exist() {
    Microservice microservice = Json.decodeValue(Json.encode(self.getMicroservice()), Microservice.class);
    store.addMicroservice(microservice);

    RegisterRequest request = self.buildRegisterRequest();
    InstanceStore instanceStore = service.register(request);

    assertThat(microservice).isNotSameAs(self.getMicroservice());
    assertThat(store.findMicroserviceStore(self.getServiceId()).getMicroservice())
        .isSameAs(microservice);
    assertThat(instanceStore.getInstance()).isSameAs(self.getInstance());
  }


  @Test
  void should_allow_update_instance_status() {
    should_register_microservice_and_instance_when_both_not_exist();

    RegisterRequest request = self.buildRegisterRequest()
        .setStatus(MicroserviceInstanceStatus.TESTING);
    ticker.setValues(1L);
    InstanceStore instanceStore = service.register(request);

    assertThat(self.getInstance().getStatus()).isEqualTo(MicroserviceInstanceStatus.TESTING);
    assertThat(instanceStore.getLastHeartBeat()).isEqualTo(1);
  }

  @Test
  void should_process_as_heartbeat_when_nothing_changed() {
    should_register_microservice_and_instance_when_both_not_exist();
    InstanceStore instanceStore = store.findInstanceStore(self.getInstanceId());
    assertThat(instanceStore.getLastHeartBeat()).isEqualTo(0);

    ticker.setValues(1L);
    should_register_microservice_and_instance_when_both_not_exist();
    assertThat(instanceStore.getLastHeartBeat()).isEqualTo(1);
  }
}
