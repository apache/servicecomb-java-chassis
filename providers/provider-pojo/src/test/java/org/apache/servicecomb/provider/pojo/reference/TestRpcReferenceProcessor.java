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

package org.apache.servicecomb.provider.pojo.reference;

import org.apache.servicecomb.foundation.test.scaffolding.spring.SpringUtils;
import org.apache.servicecomb.provider.pojo.Person;
import org.apache.servicecomb.provider.pojo.PersonReference;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import mockit.Injectable;

public class TestRpcReferenceProcessor {
  RpcReferenceProcessor consumers = new RpcReferenceProcessor();

  @Test
  public void postProcessAfterInitialization() {
    Object bean = new Object();
    Assert.assertSame(bean, consumers.postProcessAfterInitialization(bean, "test"));
  }

  @Test
  public void testReference(@Injectable ApplicationContext applicationContext) throws Exception {
    PersonReference bean = new PersonReference();

    Assert.assertNull(bean.person);

    consumers.setEmbeddedValueResolver((strVal) -> strVal);
    Assert.assertSame(bean, consumers.postProcessBeforeInitialization(bean, "id"));

    Assert.assertNotNull(bean.person);
  }

  @Test
  public void testNoReference(@Injectable ApplicationContext applicationContext) throws Exception {
    Person bean = new Person();

    Assert.assertNull(bean.name);

    Assert.assertSame(bean, consumers.postProcessBeforeInitialization(bean, "id"));

    Assert.assertNull(bean.name);
  }

  @Test
  public void ensureNoInject() {
    SpringUtils.ensureNoInject(RpcReferenceProcessor.class);
  }
}
