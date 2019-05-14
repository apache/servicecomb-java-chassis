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
import static org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils.findParameterProcessors;
import static org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils.findResponseTypeProcessor;
import static org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils.isContextParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.inject.PlaceholderResolver;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.MethodAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.OperationGenerator;
import org.apache.servicecomb.swagger.generator.ParameterGenerator;
import org.apache.servicecomb.swagger.generator.ParameterProcessor;
import org.apache.servicecomb.swagger.generator.ResponseTypeProcessor;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiParam;
import io.swagger.converter.ModelConverters;
import io.swagger.models.HttpMethod;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
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
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import io.swagger.util.Json;
import io.swagger.util.ReflectionUtils;

public abstract class AbstractOperationGenerator implements OperationGenerator {
  protected AbstractSwaggerGenerator swaggerGenerator;

  protected Swagger swagger;

  protected Method method;

  protected String httpMethod;

  protected List<ParameterGenerator> parameterGenerators = new ArrayList<>();

  protected String path;

  protected Operation swaggerOperation;

  // 根据方法上独立的ResponseHeader(s)标注生成的数据
  // 如果Response中不存在对应的header，则会将这些header补充进去
  protected Map<String, Property> methodResponseHeaders = new LinkedHashMap<>();

  public AbstractOperationGenerator(AbstractSwaggerGenerator swaggerGenerator, Method method) {
    this.swaggerGenerator = swaggerGenerator;
    this.swagger = swaggerGenerator.getSwagger();
    this.method = method;
    this.httpMethod = swaggerGenerator.getHttpMethod();

    swaggerOperation = new Operation();
  }

  @Override
  public void addMethodResponseHeader(String name, Property header) {
    methodResponseHeaders.put(name, header);
  }

