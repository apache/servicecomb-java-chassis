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

package org.apache.servicecomb.swagger.generator.jaxrs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.ws.rs.HttpMethod;

import org.apache.servicecomb.swagger.generator.OperationGenerator;
import org.apache.servicecomb.swagger.generator.rest.RestSwaggerGenerator;

public class JaxrsSwaggerGenerator extends RestSwaggerGenerator {
  public JaxrsSwaggerGenerator(Class<?> cls) {
    super(cls);
  }

  @Override
  protected boolean isSkipMethod(Method method) {
    if (super.isSkipMethod(method)) {
      return true;
    }

    for (Annotation annotation : method.getAnnotations()) {
      HttpMethod httpMethod = annotation.annotationType().getAnnotation(HttpMethod.class);
      if (httpMethod != null) {
        return false;
      }
    }

    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends OperationGenerator> T createOperationGenerator(Method method) {
    return (T) new JaxrsOperationGenerator(this, method);
  }
}
