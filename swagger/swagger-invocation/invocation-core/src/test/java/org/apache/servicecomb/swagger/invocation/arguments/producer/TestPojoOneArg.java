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

import org.apache.servicecomb.foundation.test.scaffolding.model.Color;
import org.apache.servicecomb.swagger.engine.SwaggerEnvironment;
import org.apache.servicecomb.swagger.engine.SwaggerProducer;
import org.apache.servicecomb.swagger.engine.SwaggerProducerOperation;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.schemas.PojoOneArg;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class TestPojoOneArg {
  @Test
  public void should_mapper_swagger_wrapped_body_field_to_producer_enum() {
    SwaggerProducer swaggerProducer = new SwaggerEnvironment().createProducer(new PojoOneArg(), null);
    SwaggerProducerOperation swaggerProducerOperation = swaggerProducer.findOperation("enumBody");
    Assertions.assertEquals("enumBodyBody",
        swaggerProducerOperation.getSwaggerOperation().getOperation().getRequestBody().getExtensions().get(SwaggerConst.EXT_BODY_NAME));

    ProducerArgumentsMapper mapper = swaggerProducerOperation.getArgumentsMapper();

    SwaggerInvocation invocation = new SwaggerInvocation();
    Map<String, Object> swaggerArguments = new HashMap<>();
    Map<String, Object> arguments = new HashMap<>();
    arguments.put("color", "BLUE");
    swaggerArguments.put("enumBodyBody", arguments);

    Map<String, Object> result = mapper.swaggerArgumentToInvocationArguments(invocation, swaggerArguments);

    Assertions.assertEquals(1, result.size());
    Assertions.assertSame(Color.BLUE, result.get("color"));
  }
}
