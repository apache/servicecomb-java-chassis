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

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.servicecomb.foundation.common.base.DynamicEnum;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response.Status.Family;

public final class SwaggerUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerUtils.class);


  private SwaggerUtils() {
  }

  /**
   * Only ones servers and contains only base path.
   */
  public static String getBasePath(OpenAPI swagger) {
    if (swagger.getServers() == null || swagger.getServers().size() == 0) {
      return null;
    }
    return swagger.getServers().get(0).getUrl();
  }

  /**
   * Only ones servers and contains only base path.
   */
  public static void setBasePath(OpenAPI swagger, String basePath) {
    if (swagger.getServers() == null || swagger.getServers().size() == 0) {
      swagger.setServers(Arrays.asList(new Server()));
    }
    swagger.getServers().get(0).setUrl(basePath);
  }

  public static String swaggerToString(OpenAPI swagger) {
    try {
      return Yaml.mapper().writeValueAsString(swagger);
    } catch (Throwable e) {
      throw new ServiceCombException("Convert swagger to string failed, ", e);
    }
  }

  public static OpenAPI parseSwagger(URL url) {
    try {
      String swaggerContent = IOUtils.toString(url, StandardCharsets.UTF_8);
      return internalParseSwagger(swaggerContent);
    } catch (Throwable e) {
      throw new ServiceCombException("Parse swagger from url failed, url=" + url, e);
    }
  }

  public static OpenAPI parseAndValidateSwagger(URL url) {
    OpenAPI swagger = SwaggerUtils.parseSwagger(url);
    return swagger;
  }

  public static OpenAPI parseSwagger(String swaggerContent) {
    try {
      return internalParseSwagger(swaggerContent);
    } catch (Throwable e) {
      throw new ServiceCombException("Parse swagger from content failed, ", e);
    }
  }

  public static OpenAPI parseAndValidateSwagger(String swaggerContent) {
    OpenAPI swagger = SwaggerUtils.parseSwagger(swaggerContent);
    return swagger;
  }

  private static OpenAPI internalParseSwagger(String swaggerContent) throws IOException {
    OpenAPI swagger = Yaml.mapper().readValue(swaggerContent, OpenAPI.class);
    correctResponses(swagger);
    return swagger;
  }

  public static void correctResponses(Operation operation) {
    int okCode = Status.OK.getStatusCode();
    String strOkCode = String.valueOf(okCode);
    ApiResponse okResponse = null;

    for (Entry<String, ApiResponse> responseEntry : operation.getResponses().entrySet()) {
      ApiResponse response = responseEntry.getValue();
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
      operation.getResponses().addApiResponse(strOkCode, okResponse);
    }
  }

  public static void correctResponses(OpenAPI swagger) {
    if (swagger.getPaths() == null) {
      return;
    }

    for (PathItem path : swagger.getPaths().values()) {
      for (Operation operation : path.readOperations()) {
        correctResponses(operation);
      }
    }
  }


  public static void addDefinitions(OpenAPI swagger, Type paramType) {
    JavaType javaType = TypeFactory.defaultInstance().constructType(paramType);
    if (javaType.isTypeOrSubTypeOf(DynamicEnum.class)) {
      return;
    }
    Map<String, Schema> models = ModelConverters.getInstance().readAll(javaType);
    for (Entry<String, Schema> entry : models.entrySet()) {
      Schema schema = swagger.getComponents().getSchemas().put(entry.getKey(), entry.getValue());
      if (schema != entry.getValue()) {
        LOGGER.warn("duplicate param model: " + entry.getKey());
        throw new IllegalArgumentException("duplicate param model: " + entry.getKey());
      }
    }
  }

  public static List<String> convertConsumesOrProduces(String... consumesOrProduces) {
    return Arrays.stream(consumesOrProduces)
        .map(String::trim)
        .filter(StringUtils::isNotEmpty)
        .collect(Collectors.toList());
  }

  public static boolean hasAnnotation(Class<?> cls, Class<? extends Annotation> annotation) {
    if (cls.getAnnotation(annotation) != null) {
      return true;
    }

    for (Method method : cls.getMethods()) {
      if (method.getAnnotation(annotation) != null) {
        return true;
      }
    }

    return false;
  }

  public static boolean isRawJsonType(RequestBody param) {
    Object rawJson = param.getExtensions().get(SwaggerConst.EXT_RAW_JSON_TYPE);
    if (rawJson instanceof Boolean) {
      return (boolean) rawJson;
    }
    return false;
  }

  public static String getClassName(Map<String, Object> vendorExtensions) {
    return getVendorExtension(vendorExtensions, SwaggerConst.EXT_JAVA_CLASS);
  }

  public static String getInterfaceName(Map<String, Object> vendorExtensions) {
    return getVendorExtension(vendorExtensions, SwaggerConst.EXT_JAVA_INTF);
  }

  @SuppressWarnings("unchecked")
  public static <T> T getVendorExtension(Map<String, Object> vendorExtensions, String key) {
    if (vendorExtensions == null) {
      return null;
    }

    return (T) vendorExtensions.get(key);
  }

  public static boolean isBean(RequestBody body) {
    MediaType type = body.getContent().values().iterator().next();
    return type.getSchema().get$ref() != null;
  }

  public static boolean isBean(Type type) {
    if (type == null) {
      return false;
    }

    JavaType javaType = TypeFactory.defaultInstance().constructType(type);
    if (javaType.isContainerType() || javaType.isEnumType() || javaType.isTypeOrSubTypeOf(DynamicEnum.class)) {
      return false;
    }

    Class<?> cls = javaType.getRawClass();
    if (ClassUtils.isPrimitiveOrWrapper(cls)) {
      return false;
    }

    return (cls != String.class
        && cls != Date.class
        && cls != LocalDate.class
        && cls != byte[].class
        && cls != File.class
        && !cls.getName().equals("org.springframework.web.multipart.MultipartFile")
        && !Part.class.isAssignableFrom(cls));
  }
}
