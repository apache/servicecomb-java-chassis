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

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.servicecomb.codec.protobuf.utils.ScopedProtobufSchemaManager;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoConst;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils;

import com.fasterxml.jackson.databind.JavaType;

import io.protostuff.compiler.model.Message;

@SuppressWarnings("rawtypes")
public class OperationProtobuf {
  private RequestRootSerializer requestRootSerializer;

  private RequestRootDeserializer<Object> requestRootDeserializer;

  private ResponseRootSerializer responseRootSerializer;

  private ResponseRootDeserializer<Object> responseRootDeserializer;

  private ResponseRootSerializer anyResponseRootSerializer;

  private ResponseRootDeserializer<Object> anyResponseRootDeserializer;

  public OperationProtobuf(ScopedProtobufSchemaManager scopedProtobufSchemaManager, Invocation invocation) {
    initRequestCodec(scopedProtobufSchemaManager, invocation);
    initResponseCodec(scopedProtobufSchemaManager, invocation);
  }

  public RequestRootSerializer getRequestRootSerializer() {
    return this.requestRootSerializer;
  }

  public RequestRootDeserializer<Object> getRequestRootDeserializer() {
    return this.requestRootDeserializer;
  }

  public ResponseRootSerializer findResponseRootSerializer(int statusCode) {
    if (Family.SUCCESSFUL.equals(Family.familyOf(statusCode))) {
      return this.responseRootSerializer;
    }
    return anyResponseRootSerializer;
  }

  public ResponseRootDeserializer<Object> findResponseRootDeserializer(int statusCode) {
    if (Family.SUCCESSFUL.equals(Family.familyOf(statusCode))) {
      return this.responseRootDeserializer;
    }
    return anyResponseRootDeserializer;
  }

  private void initProducerRequestCodec(Invocation invocation, Message requestMessage, ProtoMapper mapper) {
    Map<String, Type> swaggerParameterTypes = invocation.getOperationMeta().getSwaggerProducerOperation()
        .getSwaggerParameterTypes();
    if (ProtoUtils.isWrapArguments(requestMessage)) {
      requestRootDeserializer = new RequestRootDeserializer<>(
          mapper.createRootDeserializer(requestMessage, swaggerParameterTypes), true, null);
    } else {
      if (swaggerParameterTypes.isEmpty()) {
        requestRootDeserializer = new RequestRootDeserializer<>(
            mapper.createRootDeserializer(requestMessage, Object.class), false, null);
      } else if (swaggerParameterTypes.size() == 1) {
        Entry<String, Type> entry = swaggerParameterTypes.entrySet().iterator().next();
        requestRootDeserializer = new RequestRootDeserializer<>(mapper.createRootDeserializer(requestMessage,
            entry.getValue()), false, entry.getKey());
      } else {
        throw new IllegalStateException(
            "unexpected operation definition " + invocation.getOperationMeta().getMicroserviceQualifiedName());
      }
    }
  }

  private void initConsumerRequestCodec(Invocation invocation, Message requestMessage, ProtoMapper mapper) {
    if (ProtoUtils.isWrapArguments(requestMessage)) {
      requestRootSerializer = new RequestRootSerializer(
          mapper.createRootSerializer(requestMessage, Object.class), true, false);
    } else {
      if (invocation.getOperationMeta().getSwaggerOperation().getParameters().isEmpty()) {
        requestRootSerializer = new RequestRootSerializer(mapper.createRootSerializer(requestMessage, Object.class),
            false, false);
      } else if (invocation.getOperationMeta().getSwaggerOperation().getParameters().size() == 1) {
        requestRootSerializer = new RequestRootSerializer(mapper.createRootSerializer(requestMessage,
            Object.class), false, true);
      } else {
        throw new IllegalStateException(
            "unexpected operation definition " + invocation.getOperationMeta().getMicroserviceQualifiedName());
      }
    }
  }

  private void initRequestCodec(ScopedProtobufSchemaManager scopedProtobufSchemaManager, Invocation invocation) {
    ProtoMapper mapper = scopedProtobufSchemaManager.getOrCreateProtoMapper(invocation.getSchemaMeta());
    Message requestMessage = mapper.getRequestMessage(invocation.getOperationMeta().getOperationId());

    if (!invocation.isConsumer()) {
      initProducerRequestCodec(invocation, requestMessage, mapper);
    } else {
      initConsumerRequestCodec(invocation, requestMessage, mapper);
    }
  }

  private void initProviderResponseCode(Message responseMessage, ProtoMapper mapper,
      JavaType responseType) {
    if (ProtoUtils.isWrapProperty(responseMessage)) {
      responseRootSerializer = new ResponseRootSerializer(
          mapper.createRootSerializer(responseMessage, responseType), true, false);
    } else {
      if (ProtoUtils.isEmptyMessage(responseMessage)) {
        responseRootSerializer = new ResponseRootSerializer(mapper.createRootSerializer(responseMessage,
            Object.class), false, false);
      } else {
        responseRootSerializer = new ResponseRootSerializer(mapper.createRootSerializer(responseMessage,
            responseType), false, true);
      }
    }
  }

  private void initConsumerResponseCode(Message responseMessage, ProtoMapper mapper,
      JavaType responseType) {
    if (ProtoUtils.isWrapProperty(responseMessage)) {
      responseRootSerializer = new ResponseRootSerializer(
          mapper.createRootSerializer(responseMessage, responseType), true, false);
      responseRootDeserializer = new ResponseRootDeserializer<>(
          mapper.createRootDeserializer(responseMessage, responseType), false);
    } else {
      if (ProtoUtils.isEmptyMessage(responseMessage)) {
        responseRootSerializer = new ResponseRootSerializer(mapper.createRootSerializer(responseMessage,
            Object.class), false, false);
        responseRootDeserializer = new ResponseRootDeserializer<>(
            mapper.createRootDeserializer(responseMessage, Object.class), true);
      } else {
        responseRootSerializer = new ResponseRootSerializer(mapper.createRootSerializer(responseMessage,
            responseType), false, false);
        responseRootDeserializer = new ResponseRootDeserializer<>(
            mapper.createRootDeserializer(responseMessage, responseType), false);
      }
    }
  }

  private void initResponseCodec(ScopedProtobufSchemaManager scopedProtobufSchemaManager, Invocation invocation) {
    ProtoMapper mapper = scopedProtobufSchemaManager.getOrCreateProtoMapper(invocation.getSchemaMeta());
    Message responseMessage = mapper.getResponseMessage(invocation.getOperationMeta().getOperationId());

    JavaType responseType = invocation.findResponseType(Status.OK.getStatusCode());
    if (!invocation.isConsumer()) {
      initProviderResponseCode(responseMessage, mapper, responseType);
    } else {
      initConsumerResponseCode(responseMessage, mapper, responseType);
    }
    anyResponseRootSerializer = new ResponseRootSerializer(mapper.createRootSerializer(ProtoConst.ANY,
        Object.class), false, true);
    anyResponseRootDeserializer = new ResponseRootDeserializer<>(
        mapper.createRootDeserializer(ProtoConst.ANY, Object.class), false);
  }
}
