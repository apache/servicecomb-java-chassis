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

import org.apache.servicecomb.swagger.engine.SwaggerConsumer;
import org.apache.servicecomb.swagger.engine.SwaggerEnvironment;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentsMapper;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.apache.servicecomb.swagger.invocation.schemas.ConsumerAddBodyV1;
import org.apache.servicecomb.swagger.invocation.schemas.ConsumerAddV1;
import org.apache.servicecomb.swagger.invocation.schemas.ConsumerAddWithContext;
import org.apache.servicecomb.swagger.invocation.schemas.PojoAddBodyV1;
import org.apache.servicecomb.swagger.invocation.schemas.PojoAddV1;
import org.apache.servicecomb.swagger.invocation.schemas.models.AddWrapperV1;
import org.junit.Test;

import io.swagger.models.Swagger;
import org.junit.jupiter.api.Assertions;

@SuppressWarnings("unchecked")
public class TestPojoV1V1 {
  @Test
  public void add_add_class() {
    add_add(PojoAddV1.class);
  }

  @Test
  public void add_add_interface() {
    add_add(ConsumerAddV1.class);
  }

  public void add_add(Class<?> clazz) {
    SwaggerEnvironment environment = new SwaggerEnvironment();
    Swagger swagger = SwaggerGenerator.generate(clazz);

    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerAddV1.class, swagger);
    ArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

    Map<String, Object> arguments = new HashMap<>();
    arguments.put("x", 1);
    arguments.put("y", 2);
    SwaggerInvocation invocation = new SwaggerInvocation();

    Map<String, Object> result = mapper.invocationArgumentToSwaggerArguments(invocation, arguments);

    Assertions.assertEquals(1, result.size());
    result = (Map<String, Object>) result.get("addBody");
    Assertions.assertEquals(2, result.size());
    Assertions.assertEquals(1, (int) result.get("x"));
    Assertions.assertEquals(2, (int) result.get("y"));
  }

  @Test
  public void add_addBody() {
    SwaggerEnvironment environment = new SwaggerEnvironment();
    Swagger swagger = SwaggerGenerator.generate(PojoAddBodyV1.class);

    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerAddV1.class, swagger);
    ArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

    Map<String, Object> arguments = new HashMap<>();
    arguments.put("x", 1);
    arguments.put("y", 2);
    SwaggerInvocation invocation = new SwaggerInvocation();

    Map<String, Object> result = mapper.invocationArgumentToSwaggerArguments(invocation, arguments);

    Assertions.assertEquals(1, result.size());
    result = (Map<String, Object>) result.get("addBody");
    Assertions.assertEquals(2, result.size());
    Assertions.assertEquals(1, (int) result.get("x"));
    Assertions.assertEquals(2, (int) result.get("y"));
  }

  @Test
  public void addBody_add() {
    SwaggerEnvironment environment = new SwaggerEnvironment();
    Swagger swagger = SwaggerGenerator.generate(PojoAddV1.class);

    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerAddBodyV1.class, swagger);
    ArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

    Map<String, Object> arguments = new HashMap<>();
    arguments.put("addBody", new AddWrapperV1(1, 2));
    SwaggerInvocation invocation = new SwaggerInvocation();

    Map<String, Object> result = mapper.invocationArgumentToSwaggerArguments(invocation, arguments);

    Assertions.assertSame(arguments.get("addBody"), result.get("addBody"));
  }

  @Test
  public void addBody_addBody() {
    SwaggerEnvironment environment = new SwaggerEnvironment();
    Swagger swagger = SwaggerGenerator.generate(PojoAddBodyV1.class);
    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerAddBodyV1.class, swagger);
    ArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

    Map<String, Object> arguments = new HashMap<>();
    arguments.put("addBody", new AddWrapperV1(1, 2));
    SwaggerInvocation invocation = new SwaggerInvocation();

    Map<String, Object> result = mapper.invocationArgumentToSwaggerArguments(invocation, arguments);

    Assertions.assertEquals(1, result.size());
    AddWrapperV1 wrapperV1 = (AddWrapperV1) result.get("addBody");
    Assertions.assertEquals(1, wrapperV1.getX());
    Assertions.assertEquals(2, wrapperV1.y);

    Assertions.assertSame(arguments.get("addBody"), result.get("addBody"));
  }

  @Test
  public void addWithContext_add() {
    SwaggerEnvironment environment = new SwaggerEnvironment();
    Swagger swagger = SwaggerGenerator.generate(PojoAddV1.class);

    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerAddWithContext.class, swagger);
    ArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

    InvocationContext invocationContext = new InvocationContext();
    invocationContext.addContext("k1", "v1");
    invocationContext.addContext("k2", "v2");
    invocationContext.addLocalContext("k3", "v3");
    invocationContext.addLocalContext("k4", "v4");

    Map<String, Object> arguments = new HashMap<>();
    arguments.put("context", invocationContext);
    arguments.put("x", 1);
    arguments.put("y", 2);
    SwaggerInvocation invocation = new SwaggerInvocation();

    Map<String, Object> result = mapper.invocationArgumentToSwaggerArguments(invocation, arguments);

    Assertions.assertEquals(1, result.size());
    result = (Map<String, Object>) result.get("addBody");
    Assertions.assertEquals(2, result.size());
    Assertions.assertEquals(1, result.get("x"));
    Assertions.assertEquals(2, result.get("y"));

    Assertions.assertEquals(2, invocation.getContext().size());
    Assertions.assertEquals("v1", invocation.getContext().get("k1"));
    Assertions.assertEquals("v2", invocation.getContext().get("k2"));

    Assertions.assertEquals(2, invocation.getLocalContext().size());
    Assertions.assertEquals("v3", invocation.getLocalContext().get("k3"));
    Assertions.assertEquals("v4", invocation.getLocalContext().get("k4"));
  }
}
