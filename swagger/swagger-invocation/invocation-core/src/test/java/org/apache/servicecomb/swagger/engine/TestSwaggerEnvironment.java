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

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.common.javassist.JavassistUtils;
import org.apache.servicecomb.engine.SwaggerEnvironmentForTest;
import org.apache.servicecomb.swagger.generator.jaxrs.JaxrsSwaggerGeneratorContext;
import org.apache.servicecomb.swagger.generator.pojo.PojoSwaggerGeneratorContext;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentsMapperConfig;
import org.apache.servicecomb.swagger.invocation.arguments.producer.JaxRSProducerArgumentsMapperFactory;
import org.apache.servicecomb.swagger.invocation.arguments.producer.ProducerArgumentsMapperFactory;
import org.apache.servicecomb.swagger.invocation.arguments.producer.SpringMVCProducerArgumentsMapperFactory;
import org.apache.servicecomb.swagger.invocation.models.ProducerImpl;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import mockit.Deencapsulation;

public class TestSwaggerEnvironment {
  private static SwaggerEnvironmentForTest env = new SwaggerEnvironmentForTest();

  private static SwaggerProducer producer;

  private static ProducerArgumentsMapperFactory defaultProducerArgumentsMapperFactory;

  @BeforeClass
  public static void init() {
    producer = env.createProducer(new ProducerImpl());
    defaultProducerArgumentsMapperFactory = new ProducerArgumentsMapperFactory();
    List<ProducerArgumentsMapperFactory> producerArgumentsMapperFactoryList = new ArrayList<>();
    producerArgumentsMapperFactoryList.add(new JaxRSProducerArgumentsMapperFactory());
    producerArgumentsMapperFactoryList.add(new SpringMVCProducerArgumentsMapperFactory());
    producerArgumentsMapperFactoryList.add(new ProducerArgumentsMapperFactory());

    Deencapsulation.setField(
        env.getSwaggerEnvironment(), "producerArgumentsFactory", defaultProducerArgumentsMapperFactory);
    Deencapsulation.setField(
        env.getSwaggerEnvironment(), "producerArgumentsMapperFactoryList", producerArgumentsMapperFactoryList);
  }

  @AfterClass
  public static void tearDown() {
    JavassistUtils.clearByClassLoader(env.getClassLoader());
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
    SwaggerConsumer swaggerConsumer = env.getSwaggerEnvironment()
        .createConsumer(ConsumerIntf.class, ContractIntf.class);

    Assert.assertNotNull(swaggerConsumer.findOperation("exist"));
    Assert.assertNull(swaggerConsumer.findOperation("notExist"));
  }

  @Test
  public void selectProducerArgumentsMapperFactory() {
    final ArgumentsMapperConfig config = new ArgumentsMapperConfig();
    config.setSwaggerGeneratorContext(new JaxrsSwaggerGeneratorContext());

    final ProducerArgumentsMapperFactory producerArgumentsMapperFactory = env.getSwaggerEnvironment()
        .selectProducerArgumentsMapperFactory(config);

    Assert.assertEquals(JaxRSProducerArgumentsMapperFactory.class, producerArgumentsMapperFactory.getClass());
  }

  @Test
  public void selectProducerArgumentsMapperFactoryOnReturnDefault() {
    final ArgumentsMapperConfig config = new ArgumentsMapperConfig();
    config.setSwaggerGeneratorContext(new PojoSwaggerGeneratorContext());

    final ProducerArgumentsMapperFactory producerArgumentsMapperFactory = env.getSwaggerEnvironment()
        .selectProducerArgumentsMapperFactory(config);

    Assert.assertSame(defaultProducerArgumentsMapperFactory, producerArgumentsMapperFactory);
  }
}
