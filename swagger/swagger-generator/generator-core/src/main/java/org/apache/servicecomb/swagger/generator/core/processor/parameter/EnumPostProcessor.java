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
package org.apache.servicecomb.swagger.generator.core.processor.parameter;

import org.apache.servicecomb.swagger.generator.OperationPostProcessor;
import org.apache.servicecomb.swagger.generator.core.AbstractOperationGenerator;
import org.apache.servicecomb.swagger.generator.core.AbstractSwaggerGenerator;

public class EnumPostProcessor implements OperationPostProcessor {
  @Override
  public boolean shouldProcess(AbstractSwaggerGenerator swaggerGenerator,
      AbstractOperationGenerator operationGenerator) {
    return true;
  }

  @Override
  public void process(AbstractSwaggerGenerator swaggerGenerator, AbstractOperationGenerator operationGenerator) {
    // TODO: dynamic enum tests
//    for (ParameterGenerator parameterGenerator : operationGenerator.getParameterGenerators()) {
//      if (parameterGenerator.getGeneratedParameter() != null) {
//        processParameterDescription(parameterGenerator);
//      }
//    }
//
//    processResponseModelDescription(operationGenerator);
  }

//  private void processParameterDescription(ParameterGenerator parameterGenerator) {
//    JavaType genericType = parameterGenerator.getGenericType();
//    Annotation[] annotations = parameterGenerator.getAnnotations().toArray(new Annotation[0]);
//    String description = generateDescription(genericType, annotations);
//    if (description != null) {
//      Parameter parameter = parameterGenerator
//          .getGeneratedParameter();
//      parameter.setDescription(description);
//    }
//  }
//
//  private String generateDescription(Type type, Annotation[] annotations) {
//    JavaType javaType = TypeFactory.defaultInstance().constructType(type);
//    if (javaType.isEnumType()) {
//      return generateDescription(JDK, javaType.getRawClass(), annotations);
//    }
//
//    if (javaType.isTypeOrSubTypeOf(DynamicEnum.class)) {
//      return generateDescription(DYNAMIC, javaType.getRawClass(), annotations);
//    }
//
//    return null;
//  }
//
//  private String generateDescription(SwaggerEnum swaggerEnum, Class<?> enumClass, Annotation[] annotations) {
//    return swaggerEnum.findPropertyDescription(enumClass, annotations);
//  }
//
//  public void processResponseModelDescription(AbstractOperationGenerator operationGenerator) {
//    String description = generateDescription(operationGenerator.getMethod().getReturnType(), null);
//    if (description != null) {
//      operationGenerator.getOperation().getResponses().get(String.valueOf(Status.OK.getStatusCode()))
//          .getContent().get(MediaType.APPLICATION_JSON).getSchema()
//          .setDescription(description);
//    }
//  }
}
