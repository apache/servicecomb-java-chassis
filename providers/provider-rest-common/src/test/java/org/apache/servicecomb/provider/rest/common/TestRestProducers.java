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

package org.apache.servicecomb.provider.rest.common;

import org.apache.servicecomb.foundation.test.scaffolding.spring.SpringUtils;
import org.junit.Assert;
import org.junit.Test;

public class TestRestProducers {
  @RestSchema(schemaId = "test")
  public class RestSchemaForTest {
  }

  RestProducers producer = new RestProducers();

  @Test
  public void postProcessBeforeInitialization() {
    Object bean = new Object();
    Assert.assertSame(bean, producer.postProcessBeforeInitialization(bean, "test"));
  }

  @Test
  public void postProcessAfterInitializationNormal() {
    RestSchemaForTest bean = new RestSchemaForTest();
    Assert.assertSame(bean, producer.postProcessAfterInitialization(bean, ""));
    Assert.assertEquals(1, producer.getProducerMetaList().size());
  }

  @Test
  public void postProcessAfterInitializationNoAnnotation() {
    Object bean = new Object();
    Assert.assertSame(bean, producer.postProcessAfterInitialization(bean, ""));
    Assert.assertEquals(0, producer.getProducerMetaList().size());
  }

  @Test
  public void ensureNoInject() {
    SpringUtils.ensureNoInject(RestProducers.class);
  }
}
