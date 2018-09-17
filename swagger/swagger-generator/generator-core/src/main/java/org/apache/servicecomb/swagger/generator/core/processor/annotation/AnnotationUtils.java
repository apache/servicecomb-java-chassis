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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.swagger.generator.core.processor.annotation.models.ResponseConfig;
import org.apache.servicecomb.swagger.generator.core.processor.annotation.models.ResponseConfigBase;
import org.apache.servicecomb.swagger.generator.core.processor.annotation.models.ResponseHeaderConfig;
import org.springframework.util.ClassUtils;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;
import io.swagger.annotations.ResponseHeader;
import io.swagger.converter.ModelConverters;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.CookieParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.FileProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.util.ParameterProcessor;
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
    operation.response(responseConfig.getCode(), responseConfig.getResponse());
  }

  public static void addResponse(Swagger swagger, ApiResponse apiResponse) {
    ResponseConfig responseConfig = convert(apiResponse);
    generateResponse(swagger, responseConfig);
    swagger.response(String.valueOf(responseConfig.getCode()), responseConfig.getResponse());
  }

  public static void addResponse(Swagger swagger, Operation operation, ApiResponse apiResponse) {
    ResponseConfig responseConfig = convert(apiResponse);
    generateResponse(swagger, responseConfig);
    operation.response(responseConfig.getCode(), responseConfig.getResponse());
  }

  private static void generateResponse(Swagger swagger, ResponseConfig responseConfig) {
    Response response = new Response();

    Property property = generateResponseProperty(swagger, responseConfig);
    response.setSchema(property);
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

  public static Parameter createParameter(Swagger swagger, ApiImplicitParam paramAnnotation) {
    Parameter parameter = createParameterInstance(paramAnnotation);
    Type dataType = ReflectionUtils.typeFromString(paramAnnotation.dataType());
    parameter = ParameterProcessor.applyAnnotations(swagger,
        parameter,
        dataType,
        Arrays.asList(paramAnnotation));
    return parameter;
  }

  private static Parameter createParameterInstance(ApiImplicitParam paramAnnotation) {
    switch (paramAnnotation.paramType()) {
      case "path":
        return new PathParameter();
      case "query":
        return new QueryParameter();
      case "body":
        return new BodyParameter();
      case "header":
        return new HeaderParameter();
      case "form":
        return new FormParameter();
      case "cookie":
        return new CookieParameter();
      default:
        throw new Error("not support paramType " + paramAnnotation.paramType());
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T findAnnotation(Annotation[] annotations, Class<T> annotationType) {
    for (Annotation annotation : annotations) {
      if (annotation.annotationType().equals(annotationType)) {
        return (T) annotation;
      }
    }

    return null;
  }

  public static void processApiParam(Annotation[] paramAnnotations, Parameter parameter) {
    ApiParam param = findAnnotation(paramAnnotations, ApiParam.class);
    if (param == null) {
      return;
    }

    if (parameter instanceof AbstractSerializableParameter) {
      processApiParam(param, (AbstractSerializableParameter<?>) parameter);
      return;
    }

    processApiParam(param, (BodyParameter) parameter);
  }

  protected static void processApiParam(ApiParam param, BodyParameter p) {
    if (param.required()) {
      p.setRequired(true);
    }
    if (StringUtils.isNotEmpty(param.name())) {
      p.setName(param.name());
    }
    if (StringUtils.isNotEmpty(param.value())) {
      p.setDescription(param.value());
    }
    if (StringUtils.isNotEmpty(param.access())) {
      p.setAccess(param.access());
    }

    Example example = param.examples();
    if (example != null && example.value() != null) {
      for (ExampleProperty ex : example.value()) {
        String mediaType = ex.mediaType();
        String value = ex.value();
        if (!mediaType.isEmpty() && !value.isEmpty()) {
          p.example(mediaType.trim(), value.trim());
        }
      }
    }
  }

  protected static void processApiParam(ApiParam param, AbstractSerializableParameter<?> p) {
    if (param.required()) {
      p.setRequired(true);
    }
    if (param.readOnly()) {
      p.setReadOnly(true);
    }
    if (param.allowEmptyValue()) {
      p.setAllowEmptyValue(true);
    }
    if (StringUtils.isNotEmpty(param.name())) {
      p.setName(param.name());
    }
    if (StringUtils.isNotEmpty(param.value())) {
      p.setDescription(param.value());
    }
    if (StringUtils.isNotEmpty(param.example())) {
      p.setExample(param.example());
    }
    if (StringUtils.isNotEmpty(param.access())) {
      p.setAccess(param.access());
    }
    if (StringUtils.isNoneEmpty(param.collectionFormat())) {
      p.setCollectionFormat(param.collectionFormat());
    }
    if (StringUtils.isNotEmpty(param.type())) {
      if ("java.io.File".equalsIgnoreCase(param.type())) {
        p.setProperty(new FileProperty());
      } else if ("long".equalsIgnoreCase(param.type())) {
        p.setProperty(new LongProperty());
      } else {
        p.setType(param.type());
      }
    }
  }
}
