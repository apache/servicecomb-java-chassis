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
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.apache.servicecomb.swagger.invocation.schemas.ConsumerAddBodyV1;
import org.apache.servicecomb.swagger.invocation.schemas.ConsumerAddV1;
import org.apache.servicecomb.swagger.invocation.schemas.ConsumerAddWithContext;
import org.apache.servicecomb.swagger.invocation.schemas.PojoAddBodyV1;
import org.apache.servicecomb.swagger.invocation.schemas.PojoAddV1;
import org.apache.servicecomb.swagger.invocation.schemas.models.AddWrapperV1;
import org.junit.Assert;
import org.junit.Test;

import io.swagger.models.Swagger;

public class TestPojoV1V1 {
  @Test
  public void add_add() {
    SwaggerEnvironment environment = new SwaggerEnvironment();
    Swagger swagger = SwaggerGenerator.generate(PojoAddV1.class);

    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerAddV1.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

    Object[] arguments = new Object[] {1, 2};
    SwaggerInvocation invocation = new SwaggerInvocation();

    mapper.toInvocation(arguments, invocation);

    LinkedHashMap<String, Object> map = invocation.getSwaggerArgument(0);
    Assert.assertEquals(2, map.size());
    Assert.assertEquals(1, map.get("x"));
    Assert.assertEquals(2, map.get("y"));
  }

  @Test
  public void add_addBody() {
    SwaggerEnvironment environment = new SwaggerEnvironment();
    Swagger swagger = SwaggerGenerator.generate(PojoAddBodyV1.class);

    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerAddV1.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

    Object[] arguments = new Object[] {1, 2};
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
    Swagger swagger = SwaggerGenerator.generate(PojoAddV1.class);

    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerAddBodyV1.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

    Object[] arguments = new Object[] {new AddWrapperV1(1, 2)};
    SwaggerInvocation invocation = new SwaggerInvocation();

    mapper.toInvocation(arguments, invocation);

    Assert.assertSame(arguments[0], invocation.getSwaggerArgument(0));
  }

  @Test
  public void addBody_addBody() {
    SwaggerEnvironment environment = new SwaggerEnvironment();
    Swagger swagger = SwaggerGenerator.generate(PojoAddBodyV1.class);
    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerAddBodyV1.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

    Object[] arguments = new Object[] {new AddWrapperV1(1, 2)};
    SwaggerInvocation invocation = new SwaggerInvocation();

    mapper.toInvocation(arguments, invocation);

    Assert.assertSame(arguments[0], invocation.getSwaggerArgument(0));
  }

  @Test
  public void addWithContext_add() {
    SwaggerEnvironment environment = new SwaggerEnvironment();
    Swagger swagger = SwaggerGenerator.generate(PojoAddV1.class);

    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerAddWithContext.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

    InvocationContext invocationContext = new InvocationContext();
    invocationContext.addContext("k1", "v1");
    invocationContext.addContext("k2", "v2");
    invocationContext.addLocalContext("k3", "v3");
    invocationContext.addLocalContext("k4", "v4");

    Object[] arguments = new Object[] {invocationContext, 1, 2};
    SwaggerInvocation invocation = new SwaggerInvocation();

    mapper.toInvocation(arguments, invocation);

    LinkedHashMap<String, Object> map = invocation.getSwaggerArgument(0);
    Assert.assertEquals(2, map.size());
    Assert.assertEquals(1, map.get("x"));
    Assert.assertEquals(2, map.get("y"));

    Assert.assertEquals(2, invocation.getContext().size());
    Assert.assertEquals("v1", invocation.getContext().get("k1"));
    Assert.assertEquals("v2", invocation.getContext().get("k2"));

    Assert.assertEquals(2, invocation.getLocalContext().size());
    Assert.assertEquals("v3", invocation.getLocalContext().get("k3"));
    Assert.assertEquals("v4", invocation.getLocalContext().get("k4"));
  }
}
