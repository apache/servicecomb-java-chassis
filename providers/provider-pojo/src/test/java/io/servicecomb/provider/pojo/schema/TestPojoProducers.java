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

package io.servicecomb.provider.pojo.schema;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.foundation.test.scaffolding.spring.SpringUtils;
import io.servicecomb.provider.pojo.IPerson;
import io.servicecomb.provider.pojo.Person;
import io.servicecomb.provider.pojo.RpcSchema;
import mockit.Expectations;
import mockit.Injectable;

public class TestPojoProducers {
  PojoProducers producer = new PojoProducers();

  @Test
  public void postProcessBeforeInitialization() {
    Object bean = new Object();
    Assert.assertSame(bean, producer.postProcessBeforeInitialization(bean, "test"));
  }

  @Test
  public void testPojoProducers() {
    Person bean = new Person();
    Assert.assertSame(bean, producer.postProcessAfterInitialization(bean, "test"));
    Assert.assertEquals(producer.getProcucers().size(), 1);
  }

  @Test
  public void testPojoProducersSchemaNull(@Injectable RpcSchema schema) {
    IPerson bean = new IPerson() {
    };
    Assert.assertSame(bean, producer.postProcessAfterInitialization(bean, "test"));
    Assert.assertEquals(producer.getProcucers().size(), 0);
  }

  @RpcSchema
  static class PersonEmptySchema implements IPerson {

  }

  @Test
  public void testPojoProducersSchemaIdNull(@Injectable RpcSchema schema) {
    IPerson bean = new PersonEmptySchema();
    new Expectations() {
      {
      }
    };
    Assert.assertSame(bean, producer.postProcessAfterInitialization(bean, "test"));
    Assert.assertEquals(producer.getProcucers().size(), 1);
  }

  @Test
  public void ensureNoInject() {
    SpringUtils.ensureNoInject(PojoProducers.class);
  }
}
