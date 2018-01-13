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
import java.lang.reflect.Type;

public interface SwaggerGeneratorContext {
  // 用于排序，规避cse中定义的restSchema无法识别是jaxrs还是springmvc模式的问题
  // 实际使用时springmvc的context优先级最高
  int getOrder();

  boolean canProcess(Class<?> cls);

  boolean canProcess(Method method);

  ClassAnnotationProcessor findClassAnnotationProcessor(Class<? extends Annotation> annotationType);

  MethodAnnotationProcessor findMethodAnnotationProcessor(Class<? extends Annotation> annotationType);

  ParameterAnnotationProcessor findParameterAnnotationProcessor(Class<? extends Annotation> annotationType);

  // 管理方法入参的processor，用于支撑httpRequest、Context之类的特殊处理
  // key为class
  ParameterTypeProcessor findParameterTypeProcessor(Type parameterType);

  // 经过所有步骤后，仍然无法被处理的参数的默认处理器
  DefaultParameterProcessor getDefaultParamProcessor();

  ResponseTypeProcessor findResponseTypeProcessor(Type responseType);

  void postProcessOperation(OperationGenerator operationGenerator);

  String resolveStringValue(String strVal);
}
