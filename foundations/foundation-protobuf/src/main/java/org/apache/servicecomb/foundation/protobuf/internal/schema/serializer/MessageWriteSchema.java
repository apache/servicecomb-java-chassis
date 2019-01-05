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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.internal.bean.BeanDescriptor;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;

import com.fasterxml.jackson.databind.JavaType;

import io.protostuff.InputEx;
import io.protostuff.OutputEx;
import io.protostuff.SchemaEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.compiler.model.Message;
import io.protostuff.runtime.FieldMapEx;
import io.protostuff.runtime.FieldSchema;

/**
 * <pre>
 * map.put("user", new User())
 * root write from map, but user should write from pojo
 * so one schema should support dynamic and concrete logic at the same time
 * </pre>
 */
public class MessageWriteSchema<T> implements SchemaEx<T> {
  protected ProtoMapper protoMapper;

  protected Message message;

  private JavaType javaType;

  // mostly, one message only relate to one pojo
  private final Class<T> mainPojoCls;

  private FieldMapEx<T> mainPojoFieldMaps;

  private FieldMapEx<Map<Object, Object>> mapFieldMaps;

  // if not equals to mainPojoCls, then will find from pojoFieldMaps
  private final Map<Class<?>, FieldMapEx<?>> pojoFieldMaps = new ConcurrentHashMapEx<>();

  @SuppressWarnings("unchecked")
  public MessageWriteSchema(ProtoMapper protoMapper, Message message, JavaType javaType) {
    this.protoMapper = protoMapper;
    this.message = message;
    this.javaType = javaType;
    this.mainPojoCls = (Class<T>) javaType.getRawClass();
  }

  public Message getMessage() {
    return message;
  }

  @Override
  public T newMessage() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String messageName() {
    return message.getName();
  }

  public JavaType getJavaType() {
    return javaType;
  }

  public Class<T> getMainPojoCls() {
    return mainPojoCls;
  }

  public FieldMapEx<T> getMainPojoFieldMaps() {
    return mainPojoFieldMaps;
  }

  @Override
  public void init() {
    this.mainPojoFieldMaps = createPojoFields(javaType);
  }

  private FieldMapEx<T> createPojoFields(Type type) {
    SerializerSchemaManager serializerSchemaManager = protoMapper.getSerializerSchemaManager();
    BeanDescriptor beanDescriptor = protoMapper.getBeanDescriptorManager().getOrCreateBeanDescriptor(type);

    List<FieldSchema<T>> fieldSchemas = new ArrayList<>();
    for (Field protoField : message.getFields()) {
      PropertyDescriptor propertyDescriptor = beanDescriptor.getPropertyDescriptors().get(protoField.getName());
      if (propertyDescriptor == null) {
        continue;
      }

      Object getter = propertyDescriptor.getGetter();
      if (getter == null) {
        continue;
      }

      FieldSchema<T> fieldSchema = serializerSchemaManager.createSchemaField(protoField, propertyDescriptor);
      fieldSchemas.add(fieldSchema);
    }

    return FieldMapEx.createFieldMap(fieldSchemas);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void writeTo(OutputEx output, Object value) throws IOException {
    if (value instanceof Map) {
      writeFromMap(output, (Map<String, Object>) value);
      return;
    }

    if (mainPojoCls == value.getClass()) {
      writeFromMainPojo(output, (T) value);
      return;
    }

    writeDynamicPojo(output, value);
  }

  private void writeFromMainPojo(OutputEx output, T value) throws IOException {
    for (FieldSchema<T> fieldSchema : mainPojoFieldMaps.getFields()) {
      fieldSchema.getAndWriteTo(output, value);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> void writeDynamicPojo(OutputEx output, Object dynamicValue) throws IOException {
    FieldMapEx<T> fieldMapEx = (FieldMapEx<T>) this.pojoFieldMaps
        .computeIfAbsent(dynamicValue.getClass(), this::createPojoFields);

    T value = (T) dynamicValue;
    for (FieldSchema<T> fieldSchema : fieldMapEx.getFields()) {
      fieldSchema.getAndWriteTo(output, value);
    }
  }

  protected final void writeFromMap(OutputEx output, Map<String, Object> map) throws IOException {
    if (mapFieldMaps == null) {
      mapFieldMaps = protoMapper.getSerializerSchemaManager().createMapFields(message);
    }

    for (Entry<String, Object> entry : map.entrySet()) {
      if (entry.getValue() == null) {
        continue;
      }

      FieldSchema<Map<Object, Object>> fieldSchema = mapFieldMaps.getFieldByName(entry.getKey());
      if (fieldSchema != null) {
        fieldSchema.writeTo(output, entry.getValue());
      }
    }
  }

  @Override
  public void mergeFrom(InputEx input, T message) throws IOException {
    throw new UnsupportedOperationException();
  }
}
