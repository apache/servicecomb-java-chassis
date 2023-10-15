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
package org.apache.servicecomb.common.rest.codec.produce;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.codec.protobuf.utils.ScopedProtobufSchemaManager;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.RootDeserializer;
import org.apache.servicecomb.foundation.protobuf.RootSerializer;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyWrapper;
import org.apache.servicecomb.swagger.generator.SwaggerConst;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

public class ProduceProtoBufferProcessor implements ProduceProcessor {
  public static final String RESPONSE_MESSAGE_NAME = "X_RESPONSE";

  public static final String EXT_ID = "protobuf";

  private static final Object LOCK = new Object();

  private final OperationMeta operationMeta;

  private final OpenAPI openAPI;

  private final Schema<?> schema;

  private final ScopedProtobufSchemaManager scopedProtobufSchemaManager;

  public ProduceProtoBufferProcessor(OperationMeta operationMeta, OpenAPI openAPI, Schema<?> schema) {
    this.operationMeta = operationMeta;
    this.openAPI = openAPI;
    this.schema = schema;
    this.scopedProtobufSchemaManager = getOrCreateScopedProtobufSchemaManager(operationMeta.getMicroserviceMeta());
  }

  private ScopedProtobufSchemaManager getOrCreateScopedProtobufSchemaManager(MicroserviceMeta microserviceMeta) {
    ScopedProtobufSchemaManager scopedProtobufSchemaManager = microserviceMeta.getExtData(EXT_ID);
    if (scopedProtobufSchemaManager == null) {
      synchronized (LOCK) {
        scopedProtobufSchemaManager = microserviceMeta.getExtData(EXT_ID);
        if (scopedProtobufSchemaManager == null) {
          scopedProtobufSchemaManager = new ScopedProtobufSchemaManager();
          microserviceMeta.putExtData(EXT_ID, scopedProtobufSchemaManager);
        }
      }
    }
    return scopedProtobufSchemaManager;
  }

  @Override
  public String getName() {
    return SwaggerConst.PROTOBUF_TYPE;
  }

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public void doEncodeResponse(OutputStream output, Object result) throws Exception {
    ProtoMapper protoMapper = scopedProtobufSchemaManager
        .getOrCreateProtoMapper(openAPI, operationMeta.getSchemaId(),
            RESPONSE_MESSAGE_NAME, schema);
    RootSerializer serializer = protoMapper.getSerializerSchemaManager()
        .createRootSerializer(protoMapper.getProto().getMessage(RESPONSE_MESSAGE_NAME),
            Object.class);
    Map<String, Object> bodyArg = new HashMap<>(1);
    bodyArg.put("value", result);
    output.write(serializer.serialize(bodyArg));
  }

  @Override
  public Object doDecodeResponse(InputStream input, JavaType type) throws Exception {
    ProtoMapper protoMapper = scopedProtobufSchemaManager
        .getOrCreateProtoMapper(openAPI, operationMeta.getSchemaId(),
            RESPONSE_MESSAGE_NAME, schema);
    RootDeserializer<PropertyWrapper<Object>> deserializer = protoMapper.getDeserializerSchemaManager()
        .createRootDeserializer(protoMapper.getProto().getMessage(RESPONSE_MESSAGE_NAME), type);
    PropertyWrapper<Object> result = deserializer.deserialize(input.readAllBytes());
    return result.getValue();
  }
}
