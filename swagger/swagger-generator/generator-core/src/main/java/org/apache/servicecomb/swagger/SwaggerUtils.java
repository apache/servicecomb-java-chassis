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
import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.common.base.DynamicEnum;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.http.Part;

@SuppressWarnings("rawtypes")
public final class SwaggerUtils {
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
      swagger.setServers(List.of(new Server()));
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

  public static OpenAPI parseAndValidateSwagger(URL url) {
    try {
      String swaggerContent = IOUtils.toString(url, StandardCharsets.UTF_8);
      OpenAPI result = internalParseSwagger(swaggerContent);
      validateSwagger(result);
      return result;
    } catch (Throwable e) {
      throw new ServiceCombException("Parse swagger from url failed, url=" + url, e);
    }
  }

  public static OpenAPI parseSwagger(String swaggerContent) {
    try {
      return internalParseSwagger(swaggerContent);
    } catch (Throwable e) {
      throw new ServiceCombException("Parse swagger from content failed, ", e);
    }
  }

  public static OpenAPI parseAndValidateSwagger(String appId, String microserviceName,
      String schemaId, String swaggerContent) {
    try {
      OpenAPI result = internalParseSwagger(swaggerContent);
      validateSwagger(result);
      return result;
    } catch (Throwable e) {
      throw new ServiceCombException(
          String.format("Parse swagger from content failed, %s/%s/%s",
              appId, microserviceName, schemaId), e);
    }
  }

  private static void validateSwagger(OpenAPI openAPI) {
    if (openAPI.getPaths() == null) {
      return;
    }
    for (PathItem pathItem : openAPI.getPaths().values()) {
      if (pathItem.getGet() != null) {
        validateOperation(pathItem.getGet());
      }
      if (pathItem.getPost() != null) {
        validateOperation(pathItem.getPost());
      }
      if (pathItem.getDelete() != null) {
        validateOperation(pathItem.getDelete());
      }
      if (pathItem.getPut() != null) {
        validateOperation(pathItem.getPut());
      }
      if (pathItem.getPatch() != null) {
        validateOperation(pathItem.getPatch());
      }
    }
  }

  private static void validateOperation(Operation operation) {
    if (operation.getRequestBody() != null) {
      validateRequestBody(operation.getRequestBody());
    }
    if (operation.getParameters() != null) {
      validateParameters(operation.getParameters());
    }
  }

  private static void validateParameters(List<Parameter> parameters) {
    for (Parameter parameter : parameters) {
      if (parameter == null) {
        throw new ServiceCombException("Parameter can not be null.");
      }
      if (StringUtils.isEmpty(parameter.getName())) {
        throw new ServiceCombException("Parameter name is required.");
      }
    }
  }

  private static void validateRequestBody(RequestBody requestBody) {
    for (String contentType : requestBody.getContent().keySet()) {
      if (SwaggerConst.FILE_MEDIA_TYPE.equals(contentType) || SwaggerConst.FORM_MEDIA_TYPE.equals(contentType)) {
        continue;
      }
      if (requestBody.getExtensions() == null) {
        throw new ServiceCombException("Request body x-name extension is required.");
      }
      if (StringUtils.isEmpty((String) requestBody.getExtensions().get(SwaggerConst.EXT_BODY_NAME))) {
        throw new ServiceCombException("Request body x-name extension is required.");
      }
      break;
    }
  }

  private static OpenAPI internalParseSwagger(String swaggerContent) throws IOException {
    return Yaml.mapper().readValue(swaggerContent, OpenAPI.class);
  }

