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

package org.apache.servicecomb.core.invocation.endpoint;

import static com.google.common.collect.ImmutableMap.of;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.engine.SwaggerConsumer;
import org.apache.servicecomb.swagger.engine.SwaggerConsumerOperation;
import org.apache.servicecomb.swagger.engine.SwaggerEnvironment;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.swagger.models.Swagger;

public class EndpointTest {
  public interface TestSchema {
    void say(Endpoint endpoint);
  }

  @Test
  void should_ignore_endpoint_when_generate_swagger() {
    SwaggerGenerator generator = SwaggerGenerator.create(TestSchema.class);
    generator.getSwaggerGeneratorFeature()
        .setExtJavaInterfaceInVendor(false)
        .setExtJavaClassInVendor(false);
    Swagger swagger = generator.generate();

    assertThat(SwaggerUtils.swaggerToString(swagger))
        .isEqualTo("---\n"
            + "swagger: \"2.0\"\n"
            + "info:\n"
            + "  version: \"1.0.0\"\n"
            + "  title: \"swagger definition for org.apache.servicecomb.core.invocation.endpoint.EndpointTest$TestSchema\"\n"
            + "basePath: \"/TestSchema\"\n"
            + "consumes:\n"
            + "- \"application/json\"\n"
            + "produces:\n"
            + "- \"application/json\"\n"
            + "paths:\n"
            + "  /say:\n"
            + "    post:\n"
            + "      operationId: \"say\"\n"
            + "      parameters: []\n"
            + "      responses:\n"
            + "        \"200\":\n"
            + "          description: \"response of 200\"\n");
  }

  @Test
  void should_set_endpoint_to_invocation_when_map_arguments() {
    SwaggerEnvironment environment = new SwaggerEnvironment();
    SwaggerConsumer consumer = environment
        .createConsumer(TestSchema.class, SwaggerGenerator.generate(TestSchema.class));
    SwaggerConsumerOperation operation = consumer.findOperation("say");

    Endpoint endpoint = new Endpoint(Mockito.mock(Transport.class), null);
    Invocation invocation = Mockito.mock(Invocation.class);
    Holder<Object> holder = new Holder<>();
    Mockito
        .doAnswer(invocationOnMock -> {
          holder.value = invocationOnMock.getArguments()[0];
          return null;
        })
        .when(invocation)
        .setEndpoint(Mockito.any());

    Map<String, Object> argsMap = of("endpoint", endpoint);
    operation.getArgumentsMapper().invocationArgumentToSwaggerArguments(invocation, argsMap);

    assertThat(holder.value).isSameAs(endpoint);
  }
}