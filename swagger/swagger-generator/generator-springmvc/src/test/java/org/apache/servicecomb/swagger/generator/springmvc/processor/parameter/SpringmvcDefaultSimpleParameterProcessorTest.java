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

package org.apache.servicecomb.swagger.generator.springmvc.processor.parameter;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.servicecomb.swagger.generator.core.OperationGenerator;
import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.SwaggerGeneratorContext;
import org.apache.servicecomb.swagger.generator.springmvc.processor.parameter.SpringmvcDefaultParameterProcessorTest.TestProvider;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;

public class SpringmvcDefaultSimpleParameterProcessorTest {
  @Test
  public void process() throws NoSuchMethodException {
    final SwaggerGenerator swaggerGenerator = new SwaggerGenerator(Mockito.mock(SwaggerGeneratorContext.class),
        TestProvider.class);
    final Method providerMethod = TestProvider.class.getDeclaredMethod("testSimpleParam", String.class);
    final OperationGenerator operationGenerator = new OperationGenerator(swaggerGenerator, providerMethod);

    new SpringmvcDefaultSimpleParameterProcessor().process(operationGenerator, 0);

    final List<Parameter> providerParameters = operationGenerator.getProviderParameters();
    Assert.assertEquals(1, providerParameters.size());
    Parameter parameter = providerParameters.get(0);
    Assert.assertEquals(QueryParameter.class, parameter.getClass());
    Assert.assertEquals("strParam", parameter.getName());
    Assert.assertEquals("query", parameter.getIn());
    QueryParameter queryParameter = (QueryParameter) parameter;
    Assert.assertEquals("string", queryParameter.getType());
  }
}
