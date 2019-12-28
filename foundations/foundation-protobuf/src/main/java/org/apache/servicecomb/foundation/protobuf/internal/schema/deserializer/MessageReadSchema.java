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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoConst;
import org.apache.servicecomb.foundation.protobuf.internal.bean.BeanDescriptor;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;

import io.protostuff.InputEx;
import io.protostuff.OutputEx;
import io.protostuff.SchemaEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.compiler.model.Message;
import io.protostuff.runtime.FieldMapEx;
import io.protostuff.runtime.FieldSchema;
import io.protostuff.runtime.RuntimeEnv;
import io.protostuff.runtime.RuntimeEnv.Instantiator;

/**
 * <pre>
 * map.put("user", new User())
 * root write from map, but user should write from pojo
 * so one schema should support dynamic and concrete logic at the same time
 * </pre>
 */
public class MessageReadSchema<T> implements SchemaEx<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MessageReadSchema.class);

  protected ProtoMapper protoMapper;

  protected Message message;

  private FieldMapEx<T> fieldMap;

  private Instantiator<T> instantiator;

  private JavaType javaType;

  private Method method;

  @SuppressWarnings("unchecked")
  public MessageReadSchema(ProtoMapper protoMapper, Message message, JavaType javaType, Method method) {
    this.protoMapper = protoMapper;
    this.message = message;
    this.javaType = javaType;
    this.method = method;
    if (javaType.isJavaLangObject() || Map.class.isAssignableFrom(javaType.getRawClass())) {
      javaType = ProtoConst.MAP_TYPE;
    }
    this.instantiator = RuntimeEnv.newInstantiator((Class<T>) javaType.getRawClass());
  }

  @SuppressWarnings("unchecked")
  public MessageReadSchema(ProtoMapper protoMapper, Message message, JavaType javaType) {
    this.protoMapper = protoMapper;
    this.message = message;
    this.javaType = javaType;
    if (javaType.isJavaLangObject() || Map.class.isAssignableFrom(javaType.getRawClass())) {
      javaType = ProtoConst.MAP_TYPE;
    }
    this.instantiator = RuntimeEnv.newInstantiator((Class<T>) javaType.getRawClass());
  }

  public Message getMessage() {
    return message;
  }

  @Override
  public T newMessage() {
    return instantiator.newInstance();
  }

  @Override
  public String messageName() {
    return message.getName();
  }

  public FieldMapEx<T> getFieldMap() {
    return fieldMap;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void init() {
    if (Map.class.isAssignableFrom(javaType.getRawClass())) {
      if (this.method == null) {
        this.fieldMap = (FieldMapEx<T>) protoMapper.getDeserializerSchemaManager()
            .createMapFields(message);
      } else {
        this.fieldMap = (FieldMapEx<T>) protoMapper.getDeserializerSchemaManager()
            .createMapFields(message, method);
      }
      return;
    }

    this.createFieldMap();
  }

  private void createFieldMap() {
    DeserializerSchemaManager deserializerSchemaManager = protoMapper.getDeserializerSchemaManager();
    BeanDescriptor beanDescriptor = protoMapper.getBeanDescriptorManager().getOrCreateBeanDescriptor(javaType);

    List<FieldSchema<T>> fieldSchemas = new ArrayList<>();
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

      FieldSchema<T> fieldSchema = deserializerSchemaManager.createSchemaField(protoField, propertyDescriptor);
      fieldSchemas.add(fieldSchema);
    }

    this.fieldMap = FieldMapEx.createFieldMap(fieldSchemas);
  }

  @Override
  public void mergeFrom(InputEx input, T message) throws IOException {
    FieldSchema<T> fieldSchema = null;
    try {
      for (int n = input.readFieldNumber(); n != 0; ) {
        fieldSchema = fieldMap.getFieldByNumber(n);
        if (fieldSchema != null) {
          n = fieldSchema.mergeFrom(input, message);
          continue;
        }

        input.handleUnknownField(n);
        n = input.readFieldNumber();
      }
    } catch (Throwable e) {
      Field protoField = fieldSchema.getProtoField();
      LOGGER.error("Failed to mergeFrom, field={}:{}, type={}",
          protoField.getType().getCanonicalName(),
          protoField.getName(),
          protoField.getTypeName(),
          e.getMessage());
      throw e;
    }
  }

  @Override
  public void writeTo(OutputEx output, Object value) throws IOException {
    throw new UnsupportedOperationException();
  }
}
