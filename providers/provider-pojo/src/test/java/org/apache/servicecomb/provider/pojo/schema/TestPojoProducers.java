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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestPojoProducers {
  PojoProducers producer = new PojoProducers();

  @Test
  public void postProcessBeforeInitialization() {
    Object bean = new Object();
    Assertions.assertSame(bean, producer.postProcessBeforeInitialization(bean, "test"));
  }

  @Test
  public void testPojoProducers() {
    Person bean = new Person();
    Assertions.assertSame(bean, producer.postProcessAfterInitialization(bean, "test"));
    Assertions.assertEquals(producer.getProducerMetas().size(), 1);
  }

  @Test
  public void testPojoProducersSchemaNull() {
    IPerson bean = new IPerson() {
    };
    Assertions.assertSame(bean, producer.postProcessAfterInitialization(bean, "test"));
    Assertions.assertEquals(producer.getProducerMetas().size(), 0);
  }

  @RpcSchema
  static class PersonEmptySchema implements IPerson {

  }

  @Test
  public void testPojoProducersSchemaIdNull() {
    IPerson bean = new PersonEmptySchema();
    Assertions.assertSame(bean, producer.postProcessAfterInitialization(bean, "test"));
    Assertions.assertEquals(producer.getProducerMetas().size(), 1);
  }

  @Test
  public void ensureNoInject() {
    SpringUtils.ensureNoInject(PojoProducers.class);
  }
}
