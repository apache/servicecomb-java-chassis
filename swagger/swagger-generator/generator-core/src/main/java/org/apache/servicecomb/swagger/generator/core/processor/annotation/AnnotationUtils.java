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

package org.apache.servicecomb.swagger.generator.core.processor.annotation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.swagger.generator.core.processor.annotation.models.ResponseConfig;
import org.apache.servicecomb.swagger.generator.core.processor.annotation.models.ResponseConfigBase;
import org.apache.servicecomb.swagger.generator.core.processor.annotation.models.ResponseHeaderConfig;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ResponseHeader;
import io.swagger.converter.ModelConverters;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.utils.PropertyModelConverter;
import io.swagger.util.ReflectionUtils;

public final class AnnotationUtils {
  private AnnotationUtils() {

  }

  public static void appendDefinition(Swagger swagger, Map<String, Model> newDefinitions) {
    if (newDefinitions.isEmpty()) {
      return;
    }

    Map<String, Model> definitions = swagger.getDefinitions();
    if (definitions == null) {
      definitions = new LinkedHashMap<>();
      swagger.setDefinitions(definitions);
    }

    definitions.putAll(newDefinitions);
  }

  private static ResponseConfig convert(ApiOperation apiOperation) {
    ResponseConfig responseConfig = new ResponseConfig();
    responseConfig.setCode(apiOperation.code());
    responseConfig.setResponseClass(apiOperation.response());
    responseConfig.setResponseContainer(apiOperation.responseContainer());
    responseConfig.setResponseReference(apiOperation.responseReference());
    responseConfig.setResponseHeaders(apiOperation.responseHeaders());
    return responseConfig;
  }

  private static ResponseConfig convert(ApiResponse apiResponse) {
    ResponseConfig responseConfig = new ResponseConfig();
    responseConfig.setCode(apiResponse.code());
    responseConfig.setDescription(apiResponse.message());
    responseConfig.setResponseClass(apiResponse.response());
    responseConfig.setResponseContainer(apiResponse.responseContainer());
    responseConfig.setResponseReference(apiResponse.reference());
    responseConfig.setResponseHeaders(apiResponse.responseHeaders());
    return responseConfig;
  }

  public static ResponseHeaderConfig convert(ResponseHeader responseHeader) {
    if (StringUtils.isEmpty(responseHeader.name())) {
      return null;
    }

    ResponseHeaderConfig config = new ResponseHeaderConfig();
    config.setName(responseHeader.name());
    config.setDescription(responseHeader.description());
    config.setResponseClass(responseHeader.response());
    config.setResponseContainer(responseHeader.responseContainer());
    return config;
  }

  public static void addResponse(Swagger swagger, Operation operation, ApiOperation apiOperation) {
    ResponseConfig responseConfig = convert(apiOperation);
    generateResponse(swagger, responseConfig);
    mergeResponse(operation, responseConfig);
  }

  public static void addResponse(Swagger swagger, ApiResponse apiResponse) {
    ResponseConfig responseConfig = convert(apiResponse);
    generateResponse(swagger, responseConfig);
    swagger.response(String.valueOf(responseConfig.getCode()), responseConfig.getResponse());
  }

  public static void addResponse(Swagger swagger, Operation operation, ApiResponse apiResponse) {
    ResponseConfig responseConfig = convert(apiResponse);
    generateResponse(swagger, responseConfig);
    mergeResponse(operation, responseConfig);
  }

  private static void mergeResponse(Operation operation, ResponseConfig responseConfig) {
    if (operation.getResponses() == null) {
      operation.response(responseConfig.getCode(), responseConfig.getResponse());
      return;
    }
    Response response = operation.getResponses().get(String.valueOf(responseConfig.getCode()));
    if (response == null) {
      operation.response(responseConfig.getCode(), responseConfig.getResponse());
      return;
    }
    Response sourceResp = responseConfig.getResponse();
    if (StringUtils.isNotEmpty(sourceResp.getDescription()) && StringUtils.isEmpty(response.getDescription())) {
      response.setDescription(sourceResp.getDescription());
    }
    if (sourceResp.getResponseSchema() != null && response.getResponseSchema() == null) {
      response.setResponseSchema(sourceResp.getResponseSchema());
    }
    if (sourceResp.getExamples() != null && response.getExamples() == null) {
      response.setExamples(sourceResp.getExamples());
    }
    if (sourceResp.getHeaders() != null && response.getHeaders() == null) {
      response.setHeaders(sourceResp.getHeaders());
    }
    if (sourceResp.getVendorExtensions() != null && response.getVendorExtensions() == null) {
      response.setVendorExtensions(sourceResp.getVendorExtensions());
    }
  }

  private static void generateResponse(Swagger swagger, ResponseConfig responseConfig) {
    Response response = new Response();

    Property property = generateResponseProperty(swagger, responseConfig);
    if (property != null) {
      Model model = new PropertyModelConverter().propertyToModel(property);
      response.setResponseSchema(model);
    }
    response.setDescription(responseConfig.getDescription());

    if (responseConfig.getResponseHeaders() != null) {
      Map<String, Property> headers = generateResponseHeader(swagger, responseConfig.getResponseHeaders());
      response.setHeaders(headers);
    }

    responseConfig.setResponse(response);
  }

  private static Map<String, Property> generateResponseHeader(Swagger swagger,
      List<ResponseHeaderConfig> responseHeaders) {
    Map<String, Property> headers = new HashMap<>();
    for (ResponseHeaderConfig config : responseHeaders) {
      Property property = generateResponseHeaderProperty(swagger, config);
      headers.put(config.getName(), property);
    }
    return headers;
  }

  public static Property generateResponseHeaderProperty(Swagger swagger, ResponseHeaderConfig config) throws Error {
    Property property = generateResponseProperty(swagger, config);
    if (property == null) {
      throw new Error("invalid responseHeader, " + config);
    }
    return property;
  }

  public static Property generateResponseProperty(Swagger swagger, ResponseConfigBase config) throws Error {
    Class<?> responseClass = config.getResponseClass();
    if (responseClass == null || ReflectionUtils.isVoid(responseClass)) {
      return null;
    }

    if (!ClassUtils.isPrimitiveOrWrapper(responseClass)) {
      Map<String, Model> newDefinitions = ModelConverters.getInstance().readAll(responseClass);
      appendDefinition(swagger, newDefinitions);
    }

    Property property = ModelConverters.getInstance().readAsProperty(responseClass);
    // responseContainer只可能是:"List", "Set" or "Map"
    // 根据swagger定义这里是区分大小写的， 虽然不明白为何这样做，不过还是不要改标准了
    switch (config.getResponseContainer()) {
      case "List":
        property = new ArrayProperty(property);
        break;
      case "Set":
        property = new ArrayProperty(property);
        ((ArrayProperty) property).setUniqueItems(true);
        break;
      case "Map":
        property = new MapProperty(property);
        break;
      case "":
        // 不必处理
        break;
      default:
        throw new Error("not support responseContainer " + config.getResponseContainer());
    }
    return property;
  }
}
