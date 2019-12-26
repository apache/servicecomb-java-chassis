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

import java.util.Map;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.servicecomb.codec.protobuf.utils.ScopedProtobufSchemaManager;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.RootDeserializer;
import org.apache.servicecomb.foundation.protobuf.RootSerializer;

import io.protostuff.compiler.model.Message;

@SuppressWarnings("rawtypes")
public class OperationProtobuf {
  private ScopedProtobufSchemaManager scopedProtobufSchemaManager;

  private OperationMeta operationMeta;

  private RootSerializer requestSerializer;

  private RootDeserializer<Object> requestDeserializer;

  private RootSerializer responseSerializer;

  private RootDeserializer<Object> responseDeserializer;

  public OperationProtobuf(ScopedProtobufSchemaManager scopedProtobufSchemaManager, OperationMeta operationMeta) {
    this.scopedProtobufSchemaManager = scopedProtobufSchemaManager;
    this.operationMeta = operationMeta;

    ProtoMapper mapper = scopedProtobufSchemaManager.getOrCreateProtoMapper(operationMeta.getSchemaMeta());
    Message requestMessage = mapper.getRequestMessage(operationMeta.getOperationId());
    requestSerializer = mapper.createRootSerializer(requestMessage, Object.class);
    requestDeserializer = mapper.createRootDeserializer(requestMessage, Object.class);

    Message responseMessage = mapper.getResponseMessage(operationMeta.getOperationId());
    responseSerializer = mapper
        .createRootSerializer(responseMessage,
            operationMeta.getResponsesMeta().findResponseType(Status.OK.getStatusCode()));
    responseDeserializer = mapper
        .createRootDeserializer(responseMessage,
            operationMeta.getResponsesMeta().findResponseType(Status.OK.getStatusCode()));
  }

  public OperationMeta getOperationMeta() {
    return operationMeta;
  }

  public RootSerializer findRequestSerializer() {
    return requestSerializer;
  }

  public RootDeserializer<Object> findRequestDesirializer() {
    return requestDeserializer;
  }

  public RootSerializer findResponseSerializer(int statusCode) {
    if (Family.SUCCESSFUL.equals(Family.familyOf(statusCode))) {
      return responseSerializer;
    }
    // TODO : handles only one response type.
    return null;
  }

  public RootDeserializer<Object> findResponseDesirialize(int statusCode) {
    if (Family.SUCCESSFUL.equals(Family.familyOf(statusCode))) {
      return responseDeserializer;
    }
    // TODO : handles only one response type.
    return null;
  }
}
