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

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;

import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.swagger.engine.SwaggerConsumer;
import org.apache.servicecomb.swagger.engine.SwaggerEnvironment;
import org.apache.servicecomb.swagger.engine.SwaggerOperations;
import org.apache.servicecomb.swagger.engine.bootstrap.BootstrapNormal;
import org.apache.servicecomb.swagger.generator.core.CompositeSwaggerGeneratorContext;
import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.annotations.ApiOperation;
import io.swagger.models.Swagger;
import mockit.Expectations;

public class TestConsumerArgumentsMapperCreator {
  public static class AddWrapper {
    private int x;

    public int y;

    public AddWrapper(int x, int y) {
      this.x = x;
      this.y = y;
    }

    public int getX() {
      return x;
    }
  }

  public static class AddWrapperNewVersion extends AddWrapper {
    public int z;

    public AddWrapperNewVersion(int x, int y, int z) {
      super(x, y);
      this.z = z;
    }
  }

  // producer
  interface RpcCompute {
    int add(int x, int y);
  }

  interface RpcComputeNewVersion {
    int add(int x, int y, int z);
  }

  @RequestMapping
  interface RestCompute {
    @PostMapping(path = "/add")
    int add(int x, int y);
  }

  @RequestMapping
  interface RestComputeNewVersion {
    @PostMapping(path = "/add")
    int add(int x, int y, int z);
  }

  // consumer
  interface ConsumerCompute {
    int add(int x, int y);

    @ApiOperation(value = "", nickname = "add")
    int addBody(AddWrapper addBody);

    @ApiOperation(value = "", nickname = "add")
    int addQueryWrapper(AddWrapper wrapper);

    @ApiOperation(value = "", nickname = "add")
    int addWithContext(InvocationContext context, int x, int y);

    @ApiOperation(value = "", nickname = "add")
    int addNewVersion(int x, int y, int z);

    @ApiOperation(value = "", nickname = "add")
    int addBodyNewVersion(AddWrapperNewVersion addBody);

    @ApiOperation(value = "", nickname = "add")
    int addWithContextNewVersion(InvocationContext context, int x, int y, int z);
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void noParameterName() {
    Swagger swagger = new SwaggerGenerator(new CompositeSwaggerGeneratorContext(), RpcCompute.class).generate();
    SwaggerOperations swaggerOperations = new SwaggerOperations(swagger);

    Method method = ReflectUtils.findMethod(ConsumerCompute.class, "add");
    Parameter parameter = method.getParameters()[0];
    new Expectations(parameter) {
      {
        parameter.isNamePresent();
        result = false;
      }
    };

    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(
        "parameter name is not present, method=public abstract int org.apache.servicecomb.swagger.invocation.arguments.consumer.TestConsumerArgumentsMapperCreator$ConsumerCompute.add(int,int)\n"
            + "solution:\n"
            + "  change pom.xml, add compiler argument: -parameters, for example:\n"
            + "    <plugin>\n"
            + "      <groupId>org.apache.maven.plugins</groupId>\n"
            + "      <artifactId>maven-compiler-plugin</artifactId>\n"
            + "      <configuration>\n"
            + "        <compilerArgument>-parameters</compilerArgument>\n"
            + "      </configuration>\n"
            + "    </plugin>");

    new ConsumerArgumentsMapperCreator(null, null, method, swaggerOperations.findOperation("add"));
  }

  // typical transparent rpc mode case
  @Test
  public void add_rpcAdd() {
    SwaggerEnvironment environment = new BootstrapNormal().boot();
    Swagger swagger = new SwaggerGenerator(new CompositeSwaggerGeneratorContext(), RpcCompute.class).generate();
    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerCompute.class, swagger);
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
  public void add_rpcAddNewVersion() {
    SwaggerEnvironment environment = new BootstrapNormal().boot();
    Swagger swagger = new SwaggerGenerator(new CompositeSwaggerGeneratorContext(), RpcComputeNewVersion.class)
        .generate();
    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerCompute.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

    Object[] arguments = new Object[] {1, 2};
    SwaggerInvocation invocation = new SwaggerInvocation();

    mapper.toInvocation(arguments, invocation);

    LinkedHashMap<String, Object> map = invocation.getSwaggerArgument(0);
    Assert.assertEquals(2, map.size());
    Assert.assertEquals(1, map.get("x"));
    Assert.assertEquals(2, map.get("y"));
  }

  // typical transparent rpc mode case
  // new version invoke old version
  @Test
  public void addNewVersion_rpcAdd() {
    SwaggerEnvironment environment = new BootstrapNormal().boot();
    Swagger swagger = new SwaggerGenerator(new CompositeSwaggerGeneratorContext(), RpcCompute.class).generate();
    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerCompute.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("addNewVersion").getArgumentsMapper();

    Object[] arguments = new Object[] {1, 2, 3};
    SwaggerInvocation invocation = new SwaggerInvocation();

    mapper.toInvocation(arguments, invocation);

    LinkedHashMap<String, Object> map = invocation.getSwaggerArgument(0);
    Assert.assertEquals(2, map.size());
    Assert.assertEquals(1, map.get("x"));
    Assert.assertEquals(2, map.get("y"));
  }

