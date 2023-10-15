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

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.foundation.test.scaffolding.spring.SpringUtils;
import org.apache.servicecomb.provider.pojo.Person;
import org.apache.servicecomb.provider.pojo.PersonReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

public class TestRpcReferenceProcessor {
  static Environment environment = Mockito.mock(Environment.class);

  RpcReferenceProcessor consumers = new RpcReferenceProcessor();

  @BeforeEach
  public void setUp() {
  }

  @AfterEach
  public void teardown() {
  }

  @Test
  public void postProcessAfterInitialization() {
    Object bean = new Object();
    Assertions.assertSame(bean, consumers.postProcessAfterInitialization(bean, "test"));
  }

  @Test
  public void testReference() {
    SCBEngine scbEngine = SCBBootstrap.createSCBEngineForTest(environment);

    PersonReference bean = new PersonReference();

    Assertions.assertNull(bean.person);

    consumers.setEmbeddedValueResolver((strVal) -> strVal);
    Assertions.assertSame(bean, consumers.postProcessBeforeInitialization(bean, "id"));

    Assertions.assertNotNull(bean.person);

    scbEngine.destroy();
  }

  @Test
  public void testNoReference() {
    Person bean = new Person();

    Assertions.assertNull(bean.name);

    Assertions.assertSame(bean, consumers.postProcessBeforeInitialization(bean, "id"));

    Assertions.assertNull(bean.name);
  }

  @Test
  public void ensureNoInject() {
    SpringUtils.ensureNoInject(RpcReferenceProcessor.class);
  }
}
