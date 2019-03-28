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
package org.apache.servicecomb.swagger.generator.pojo;

import java.lang.annotation.Annotation;

import javax.ws.rs.Path;

import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.SwaggerGeneratorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PojoSwaggerGeneratorFactory implements SwaggerGeneratorFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(PojoSwaggerGeneratorFactory.class);

  @Override
  public int getOrder() {
    return Integer.MAX_VALUE;
  }

  @Override
  public boolean canProcess(Class<?> cls) {
    for (Annotation annotation : cls.getAnnotations()) {
      // we check the annotations by class name to avoid importing extra dependencies in this module
      if (annotation instanceof Path
          || "org.springframework.web.bind.annotation.RequestMapping"
          .equals(annotation.annotationType().getCanonicalName())) {
        LOGGER.info(
            "There is @RequestMapping or @Path annotation on the REST interface class, but POJO swagger generator is chosen. "
                + "If this is unexpected, maybe you should check your dependency jar files.");
      }
    }
    return true;
  }

  @Override
  public SwaggerGenerator create(Class<?> cls) {
    return new PojoSwaggerGenerator(cls);
  }
}