  // rarely happens, but can support
  @Test
  public void addBody_rpcAdd() {
    SwaggerEnvironment environment = new BootstrapNormal().boot();
    Swagger swagger = new SwaggerGenerator(new CompositeSwaggerGeneratorContext(), RpcCompute.class).generate();
    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerCompute.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("addBody").getArgumentsMapper();

    Object[] arguments = new Object[] {new AddWrapper(1, 2)};
    SwaggerInvocation invocation = new SwaggerInvocation();

    mapper.toInvocation(arguments, invocation);

    Assert.assertSame(arguments[0], invocation.getSwaggerArgument(0));
  }

  // rarely happens, but can support
  @Test
  public void addBodyNewVersion_rpcAdd() {
    SwaggerEnvironment environment = new BootstrapNormal().boot();
    Swagger swagger = new SwaggerGenerator(new CompositeSwaggerGeneratorContext(), RpcCompute.class).generate();
    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerCompute.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("addBodyNewVersion").getArgumentsMapper();

    Object[] arguments = new Object[] {new AddWrapperNewVersion(1, 2, 3)};
    SwaggerInvocation invocation = new SwaggerInvocation();

    mapper.toInvocation(arguments, invocation);

    Assert.assertSame(arguments[0], invocation.getSwaggerArgument(0));
  }

