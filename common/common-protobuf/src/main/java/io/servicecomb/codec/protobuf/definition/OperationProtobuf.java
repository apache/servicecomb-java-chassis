/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.codec.protobuf.definition;

import java.lang.reflect.Method;

import javax.ws.rs.core.Response.Status.Family;

import io.servicecomb.codec.protobuf.utils.ProtobufSchemaUtils;
import io.servicecomb.codec.protobuf.utils.WrapSchema;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.swagger.invocation.response.ResponseMeta;

public class OperationProtobuf {
  private OperationMeta operationMeta;

  private WrapSchema requestSchema;

  private WrapSchema responseSchema;

  public OperationProtobuf(OperationMeta operationMeta)
      throws Exception {
    this.operationMeta = operationMeta;

    requestSchema = ProtobufSchemaUtils.getOrCreateArgsSchema(operationMeta);

    Method method = operationMeta.getMethod();
    responseSchema = ProtobufSchemaUtils.getOrCreateSchema(method.getGenericReturnType());
  }

  public OperationMeta getOperationMeta() {
    return operationMeta;
  }

  public WrapSchema getRequestSchema() {
    return requestSchema;
  }

  public WrapSchema getResponseSchema() {
    return responseSchema;
  }

  public WrapSchema findResponseSchema(int statusCode) {
    if (Family.SUCCESSFUL.equals(Family.familyOf(statusCode))) {
      return responseSchema;
    }

    ResponseMeta responseMeta = operationMeta.findResponseMeta(statusCode);
    return ProtobufSchemaUtils.getOrCreateSchema(responseMeta.getJavaType());
  }
}
