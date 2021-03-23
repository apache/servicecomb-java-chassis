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
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Matchers.any;

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.foundation.test.scaffolding.MockTicker;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.lightweight.store.InstanceStore;
import org.apache.servicecomb.registry.lightweight.store.MicroserviceStore;
import org.apache.servicecomb.registry.lightweight.store.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import io.vertx.core.json.Json;

class StoreServiceTest extends TestBase {
  EventBus eventBus = new EventBus();

  MockTicker ticker = new MockTicker();

  Store store = new Store().setTicker(ticker);

  DiscoveryClient discoveryClient = Mockito.mock(DiscoveryClient.class);

  StoreService service = new StoreService(eventBus, store, discoveryClient);

  @BeforeEach
  void setUp() {
    Mockito.when(discoveryClient.getInfoAsync(any()))
        .thenReturn(CompletableFuture.completedFuture(self.getMicroserviceInfo()));
    Mockito.when(discoveryClient.getInstanceAsync(any()))
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
    store.addMicroservice(microservice, self.getSchemasSummary());

    RegisterRequest request = self.buildRegisterRequest();
    InstanceStore instanceStore = service.register(request);

    assertThat(microservice).isNotSameAs(self.getMicroservice());
    assertThat(store.findMicroserviceStore(self.getServiceId()).getMicroservice())
        .isSameAs(microservice);
    assertThat(instanceStore.getInstance()).isSameAs(self.getInstance());
  }

  @Test
  void should_reject_when_schema_changed_and_has_existing_instance() {
    should_register_microservice_and_instance_when_both_not_exist();

    RegisterRequest request = self.buildRegisterRequest()
        .setInstanceId("new id")
        .setSchemasSummary("new summary");
    Throwable throwable = catchThrowable(() -> service.register(request));

    assertThat(throwable)
        .isInstanceOf(RegisterException.class)
        .hasMessage("schemas changed, but version not changed, and has 1 existing instances");
  }

  SchemaChangedEvent schemaChangedEvent;

  @Subscribe
  public void onSchemaChanged(SchemaChangedEvent event) {
    this.schemaChangedEvent = event;
  }

  @Test
  void should_register_and_notify_when_schema_changed_and_has_not_existing_instance() {
    should_register_microservice_and_instance_when_both_not_exist();

    MicroserviceStore microserviceStore = store.findMicroserviceStore(self.getServiceId());
    assertThat(microserviceStore.hasInstance()).isTrue();
    store.deleteInstance(self.getServiceId(), self.getInstanceId());
    assertThat(microserviceStore.hasInstance()).isFalse();

    eventBus.register(this);

    RegisterRequest request = self.buildRegisterRequest()
        .setSchemasSummary("new summary");
    InstanceStore instanceStore = service.register(request);

    assertThat(microserviceStore.isSchemaChanged("new summary")).isFalse();
    assertThat(instanceStore.getInstance()).isSameAs(self.getInstance());
    assertThat(schemaChangedEvent.getMicroservice()).isSameAs(self.getMicroservice());
  }

  @Test
  void should_allow_update_instance_status() {
    should_register_microservice_and_instance_when_both_not_exist();

    RegisterRequest request = self.buildRegisterRequest()
        .setStatus(MicroserviceInstanceStatus.TESTING);
    ticker.setValues(1);
    InstanceStore instanceStore = service.register(request);

    assertThat(self.getInstance().getStatus()).isEqualTo(MicroserviceInstanceStatus.TESTING);
    assertThat(instanceStore.getLastHeartBeat()).isEqualTo(1);
  }

  @Test
  void should_process_as_heartbeat_when_nothing_changed() {
    should_register_microservice_and_instance_when_both_not_exist();
    InstanceStore instanceStore = store.findInstanceStore(self.getInstanceId());
    assertThat(instanceStore.getLastHeartBeat()).isEqualTo(0);

    ticker.setValues(1);
    should_register_microservice_and_instance_when_both_not_exist();
    assertThat(instanceStore.getLastHeartBeat()).isEqualTo(1);
  }
}