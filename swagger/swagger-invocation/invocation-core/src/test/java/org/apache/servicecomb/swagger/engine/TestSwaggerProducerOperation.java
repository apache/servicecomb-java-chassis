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

import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.apache.servicecomb.swagger.invocation.models.PojoImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestSwaggerProducerOperation {
  private static final SwaggerEnvironment env = new SwaggerEnvironment();

  private static SwaggerProducer producer;

  @Test
  public void testGetParameterType() {
    PojoImpl pojo = new PojoImpl();
    producer = env.createProducer(pojo, null);

    SwaggerProducerOperation swaggerProducerOperation = producer.findOperation("testBytes");
    Assertions.assertEquals(true,
        swaggerProducerOperation.getSwaggerOperation().getOperation().getRequestBody() != null);
    Assertions.assertEquals(null, swaggerProducerOperation.getSwaggerParameterType("bytes"));

    swaggerProducerOperation = producer.findOperation("testSimple");
    Assertions.assertEquals(true,
        swaggerProducerOperation.getSwaggerOperation().getOperation().getRequestBody() != null);
    Assertions.assertEquals(Object.class, swaggerProducerOperation.getSwaggerParameterType(
        (String) swaggerProducerOperation.getSwaggerOperation().getOperation().getRequestBody().getExtensions()
            .get(SwaggerConst.EXT_BODY_NAME)));
  }
}
