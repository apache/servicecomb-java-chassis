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

import org.apache.servicecomb.swagger.invocation.models.PojoImpl;
import org.junit.Assert;
import org.junit.Test;

public class TestSwaggerProducerOperation {
  private static SwaggerEnvironment env = new SwaggerEnvironment();

  private static SwaggerProducer producer;

  @Test
  public void testgetParameterType() {
    PojoImpl pojo = new PojoImpl();
    producer = env.createProducer(pojo, null);

    SwaggerProducerOperation swaggerProducerOperation = producer.findOperation("testBytes");
    Assert.assertEquals(1, swaggerProducerOperation.getSwaggerOperation().getOperation().getParameters().size());
    Assert.assertEquals(byte[].class, swaggerProducerOperation.getSwaggerParameterType("bytes"));

    swaggerProducerOperation = producer.findOperation("testSimple");
    Assert.assertEquals(1, swaggerProducerOperation.getSwaggerOperation().getOperation().getParameters().size());
    Assert.assertEquals(Object.class, swaggerProducerOperation.getSwaggerParameterType(
        swaggerProducerOperation.getSwaggerOperation().getOperation().getParameters().get(0).getName()));
  }
}