  // add descriptions to response and add a default response if absent.
  public static void correctResponses(Operation operation) {
    if (operation.getResponses() == null) {
      operation.setResponses(new ApiResponses());
    }
    if (operation.getResponses().size() == 0) {
      operation.getResponses().addApiResponse(SwaggerConst.SUCCESS_KEY, new ApiResponse());
    }

    for (Entry<String, ApiResponse> responseEntry : operation.getResponses().entrySet()) {
      ApiResponse response = responseEntry.getValue();
      if (StringUtils.isEmpty(response.getDescription())) {
        response.setDescription("response of " + responseEntry.getKey());
      }
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

  public static Schema resolveTypeSchemas(OpenAPI swagger, Type type) {
    ResolvedSchema resolvedSchema = ModelConverters.getInstance().resolveAsResolvedSchema(
        new AnnotatedType(type).resolveAsRef(true));

    if (resolvedSchema == null || resolvedSchema.schema == null) {
      throw new IllegalArgumentException("cannot resolve type : " + type);
    }

    if (swagger.getComponents() == null) {
      swagger.setComponents(new Components());
    }

    Map<String, Schema> schemaMap = resolvedSchema.referencedSchemas;
    if (!CollectionUtils.isEmpty(schemaMap)) {
      Map<String, Schema> componentSchemas = swagger.getComponents().getSchemas();
      if (componentSchemas == null) {
        componentSchemas = new LinkedHashMap<>(schemaMap);
      } else {
        for (Map.Entry<String, Schema> entry : schemaMap.entrySet()) {
          if (!componentSchemas.containsKey(entry.getKey())) {
            componentSchemas.put(entry.getKey(), entry.getValue());
          } else {
            if (!schemaEquals(entry.getValue(), componentSchemas.get(entry.getKey()))) {
              throw new IllegalArgumentException("duplicate param model: " + entry.getKey());
            }
          }
        }
      }
      swagger.getComponents().setSchemas(componentSchemas);
    }
    return resolvedSchema.schema;
  }

  // swagger api equals method will compare Map address(extensions)
  // and is not applicable for usage.
  public static int schemaHashCode(Schema<?> schema) {
    int result = schema.getType() != null ? schema.getType().hashCode() : 0;
    result = result ^ (schema.getFormat() != null ? schema.getFormat().hashCode() : 0);
    result = result ^ (schema.getName() != null ? schema.getName().hashCode() : 0);
    result = result ^ (schema.get$ref() != null ? schema.get$ref().hashCode() : 0);
    result = result ^ (schema.getItems() != null ? schemaHashCode(schema.getItems()) : 0);
    result = result ^ (schema.getAdditionalItems() != null ? schemaHashCode(schema.getAdditionalItems()) : 0);
    result = result ^ (schema.getProperties() != null ? propertiesHashCode(schema.getProperties()) : 0);
    return result;
  }

  private static int propertiesHashCode(Map<String, Schema> properties) {
    int result = 0;
    for (Entry<String, Schema> entry : properties.entrySet()) {
      result = result ^ (entry.getKey().hashCode() ^ schemaHashCode(entry.getValue()));
    }
    return result;
  }

  // swagger api equals method will compare Map address(extensions)
  // and is not applicable for usage.
  public static boolean schemaEquals(Schema<?> schema1, Schema<?> schema2) {
    if (schema1 == null && schema2 == null) {
      return true;
    }
    if (schema1 == null || schema2 == null) {
      return false;
    }
    return StringUtils.equals(schema1.getType(), schema2.getType())
        && StringUtils.equals(schema1.getFormat(), schema2.getFormat())
        && StringUtils.equals(schema1.getName(), schema2.getName())
        && StringUtils.equals(schema1.get$ref(), schema2.get$ref())
        && schemaEquals(schema1.getItems(), schema2.getItems())
        && schemaEquals(schema1.getAdditionalItems(), schema2.getAdditionalItems())
        && propertiesEquals(schema1.getProperties(), schema2.getProperties());
  }

  public static boolean propertiesEquals(Map<String, Schema> properties1, Map<String, Schema> properties2) {
    if (properties1 == null && properties2 == null) {
      return true;
    }
    if (properties1 == null || properties2 == null) {
      return false;
    }
    if (properties1.size() != properties2.size()) {
      return false;
    }
    boolean result = true;
    for (String key : properties1.keySet()) {
      if (!schemaEquals(properties1.get(key), properties2.get(key))) {
        result = false;
        break;
      }
    }
    return result;
  }

  public static Schema getSchema(OpenAPI swagger, String ref) {
    return swagger.getComponents().getSchemas().get(ref.substring(Components.COMPONENTS_SCHEMAS_REF.length()));
  }

  public static String getSchemaName(String ref) {
    return ref.substring(Components.COMPONENTS_SCHEMAS_REF.length());
  }

  public static Schema getSchema(OpenAPI swagger, Schema ref) {
    if (ref == null) {
      return null;
    }
    if (ref.get$ref() != null) {
      return getSchema(swagger, ref.get$ref());
    }
    return ref;
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
    if (param.getExtensions() == null) {
      return false;
    }
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
        && cls != LocalDateTime.class
        && cls != byte[].class
        && cls != File.class
        && !cls.getName().equals("org.springframework.web.multipart.MultipartFile")
        && !Part.class.isAssignableFrom(cls));
  }

  public static void updateProduces(Operation operation, String[] produces) {
    if (produces == null || produces.length == 0) {
      return;
    }
    if (operation.getResponses() == null) {
      operation.setResponses(new ApiResponses());
    }
    if (operation.getResponses().size() == 0) {
      operation.getResponses().addApiResponse(SwaggerConst.SUCCESS_KEY, new ApiResponse());
    }
    for (String produce : produces) {
      operation.getResponses().forEach((k, v) -> {
        if (v.getContent() == null) {
          v.setContent(new Content());
        }
        if (v.getContent().get(produce) == null) {
          v.getContent().addMediaType(produce, new MediaType());
        }
      });
    }
  }

  public static boolean methodExists(PathItem pathItem, String httpMethod) {
    PathItem.HttpMethod method = PathItem.HttpMethod.valueOf(httpMethod);
    return switch (method) {
      case GET -> pathItem.getGet() != null;
      case PUT -> pathItem.getPut() != null;
      case POST -> pathItem.getPost() != null;
      case PATCH -> pathItem.getPatch() != null;
      case DELETE -> pathItem.getDelete() != null;
      default -> false;
    };
  }
}
