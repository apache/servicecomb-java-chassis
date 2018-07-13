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
package org.apache.servicecomb.engine;

import java.util.LinkedHashMap;

import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.swagger.converter.SwaggerToClassGenerator;
import org.apache.servicecomb.swagger.engine.SwaggerEnvironment;
import org.apache.servicecomb.swagger.engine.SwaggerProducer;
import org.apache.servicecomb.swagger.engine.bootstrap.BootstrapNormal;
import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;

import io.swagger.models.Swagger;

public class SwaggerEnvironmentForTest {
  private SwaggerEnvironment swaggerEnvironment = new BootstrapNormal().boot();

  private ClassLoader classLoader = new ClassLoader() {
  };

  public ClassLoader getClassLoader() {
    return classLoader;
  }

  public SwaggerEnvironment getSwaggerEnvironment() {
    return swaggerEnvironment;
  }

  public SwaggerProducer createProducer(Object producerInstance) {
    Class<?> producerCls = BeanUtils.getImplClassFromBean(producerInstance);
    SwaggerGenerator producerGenerator = UnitTestSwaggerUtils.generateSwagger(classLoader, producerCls);
    Swagger swagger = producerGenerator.getSwagger();

    SwaggerToClassGenerator swaggerToClassGenerator = new SwaggerToClassGenerator(classLoader, swagger,
        producerInstance.getClass().getPackage().getName());
    return swaggerEnvironment.createProducer(producerInstance, swaggerToClassGenerator.convert(),
        new LinkedHashMap<>());
  }
}
