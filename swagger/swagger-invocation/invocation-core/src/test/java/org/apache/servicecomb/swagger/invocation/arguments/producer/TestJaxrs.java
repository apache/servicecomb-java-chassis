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
import org.apache.servicecomb.swagger.invocation.schemas.JaxrsAddBeanParamV1;
import org.apache.servicecomb.swagger.invocation.schemas.JaxrsAddBodyV1;
import org.apache.servicecomb.swagger.invocation.schemas.JaxrsAddV1;
import org.apache.servicecomb.swagger.invocation.schemas.models.AddBeanParamV1;
import org.apache.servicecomb.swagger.invocation.schemas.models.AddWrapperV1;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestJaxrs {
  @Test
  public void add() {
    SwaggerProducer swaggerProducer = new SwaggerEnvironment().createProducer(new JaxrsAddV1(), null);
    ProducerArgumentsMapper mapper = swaggerProducer.findOperation("add").getArgumentsMapper();

    Map<String, Object> arguments = new HashMap<>();
    arguments.put("x", 1);
    arguments.put("y", 2);
    SwaggerInvocation invocation = new SwaggerInvocation();

    Map<String, Object> result = mapper.swaggerArgumentToInvocationArguments(invocation, arguments);

    Assertions.assertEquals(2, result.size());
    Assertions.assertEquals(1, (int) result.get("x"));
    Assertions.assertEquals(2, (int) result.get("y"));
  }

  @Test
  public void addBeanParam() {
    SwaggerProducer swaggerProducer = new SwaggerEnvironment().createProducer(new JaxrsAddBeanParamV1(), null);
    ProducerArgumentsMapper mapper = swaggerProducer.findOperation("add").getArgumentsMapper();

    Map<String, Object> arguments = new HashMap<>();
    arguments.put("x", 1);
    arguments.put("y", 2);
    SwaggerInvocation invocation = new SwaggerInvocation();

    Map<String, Object> result = mapper.swaggerArgumentToInvocationArguments(invocation, arguments);

    Assertions.assertEquals(1, result.size());
    AddBeanParamV1 paramV1 = (AddBeanParamV1) result.get("wrapper");
    Assertions.assertEquals(1, paramV1.getX());
    Assertions.assertEquals(2, paramV1.y);
  }

  @Test
  public void addBody() {
    SwaggerProducer swaggerProducer = new SwaggerEnvironment().createProducer(new JaxrsAddBodyV1(), null);
    ProducerArgumentsMapper mapper = swaggerProducer.findOperation("add").getArgumentsMapper();

    AddWrapperV1 addBody = new AddWrapperV1();
    Map<String, Object> arguments = new HashMap<>();
    arguments.put("addBody", addBody);

    SwaggerInvocation invocation = new SwaggerInvocation();
    Map<String, Object> result = mapper.swaggerArgumentToInvocationArguments(invocation, arguments);

    Assertions.assertEquals(1, result.size());
    Assertions.assertSame(addBody, result.get("addBody"));
  }
}
