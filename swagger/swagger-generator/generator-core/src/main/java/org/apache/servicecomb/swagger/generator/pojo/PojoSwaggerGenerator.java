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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.apache.servicecomb.swagger.generator.OperationGenerator;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.apache.servicecomb.swagger.generator.core.AbstractSwaggerGenerator;

import jakarta.ws.rs.core.MediaType;

public class PojoSwaggerGenerator extends AbstractSwaggerGenerator {
  protected static final List<String> SUPPORTED_CONTENT_TYPE
      = Arrays.asList(MediaType.APPLICATION_JSON, SwaggerConst.PROTOBUF_TYPE, MediaType.TEXT_PLAIN);

  public PojoSwaggerGenerator(Class<?> cls) {
    super(cls);
    swaggerGeneratorContext.updateConsumes(SUPPORTED_CONTENT_TYPE);
    swaggerGeneratorContext.updateProduces(SUPPORTED_CONTENT_TYPE);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends OperationGenerator> T createOperationGenerator(Method method) {
    return (T) new PojoOperationGenerator(this, method);
  }
}
