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
package org.apache.servicecomb.swagger.generator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.swagger.generator.core.ParameterGeneratorContext;
import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.CookieParameter;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

public class ParameterGenerator {
  private final List<Annotation> annotations;

  /**
   * when wrap parameters to body, genericType is null
   */
  private final JavaType genericType;

  private HttpParameterType httpParameterType;

  private final OperationGenerator operationGenerator;

  private final ParameterGeneratorContext parameterGeneratorContext;

  public ParameterGenerator(OperationGenerator operationGenerator, String parameterName) {
    this.genericType = null;
    this.operationGenerator = operationGenerator;
    this.parameterGeneratorContext = new ParameterGeneratorContext(operationGenerator.getOperationGeneratorContext());
    this.parameterGeneratorContext.setParameterName(parameterName);
    this.annotations = Collections.emptyList();
  }

  public ParameterGenerator(OperationGenerator operationGenerator, Executable executable,
      Map<String, List<Annotation>> methodAnnotationMap,
      String defaultName,
      Annotation[] parameterAnnotations, Type genericType) {
    this.operationGenerator = operationGenerator;
    String parameterName = SwaggerGeneratorUtils.collectParameterName(executable, parameterAnnotations,
        defaultName);
    this.annotations = SwaggerGeneratorUtils.collectParameterAnnotations(parameterAnnotations,
        methodAnnotationMap,
        parameterName);
    this.parameterGeneratorContext = new ParameterGeneratorContext(operationGenerator.getOperationGeneratorContext());
    this.parameterGeneratorContext.setParameterName(parameterName);
    this.genericType = TypeFactory.defaultInstance()
        .constructType(SwaggerGeneratorUtils.collectGenericType(annotations, genericType));
    this.httpParameterType = SwaggerGeneratorUtils.collectHttpParameterType(annotations, genericType);
  }

  public ParameterGenerator(OperationGenerator operationGenerator, Executable executable,
      Map<String, List<Annotation>> methodAnnotationMap,
      java.lang.reflect.Parameter methodParameter, Type genericType) {
    this(operationGenerator, executable,
        methodAnnotationMap,
        methodParameter.isNamePresent() ? methodParameter.getName() : null,
        methodParameter.getAnnotations(),
        genericType);
  }

  public ParameterGenerator(OperationGenerator operationGenerator, String parameterName, List<Annotation> annotations) {
    this.operationGenerator = operationGenerator;
    this.parameterGeneratorContext = new ParameterGeneratorContext(operationGenerator.getOperationGeneratorContext());
    this.parameterGeneratorContext.setParameterName(parameterName);
    this.annotations = annotations;
    this.genericType = TypeFactory.defaultInstance()
        .constructType(SwaggerGeneratorUtils.collectGenericType(annotations, null));
    this.httpParameterType = SwaggerGeneratorUtils.collectHttpParameterType(annotations, genericType);
  }

  public ParameterGeneratorContext getParameterGeneratorContext() {
    return this.parameterGeneratorContext;
  }

  public List<Annotation> getAnnotations() {
    return annotations;
  }

  public JavaType getGenericType() {
    return genericType;
  }

  public HttpParameterType getHttpParameterType() {
    return httpParameterType;
  }

  public void setHttpParameterType(HttpParameterType httpParameterType) {
    this.httpParameterType = httpParameterType;
  }

  public void generate() {
    if (httpParameterType == HttpParameterType.BODY) {
      RequestBody requestBody = new RequestBody();
      Map<String, Object> extensions = new HashMap<>();
      extensions.put(SwaggerConst.EXT_BODY_NAME, parameterGeneratorContext.getParameterName());
      requestBody.setExtensions(extensions);
      requestBody.setContent(new Content());
      for (String media : parameterGeneratorContext.getSupportedConsumes()) {
        MediaType mediaType = new MediaType();
        mediaType.setSchema(parameterGeneratorContext.getSchema());
        requestBody.getContent().addMediaType(media, mediaType);
      }
      this.operationGenerator.getOperation().setRequestBody(requestBody);
      return;
    }
    if (httpParameterType == HttpParameterType.FORM) {
      RequestBody requestBody = this.operationGenerator.getOperation().getRequestBody();
      if (requestBody == null) {
        requestBody = new RequestBody();
        requestBody.setContent(new Content());
        this.operationGenerator.getOperation().setRequestBody(requestBody);
      }
      for (String media : parameterGeneratorContext.getSupportedConsumes()) {
        MediaType mediaType = requestBody.getContent().get(media);
        if (mediaType == null) {
          mediaType = new MediaType();
          requestBody.getContent().addMediaType(media, mediaType);
        }
        mediaType.getSchema().addProperty(parameterGeneratorContext.getParameterName(),
            parameterGeneratorContext.getSchema());
      }
      return;
    }
    Parameter parameter;
    switch (httpParameterType) {
      case PATH -> parameter = new PathParameter();
      case QUERY -> parameter = new QueryParameter();
      case HEADER -> parameter = new HeaderParameter();
      case COOKIE -> parameter = new CookieParameter();
      default -> throw new IllegalStateException("not support httpParameterType " + httpParameterType);
    }
    parameter.setSchema(parameterGeneratorContext.getSchema());
    parameter.setRequired(parameterGeneratorContext.getRequired());
    parameter.setExplode(parameterGeneratorContext.getExplode());
    parameter.setIn(httpParameterType.name());
    this.operationGenerator.getOperation().addParametersItem(parameter);

    // validations and other annotations supported by swagger default
    if (parameterGeneratorContext.getParameterType() != null) {
      io.swagger.v3.core.util.ParameterProcessor.applyAnnotations(parameter,
          parameterGeneratorContext.getParameterType(),
          annotations, operationGenerator.getSwagger().getComponents(),
          null, null, null);
    }
  }
}
