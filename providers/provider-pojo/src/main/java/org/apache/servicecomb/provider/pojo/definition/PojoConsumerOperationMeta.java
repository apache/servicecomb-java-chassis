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
package org.apache.servicecomb.provider.pojo.definition;

import static org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils.findResponseTypeProcessor;

import java.lang.reflect.Type;

import javax.servlet.http.Part;

import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.swagger.engine.SwaggerConsumerOperation;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.reflect.TypeToken;

public class PojoConsumerOperationMeta {
  private PojoConsumerMeta pojoConsumerMeta;

  private OperationMeta operationMeta;

  private SwaggerConsumerOperation swaggerConsumerOperation;

  private JavaType responseType;

  public PojoConsumerOperationMeta(PojoConsumerMeta pojoConsumerMeta, OperationMeta operationMeta,
      SwaggerConsumerOperation swaggerConsumerOperation) {
    this.pojoConsumerMeta = pojoConsumerMeta;
    this.operationMeta = operationMeta;
    this.swaggerConsumerOperation = swaggerConsumerOperation;

    Type intfResponseType =
        TypeToken.of(swaggerConsumerOperation.getConsumerClass())
            .resolveType(swaggerConsumerOperation.getConsumerMethod().getGenericReturnType())
            .getType();
    if (intfResponseType instanceof Class && Part.class.isAssignableFrom((Class<?>) intfResponseType)) {
      responseType = TypeFactory.defaultInstance().constructType(Part.class);
      return;
    }

    intfResponseType = findResponseTypeProcessor(intfResponseType).extractResponseType(intfResponseType);
    if (intfResponseType != null) {
      responseType = TypeFactory.defaultInstance().constructType(intfResponseType);
    }
  }

  public PojoConsumerMeta getPojoConsumerMeta() {
    return pojoConsumerMeta;
  }

  public OperationMeta getOperationMeta() {
    return operationMeta;
  }

  public SwaggerConsumerOperation getSwaggerConsumerOperation() {
    return swaggerConsumerOperation;
  }

  public JavaType getResponsesType() {
    return responseType;
  }
}
