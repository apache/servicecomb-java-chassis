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

package org.apache.servicecomb.swagger;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.test.scaffolding.exception.RuntimeExceptionWithoutStackTrace;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.responses.ApiResponse;

public class TestSwaggerUtils {

  @Test
  public void swaggerToStringNormal() {
    OpenAPI swagger = new OpenAPI();
    String content = SwaggerUtils.swaggerToString(swagger);

    OpenAPI newSwagger = SwaggerUtils.parseSwagger(content);
    Assertions.assertEquals(swagger, newSwagger);
  }


  @Test
  public void parseSwaggerUrlNormal() throws IOException {
    String content = "swagger: \"2.0\"";
    URL url = Mockito.mock(URL.class);
    try (MockedStatic<IOUtils> ioUtilsMockedStatic = Mockito.mockStatic(IOUtils.class)) {
      ioUtilsMockedStatic.when(() -> IOUtils.toString(url, StandardCharsets.UTF_8)).thenReturn(content);
      OpenAPI swagger = Yaml.mapper().readValue(content, OpenAPI.class);
      OpenAPI result = SwaggerUtils.parseSwagger(url);
      Assertions.assertEquals(swagger, result);
    }
  }

  @Test
  public void parseSwaggerUrlException() throws IOException {
    URL url = Mockito.mock(URL.class);
    try (MockedStatic<IOUtils> ioUtilsMockedStatic = Mockito.mockStatic(IOUtils.class)) {
      ioUtilsMockedStatic.when(() -> IOUtils.toString(url, StandardCharsets.UTF_8))
          .thenThrow(new RuntimeExceptionWithoutStackTrace("failed"));
      ServiceCombException exception = Assertions.assertThrows(ServiceCombException.class,
          () -> SwaggerUtils.parseSwagger(url));
      Assertions.assertTrue(exception.getMessage().contains("Parse swagger from url failed, "));
    }
  }

  @Test
  public void parseSwaggerContentException() {
    ServiceCombException exception = Assertions.assertThrows(ServiceCombException.class,
        () -> SwaggerUtils.parseSwagger(""));
    Assertions.assertEquals("Parse swagger from content failed, ", exception.getMessage());
  }

  @Test
  public void correctResponsesOperationFixEmptyDescription() {
    ApiResponse response = new ApiResponse();

    Operation operation = new Operation();
    operation.getResponses().addApiResponse("200", response);

    SwaggerUtils.correctResponses(operation);
    Assertions.assertEquals("response of 200", response.getDescription());
  }

  @Test
  public void correctResponsesOperationNotChangeExistDescription() {
    ApiResponse response = new ApiResponse();
    response.setDescription("description");

    Operation operation = new Operation();
    operation.getResponses().addApiResponse("200", response);

    SwaggerUtils.correctResponses(operation);
    Assertions.assertEquals("description", response.getDescription());
  }

  @Test
  public void correctResponsesOperationDefaultTo200() {
    ApiResponse response = new ApiResponse();

    Operation operation = new Operation();
    operation.getResponses().addApiResponse("default", response);

    SwaggerUtils.correctResponses(operation);
    Assertions.assertSame(response, operation.getResponses().get("200"));
  }

  @Test
  public void correctResponsesOperation2xxTo200() {
    ApiResponse response = new ApiResponse();

    Operation operation = new Operation();
    operation.getResponses().addApiResponse("default", new ApiResponse());
    operation.getResponses().addApiResponse("201", response);
    operation.getResponses().addApiResponse("301", new ApiResponse());

    SwaggerUtils.correctResponses(operation);
    Assertions.assertSame(response, operation.getResponses().get("200"));
  }

  @Test
  public void correctResponsesHavePaths() {
    ApiResponse response = new ApiResponse();

    Operation operation = new Operation();
    operation.getResponses().addApiResponse("200", response);

    PathItem path = new PathItem();
    path.get(operation);

    OpenAPI swagger = new OpenAPI();
    swagger.path("/base", path);

    SwaggerUtils.correctResponses(swagger);

    Assertions.assertEquals("response of 200", response.getDescription());
  }
}
