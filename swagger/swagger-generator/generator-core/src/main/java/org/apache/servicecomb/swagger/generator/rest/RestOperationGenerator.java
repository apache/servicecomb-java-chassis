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
package org.apache.servicecomb.swagger.generator.rest;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.swagger.generator.core.AbstractOperationGenerator;
import org.apache.servicecomb.swagger.generator.core.AbstractSwaggerGenerator;

public abstract class RestOperationGenerator extends AbstractOperationGenerator {
  public RestOperationGenerator(AbstractSwaggerGenerator swaggerGenerator, Method method) {
    super(swaggerGenerator, method);
  }

  @Override
  public void correctOperation() {
    checkPath();
    correctPath();
    super.correctOperation();
  }

  protected void checkPath() {
    if (StringUtils.isEmpty(path)
        && StringUtils.isEmpty(swagger.getBasePath())) {
      throw new IllegalStateException("Path must not both be empty in class and method");
    }
  }

  protected void correctPath() {
    if (StringUtils.isEmpty(path)) {
      path = "/";
    }
  }
}