  @Override
  public void setHttpMethod(String httpMethod) {
    if (StringUtils.isEmpty(httpMethod)) {
      return;
    }

    this.httpMethod = httpMethod.toLowerCase(Locale.US);
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

  @Override
  public void generateResponse() {
    scanMethodAnnotation();
    scanResponse();
    correctOperation();
  }

  public void generate() {
    scanMethodAnnotation();
    scanMethodParameters();
    scanResponse();

    correctOperation();
  }

  protected void scanMethodAnnotation() {
    for (Annotation annotation : method.getAnnotations()) {
      MethodAnnotationProcessor<Annotation> processor = findMethodAnnotationProcessor(annotation.annotationType());
      if (processor == null) {
        continue;
      }
      processor.process(swaggerGenerator, this, annotation);
    }

    if (StringUtils.isEmpty(swaggerOperation.getOperationId())) {
      swaggerOperation.setOperationId(method.getName());
    }

    setDefaultTag();
  }

  private void setDefaultTag() {
    // if tag has been defined, do nothing
    if (null != swaggerOperation.getTags()) {
      for (String tag : swaggerOperation.getTags()) {
        if (StringUtils.isNotEmpty(tag)) {
          return;
        }
      }
    }

    // if there is no tag, set default tag
    if (!swaggerGenerator.getDefaultTags().isEmpty()) {
      swaggerOperation.setTags(new ArrayList<>(swaggerGenerator.getDefaultTags()));
    }
  }

  protected void scanMethodParameters() {
    initParameterGenerators();

    Set<String> names = new HashSet<>();
    for (ParameterGenerator parameterGenerator : parameterGenerators) {
      scanMethodParameter(parameterGenerator);

      if (!names.add(parameterGenerator.getParameterName())) {
        throw new IllegalStateException(
            String.format("not support duplicated parameter, name=%s.", parameterGenerator.getParameterName()));
      }
      swaggerOperation.addParameter(parameterGenerator.getGeneratedParameter());
    }
  }

  private void initParameterGenerators() {
    // 1.group method annotations by parameter name
    // key is parameter name
    Map<String, List<Annotation>> methodAnnotationMap = initMethodAnnotationByParameterName();

    // 2.create ParameterGenerators by method parameters, merge annotations with method annotations
    initMethodParameterGenerators(methodAnnotationMap);

    // 3.create ParameterGenerators remains method annotations
    initRemainMethodAnnotationsParameterGenerators(methodAnnotationMap);

    // 4.check
    //   httpParameterType should not be null
    long bodyCount = parameterGenerators.stream().filter(p -> p.getHttpParameterType().equals(HttpParameterType.BODY))
        .count();
    if (bodyCount > 1) {
      throw new IllegalStateException(String.format("defined %d body parameter.", bodyCount));
    }
  }

  protected void initMethodParameterGenerators(Map<String, List<Annotation>> methodAnnotationMap) {
    for (java.lang.reflect.Parameter methodParameter : method.getParameters()) {
      ParameterGenerator parameterGenerator = new ParameterGenerator(method, methodAnnotationMap, methodParameter);
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
      ParameterGenerator propertyParameterGenerator = new ParameterGenerator(method,
          methodAnnotationMap,
          propertyDefinition.getName(),
          annotations,
          propertyDefinition.getPrimaryType().getRawClass());
      parameterGenerators.add(propertyParameterGenerator);
    }
  }

  protected void initRemainMethodAnnotationsParameterGenerators(Map<String, List<Annotation>> methodAnnotationMap) {
    for (Entry<String, List<Annotation>> entry : methodAnnotationMap.entrySet()) {
      ParameterGenerator parameterGenerator = new ParameterGenerator(entry.getKey(), entry.getValue());
      parameterGenerators.add(parameterGenerator);
    }
  }

  private Map<String, List<Annotation>> initMethodAnnotationByParameterName() {
    Map<String, List<Annotation>> methodAnnotations = new LinkedHashMap<>();
    for (Annotation annotation : method.getAnnotations()) {
      if (annotation instanceof ApiImplicitParams) {
        for (ApiImplicitParam apiImplicitParam : ((ApiImplicitParams) annotation).value()) {
          addMethodAnnotationByParameterName(methodAnnotations, apiImplicitParam.name(), apiImplicitParam);
        }
        continue;
      }

      if (annotation instanceof ApiParam) {
        addMethodAnnotationByParameterName(methodAnnotations, ((ApiParam) annotation).name(), annotation);
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

  protected void validateParameter(Type type) {
    if (type instanceof HttpServletResponse) {
      // not support, log the reason
      throw new IllegalStateException(
          "all input/output of ServiceComb operation are models, not allow to use HttpServletResponse.");
    }
  }

  protected void scanMethodParameter(ParameterGenerator parameterGenerator) {
    Parameter parameter = createParameter(parameterGenerator);

    try {
      fillParameter(swagger,
          parameter,
          parameterGenerator.getParameterName(),
          parameterGenerator.getGenericType(),
          parameterGenerator.getAnnotations());
    } catch (Throwable e) {
      throw new IllegalStateException(
          String.format("failed to fill parameter, parameterName=%s.",
              parameterGenerator.getParameterName()),
          e);
    }
  }

  protected Parameter createParameter(ParameterGenerator parameterGenerator) {
    if (parameterGenerator.getGeneratedParameter() == null) {
      Parameter parameter = createParameter(parameterGenerator.getHttpParameterType());
      parameterGenerator.setGeneratedParameter(parameter);
    }
    parameterGenerator.getGeneratedParameter().setName(parameterGenerator.getParameterName());
    return parameterGenerator.getGeneratedParameter();
  }

  protected Parameter createParameter(HttpParameterType httpParameterType) {
    switch (httpParameterType) {
      case PATH:
        return new PathParameter();
      case QUERY:
        return new QueryParameter();
      case HEADER:
        return new HeaderParameter();
      case FORM:
        return new FormParameter();
      case COOKIE:
        return new CookieParameter();
      case BODY:
        return new BodyParameter();
      default:
        throw new IllegalStateException("not support httpParameterType " + httpParameterType);
    }
  }

  protected void fillParameter(Swagger swagger, Parameter parameter, String parameterName, Type type,
      List<Annotation> annotations) {
    for (Annotation annotation : annotations) {
      ParameterProcessor<Parameter, Annotation> processor = findParameterProcessors(annotation.annotationType());
      if (processor != null) {
        processor.fillParameter(swagger, swaggerOperation, parameter, type, annotation);
      }
    }

    if (type == null) {
      return;
    }

    ParameterProcessor<Parameter, Annotation> processor = findParameterProcessors(type);
    if (processor != null) {
      processor.fillParameter(swagger, swaggerOperation, parameter, type, null);
    }

    if (parameter instanceof AbstractSerializableParameter) {
      io.swagger.util.ParameterProcessor.applyAnnotations(swagger, parameter, type, annotations);
      return;
    }

    fillBodyParameter(swagger, parameter, type, annotations);
  }

  protected void fillBodyParameter(Swagger swagger, Parameter parameter, Type type, List<Annotation> annotations) {
    // so strange, for bodyParameter, swagger return a new instance
    // that will cause lost some information
    // so we must merge them
    BodyParameter newBodyParameter = (BodyParameter) io.swagger.util.ParameterProcessor.applyAnnotations(
        swagger, parameter, type, annotations);

    // swagger missed enum data, fix it
    ModelImpl model = SwaggerUtils.getModelImpl(swagger, newBodyParameter);
    if (model != null) {
      Property property = ModelConverters.getInstance().readAsProperty(type);
      if (property instanceof StringProperty) {
        model.setEnum(((StringProperty) property).getEnum());
      }
    }

    mergeBodyParameter((BodyParameter) parameter, newBodyParameter);
  }

  private void mergeBodyParameter(BodyParameter bodyParameter, BodyParameter fromBodyParameter) {
    if (fromBodyParameter.getExamples() != null) {
      bodyParameter.setExamples(fromBodyParameter.getExamples());
    }
    if (fromBodyParameter.getRequired()) {
      bodyParameter.setRequired(true);
    }
    if (StringUtils.isNotEmpty(fromBodyParameter.getDescription())) {
      bodyParameter.setDescription(fromBodyParameter.getDescription());
    }
    if (StringUtils.isNotEmpty(fromBodyParameter.getAccess())) {
      bodyParameter.setAccess(fromBodyParameter.getAccess());
    }
    if (fromBodyParameter.getSchema() != null) {
      bodyParameter.setSchema(fromBodyParameter.getSchema());
    }
  }

  @Override
  public void addOperationToSwagger() {
    if (StringUtils.isEmpty(httpMethod)) {
      return;
    }

    Path pathObj = swagger.getPath(path);
    if (pathObj == null) {
      pathObj = new Path();
      swagger.path(path, pathObj);
    }

    HttpMethod hm = HttpMethod.valueOf(httpMethod.toUpperCase(Locale.US));
    if (pathObj.getOperationMap().get(hm) != null) {
      throw new IllegalStateException(String.format("Only allowed one default path. method=%s:%s.",
          method.getDeclaringClass().getName(),
          method.getName()));
    }
    pathObj.set(httpMethod, swaggerOperation);
  }

  public void correctOperation() {
    if (swaggerOperation.getConsumes() == null) {
      if (swaggerOperation.getParameters().stream()
          .filter(SwaggerUtils::isFileParameter)
          .findAny()
          .isPresent()) {
        swaggerOperation.addConsumes(MediaType.MULTIPART_FORM_DATA);
      }
    }

    SwaggerUtils.correctResponses(swaggerOperation);
    addHeaderToResponse();
  }

  private void addHeaderToResponse() {
    for (Entry<String, Response> responseEntry : swaggerOperation.getResponses().entrySet()) {
      Response response = responseEntry.getValue();

      for (Entry<String, Property> entry : methodResponseHeaders.entrySet()) {
        if (response.getHeaders() != null && response.getHeaders().containsKey(entry.getKey())) {
          continue;
        }

        response.addHeader(entry.getKey(), entry.getValue());
      }
    }
  }

  public void scanResponse() {
    if (swaggerOperation.getResponses() != null) {
      Response successResponse = swaggerOperation.getResponses().get(SwaggerConst.SUCCESS_KEY);
      if (successResponse != null) {
        if (successResponse.getResponseSchema() == null) {
          // 标注已经定义了response，但是是void，这可能是在标注上未定义
          // 根据函数原型来处理response
          Model model = createResponseModel();
          successResponse.setResponseSchema(model);
        }
        return;
      }
    }

    Model model = createResponseModel();
    Response response = new Response();
    response.setResponseSchema(model);
    swaggerOperation.addResponse(SwaggerConst.SUCCESS_KEY, response);
  }

  protected Model createResponseModel() {
    Type responseType = method.getGenericReturnType();
    if (ReflectionUtils.isVoid(responseType)) {
      return null;
    }

    ResponseTypeProcessor processor = findResponseTypeProcessor(responseType);
    return processor.process(swaggerGenerator, this, responseType);
  }
}
