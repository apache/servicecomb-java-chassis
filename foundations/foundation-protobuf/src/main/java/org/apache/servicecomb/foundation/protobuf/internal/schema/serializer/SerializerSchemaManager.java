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
package org.apache.servicecomb.foundation.protobuf.internal.schema.serializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.RootSerializer;
import org.apache.servicecomb.foundation.protobuf.internal.schema.AnySchema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.FieldSchema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.MapSchema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.MessageAsFieldSchema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.RepeatedSchema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.SchemaCreateContext;
import org.apache.servicecomb.foundation.protobuf.internal.schema.SchemaManager;

import io.protostuff.compiler.model.Field;
import io.protostuff.compiler.model.Message;
import io.protostuff.runtime.MessageSchema;

public class SerializerSchemaManager extends SchemaManager {
  // key is short message name
  private Map<String, RootSerializer> schemas = new HashMap<>();

  private Map<String, RootSerializer> canonicalSchemas = new HashMap<>();

  public SerializerSchemaManager(ProtoMapper protoMapper) {
    super(protoMapper);

    buildSchemas();
  }

  public RootSerializer findRootSerializer(String shortMessageName) {
    return schemas.get(shortMessageName);
  }

  public RootSerializer findRootSerializerByCanonical(String canonicalMessageName) {
    return canonicalSchemas.get(canonicalMessageName);
  }

  protected void buildSchemas() {
    SchemaCreateContext context = new SchemaCreateContext();
    for (Message message : proto.getMessages()) {
      RootSerializer rootSerializer = createRootSerializer(context, message);
      schemas.put(message.getName(), rootSerializer);
      canonicalSchemas.put(message.getCanonicalName(), rootSerializer);
    }
  }

  protected RootSerializer createRootSerializer(SchemaCreateContext context, Message message) {
    MessageSchema schema = createSchema(context, message);

    return new RootSerializer(schema);
  }

  protected MessageSchema createSchema(SchemaCreateContext context, Message message) {
    MessageSchema schema = context.getSchemas().get(message.getName());
    if (schema != null) {
      return schema;
    }

    schema = new MessageSchema();
    context.getSchemas().put(message.getName(), schema);

    List<io.protostuff.runtime.Field<Object>> fieldSchemas = new ArrayList<>();
    for (Field protoField : message.getFields()) {
      FieldSchema fieldSchema = createSchemaField(context, protoField, protoField.isRepeated());
      fieldSchemas.add(fieldSchema);
    }
    schema.init(protoMapper, fieldSchemas, message);
    return schema;
  }

  protected FieldSchema createSchemaField(SchemaCreateContext context, Field protoField, boolean repeated) {
    if (protoField.isMap()) {
      Message entryMessage = (Message) protoField.getType();
      FieldSchema keySchema = createSchemaField(context, entryMessage.getField(1), false);
      FieldSchema valueSchema = createSchemaField(context, entryMessage.getField(2), false);
      return new MapSchema(protoField, keySchema, valueSchema);
    }

    if (protoField.isOneofPart()) {
      throw new IllegalStateException("not IMPL oneof  now.");
    }

    if (repeated) {
      FieldSchema schema = createSchemaField(context, protoField, false);
      return new RepeatedSchema(protoField, schema);
    }

    if (isAnyField(protoField, repeated)) {
      return new AnySchema(protoMapper, protoField);
    }

    if (protoField.getType().isEnum()) {
      return new EnumSerializerSchema(protoField);
    }

    if (protoField.getType().isScalar()) {
      return createScalarField(protoField);
    }

    // message
    MessageSchema messageSchema = createSchema(context, (Message) protoField.getType());
    MessageAsFieldSchema messageAsFieldSchema = new MessageAsFieldSchema(protoField, messageSchema);
    return messageAsFieldSchema;
  }
}
