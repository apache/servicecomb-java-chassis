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
package org.apache.servicecomb.swagger.generator.core;

import static org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils.collectAnnotations;
import static org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils.findMethodAnnotationProcessor;
import static org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils.findResponseTypeProcessor;
import static org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils.isContextParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.inject.PlaceholderResolver;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.MethodAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.OperationGenerator;
import org.apache.servicecomb.swagger.generator.ParameterAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.ParameterGenerator;
import org.apache.servicecomb.swagger.generator.ParameterTypeProcessor;
import org.apache.servicecomb.swagger.generator.ResponseTypeProcessor;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils;
import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;
import org.apache.servicecomb.swagger.generator.core.utils.MethodUtils;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.reflect.TypeToken;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.ReflectionUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("rawtypes")
public abstract class AbstractOperationGenerator implements OperationGenerator {
  protected AbstractSwaggerGenerator swaggerGenerator;

  protected OpenAPI swagger;

  protected Class<?> clazz;

  protected Method method;

  protected String httpMethod;

  protected List<ParameterGenerator> parameterGenerators = new ArrayList<>();

  protected String path;

  protected Operation swaggerOperation;

  protected OperationGeneratorContext operationGeneratorContext;

  public AbstractOperationGenerator(AbstractSwaggerGenerator swaggerGenerator, Method method) {
    this.swaggerGenerator = swaggerGenerator;
    this.swagger = swaggerGenerator.getOpenAPI();
    this.clazz = swaggerGenerator.getClazz();
    this.method = method;
    this.httpMethod = swaggerGenerator.getHttpMethod();

    operationGeneratorContext = new OperationGeneratorContext(swaggerGenerator.getSwaggerGeneratorContext());
    swaggerOperation = new Operation();
  }

  @Override
  public OperationGeneratorContext getOperationGeneratorContext() {
    return operationGeneratorContext;
  }

  @Override
  public void setHttpMethod(String httpMethod) {
    if (StringUtils.isEmpty(httpMethod)) {
      return;
    }

    this.httpMethod = httpMethod.toUpperCase(Locale.US);
  }

  @Override
  public OpenAPI getSwagger() {
    return this.swagger;
  }

  @Override
  public String getHttpMethod() {
    return httpMethod;
  }

  @Override
  public Operation getOperation() {
    return swaggerOperation;
  }

  public String getOperationId() {
    return swaggerOperation.getOperationId();
  }

  @Override
  public void setPath(String path) {
    path = new PlaceholderResolver().replaceFirst(path);
    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    this.path = path;
  }

  public void generate() {
    scanMethodAnnotation();
    scanMethodParameters();
    scanResponse();
    correctOperation();
  }

  protected void scanMethodAnnotation() {
    for (Annotation annotation : Arrays.stream(method.getAnnotations())
        .sorted(Comparator.comparing(a -> a.annotationType().getSimpleName()))
        .collect(Collectors.toList())) {
      MethodAnnotationProcessor<Annotation> processor = findMethodAnnotationProcessor(annotation.annotationType());
      if (processor == null) {
        continue;
      }
      processor.process(swaggerGenerator, this, annotation);
    }

    if (StringUtils.isEmpty(swaggerOperation.getOperationId())) {
      swaggerOperation.setOperationId(MethodUtils.findSwaggerMethodName(method));
    }
  }

  protected void scanMethodParameters() {
    // init generators
    initParameterGenerators();

    // scan annotations and types
    Set<String> names = new HashSet<>();
    int bodyCount = 0;
    for (ParameterGenerator parameterGenerator : parameterGenerators) {
      scanMethodParameter(parameterGenerator);

      if (!names.add(parameterGenerator.getParameterGeneratorContext().getParameterName())) {
        throw new IllegalArgumentException(
            String.format("not support duplicated parameter, name=%s.",
                parameterGenerator.getParameterGeneratorContext().getParameterName()));
      }
      if (parameterGenerator.getHttpParameterType() == HttpParameterType.BODY) {
        if (bodyCount > 0) {
          throw new IllegalArgumentException(String.format("Defined %d body parameter.", bodyCount));
        }
        bodyCount++;
      }
    }

    // generate
    for (ParameterGenerator parameterGenerator : parameterGenerators) {
      parameterGenerator.generate();
    }
  }

