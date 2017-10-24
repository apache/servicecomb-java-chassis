/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.core.definition;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.core.CseContext;
import io.servicecomb.core.definition.classloader.MicroserviceClassLoader;
import io.servicecomb.core.definition.classloader.PrivateMicroserviceClassLoaderFactory;
import io.servicecomb.core.definition.loader.SchemaListenerManager;
import io.servicecomb.core.definition.schema.ConsumerSchemaFactory;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;

public class TestMicroserviceVersionMeta {
  @AfterClass
  public static void teardown() {
    CseContext.getInstance().setConsumerSchemaFactory(null);
    CseContext.getInstance().setSchemaListenerManager(null);
  }

  @Test
  public void construct() {
    String microserviceName = "app:ms";
    String microserviceId = "id";
    Microservice microservice = new Microservice();
    microservice.setVersion("1.0.0");

    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getMicroservice(microserviceId);
        result = microservice;
      }
    };

    List<String> logs = new ArrayList<>();
    CseContext.getInstance().setConsumerSchemaFactory(new MockUp<ConsumerSchemaFactory>() {
      @Mock
      void getOrCreateConsumerSchema(MicroserviceMeta microserviceMeta, Microservice microservice) {
        logs.add("getOrCreateConsumerSchema");
      }
    }.getMockInstance());
    CseContext.getInstance().setSchemaListenerManager(new MockUp<SchemaListenerManager>() {
      @Mock
      void notifySchemaListener(MicroserviceMeta... microserviceMetas) {
        logs.add("notifySchemaListener");
      }
    }.getMockInstance());

    MicroserviceVersionMeta microserviceVersionMeta =
        new MicroserviceVersionMeta(microserviceName, microserviceId, PrivateMicroserviceClassLoaderFactory.INSTANCE);

    Assert.assertThat(logs, Matchers.contains("getOrCreateConsumerSchema", "notifySchemaListener"));
    Assert.assertEquals(microserviceName, microserviceVersionMeta.getMicroserviceMeta().getName());
    Assert.assertThat(microserviceVersionMeta.getMicroserviceMeta().getClassLoader(),
        Matchers.instanceOf(MicroserviceClassLoader.class));
  }
}
