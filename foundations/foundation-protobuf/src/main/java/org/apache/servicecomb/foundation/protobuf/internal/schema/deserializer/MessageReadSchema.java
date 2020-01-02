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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoConst;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils;
import org.apache.servicecomb.foundation.protobuf.internal.bean.BeanDescriptor;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

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

  private final JavaType javaType;

  private final Map<String, Type> types;

  @SuppressWarnings("unchecked")
  // construct for request parameters
  public MessageReadSchema(ProtoMapper protoMapper, Message message, Map<String, Type> types) {
    this.protoMapper = protoMapper;
    this.message = message;
    this.types = types;
    if (!ProtoUtils.isWrapArguments(message) && types.size() > 0) {
      this.javaType = TypeFactory.defaultInstance().constructType(types.values().iterator().next());
      this.instantiator = RuntimeEnv.newInstantiator((Class<T>) javaType.getRawClass());
    } else {
      this.javaType = ProtoConst.MAP_TYPE;
      this.instantiator = RuntimeEnv.newInstantiator((Class<T>) ProtoConst.MAP_TYPE.getRawClass());
    }
  }

  @SuppressWarnings("unchecked")
  // construct for response type
  public MessageReadSchema(ProtoMapper protoMapper, Message message, JavaType javaType) {
    this.protoMapper = protoMapper;
    this.message = message;
    this.javaType = javaType;
    this.types = null;
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
    if (types != null) {
      if (ProtoUtils.isWrapArguments(message)) {
        this.fieldMap = (FieldMapEx<T>) protoMapper.getDeserializerSchemaManager()
            .createMapFields(message, types);
        return;
      }
      this.createFieldMap();
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
