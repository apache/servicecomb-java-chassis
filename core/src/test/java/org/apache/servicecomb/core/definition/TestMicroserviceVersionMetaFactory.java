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

package org.apache.servicecomb.core.definition;

import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.definition.classloader.DefaultMicroserviceClassLoaderFactory;
import org.apache.servicecomb.core.definition.classloader.PrivateMicroserviceClassLoaderFactory;
import org.apache.servicecomb.core.definition.loader.SchemaListenerManager;
import org.apache.servicecomb.core.definition.schema.ConsumerSchemaFactory;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersion;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

public class TestMicroserviceVersionMetaFactory {
  @AfterClass
  public static void teardown() {
    CseContext.getInstance().setConsumerSchemaFactory(null);
    CseContext.getInstance().setSchemaListenerManager(null);
  }

  @Test
  public void construct() {
    MicroserviceVersionMetaFactory factory = new MicroserviceVersionMetaFactory();
    Assert.assertSame(DefaultMicroserviceClassLoaderFactory.INSTANCE,
        Deencapsulation.getField(factory, "classLoaderFactory"));
  }

  @Test
  public void constructWithFactory() {
    MicroserviceVersionMetaFactory factory =
        new MicroserviceVersionMetaFactory(PrivateMicroserviceClassLoaderFactory.INSTANCE);
    Assert.assertSame(PrivateMicroserviceClassLoaderFactory.INSTANCE,
        Deencapsulation.getField(factory, "classLoaderFactory"));
  }

  @Test
  public void create(@Mocked ConsumerSchemaFactory consumerSchemaFactory,
      @Mocked SchemaListenerManager schemaListenerManager) {
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
    CseContext.getInstance().setConsumerSchemaFactory(consumerSchemaFactory);
    CseContext.getInstance().setSchemaListenerManager(schemaListenerManager);

    MicroserviceVersionMetaFactory factory = new MicroserviceVersionMetaFactory();
    MicroserviceVersion microserviceVersion = factory.create(microserviceName, microserviceId);
    Assert.assertThat(microserviceVersion, Matchers.instanceOf(MicroserviceVersionMeta.class));
  }
}
