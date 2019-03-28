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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;
import org.apache.servicecomb.swagger.generator.core.processor.response.DefaultResponseTypeProcessor;

import io.swagger.models.parameters.Parameter;

public final class SwaggerGeneratorUtils {
  // all static fields load from SPI and stateless
  private static Set<Type> contextTypes = SPIServiceUtils.getOrLoadSortedService(SwaggerContextRegister.class).stream()
      .map(SwaggerContextRegister::getContextType)
      .collect(Collectors.toSet());

  private static Map<Type, ClassAnnotationProcessor<?>> classAnnotationProcessors = new HashMap<>();

  private static Map<Type, MethodAnnotationProcessor<?>> methodAnnotationProcessors = new HashMap<>();

  private static Map<Type, ParameterProcessor<?, ?>> parameterProcessors = new HashMap<>();

  private static Map<Type, ResponseTypeProcessor> responseTypeProcessors = new HashMap<>();

  private static DefaultResponseTypeProcessor defaultResponseTypeProcessor = new DefaultResponseTypeProcessor();

  static {
    for (ClassAnnotationProcessor<?> processor : SPIServiceUtils
        .getOrLoadSortedService(ClassAnnotationProcessor.class)) {
      classAnnotationProcessors.put(processor.getProcessType(), processor);
    }
    for (MethodAnnotationProcessor<?> processor : SPIServiceUtils
        .getOrLoadSortedService(MethodAnnotationProcessor.class)) {
      methodAnnotationProcessors.put(processor.getProcessType(), processor);
    }
    for (ParameterProcessor<?, ?> processor : SPIServiceUtils.getOrLoadSortedService(ParameterProcessor.class)) {
      parameterProcessors.put(processor.getProcessType(), processor);
    }
    for (ResponseTypeProcessor processor : SPIServiceUtils.getOrLoadSortedService(ResponseTypeProcessor.class)) {
      responseTypeProcessors.put(processor.getProcessType(), processor);
    }
  }

  private SwaggerGeneratorUtils() {
  }

  @SuppressWarnings("unchecked")
  public static <ANNOTATION> ClassAnnotationProcessor<ANNOTATION> findClassAnnotationProcessor(Type type) {
    return (ClassAnnotationProcessor<ANNOTATION>) classAnnotationProcessors.get(type);
  }

  @SuppressWarnings("unchecked")
  public static <ANNOTATION> MethodAnnotationProcessor<ANNOTATION> findMethodAnnotationProcessor(Type type) {
    return (MethodAnnotationProcessor<ANNOTATION>) methodAnnotationProcessors.get(type);
  }

  @SuppressWarnings("unchecked")
  public static <SWAGGER_PARAMETER, ANNOTATION> ParameterProcessor<SWAGGER_PARAMETER, ANNOTATION> findParameterProcessors(
      Type type) {
    return (ParameterProcessor<SWAGGER_PARAMETER, ANNOTATION>) parameterProcessors.get(type);
  }

  public static ResponseTypeProcessor findResponseTypeProcessor(Type type) {
    return responseTypeProcessors.getOrDefault(type, defaultResponseTypeProcessor);
  }

  public static boolean isContextParameter(Type type) {
    return contextTypes.contains(type);
  }

  public static String collectParameterName(java.lang.reflect.Parameter methodParameter) {
    return collectParameterName(methodParameter.getDeclaringExecutable(), methodParameter.getAnnotations(),
        methodParameter.isNamePresent() ? methodParameter.getName() : null);
  }

  public static String collectParameterName(Executable executable, Annotation[] annotations, String defaultName) {
    // 1.annotations
    //   it's ambiguous to use different name in different annotation
    //   so we only read the first available name
    for (Annotation annotation : annotations) {
      ParameterProcessor<Parameter, Annotation> processor = findParameterProcessors(annotation.annotationType());
      if (processor == null) {
        continue;
      }

      String name = processor.getParameterName(annotation);
      if (StringUtils.isNotEmpty(name)) {
        return name;
      }
    }

    // 2.use signature name
    // ensure present parameter name
    if (StringUtils.isNotEmpty(defaultName)) {
      return defaultName;
    }

    String msg = String.format("parameter name is not present, method=%s:%s\n"
            + "solution:\n"
            + "  change pom.xml, add compiler argument: -parameters, for example:\n"
            + "    <plugin>\n"
            + "      <groupId>org.apache.maven.plugins</groupId>\n"
            + "      <artifactId>maven-compiler-plugin</artifactId>\n"
            + "      <configuration>\n"
            + "        <compilerArgument>-parameters</compilerArgument>\n"
            + "      </configuration>\n"
            + "    </plugin>",
        executable.getDeclaringClass().getName(), executable.getName());
    throw new IllegalStateException(msg);
  }

  public static Type collectGenericType(List<Annotation> annotations, Type defaultType) {
    Type genericType = null;
    for (Annotation annotation : annotations) {
      ParameterProcessor<Parameter, Annotation> processor = findParameterProcessors(annotation.annotationType());
      if (processor == null) {
        continue;
      }

      Type type = processor.getGenericType(annotation);
      if (type != null) {
        genericType = type;
      }
    }

    return genericType != null ? genericType : defaultType;
  }

  public static List<Annotation> collectParameterAnnotations(Annotation[] parameterAnnotations,
      Map<String, List<Annotation>> methodAnnotationMap, String parameterName) {
    List<Annotation> methodAnnotations = methodAnnotationMap.remove(parameterName);
    if (methodAnnotations == null) {
      methodAnnotations = Collections.emptyList();
    }

    List<Annotation> annotations = new ArrayList<>();
    Collections.addAll(annotations, parameterAnnotations);
    annotations.addAll(methodAnnotations);

    return annotations;
  }

  public static HttpParameterType collectHttpParameterType(List<Annotation> annotations, Type genericType) {
    // use the last available type
    for (int idx = annotations.size() - 1; idx >= 0; idx--) {
      Annotation annotation = annotations.get(idx);
      HttpParameterType httpParameterType = collectHttpParameterType(annotation, annotation.annotationType());
      if (httpParameterType != null) {
        return httpParameterType;
      }
    }

    // try by parameter type
    return collectHttpParameterType((Annotation) null, genericType);
  }

  private static HttpParameterType collectHttpParameterType(Annotation parameterAnnotation, Type type) {
    ParameterProcessor<Parameter, Annotation> processor = findParameterProcessors(type);
    if (processor == null) {
      return null;
    }

    return processor.getHttpParameterType(parameterAnnotation);
  }
}
