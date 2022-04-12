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
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.util.Yaml;
import mockit.Expectations;
import mockit.Mocked;

public class TestSwaggerUtils {

  @Test
  public void swaggerToStringNormal() {
    Swagger swagger = new Swagger();
    String content = SwaggerUtils.swaggerToString(swagger);

    Swagger newSwagger = SwaggerUtils.parseSwagger(content);
    Assert.assertEquals(swagger, newSwagger);
  }

  @Test
  public void swaggerToStringException(@Mocked Swagger swagger) {
    new Expectations() {
      {
        swagger.getBasePath();
        result = new RuntimeExceptionWithoutStackTrace();
      }
    };
    ServiceCombException exception = Assertions.assertThrows(ServiceCombException.class,
            () -> SwaggerUtils.swaggerToString(swagger));
    Assertions.assertEquals("Convert swagger to string failed, ", exception.getMessage());
  }

  @Test
  public void parseSwaggerUrlNormal(@Mocked URL url) throws IOException {
    String content = "swagger: \"2.0\"";
    new Expectations(IOUtils.class) {
      {
        IOUtils.toString(url, StandardCharsets.UTF_8);
        result = content;
      }
    };

    Swagger swagger = Yaml.mapper().readValue(content, Swagger.class);
    Swagger result = SwaggerUtils.parseSwagger(url);
    Assert.assertEquals(swagger, result);
  }

  @Test
  public void parseSwaggerUrlException(@Mocked URL url) throws IOException {
    new Expectations(IOUtils.class) {
      {
        IOUtils.toString(url, StandardCharsets.UTF_8);
        result = new RuntimeExceptionWithoutStackTrace("failed");
      }
    };

    ServiceCombException exception = Assertions.assertThrows(ServiceCombException.class,
            () -> SwaggerUtils.parseSwagger(url));
    Assertions.assertTrue(exception.getMessage().contains("Parse swagger from url failed, "));
  }

  @Test
  public void parseSwaggerContentException() {
    ServiceCombException exception = Assertions.assertThrows(ServiceCombException.class,
            () -> SwaggerUtils.parseSwagger(""));
    Assertions.assertEquals("Parse swagger from content failed, ", exception.getMessage());
  }

  @Test
  public void correctResponsesOperationFixEmptyDescription() {
    Response response = new Response();

    Operation operation = new Operation();
    operation.addResponse("200", response);

    SwaggerUtils.correctResponses(operation);
    Assert.assertEquals("response of 200", response.getDescription());
  }

  @Test
  public void correctResponsesOperationNotChangeExistDescription() {
    Response response = new Response();
    response.setDescription("description");

    Operation operation = new Operation();
    operation.addResponse("200", response);

    SwaggerUtils.correctResponses(operation);
    Assert.assertEquals("description", response.getDescription());
  }

  @Test
  public void correctResponsesOperationDefaultTo200() {
    Response response = new Response();

    Operation operation = new Operation();
    operation.addResponse("default", response);

    SwaggerUtils.correctResponses(operation);
    Assert.assertSame(response, operation.getResponses().get("200"));
  }

  @Test
  public void correctResponsesOperation2xxTo200() {
    Response response = new Response();

    Operation operation = new Operation();
    operation.addResponse("default", new Response());
    operation.addResponse("201", response);
    operation.addResponse("301", new Response());

    SwaggerUtils.correctResponses(operation);
    Assert.assertSame(response, operation.getResponses().get("200"));
  }

  @Test
  public void correctResponsesNoPaths() {
    Swagger swagger = new Swagger();

    // not throw exception
    SwaggerUtils.correctResponses(swagger);
  }

  @Test
  public void correctResponsesHavePaths() {
    Response response = new Response();

    Operation operation = new Operation();
    operation.addResponse("200", response);

    Path path = new Path();
    path.set("get", operation);

    Swagger swagger = new Swagger();
    swagger.path("/base", path);

    SwaggerUtils.correctResponses(swagger);

    Assert.assertEquals("response of 200", response.getDescription());
  }

  @Test(expected = ServiceCombException.class)
  public void testInvalidate() {
    URL resource = TestSwaggerUtils.class.getResource("/swagger1.yaml");
    Swagger swagger = SwaggerUtils.parseSwagger(resource);
    SwaggerUtils.validateSwagger(swagger);
  }

  @Test
  public void testInvalidateValid() {
    URL resource = TestSwaggerUtils.class.getResource("/swagger2.yaml");
    Swagger swagger = SwaggerUtils.parseSwagger(resource);
    SwaggerUtils.validateSwagger(swagger);
  }
}
