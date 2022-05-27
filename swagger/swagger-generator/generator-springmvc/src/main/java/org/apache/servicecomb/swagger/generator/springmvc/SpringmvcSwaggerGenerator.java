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

package org.apache.servicecomb.swagger.generator.springmvc;

import java.lang.reflect.Method;

import org.apache.servicecomb.swagger.generator.OperationGenerator;
import org.apache.servicecomb.swagger.generator.rest.RestSwaggerGenerator;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

public class SpringmvcSwaggerGenerator extends RestSwaggerGenerator {
  public SpringmvcSwaggerGenerator(Class<?> cls) {
    super(cls);
  }

  @Override
  protected boolean isSkipMethod(Method method) {
    if (super.isSkipMethod(method)) {
      return true;
    }

    return method.getAnnotation(RequestMapping.class) == null &&
        method.getAnnotation(GetMapping.class) == null &&
        method.getAnnotation(PutMapping.class) == null &&
        method.getAnnotation(PostMapping.class) == null &&
        method.getAnnotation(PatchMapping.class) == null &&
        method.getAnnotation(DeleteMapping.class) == null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends OperationGenerator> T createOperationGenerator(Method method) {
    return (T) new SpringmvcOperationGenerator(this, method);
  }
}
