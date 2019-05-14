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

import java.lang.reflect.Method;
import java.util.List;

import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.models.Swagger;

public interface SwaggerGenerator {
  Logger LOGGER = LoggerFactory.getLogger(SwaggerGenerator.class);

  static Swagger generate(Class<?> cls) {
    return create(cls).generate();
  }

  static SwaggerGenerator create(Class<?> cls) {
    List<SwaggerGeneratorFactory> factories = SPIServiceUtils.getOrLoadSortedService(SwaggerGeneratorFactory.class);
    for (SwaggerGeneratorFactory factory : factories) {
      if (factory.canProcess(cls)) {
        LOGGER.info("select [{}] for [{}] to generate schema.", factory.getClass().getName(), cls.getName());
        return factory.create(cls);
      }
    }

    throw new IllegalStateException("impossible, must be bug. can not generate swagger for " + cls.getName());
  }

  SwaggerGeneratorFeature getSwaggerGeneratorFeature();

  /**
   * support placeholder
   * @param basePath
   */
  void setBasePath(String basePath);

  void scanClassAnnotation();

  Swagger generate();

  Class<?> getClazz();

  Swagger getSwagger();

  void setHttpMethod(String httpMethod);

  /**
   *
   * @param tagName default tag for operations
   */
  void addDefaultTag(String tagName);

  void replaceMethodWhiteList(String... methodNames);

  <T extends OperationGenerator> T createOperationGenerator(Method method);
}
