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

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.test.scaffolding.model.Color;
import org.apache.servicecomb.foundation.test.scaffolding.model.User;
import org.apache.servicecomb.swagger.engine.SwaggerConsumer;
import org.apache.servicecomb.swagger.engine.SwaggerEnvironment;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.schemas.ConsumerOneArg;
import org.apache.servicecomb.swagger.invocation.schemas.PojoOneArg;
import org.junit.Assert;
import org.junit.Test;

import io.swagger.models.Swagger;

@SuppressWarnings("unchecked")
public class TestPojoOneArg {
  @Test
  public void should_mapper_consumer_simple_to_swagger_body() {
    SwaggerEnvironment environment = new SwaggerEnvironment();
    Swagger swagger = SwaggerGenerator.generate(PojoOneArg.class);

    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerOneArg.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("simple").getArgumentsMapper();

    Map<String, Object> arguments = new HashMap<>();
    arguments.put("name", "name");
    SwaggerInvocation invocation = new SwaggerInvocation();

    Map<String, Object> result = mapper.invocationArgumentToSwaggerArguments(invocation, arguments);

    Assert.assertEquals(1, result.size());
    result = (Map<String, Object>) result.get("name");
    Assert.assertEquals(1, result.size());
    Assert.assertEquals("name", result.get("name"));
  }

  @Test
  public void should_mapper_consumer_bean_to_swagger_body() {
    SwaggerEnvironment environment = new SwaggerEnvironment();
    Swagger swagger = SwaggerGenerator.generate(PojoOneArg.class);

    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerOneArg.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("bean").getArgumentsMapper();

    Map<String, Object> arguments = new HashMap<>();
    arguments.put("user", new User());
    SwaggerInvocation invocation = new SwaggerInvocation();

    Map<String, Object> result = mapper.invocationArgumentToSwaggerArguments(invocation, arguments);

    Assert.assertEquals(1, result.size());
    Assert.assertSame(arguments.get("user"), result.get("user"));
  }

  @Test
  public void should_mapper_consumer_enum_to_swagger_body_field() {
    SwaggerEnvironment environment = new SwaggerEnvironment();
    Swagger swagger = SwaggerGenerator.generate(PojoOneArg.class);

    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerOneArg.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("enumBody").getArgumentsMapper();

    Map<String, Object> arguments = new HashMap<>();
    arguments.put("color", Color.BLUE);
    SwaggerInvocation invocation = new SwaggerInvocation();

    Map<String, Object> result = mapper.invocationArgumentToSwaggerArguments(invocation, arguments);

    Assert.assertEquals(1, result.size());
    result = (Map<String, Object>) result.get("color");
    Assert.assertEquals(1, result.size());
    Assert.assertEquals(Color.BLUE, result.get("color"));
  }
}
