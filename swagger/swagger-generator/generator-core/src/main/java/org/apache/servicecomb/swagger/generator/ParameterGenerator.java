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

import static org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils.collectGenericType;
import static org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils.collectHttpParameterType;
import static org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils.collectParameterAnnotations;
import static org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils.collectParameterName;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;

import io.swagger.models.parameters.Parameter;

public class ParameterGenerator {
  public String parameterName;

  public List<Annotation> annotations;

  /**
   * when wrap parameters to body, genericType is null
   */
  public Type genericType;

  public HttpParameterType httpParameterType;

  public Parameter generatedParameter;

  public ParameterGenerator(String parameterName, List<Annotation> annotations, Type genericType,
      HttpParameterType httpParameterType, Parameter generatedParameter) {
    this.parameterName = parameterName;
    this.annotations = annotations;
    this.genericType = genericType;
    this.httpParameterType = httpParameterType;
    this.generatedParameter = generatedParameter;
  }

  public ParameterGenerator(Executable executable, Map<String, List<Annotation>> methodAnnotationMap,
      String defaultName,
      Annotation[] parameterAnnotations, Type genericType) {
    this.parameterName = collectParameterName(executable, parameterAnnotations,
        defaultName);
    this.annotations = collectParameterAnnotations(parameterAnnotations,
        methodAnnotationMap,
        parameterName);
    this.genericType = collectGenericType(annotations, genericType);
    this.httpParameterType = collectHttpParameterType(annotations, genericType);
  }

  public ParameterGenerator(Executable executable, Map<String, List<Annotation>> methodAnnotationMap,
      java.lang.reflect.Parameter methodParameter) {
    this(executable,
        methodAnnotationMap,
        methodParameter.isNamePresent() ? methodParameter.getName() : null,
        methodParameter.getAnnotations(),
        methodParameter.getParameterizedType());
  }

  public ParameterGenerator(String parameterName, List<Annotation> annotations) {
    this.parameterName = parameterName;
    this.annotations = annotations;
    this.genericType = collectGenericType(annotations, null);
    this.httpParameterType = collectHttpParameterType(annotations, genericType);
  }
}
