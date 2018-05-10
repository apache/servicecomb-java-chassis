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
package org.apache.servicecomb.swagger.converter.swaggerToClassGenerator;

import org.apache.servicecomb.common.javassist.JavassistUtils;
import org.apache.servicecomb.swagger.converter.SwaggerToClassGenerator;
import org.apache.servicecomb.swagger.generator.core.SwaggerConst;
import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.SwaggerGeneratorContext;
import org.apache.servicecomb.swagger.generator.springmvc.SpringmvcSwaggerGeneratorContext;

import io.swagger.models.Model;
import io.swagger.models.Swagger;

public class TestSwaggerToClassGenerator_base {
  ClassLoader classLoader = new ClassLoader() {
  };

  SwaggerGeneratorContext context = new SpringmvcSwaggerGeneratorContext();

  SwaggerGenerator swaggerGenerator = new SwaggerGenerator(context, ToClassSchema.class);

  Swagger swagger = swaggerGenerator.generate();

  SwaggerToClassGenerator swaggerToClassGenerator = new SwaggerToClassGenerator(classLoader, swagger, "gen");

  Class<?> swaggerIntf;

  public TestSwaggerToClassGenerator_base(boolean clearXJavaClass) {
    if (clearXJavaClass) {
      for (Model model : swagger.getDefinitions().values()) {
        model.getVendorExtensions().remove(SwaggerConst.EXT_JAVA_CLASS);
      }
    }
    swaggerIntf = swaggerToClassGenerator.convert();
  }

  public void tearDown() {
    JavassistUtils.clearByClassLoader(classLoader);
  }
}
