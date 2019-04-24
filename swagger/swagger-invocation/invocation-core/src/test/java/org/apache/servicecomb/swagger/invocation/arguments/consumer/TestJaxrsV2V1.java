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
package org.apache.servicecomb.swagger.invocation.arguments.consumer;

import java.util.LinkedHashMap;

import org.apache.servicecomb.swagger.engine.SwaggerConsumer;
import org.apache.servicecomb.swagger.engine.SwaggerEnvironment;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.schemas.ConsumerAddBodyV2;
import org.apache.servicecomb.swagger.invocation.schemas.ConsumerAddV2;
import org.apache.servicecomb.swagger.invocation.schemas.JaxrsAddBeanParamV1;
import org.apache.servicecomb.swagger.invocation.schemas.JaxrsAddBodyV1;
import org.apache.servicecomb.swagger.invocation.schemas.JaxrsAddV1;
import org.apache.servicecomb.swagger.invocation.schemas.models.AddWrapperV2;
import org.junit.Assert;
import org.junit.Test;

import io.swagger.models.Swagger;

public class TestJaxrsV2V1 {
  @Test
  public void add_add() {
    SwaggerEnvironment environment = new SwaggerEnvironment();
    Swagger swagger = SwaggerGenerator.generate(JaxrsAddV1.class);

    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerAddV2.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

    Object[] arguments = new Object[] {1, 2, 3};
    SwaggerInvocation invocation = new SwaggerInvocation();

    mapper.toInvocation(arguments, invocation);

    Assert.assertEquals(2, invocation.getSwaggerArguments().length);
    Assert.assertEquals(1, (int) invocation.getSwaggerArgument(0));
    Assert.assertEquals(2, (int) invocation.getSwaggerArgument(1));
  }

  @Test
  public void add_addBeanParam() {
    SwaggerEnvironment environment = new SwaggerEnvironment();
    Swagger swagger = SwaggerGenerator.generate(JaxrsAddBeanParamV1.class);

    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerAddV2.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

    Object[] arguments = new Object[] {1, 2, 3};
    SwaggerInvocation invocation = new SwaggerInvocation();

    mapper.toInvocation(arguments, invocation);

    Assert.assertEquals(2, invocation.getSwaggerArguments().length);
    Assert.assertEquals(1, (int) invocation.getSwaggerArgument(0));
    Assert.assertEquals(2, (int) invocation.getSwaggerArgument(1));
  }

  @Test
  public void add_addBody() {
    SwaggerEnvironment environment = new SwaggerEnvironment();
    Swagger swagger = SwaggerGenerator.generate(JaxrsAddBodyV1.class);

    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerAddV2.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

    Object[] arguments = new Object[] {1, 2, 3};
    SwaggerInvocation invocation = new SwaggerInvocation();

    mapper.toInvocation(arguments, invocation);

    LinkedHashMap<String, Object> map = invocation.getSwaggerArgument(0);
    Assert.assertEquals(2, map.size());
    Assert.assertEquals(1, map.get("x"));
    Assert.assertEquals(2, map.get("y"));
  }

  @Test
  public void addBody_add() {
    SwaggerEnvironment environment = new SwaggerEnvironment();
    Swagger swagger = SwaggerGenerator.generate(JaxrsAddV1.class);

    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerAddBodyV2.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

    Object[] arguments = new Object[] {new AddWrapperV2(1, 2, 3)};
    SwaggerInvocation invocation = new SwaggerInvocation();

    mapper.toInvocation(arguments, invocation);

    Assert.assertEquals(2, invocation.getSwaggerArguments().length);
    Assert.assertEquals(1, (int) invocation.getSwaggerArgument(0));
    Assert.assertEquals(2, (int) invocation.getSwaggerArgument(1));
  }

  @Test
  public void addBody_addBeanParam() {
    SwaggerEnvironment environment = new SwaggerEnvironment();
    Swagger swagger = SwaggerGenerator.generate(JaxrsAddBeanParamV1.class);

    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerAddBodyV2.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

    Object[] arguments = new Object[] {new AddWrapperV2(1, 2, 3)};
    SwaggerInvocation invocation = new SwaggerInvocation();

    mapper.toInvocation(arguments, invocation);

    Assert.assertEquals(2, invocation.getSwaggerArguments().length);
    Assert.assertEquals(1, (int) invocation.getSwaggerArgument(0));
    Assert.assertEquals(2, (int) invocation.getSwaggerArgument(1));
  }

  @Test
  public void addBody_addBody() {
    SwaggerEnvironment environment = new SwaggerEnvironment();
    Swagger swagger = SwaggerGenerator.generate(JaxrsAddBodyV1.class);
    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerAddBodyV2.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

    Object[] arguments = new Object[] {new AddWrapperV2(1, 2, 3)};
    SwaggerInvocation invocation = new SwaggerInvocation();

    mapper.toInvocation(arguments, invocation);

    Assert.assertSame(arguments[0], invocation.getSwaggerArgument(0));
  }
}