  @Test
  public void addWithContext_rpcAdd() {
    SwaggerEnvironment environment = new BootstrapNormal().boot();
    Swagger swagger = new SwaggerGenerator(new CompositeSwaggerGeneratorContext(), RpcCompute.class).generate();
    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerCompute.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("addWithContext").getArgumentsMapper();

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

  @Test
  public void addWithContextNewVersion_rpcAdd() {
    SwaggerEnvironment environment = new BootstrapNormal().boot();
    Swagger swagger = new SwaggerGenerator(new CompositeSwaggerGeneratorContext(), RpcCompute.class).generate();
    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerCompute.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("addWithContextNewVersion").getArgumentsMapper();

    InvocationContext invocationContext = new InvocationContext();
    invocationContext.addContext("k1", "v1");
    invocationContext.addContext("k2", "v2");
    invocationContext.addLocalContext("k3", "v3");
    invocationContext.addLocalContext("k4", "v4");

    Object[] arguments = new Object[] {invocationContext, 1, 2, 3};
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

  @Test
  public void add_restAdd() {
    SwaggerEnvironment environment = new BootstrapNormal().boot();
    Swagger swagger = new SwaggerGenerator(new CompositeSwaggerGeneratorContext(), RestCompute.class).generate();
    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerCompute.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

    Object[] arguments = new Object[] {1, 2};
    SwaggerInvocation invocation = new SwaggerInvocation();

    mapper.toInvocation(arguments, invocation);

    Assert.assertEquals(2, invocation.getSwaggerArguments().length);
    Assert.assertEquals(1, (int) invocation.getSwaggerArgument(0));
    Assert.assertEquals(2, (int) invocation.getSwaggerArgument(1));
  }

  @Test
  public void add_restAddNewVersion() {
    SwaggerEnvironment environment = new BootstrapNormal().boot();
    Swagger swagger = new SwaggerGenerator(new CompositeSwaggerGeneratorContext(), RestComputeNewVersion.class)
        .generate();
    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerCompute.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("add").getArgumentsMapper();

    Object[] arguments = new Object[] {1, 2};
    SwaggerInvocation invocation = new SwaggerInvocation();

    mapper.toInvocation(arguments, invocation);

    Assert.assertEquals(3, invocation.getSwaggerArguments().length);
    Assert.assertEquals(1, (int) invocation.getSwaggerArgument(0));
    Assert.assertEquals(2, (int) invocation.getSwaggerArgument(1));
    Assert.assertNull(invocation.getSwaggerArgument(2));
  }

  @Test
  public void addNewVersion_restAdd() {
    SwaggerEnvironment environment = new BootstrapNormal().boot();
    Swagger swagger = new SwaggerGenerator(new CompositeSwaggerGeneratorContext(), RestCompute.class).generate();
    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerCompute.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("addNewVersion").getArgumentsMapper();

    Object[] arguments = new Object[] {1, 2, 3};
    SwaggerInvocation invocation = new SwaggerInvocation();

    mapper.toInvocation(arguments, invocation);

    Assert.assertEquals(2, invocation.getSwaggerArguments().length);
    Assert.assertEquals(1, (int) invocation.getSwaggerArgument(0));
    Assert.assertEquals(2, (int) invocation.getSwaggerArgument(1));
  }

  @Test
  public void addBody_restAdd() {
    SwaggerEnvironment environment = new BootstrapNormal().boot();
    Swagger swagger = new SwaggerGenerator(new CompositeSwaggerGeneratorContext(), RestCompute.class).generate();
    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerCompute.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("addBody").getArgumentsMapper();

    Object[] arguments = new Object[] {new AddWrapper(1, 2)};
    SwaggerInvocation invocation = new SwaggerInvocation();

    mapper.toInvocation(arguments, invocation);

    Assert.assertEquals(2, invocation.getSwaggerArguments().length);
    Assert.assertEquals(1, (int) invocation.getSwaggerArgument(0));
    Assert.assertEquals(2, (int) invocation.getSwaggerArgument(1));
  }

  @Test
  public void addBodyNull_restAdd() {
    SwaggerEnvironment environment = new BootstrapNormal().boot();
    Swagger swagger = new SwaggerGenerator(new CompositeSwaggerGeneratorContext(), RestCompute.class).generate();
    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerCompute.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("addBody").getArgumentsMapper();

    Object[] arguments = new Object[] {null};
    SwaggerInvocation invocation = new SwaggerInvocation();

    mapper.toInvocation(arguments, invocation);

    Assert.assertEquals(2, invocation.getSwaggerArguments().length);
    Assert.assertNull(invocation.getSwaggerArgument(0));
    Assert.assertNull(invocation.getSwaggerArgument(1));
  }

  @Test
  public void addBodyNewVersion_restAdd() {
    SwaggerEnvironment environment = new BootstrapNormal().boot();
    Swagger swagger = new SwaggerGenerator(new CompositeSwaggerGeneratorContext(), RestCompute.class).generate();
    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerCompute.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("addBodyNewVersion").getArgumentsMapper();

    Object[] arguments = new Object[] {new AddWrapperNewVersion(1, 2, 3)};
    SwaggerInvocation invocation = new SwaggerInvocation();

    mapper.toInvocation(arguments, invocation);

    Assert.assertEquals(2, invocation.getSwaggerArguments().length);
    Assert.assertEquals(1, (int) invocation.getSwaggerArgument(0));
    Assert.assertEquals(2, (int) invocation.getSwaggerArgument(1));
  }

  @Test
  public void addWithContext_restAdd() {
    SwaggerEnvironment environment = new BootstrapNormal().boot();
    Swagger swagger = new SwaggerGenerator(new CompositeSwaggerGeneratorContext(), RestCompute.class).generate();
    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerCompute.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("addWithContext").getArgumentsMapper();

    InvocationContext invocationContext = new InvocationContext();
    invocationContext.addContext("k1", "v1");
    invocationContext.addContext("k2", "v2");
    invocationContext.addLocalContext("k3", "v3");
    invocationContext.addLocalContext("k4", "v4");

    Object[] arguments = new Object[] {invocationContext, 1, 2};
    SwaggerInvocation invocation = new SwaggerInvocation();

    mapper.toInvocation(arguments, invocation);

    Assert.assertEquals(2, invocation.getSwaggerArguments().length);
    Assert.assertEquals(1, (int) invocation.getSwaggerArgument(0));
    Assert.assertEquals(2, (int) invocation.getSwaggerArgument(1));

    Assert.assertEquals(2, invocation.getContext().size());
    Assert.assertEquals("v1", invocation.getContext().get("k1"));
    Assert.assertEquals("v2", invocation.getContext().get("k2"));

    Assert.assertEquals(2, invocation.getLocalContext().size());
    Assert.assertEquals("v3", invocation.getLocalContext().get("k3"));
    Assert.assertEquals("v4", invocation.getLocalContext().get("k4"));
  }

  @Test
  public void addWithContextNewVersion_restAdd() {
    SwaggerEnvironment environment = new BootstrapNormal().boot();
    Swagger swagger = new SwaggerGenerator(new CompositeSwaggerGeneratorContext(), RestCompute.class).generate();
    SwaggerConsumer swaggerConsumer = environment.createConsumer(ConsumerCompute.class, swagger);
    ConsumerArgumentsMapper mapper = swaggerConsumer.findOperation("addWithContextNewVersion").getArgumentsMapper();

    InvocationContext invocationContext = new InvocationContext();
    invocationContext.addContext("k1", "v1");
    invocationContext.addContext("k2", "v2");
    invocationContext.addLocalContext("k3", "v3");
    invocationContext.addLocalContext("k4", "v4");

    Object[] arguments = new Object[] {invocationContext, 1, 2, 3};
    SwaggerInvocation invocation = new SwaggerInvocation();

    mapper.toInvocation(arguments, invocation);

    Assert.assertEquals(2, invocation.getSwaggerArguments().length);
    Assert.assertEquals(1, (int) invocation.getSwaggerArgument(0));
    Assert.assertEquals(2, (int) invocation.getSwaggerArgument(1));

    Assert.assertEquals(2, invocation.getContext().size());
    Assert.assertEquals("v1", invocation.getContext().get("k1"));
    Assert.assertEquals("v2", invocation.getContext().get("k2"));

    Assert.assertEquals(2, invocation.getLocalContext().size());
    Assert.assertEquals("v3", invocation.getLocalContext().get("k3"));
    Assert.assertEquals("v4", invocation.getLocalContext().get("k4"));
  }
}