  protected void initParameterGenerators() {
    // 1.group method annotations by parameter name
    // key is parameter name
    Map<String, List<Annotation>> methodAnnotationMap = initMethodAnnotationByParameterName();

    // 2.create ParameterGenerators by method parameters, merge annotations with method annotations
    initMethodParameterGenerators(methodAnnotationMap);

    // 3.create ParameterGenerators remains method annotations
    initRemainMethodAnnotationsParameterGenerators(methodAnnotationMap);
  }

  protected void initMethodParameterGenerators(Map<String, List<Annotation>> methodAnnotationMap) {
    for (java.lang.reflect.Parameter methodParameter : method.getParameters()) {
      Type genericType = TypeToken.of(clazz)
          .resolveType(methodParameter.getParameterizedType())
          .getType();
      ParameterGenerator parameterGenerator = new ParameterGenerator(
          this, methodAnnotationMap, methodParameter,
          TypeFactory.defaultInstance().constructType(genericType));
      validateParameter(parameterGenerator.getGenericType());
      if (isContextParameter(parameterGenerator.getGenericType())) {
        continue;
      }

      // jaxrs: @BeanParam
      // springmvc: is query, and is bean type
      if (isAggregatedParameter(parameterGenerator, methodParameter)) {
        extractAggregatedParameterGenerators(methodAnnotationMap, methodParameter);
        continue;
      }

      parameterGenerators.add(parameterGenerator);
    }
  }

  protected boolean isAggregatedParameter(ParameterGenerator parameterGenerator,
      java.lang.reflect.Parameter methodParameter) {
    return false;
  }

  protected void extractAggregatedParameterGenerators(Map<String, List<Annotation>> methodAnnotationMap,
      java.lang.reflect.Parameter methodParameter) {
    JavaType javaType = TypeFactory.defaultInstance().constructType(methodParameter.getParameterizedType());
    BeanDescription beanDescription = Json.mapper().getSerializationConfig().introspect(javaType);
    for (BeanPropertyDefinition propertyDefinition : beanDescription.findProperties()) {
      if (!propertyDefinition.couldSerialize()) {
        continue;
      }

      Annotation[] annotations = collectAnnotations(propertyDefinition);
      ParameterGenerator propertyParameterGenerator = new ParameterGenerator(this,
          methodAnnotationMap,
          propertyDefinition.getName(),
          annotations,
          propertyDefinition.getPrimaryType());
      parameterGenerators.add(propertyParameterGenerator);
    }
  }

  protected void initRemainMethodAnnotationsParameterGenerators(Map<String, List<Annotation>> methodAnnotationMap) {
    for (Entry<String, List<Annotation>> entry : methodAnnotationMap.entrySet()) {
      ParameterGenerator parameterGenerator = new ParameterGenerator(this, entry.getKey(), entry.getValue());
      parameterGenerators.add(parameterGenerator);
    }
  }

  private Map<String, List<Annotation>> initMethodAnnotationByParameterName() {
    Map<String, List<Annotation>> methodAnnotations = new LinkedHashMap<>();
    for (Annotation annotation : method.getAnnotations()) {
      if (annotation instanceof io.swagger.v3.oas.annotations.Parameters) {
        for (io.swagger.v3.oas.annotations.Parameter apiImplicitParam
            : ((io.swagger.v3.oas.annotations.Parameters) annotation).value()) {
          addMethodAnnotationByParameterName(methodAnnotations, apiImplicitParam.name(), apiImplicitParam);
        }
        continue;
      }

      if (annotation instanceof io.swagger.v3.oas.annotations.Parameter) {
        addMethodAnnotationByParameterName(methodAnnotations,
            ((io.swagger.v3.oas.annotations.Parameter) annotation).name(), annotation);
      }
    }
    return methodAnnotations;
  }

  private void addMethodAnnotationByParameterName(Map<String, List<Annotation>> methodAnnotations, String name,
      Annotation annotation) {
    if (StringUtils.isEmpty(name)) {
      throw new IllegalStateException(String.format("%s.name should not be empty. method=%s:%s",
          annotation.annotationType().getSimpleName(),
          method.getDeclaringClass().getName(),
          method.getName()));
    }

    methodAnnotations.computeIfAbsent(name, n -> new ArrayList<>())
        .add(annotation);
  }

