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
import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.swagger.engine.SwaggerConsumerOperation;
import org.apache.servicecomb.swagger.generator.core.utils.ParamUtils;
import org.apache.servicecomb.swagger.invocation.response.ResponsesMeta;

import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.models.Operation;
import io.swagger.models.Swagger;

public class PojoConsumerOperationMeta {
  private PojoConsumerMeta pojoConsumerMeta;

  private OperationMeta operationMeta;

  private SwaggerConsumerOperation swaggerConsumerOperation;

  private ResponsesMeta responsesMeta = new ResponsesMeta();

  public PojoConsumerOperationMeta(PojoConsumerMeta pojoConsumerMeta, OperationMeta operationMeta,
      SwaggerConsumerOperation swaggerConsumerOperation,
      Swagger intfSwagger, Operation intfOperation) {
    this.pojoConsumerMeta = pojoConsumerMeta;
    this.operationMeta = operationMeta;
    this.swaggerConsumerOperation = swaggerConsumerOperation;

    operationMeta.getResponsesMeta().cloneTo(responsesMeta);
    operationMeta.setSwaggerConsumerOperation(swaggerConsumerOperation);
    responsesMeta.init(intfSwagger, intfOperation);
    Type intfResponseType = ParamUtils
        .getGenericParameterType(swaggerConsumerOperation.getConsumerClass(),
            swaggerConsumerOperation.getConsumerMethod().getDeclaringClass(),
            swaggerConsumerOperation.getConsumerMethod().getGenericReturnType());

    if (intfResponseType instanceof Class && Part.class.isAssignableFrom((Class<?>) intfResponseType)) {
      return;
    }

    intfResponseType = findResponseTypeProcessor(intfResponseType).extractResponseType(intfResponseType);
    if (intfResponseType != null) {
      responsesMeta.getResponseMap().put(Status.OK.getStatusCode(),
          TypeFactory.defaultInstance().constructType(intfResponseType));
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

  public ResponsesMeta getResponsesMeta() {
    return responsesMeta;
  }
}
