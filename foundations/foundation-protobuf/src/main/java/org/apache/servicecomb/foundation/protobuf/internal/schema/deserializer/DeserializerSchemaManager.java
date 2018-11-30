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
package org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.bean.MapGetter;
import org.apache.servicecomb.foundation.common.utils.bean.MapSetter;
import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.RootDeserializer;
import org.apache.servicecomb.foundation.protobuf.internal.bean.BeanDescriptor;
import org.apache.servicecomb.foundation.protobuf.internal.bean.BeanFactory;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;
import org.apache.servicecomb.foundation.protobuf.internal.schema.AnySchema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.FieldSchema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.MapSchema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.MessageAsFieldSchema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.RepeatedSchema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.SchemaCreateContext;
import org.apache.servicecomb.foundation.protobuf.internal.schema.SchemaManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;

import io.protostuff.compiler.model.Field;
import io.protostuff.compiler.model.Message;
import io.protostuff.runtime.MessageSchema;
import io.protostuff.runtime.RuntimeEnv;

public class DeserializerSchemaManager extends SchemaManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(DeserializerSchemaManager.class);

  public DeserializerSchemaManager(ProtoMapper protoMapper) {
    super(protoMapper);
  }

  public RootDeserializer createRootDeserializer(JavaType javaType, String shortMessageName) {
    Message message = proto.getMessage(shortMessageName);
    if (message == null) {
      throw new IllegalStateException("can not find proto message to create deserializer, name=" + shortMessageName);
    }

    return createRootDeserializer(javaType, message);
  }

  public RootDeserializer createRootDeserializer(JavaType javaType, Message message) {
    SchemaCreateContext context = new SchemaCreateContext();
    MessageSchema schema = createSchema(context, javaType, message);

    BeanDescriptor beanDescriptor = protoMapper.getBeanDescriptorManager().getOrCreateBeanDescriptor(javaType);
    return new RootDeserializer(beanDescriptor, schema);
  }

  protected MessageSchema createSchema(SchemaCreateContext context, JavaType javaType,
      Message message) {
    MessageSchema schema = context.getSchemas().get(message.getName());
    if (schema != null) {
      return schema;
    }

    schema = new MessageSchema();
    context.getSchemas().put(message.getName(), schema);

    doCreateSchema(context, schema, javaType, message);
    return schema;
  }

  @SuppressWarnings("unchecked")
  protected void doCreateSchema(SchemaCreateContext context, MessageSchema schema,
      JavaType javaType,
      Message message) {
    if (Map.class.isAssignableFrom(javaType.getRawClass())) {
      doCreateSchemaToMap(context, schema, javaType, message);
      return;
    }

    BeanDescriptor beanDescriptor = protoMapper.getBeanDescriptorManager().getOrCreateBeanDescriptor(javaType);

    List<io.protostuff.runtime.Field<Object>> fieldSchemas = new ArrayList<>();
    for (PropertyDescriptor propertyDescriptor : beanDescriptor.getPropertyDescriptors().values()) {
      Field protoField = message.getField(propertyDescriptor.getName());
      if (protoField == null) {
        LOGGER.info("java field {}:{} not exist in proto message {}, ignore it.",
            beanDescriptor.getJavaType().getRawClass().getName(),
            propertyDescriptor.getName(), message.getCanonicalName());
        continue;
      }
      if (propertyDescriptor.getSetter() == null) {
        LOGGER.info("no setter for java field {}:{} in proto message {}, ignore it.",
            beanDescriptor.getJavaType().getRawClass().getName(),
            propertyDescriptor.getName(), message.getCanonicalName());
        continue;
      }

      FieldSchema fieldSchema = createSchemaField(context, propertyDescriptor.getJavaType(), protoField,
          protoField.isRepeated());
      fieldSchema.setGetter(propertyDescriptor.getGetter());
      fieldSchema.setSetter(propertyDescriptor.getSetter());
      if (isAnyField(protoField, protoField.isRepeated())) {
        fieldSchema.setFactory(BeanFactory::mapFactory);
      } else {
        fieldSchema.setFactory(propertyDescriptor.getFactory());
      }
      fieldSchemas.add(fieldSchema);
    }

    schema.init(protoMapper, (Class<Object>) javaType.getRawClass(),
        fieldSchemas,
        RuntimeEnv.newInstantiator((Class<Object>) javaType.getRawClass()), message);
  }

  @SuppressWarnings("unchecked")
  protected void doCreateSchemaToMap(SchemaCreateContext context,
      MessageSchema schema, JavaType mapJavaType,
      Message message) {
    List<io.protostuff.runtime.Field<Object>> fieldSchemas = new ArrayList<>();
    for (Field protoField : message.getFields()) {
      FieldSchema fieldSchema = createSchemaField(context, mapJavaType, protoField, protoField.isRepeated());
      fieldSchema.setGetter(new MapGetter(protoField.getName()));
      fieldSchema.setSetter(new MapSetter(protoField.getName()));
      fieldSchema.setFactory(BeanFactory.createFactory(protoField));
      fieldSchemas.add(fieldSchema);
    }

    schema.init(protoMapper, (Class<Object>) mapJavaType.getRawClass(),
        fieldSchemas,
        RuntimeEnv.newInstantiator((Class<Object>) (Object) LinkedHashMap.class), message);
  }

  protected FieldSchema createSchemaField(SchemaCreateContext context, JavaType javaType, Field protoField,
      boolean repeated) {
    if (protoField.isMap()) {
      Message entryMessage = (Message) protoField.getType();
      FieldSchema keySchema = createSchemaField(context, javaType.getKeyType(), entryMessage.getField(1), false);
      FieldSchema valueSchema = createSchemaField(context, javaType.getContentType(), entryMessage.getField(2), false);
      return new MapSchema(protoField, keySchema, valueSchema);
    }

    if (protoField.isOneofPart()) {
      throw new IllegalStateException("not IMPL oneof  now.");
    }

    if (repeated) {
      FieldSchema schema = createSchemaField(context, javaType.getContentType(), protoField, false);
      return new RepeatedSchema(protoField, schema);
    }

    if (isAnyField(protoField, repeated)) {
      return new AnySchema(protoMapper, protoField);
    }

    if (protoField.getType().isEnum()) {
      return new EnumDeserializerSchema(protoField, javaType);
    }

    if (protoField.getType().isScalar()) {
      return createScalarField(protoField);
    }

    // message
    MessageSchema messageSchema = createSchema(context, javaType, (Message) protoField.getType());
    MessageAsFieldSchema messageAsFieldSchema = new MessageAsFieldSchema(protoField, messageSchema);
    return messageAsFieldSchema;
  }
}
