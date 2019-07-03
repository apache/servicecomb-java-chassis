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

package org.apache.servicecomb.swagger.engine;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.apache.servicecomb.swagger.invocation.models.ProducerImpl;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.swagger.models.Swagger;

public class TestSwaggerEnvironment {
  private static SwaggerEnvironment env = new SwaggerEnvironment();

  private static SwaggerProducer producer;

  @BeforeClass
  public static void init() {
    producer = env.createProducer(new ProducerImpl(), null);
  }

  @Test
  public void ableToFindVisibleMethod() {
    assertThat(producer.findOperation("visibleMethod"), is(notNullValue()));
  }

  @Test
  public void unableToFindHiddenMethod() {
    assertThat(producer.findOperation("hiddenMethod"), is(nullValue()));
  }

  interface ConsumerIntf {
    void exist();

    void notExist();
  }

  interface ContractIntf {
    void exist();
  }

  @Test
  public void createConsumer_consumerMethodSetBigger() {
    Swagger swagger = SwaggerGenerator.generate(ContractIntf.class);
    SwaggerConsumer swaggerConsumer = env.createConsumer(ConsumerIntf.class, swagger);

    Assert.assertNotNull(swaggerConsumer.findOperation("exist"));
    Assert.assertNull(swaggerConsumer.findOperation("notExist"));
  }
}
