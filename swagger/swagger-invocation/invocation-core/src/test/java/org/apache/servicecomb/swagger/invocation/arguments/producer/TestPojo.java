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
package org.apache.servicecomb.swagger.invocation.arguments.producer;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.swagger.engine.SwaggerEnvironment;
import org.apache.servicecomb.swagger.engine.SwaggerProducer;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.schemas.PojoAddBodyV1;
import org.apache.servicecomb.swagger.invocation.schemas.PojoAddV1;
import org.apache.servicecomb.swagger.invocation.schemas.PojoAddWithContextV1;
import org.junit.Assert;
import org.junit.Test;

public class TestPojo {
  static Map<String, Object> addBody = new HashMap<>();

  static {
    addBody.put("x", 1);
    addBody.put("y", 2);
  }

  @Test
  public void add() {
    SwaggerProducer swaggerProducer = new SwaggerEnvironment().createProducer(new PojoAddV1(), null);
    ProducerArgumentsMapper mapper = swaggerProducer.findOperation("add").getArgumentsMapper();

    SwaggerInvocation invocation = new SwaggerInvocation();
    Map<String, Object> arguments = new HashMap<>();
    arguments.put("addBody", addBody);

    Map<String, Object> result = mapper.swaggerArgumentToInvocationArguments(invocation, arguments);

    Assert.assertEquals(2, result.size());
    Assert.assertEquals(1, (int) result.get("x"));
    Assert.assertEquals(2, (int) result.get("y"));
  }

  @Test
  public void addBody() {
    SwaggerProducer swaggerProducer = new SwaggerEnvironment().createProducer(new PojoAddBodyV1(), null);
    ProducerArgumentsMapper mapper = swaggerProducer.findOperation("add").getArgumentsMapper();

    SwaggerInvocation invocation = new SwaggerInvocation();
    Map<String, Object> arguments = new HashMap<>();
    arguments.put("addBody", addBody);

    Map<String, Object> result = mapper.swaggerArgumentToInvocationArguments(invocation, arguments);

    Assert.assertEquals(1, result.size());
    Assert.assertSame(addBody, result.get("addBody"));
  }

  @Test
  public void addWithContext_add() {
    SwaggerProducer swaggerProducer = new SwaggerEnvironment().createProducer(new PojoAddWithContextV1(), null);
    ProducerArgumentsMapper mapper = swaggerProducer.findOperation("add").getArgumentsMapper();

    SwaggerInvocation invocation = new SwaggerInvocation();
    Map<String, Object> arguments = new HashMap<>();
    arguments.put("addBody", addBody);

    Map<String, Object> result = mapper.swaggerArgumentToInvocationArguments(invocation, arguments);

    Assert.assertEquals(3, result.size());
    Assert.assertSame(invocation, result.get("context"));
    Assert.assertEquals(1, result.get("x"));
    Assert.assertEquals(2, result.get("y"));
  }
}
