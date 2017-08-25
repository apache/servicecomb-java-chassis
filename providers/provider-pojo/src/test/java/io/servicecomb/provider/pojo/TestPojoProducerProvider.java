/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.provider.pojo;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.core.definition.schema.ProducerSchemaFactory;
import io.servicecomb.provider.pojo.schema.PojoProducerMeta;
import io.servicecomb.provider.pojo.schema.PojoProducers;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;

public class TestPojoProducerProvider {
  @Test
  public void testPojoProducerProviderAlreadyInited(@Injectable ProducerSchemaFactory factory,
      @Injectable PojoProducers producers, @Injectable PojoProducerMeta meta, @Mocked RegistryUtils utils,
      @Injectable Microservice service) throws Exception {
    List<PojoProducerMeta> producersMeta = new ArrayList<>();
    producersMeta.add(meta);
    Person bean = new Person();
    new Expectations() {
      {
        producers.getProcucers();
        result = producersMeta;
        meta.getInstance();
        result = bean;
      }
    };
    PojoProducerProvider provider = new PojoProducerProvider();
    Deencapsulation.setField(provider, "producerSchemaFactory", factory);
    Deencapsulation.setField(provider, "pojoProducers", producers);
    provider.init();
    // expectations done in Expectations
  }

  @Test
  public void testPojoProducerProvider(@Injectable ProducerSchemaFactory factory,
      @Injectable PojoProducers producers, @Injectable PojoProducerMeta meta, @Mocked RegistryUtils utils,
      @Injectable Microservice service) throws Exception {
    List<PojoProducerMeta> producersMeta = new ArrayList<>();
    producersMeta.add(meta);
    new Expectations() {
      {
        producers.getProcucers();
        result = producersMeta;
        meta.getInstance();
        result = null;
        meta.getImplementation();
        result = "pojo:io.servicecomb.provider.pojo.Person";
        service.getServiceName();
        result = "test";
      }
    };
    PojoProducerProvider provider = new PojoProducerProvider();
    Deencapsulation.setField(provider, "producerSchemaFactory", factory);
    Deencapsulation.setField(provider, "pojoProducers", producers);
    provider.init();
    Assert.assertEquals(provider.getName(), "pojo");
    // expectations done in Expectations
  }
}
