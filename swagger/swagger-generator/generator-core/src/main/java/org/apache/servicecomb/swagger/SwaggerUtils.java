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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.util.Yaml;

public final class SwaggerUtils {
  private SwaggerUtils() {
  }

  public static String swaggerToString(Swagger swagger) {
    try {
      return Yaml.mapper().writeValueAsString(swagger);
    } catch (Throwable e) {
      throw new ServiceCombException("Convert swagger to string failed, ", e);
    }
  }

  public static Swagger parseSwagger(URL url) {
    try {
      String swaggerContent = IOUtils.toString(url);
      return internalParseSwagger(swaggerContent);
    } catch (Throwable e) {
      throw new ServiceCombException("Parse swagger from url failed, ", e);
    }
  }

  public static Swagger parseSwagger(String swaggerContent) {
    try {
      return internalParseSwagger(swaggerContent);
    } catch (Throwable e) {
      throw new ServiceCombException("Parse swagger from content failed, ", e);
    }
  }

  /**
   * Provide a method to validate swagger. This method is now implemented to check common errors, and the logic
   * will be changed when necessary. For internal use only.
   */
  public static void validateSwagger(Swagger swagger) {
    Map<String, Path> paths = swagger.getPaths();
    for (Path path : paths.values()) {
      Operation operation = path.getPost();
      if (operation != null) {
        List<Parameter> parameters = operation.getParameters();
        for (Parameter parameter : parameters) {
          if (BodyParameter.class.isInstance(parameter)) {
            if (((BodyParameter) parameter).getSchema() == null) {
              throw new ServiceCombException("swagger validator: body parameter schema is empty.");
            }
          }
        }
      }
    }
  }

  private static Swagger internalParseSwagger(String swaggerContent)
      throws JsonParseException, JsonMappingException, IOException {
    Swagger swagger = Yaml.mapper().readValue(swaggerContent, Swagger.class);
    correctResponses(swagger);
    return swagger;
  }

  public static void correctResponses(Operation operation) {
    int okCode = Status.OK.getStatusCode();
    String strOkCode = String.valueOf(okCode);
    Response okResponse = null;

    for (Entry<String, Response> responseEntry : operation.getResponses().entrySet()) {
      Response response = responseEntry.getValue();
      if (StringUtils.isEmpty(response.getDescription())) {
        response.setDescription("response of " + responseEntry.getKey());
      }

      if (operation.getResponses().get(strOkCode) != null) {
        continue;
      }

      int statusCode = NumberUtils.toInt(responseEntry.getKey());
      if ("default".equals(responseEntry.getKey())) {
        statusCode = okCode;
      }
      if (Family.SUCCESSFUL.equals(Family.familyOf(statusCode))) {
        okResponse = response;
      }
    }

    if (okResponse != null) {
      operation.addResponse(strOkCode, okResponse);
    }
  }

  public static void correctResponses(Swagger swagger) {
    if (swagger.getPaths() == null) {
      return;
    }

    for (Path path : swagger.getPaths().values()) {
      for (Operation operation : path.getOperations()) {
        correctResponses(operation);
      }
    }
  }
}
