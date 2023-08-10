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

import static org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils.findClassAnnotationProcessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.inject.PlaceholderResolver;
import org.apache.servicecomb.swagger.generator.ClassAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.OperationGenerator;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.SwaggerGeneratorFeature;
import org.apache.servicecomb.swagger.generator.core.utils.MethodUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;


/**
 * <pre>
 * support:
 * 1.pojo + swagger annotation
 *   wrap all input parameter to be fields of body
 * 2.pojo + swagger annotation + jaxrs annotation
 * 3.pojo + swagger annotation + springmvc annotation
 * </pre>
 */
public abstract class AbstractSwaggerGenerator implements SwaggerGenerator {
  protected SwaggerGeneratorFeature swaggerGeneratorFeature = new SwaggerGeneratorFeature();

  protected SwaggerGeneratorContext swaggerGeneratorContext = new SwaggerGeneratorContext();

  protected Class<?> cls;

  protected OpenAPI openAPI;

  // allowed to control only process some methods
  // empty means all methods are available
  protected Set<String> methodWhiteList = new HashSet<>();

  // key is operationId
  // to check if operationId is duplicated
  protected Map<String, AbstractOperationGenerator> operationGenerators = new LinkedHashMap<>();

  protected String httpMethod;

  public AbstractSwaggerGenerator(Class<?> cls) {
    this.openAPI = new OpenAPI();
    this.openAPI.components(new Components())
        .paths(new Paths())
        .servers(new ArrayList<>())
        .info(new Info());
    this.cls = cls;
  }

  public OpenAPI getOpenAPI() {
    return openAPI;
  }

  @Override
  public Class<?> getClazz() {
    return cls;
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  @Override
  public void setHttpMethod(String httpMethod) {
    this.httpMethod = httpMethod.toUpperCase(Locale.US);
  }

  @Override
  public SwaggerGeneratorFeature getSwaggerGeneratorFeature() {
    return swaggerGeneratorFeature;
  }

  @Override
  public SwaggerGeneratorContext getSwaggerGeneratorContext() {
    return swaggerGeneratorContext;
  }

  public OpenAPI generate() {
    LOGGER.info("generate schema from [{}]", cls);
    scanClassAnnotation();

    ThreadLocal<SwaggerGeneratorFeature> featureThreadLocal = SwaggerGeneratorFeature.getFeatureThreadLocal();
    featureThreadLocal.set(swaggerGeneratorFeature);
    try {
      scanMethods();
      addOperationsToSwagger();

      correctSwagger();

      return openAPI;
    } finally {
      featureThreadLocal.remove();
    }
  }

  public void scanClassAnnotation() {
    ThreadLocal<SwaggerGeneratorFeature> featureThreadLocal = SwaggerGeneratorFeature.getFeatureThreadLocal();
    featureThreadLocal.set(swaggerGeneratorFeature);
    try {
      for (Annotation annotation : cls.getAnnotations()) {
        ClassAnnotationProcessor<Annotation> processor = findClassAnnotationProcessor(annotation.annotationType());
        if (processor == null) {
          continue;
        }
        processor.process(this, annotation);
      }
    } finally {
      featureThreadLocal.remove();
    }
  }

  /**
   * fill empty and required field to be default value
   * if can not build default value, then throw exceptions
   */
  protected void correctSwagger() {
    correctBasePath();
    correctInfo();
  }

  private void correctBasePath() {
    if (openAPI.getServers() == null) {
      openAPI.setServers(new ArrayList<>());
    }
    if (openAPI.getServers().size() <= 0) {
      Server server = new Server();
      server.setUrl("/" + cls.getSimpleName());
      openAPI.getServers().add(server);
    }
  }

  private void correctInfo() {
    Info info = openAPI.getInfo();
    if (info == null) {
      info = new Info();
      openAPI.setInfo(info);
    }

    if (StringUtils.isEmpty(info.getTitle())) {
      info.setTitle("swagger definition for " + cls.getName());
    }
    if (StringUtils.isEmpty(info.getVersion())) {
      info.setVersion("1.0.0");
    }
  }

  @Override
  public void replaceMethodWhiteList(String... methodNames) {
    methodWhiteList.clear();

    if (methodNames == null || methodNames.length == 0) {
      return;
    }

    methodWhiteList.addAll(Arrays.asList(methodNames));
  }

  /**
   * Whether this method should be processed as a swagger operation
   * @return true if this isn't a swagger operation; otherwise, false.
   */
  protected boolean isSkipMethod(Method method) {
    if (method.getDeclaringClass().getName().equals(Object.class.getName())) {
      return true;
    }
    // skip static method
    int modifiers = method.getModifiers();
    if (Modifier.isStatic(modifiers)) {
      return true;
    }
    // skip bridge method
    if (method.isBridge()) {
      return true;
    }

    Operation apiOperation = method.getAnnotation(Operation.class);
    if (apiOperation != null && apiOperation.hidden()) {
      return true;
    }

    if (!methodWhiteList.isEmpty()) {
      return !methodWhiteList.contains(MethodUtils.findSwaggerMethodName(method));
    }

    return false;
  }

  protected void scanMethods() {
    List<Method> methods = MethodUtils.findSwaggerMethods(cls);

    for (Method method : methods) {
      if (isSkipMethod(method)) {
        continue;
      }

      AbstractOperationGenerator operationGenerator = createOperationGenerator(method);
      operationGenerator.setHttpMethod(httpMethod);
      try {
        operationGenerator.generate();
      } catch (Throwable e) {
        String msg = String.format("generate swagger operation failed, method=%s:%s.",
            this.cls.getName(), method.getName());
        throw new IllegalStateException(msg, e);
      }

      if (StringUtils.isEmpty(operationGenerator.httpMethod)) {
        throw new IllegalStateException(
            String.format("HttpMethod must not both be empty in class and method, method=%s:%s.",
                cls.getName(), method.getName()));
      }

      if (operationGenerators.putIfAbsent(operationGenerator.getOperationId(), operationGenerator) != null) {
        throw new IllegalStateException(
            String.format("OperationId must be unique. method=%s:%s.", cls.getName(), method.getName()));
      }
    }
  }

  protected void addOperationsToSwagger() {
    for (OperationGenerator operationGenerator : operationGenerators.values()) {
      operationGenerator.addOperationToSwagger();
    }
  }

  @Override
  public void setBasePath(String basePath) {
    basePath = new PlaceholderResolver().replaceFirst(basePath);
    Server server = new Server();
    server.setUrl(basePath);
    if (openAPI.getServers() == null) {
      openAPI.setServers(new ArrayList<>());
    }
    openAPI.getServers().add(server);
  }
}