  protected void validateParameter(JavaType type) {
    if (type.isTypeOrSubTypeOf(HttpServletResponse.class)) {
      // not support, log the reason
      throw new IllegalStateException(
          "all input/output of ServiceComb operation are models, not allow to use HttpServletResponse.");
    }
  }

  protected void scanMethodParameter(ParameterGenerator parameterGenerator) {
    for (Annotation annotation : parameterGenerator.getAnnotations()) {
      ParameterAnnotationProcessor<Annotation> processor = SwaggerGeneratorUtils
          .findParameterAnnotationProcessor(annotation.annotationType());
      if (processor != null) {
        processor.process(this.swaggerGenerator, this, parameterGenerator, annotation);
      }
    }

    Schema<?> schema = parameterGenerator.getParameterGeneratorContext().getSchema();
    if (schema == null) {
      JavaType parameterType = parameterGenerator.getGenericType();
      ParameterTypeProcessor processor = SwaggerGeneratorUtils.findParameterTypeProcessor(parameterType);
      if (processor != null) {
        processor.process(this.swaggerGenerator, this, parameterGenerator);
      } else {
        parameterGenerator.getParameterGeneratorContext().setSchema(SwaggerUtils.resolveTypeSchemas(this.swagger,
            parameterType));
      }
    }
  }

  @Override
  public boolean isForm() {
    for (ParameterGenerator parameterGenerator : parameterGenerators) {
      if (parameterGenerator.isForm()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isBinary() {
    for (ParameterGenerator parameterGenerator : parameterGenerators) {
      if (parameterGenerator.isBinary()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void addOperationToSwagger() {
    if (StringUtils.isEmpty(httpMethod)) {
      return;
    }

    if (swagger.getPaths() == null) {
      swagger.setPaths(new Paths());
    }

    PathItem pathObj = swagger.getPaths().get(path);
    if (pathObj == null) {
      pathObj = new PathItem();
      swagger.path(path, pathObj);
    } else if (SwaggerUtils.methodExists(pathObj, httpMethod)) {
      throw new IllegalStateException(String.format("Duplicate operation path detected. method=%s:%s.",
          method.getDeclaringClass().getName(),
          method.getName()));
    }

    pathObj.operation(PathItem.HttpMethod.valueOf(httpMethod), swaggerOperation);
  }

  public void correctOperation() {
    SwaggerUtils.correctResponses(swaggerOperation);
  }

  public void scanResponse() {
    if (swaggerOperation.getResponses() == null) {
      swaggerOperation.setResponses(new ApiResponses());
    }

    // If annotations contains ApiResponse, schema must be defined.
    if (swaggerOperation.getResponses().get(SwaggerConst.SUCCESS_KEY) != null) {
      return;
    }

    Schema model = createResponseModel();
    if (model == null) {
      return;
    }
    swaggerOperation.getResponses().addApiResponse(SwaggerConst.SUCCESS_KEY, new ApiResponse());
    swaggerOperation.getResponses().get(SwaggerConst.SUCCESS_KEY).setContent(new Content());
    swaggerOperation.getResponses().get(SwaggerConst.SUCCESS_KEY).getContent()
        .addMediaType(SwaggerConst.DEFAULT_MEDIA_TYPE, new io.swagger.v3.oas.models.media.MediaType());
    swaggerOperation.getResponses().get(SwaggerConst.SUCCESS_KEY).getContent()
        .get(SwaggerConst.DEFAULT_MEDIA_TYPE)
        .setSchema(model);
  }

  protected Schema createResponseModel() {
    Type responseType =
        TypeToken.of(clazz)
            .resolveType(method.getGenericReturnType())
            .getType();
    if (ReflectionUtils.isVoid(responseType)) {
      return null;
    }

    ResponseTypeProcessor processor = findResponseTypeProcessor(responseType);
    return processor.process(swaggerGenerator, this, responseType);
  }

  public Method getMethod() {
    return method;
  }

  public List<ParameterGenerator> getParameterGenerators() {
    return parameterGenerators;
  }

  public Operation getSwaggerOperation() {
    return swaggerOperation;
  }
}
