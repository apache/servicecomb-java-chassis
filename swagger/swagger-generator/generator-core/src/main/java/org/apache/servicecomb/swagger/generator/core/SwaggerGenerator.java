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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.foundation.common.utils.JvmUtils;
import org.springframework.util.StringUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.Info;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.parameters.Parameter;

/**
 * 根据class提取swagger信息
 * 支持以下场景的使用：
 * 1.pojo + swagger annotation
 * 2.pojo + swagger annotation + jaxrs annotation
 * 3.pojo + swagger annotation + springmvc annotation
 * 场景1，使用默认规则处理rest入参，比如将所有参数包装为一个class，统一放在body中
 * jaxrs/spring mvc的场景，需要配合各自的插件jar包
 * 非场景1，如果标注不完整，仍然会尝试使用场景1的规则产生默认数据
 */
public class SwaggerGenerator {
  protected SwaggerGeneratorContext context;

  // 需要生成class时，使用这个值
  protected String packageName;

  // 动态生成的class，加载在该classLoader中
  // 如果为null，表示加载在线程相关的classLoader中
  protected ClassLoader classLoader;

  protected Class<?> cls;

  protected Swagger swagger;

  /**
   * According to the definition of swagger, the {@link Tag} defined in {@link Api#tags()} will be set
   * to all of the operations in this swagger. And the {@link Tag} definde in {@link ApiOperation#tags()} will overwrite
   * the {@link Api#tags()}.
   */
  protected Set<String> defaultTags = new LinkedHashSet<>();

  private Map<String, OperationGenerator> operationGeneratorMap = new LinkedHashMap<>();

  private String httpMethod;

  public SwaggerGenerator(SwaggerGeneratorContext context, Class<?> cls) {
    this.swagger = new Swagger();
    this.context = context;
    this.cls = cls;
    this.classLoader = JvmUtils.findClassLoader();
    this.packageName = "gen.swagger";
  }

  public SwaggerGeneratorContext getContext() {
    return context;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String ensureGetPackageName() {
    // 不自动指定package，还是事先规划会比较合适
    if (packageName == null) {
      throw new Error("package name must not be null.");
    }

    return packageName;
  }

  public ClassLoader getClassLoader() {
    return classLoader;
  }

  public void setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  public Swagger getSwagger() {
    return swagger;
  }

  public Class<?> getCls() {
    return cls;
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public void setHttpMethod(String httpMethod) {
    this.httpMethod = httpMethod.toLowerCase(Locale.US);
  }

  public Swagger generate() {
    for (Annotation annotation : cls.getAnnotations()) {
      ClassAnnotationProcessor processor = context.findClassAnnotationProcessor(annotation.annotationType());
      if (processor == null) {
        continue;
      }
      processor.process(annotation, this);
    }

    scanMethods();
    addOperationsToSwagger();

    correctSwagger();

    return swagger;
  }

  /**
   * 查找必填但是没值的字段，将之设置为默认值
   * 如果无法构造默认值，则抛出异常
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

    setJavaInterface(info, cls);
  }

  protected void setJavaInterface(Info info, Class<?> cls) {
    if (cls.isInterface() && !isInterfaceReactive(cls)) {
      info.setVendorExtension(SwaggerConst.EXT_JAVA_INTF, cls.getName());
      return;
    }

    //        Class<?>[] interfaces = cls.getInterfaces();
    //        if (interfaces.length == 1) {
    //            info.setVendorExtension(SwaggerConst.EXT_JAVA_INTF, interfaces[0].getName());
    //            return;
    //        }

    String intfName = ensureGetPackageName() + "." + cls.getSimpleName() + "Intf";
    info.setVendorExtension(SwaggerConst.EXT_JAVA_INTF, intfName);
  }

  /**
   * Whether this interface class is reactive.
   * This is used for the situation that the {@code interfaceCls} may be used as swagger interface directly
   */
  private boolean isInterfaceReactive(Class<?> interfaceCls) {
    for (Method method : interfaceCls.getDeclaredMethods()) {
      if (isSkipMethod(method)) {
        continue;
      }
      if (CompletableFuture.class.isAssignableFrom(method.getReturnType())) {
        return true;
      }
    }
    return false;
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

    ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
    if (apiOperation != null && apiOperation.hidden()) {
      return apiOperation.hidden();
    }

    return !context.canProcess(method);
  }

  protected void scanMethods() {
    // 有时方法顺序不同，很不利于测试，所以先排序
    List<Method> methods = Arrays.asList(cls.getMethods());
    methods.sort(Comparator.comparing(Method::getName));
    for (Method method : methods) {
      if (isSkipMethod(method)) {
        continue;
      }
      OperationGenerator operationGenerator = new OperationGenerator(this, method);
      operationGenerator.setHttpMethod(httpMethod);
      try {
        operationGenerator.generate();
      } catch (Throwable e) {
        String msg =
            String.format("generate operation swagger failed, %s:%s", this.cls.getName(), method.getName());
        throw new Error(msg, e);
      }

      String operationId = operationGenerator.getOperation().getOperationId();
      if (operationGeneratorMap.containsKey(operationId)) {
        throw new Error(String.format("OperationId must be unique. %s:%s", cls.getName(), method.getName()));
      }
      operationGeneratorMap.put(operationId, operationGenerator);
    }
  }

  protected void addOperationsToSwagger() {
    for (OperationGenerator operationGenerator : operationGeneratorMap.values()) {
      operationGenerator.addOperationToSwagger();
    }
  }

  public void setBasePath(String basePath) {
    basePath = context.resolveStringValue(basePath);
    swagger.setBasePath(basePath);
  }

  public Map<String, OperationGenerator> getOperationGeneratorMap() {
    return operationGeneratorMap;
  }

  public List<Parameter> findProviderParameter(String methodName) {
    OperationGenerator operationGenerator = operationGeneratorMap.get(methodName);
    if (operationGenerator == null) {
      throw new Error("method not found, name=" + methodName);
    }

    return operationGenerator.getProviderParameters();
  }

  public List<Parameter> findSwaggerParameter(String methodName) {
    for (Path path : swagger.getPaths().values()) {
      for (Operation operation : path.getOperations()) {
        if (methodName.equals(operation.getOperationId())) {
          return operation.getParameters();
        }
      }
    }

    throw new Error("method not found, name=" + methodName);
  }

  /**
   * Add a tag to {@link #defaultTags} if the corresponding tag not exists.
   * @param tagName the name of the added tag
   */
  public void addDefaultTag(String tagName) {
    if (StringUtils.isEmpty(tagName) || defaultTags.contains(tagName)) {
      return;
    }

    defaultTags.add(tagName);
  }

  public Set<String> getDefaultTags() {
    return defaultTags;
  }
}
