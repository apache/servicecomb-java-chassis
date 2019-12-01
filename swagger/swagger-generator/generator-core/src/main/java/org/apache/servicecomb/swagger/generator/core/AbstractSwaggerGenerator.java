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
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.inject.PlaceholderResolver;
import org.apache.servicecomb.swagger.generator.ClassAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.OperationGenerator;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.SwaggerGeneratorFeature;
import org.apache.servicecomb.swagger.generator.core.utils.MethodUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.Info;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;

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

  protected Class<?> cls;

  protected Swagger swagger;

  // allowed to control only process some methods
  // empty means all methods are available
  protected Set<String> methodWhiteList = new HashSet<>();

  // key is operationId
  // to check if operationId is duplicated
  protected Map<String, AbstractOperationGenerator> operationGenerators = new LinkedHashMap<>();

  /**
   * According to the definition of swagger, the {@link Tag} defined in {@link Api#tags()} will be set
   * to all of the operations in this swagger. And the {@link Tag} definde in {@link ApiOperation#tags()} will overwrite
   * the {@link Api#tags()}.
   */
  protected Set<String> defaultTags = new LinkedHashSet<>();

  protected String httpMethod;

  @SuppressWarnings("unchecked")
  public AbstractSwaggerGenerator(Class<?> cls) {
    this.swagger = new Swagger();
    this.cls = cls;
  }

  public Swagger getSwagger() {
    return swagger;
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
    this.httpMethod = httpMethod.toLowerCase(Locale.US);
  }

  public SwaggerGeneratorFeature getSwaggerGeneratorFeature() {
    return swaggerGeneratorFeature;
  }

  public Swagger generate() {
    LOGGER.info("generate schema from [{}]", cls);
    scanClassAnnotation();

    ThreadLocal<SwaggerGeneratorFeature> featureThreadLocal = SwaggerGeneratorFeature.getFeatureThreadLocal();
    featureThreadLocal.set(swaggerGeneratorFeature);
    try {
      scanMethods();
      addOperationsToSwagger();

      correctSwagger();

      return swagger;
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
    if (StringUtils.isEmpty(swagger.getSwagger())) {
      swagger.setSwagger("2.0");
    }

    correctBasePath();
    correctInfo();
    correctProduces();
    correctConsumes();
  }

  private void correctProduces() {
    List<String> produces = swagger.getProduces();
    if (produces == null || produces.isEmpty()) {
      produces = Arrays.asList(MediaType.APPLICATION_JSON);
      swagger.setProduces(produces);
    }
  }

  private void correctConsumes() {
    List<String> consumes = swagger.getConsumes();
    if (consumes == null || consumes.isEmpty()) {
      consumes = Arrays.asList(MediaType.APPLICATION_JSON);
      swagger.setConsumes(consumes);
    }
  }

  protected void correctBasePath() {
    String basePath = swagger.getBasePath();
    if (StringUtils.isEmpty(basePath)) {
      basePath = "/" + cls.getSimpleName();
    }
    if (!basePath.startsWith("/")) {
      basePath = "/" + basePath;
    }
    swagger.setBasePath(basePath);
  }

  private void correctInfo() {
    Info info = swagger.getInfo();
    if (info == null) {
      info = new Info();
      swagger.setInfo(info);
    }

    if (StringUtils.isEmpty(info.getTitle())) {
      info.setTitle("swagger definition for " + cls.getName());
    }
    if (StringUtils.isEmpty(info.getVersion())) {
      info.setVersion("1.0.0");
    }

    setJavaInterface(info);
  }

  protected void setJavaInterface(Info info) {
    if (!swaggerGeneratorFeature.isExtJavaInterfaceInVendor()) {
      return;
    }

    if (cls.isInterface()) {// && !isInterfaceReactive(cls)) {
      info.setVendorExtension(SwaggerConst.EXT_JAVA_INTF, cls.getName());
      return;
    }

//    if (cls.getInterfaces().length > 0) {
//      info.setVendorExtension(SwaggerConst.EXT_JAVA_INTF, cls.getInterfaces()[0].getName());
//      return;
//    }

    if (StringUtils.isEmpty(swaggerGeneratorFeature.getPackageName())) {
      return;
    }

    String intfName = swaggerGeneratorFeature.getPackageName() + "." + cls.getSimpleName() + "Intf";
    info.setVendorExtension(SwaggerConst.EXT_JAVA_INTF, intfName);
  }

//  /**
//   * to avoid old invocation bug.
//   * @param interfaceCls
//   * @return
//   */
//  private boolean isInterfaceReactive(Class<?> interfaceCls) {
//    for (Method method : interfaceCls.getDeclaredMethods()) {
//      if (isSkipMethod(method)) {
//        continue;
//      }
//      if (CompletableFuture.class.isAssignableFrom(method.getReturnType())) {
//        return true;
//      }
//    }
//    return false;
//  }

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

    ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
    if (apiOperation != null && apiOperation.hidden()) {
      return apiOperation.hidden();
    }

    if (!methodWhiteList.isEmpty()) {
      return !methodWhiteList.contains(MethodUtils.findSwaggerMethodName(method));
    }

    return false;
  }

  protected void scanMethods() {
    List<Method> methods = MethodUtils.findProducerMethods(cls);

    for (Method method : methods) {
      if (isSkipMethod(method)) {
        continue;
      }

      AbstractOperationGenerator operationGenerator = createOperationGenerator(method);
      operationGenerator.setHttpMethod(httpMethod);
      try {
        operationGenerator.generate();
      } catch (Throwable e) {
        String msg = String
            .format("generate swagger operation failed, method=%s:%s.", this.cls.getName(), method.getName());
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
    swagger.setBasePath(basePath);
  }

  /**
   * Add a tag to {@link #defaultTags} if the corresponding tag not exists.
   * @param tagName the name of the added tag
   */
  public void addDefaultTag(String tagName) {
    if (StringUtils.isEmpty(tagName)) {
      return;
    }

    defaultTags.add(tagName);
  }

  public Set<String> getDefaultTags() {
    return defaultTags;
  }
}
