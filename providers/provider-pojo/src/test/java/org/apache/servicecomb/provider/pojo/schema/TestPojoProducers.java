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

package org.apache.servicecomb.provider.pojo.schema;

import org.apache.servicecomb.foundation.test.scaffolding.spring.SpringUtils;
import org.apache.servicecomb.provider.pojo.IPerson;
import org.apache.servicecomb.provider.pojo.Person;
import org.apache.servicecomb.provider.pojo.RpcSchema;
import org.junit.Assert;
import org.junit.Test;

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
    Assert.assertEquals(producer.getProducers().size(), 1);
  }

  @Test
  public void testPojoProducersSchemaNull(@Injectable RpcSchema schema) {
    IPerson bean = new IPerson() {
    };
    Assert.assertSame(bean, producer.postProcessAfterInitialization(bean, "test"));
    Assert.assertEquals(producer.getProducers().size(), 0);
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
    Assert.assertEquals(producer.getProducers().size(), 1);
  }

  @Test
  public void ensureNoInject() {
    SpringUtils.ensureNoInject(PojoProducers.class);
  }
}
