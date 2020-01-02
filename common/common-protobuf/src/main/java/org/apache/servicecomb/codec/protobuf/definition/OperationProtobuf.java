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

package org.apache.servicecomb.codec.protobuf.definition;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.servicecomb.codec.protobuf.utils.ScopedProtobufSchemaManager;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.RequestRootDeserializer;
import org.apache.servicecomb.foundation.protobuf.RequestRootSerializer;
import org.apache.servicecomb.foundation.protobuf.ResponseRootDeserializer;
import org.apache.servicecomb.foundation.protobuf.RootSerializer;
import org.apache.servicecomb.swagger.engine.SwaggerConsumerOperation;
import org.apache.servicecomb.swagger.engine.SwaggerProducerOperation;

import io.protostuff.compiler.model.Message;

@SuppressWarnings("rawtypes")
public class OperationProtobuf {
  private ScopedProtobufSchemaManager scopedProtobufSchemaManager;

  private OperationMeta operationMeta;

  private RequestRootSerializer requestSerializer;

  // For wrapped parameters, this is a Map. While for POJO body, this is an Object.
  private RequestRootDeserializer<Object> requestDeserializer;

  private RootSerializer responseSerializer;

  private ResponseRootDeserializer<Object> responseDeserializer;

  public OperationProtobuf(ScopedProtobufSchemaManager scopedProtobufSchemaManager, OperationMeta operationMeta) {
    this.scopedProtobufSchemaManager = scopedProtobufSchemaManager;
    this.operationMeta = operationMeta;

    ProtoMapper mapper = scopedProtobufSchemaManager.getOrCreateProtoMapper(operationMeta.getSchemaMeta());
    Message requestMessage = mapper.getRequestMessage(operationMeta.getOperationId());

    if (operationMeta.getExtData(Const.PRODUCER_OPERATION) != null &&
        ((SwaggerProducerOperation) operationMeta.getExtData(Const.PRODUCER_OPERATION)).getProducerMethod() != null) {
      // producer invocation
      requestDeserializer = mapper
          .createRequestRootDeserializer(requestMessage, getMethodParameterTypesMap(
              ((SwaggerProducerOperation) operationMeta.getExtData(Const.PRODUCER_OPERATION)).getProducerMethod()));
      requestSerializer = mapper
          .createRequestRootSerializer(requestMessage, getMethodParameterTypesMap(
              ((SwaggerProducerOperation) operationMeta.getExtData(Const.PRODUCER_OPERATION)).getProducerMethod()),
              false);
    } else if (operationMeta.getExtData(Const.CONSUMER_OPERATION) != null
        && ((SwaggerConsumerOperation) operationMeta.getExtData(Const.CONSUMER_OPERATION)).getConsumerMethod()
        != null) {
      // consumer pojo invocation
      requestDeserializer = mapper
          .createRequestRootDeserializer(requestMessage, getMethodParameterTypesMap(
              ((SwaggerConsumerOperation) operationMeta.getExtData(Const.CONSUMER_OPERATION)).getConsumerMethod()));
      requestSerializer = mapper
          .createRequestRootSerializer(requestMessage, getMethodParameterTypesMap(
              ((SwaggerConsumerOperation) operationMeta.getExtData(Const.CONSUMER_OPERATION)).getConsumerMethod()),
              false);
    } else {
      // consumer RestTemplate invocation
      requestSerializer = mapper.createRequestRootSerializer(requestMessage, (Map<String, Type>) null, true);
      requestDeserializer = mapper.createRequestRootDeserializer(requestMessage, Object.class);
    }

    Message responseMessage = mapper.getResponseMessage(operationMeta.getOperationId());
    responseSerializer = mapper
        .createRootSerializer(responseMessage,
            operationMeta.getResponsesMeta().findResponseType(Status.OK.getStatusCode()));
    responseDeserializer = mapper
        .createResponseRootDeserializer(responseMessage,
            operationMeta.getResponsesMeta().findResponseType(Status.OK.getStatusCode()));
  }

  private Map<String, Type> getMethodParameterTypesMap(Method method) {
    Map<String, Type> parameters = new HashMap<>();
    for (Parameter parameter : method.getParameters()) {
      // TODO : WEAK parameter generics
      parameters.put(parameter.getName(), parameter.getType());
    }
    return parameters;
  }

  public OperationMeta getOperationMeta() {
    return operationMeta;
  }

  public RequestRootSerializer findRequestSerializer() {
    return requestSerializer;
  }

  public RequestRootDeserializer<Object> findRequestDesirializer() {
    return requestDeserializer;
  }

  public RootSerializer findResponseSerializer(int statusCode) {
    if (Family.SUCCESSFUL.equals(Family.familyOf(statusCode))) {
      return responseSerializer;
    }
    // TODO : handles only one response type.
    return null;
  }

  public ResponseRootDeserializer<Object> findResponseDesirialize(int statusCode) {
    if (Family.SUCCESSFUL.equals(Family.familyOf(statusCode))) {
      return responseDeserializer;
    }
    // TODO : handles only one response type.
    throw new IllegalStateException("not implemented now, statusCode = " + statusCode);
  }
}
