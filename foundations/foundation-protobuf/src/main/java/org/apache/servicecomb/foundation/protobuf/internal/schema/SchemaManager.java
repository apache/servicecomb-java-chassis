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
package org.apache.servicecomb.foundation.protobuf.internal.schema;

import static org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils.isAnyField;
import static org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils.isWrapProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.utils.bean.MapGetter;
import org.apache.servicecomb.foundation.common.utils.bean.MapSetter;
import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoConst;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;
import org.apache.servicecomb.foundation.protobuf.internal.schema.any.AnySchema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.map.MapEntry;
import org.apache.servicecomb.foundation.protobuf.internal.schema.map.MapSchema;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.protostuff.SchemaEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.compiler.model.Message;
import io.protostuff.compiler.model.Proto;
import io.protostuff.runtime.FieldMapEx;
import io.protostuff.runtime.FieldSchema;

public abstract class SchemaManager {
  protected final ProtoMapper protoMapper;

  protected final Proto proto;

  // key is canonical message name + ":" + canonical type name
  protected Map<String, SchemaEx<?>> canonicalSchemas = new ConcurrentHashMapEx<>();

  public SchemaManager(ProtoMapper protoMapper) {
    this.protoMapper = protoMapper;
    this.proto = protoMapper.getProto();
  }

  public Map<String, SchemaEx<?>> getCanonicalSchemas() {
    return canonicalSchemas;
  }

  protected String generateCacheKey(Message message, JavaType javaType) {
    return message.getCanonicalName() + ":" + javaType.toCanonical();
  }

  protected abstract <T> SchemaEx<T> newMessageSchema(Message message, JavaType javaType);

  /**
   *
   * @param protoField
   * @param propertyDescriptor provide getter/setter/javaType
   * @return
   */
  protected abstract <T> FieldSchema<T> createScalarField(Field protoField, PropertyDescriptor propertyDescriptor);

  @SuppressWarnings("unchecked")
  protected <T> SchemaEx<T> getOrCreateMessageSchema(Message message, JavaType javaType) {
    String cacheKey = generateCacheKey(message, javaType);
    SchemaEx<T> messageSchema = (SchemaEx<T>) canonicalSchemas.get(cacheKey);
    if (messageSchema == null) {
      // messageSchema already put into canonicalSchemas inside createMessageSchema
      messageSchema = createMessageSchema(message, javaType);
    }
    return messageSchema;
  }

  @SuppressWarnings("unchecked")
  protected <T> SchemaEx<T> findSchema(String key) {
    return (SchemaEx<T>) canonicalSchemas.get(key);
  }

  protected <T> SchemaEx<T> createMessageSchema(Message message, JavaType javaType) {
    String cacheKey = generateCacheKey(message, javaType);
    SchemaEx<T> schema = findSchema(cacheKey);
    if (schema != null) {
      return schema;
    }

    schema = newMessageSchema(message, javaType);
    canonicalSchemas.put(cacheKey, schema);

    schema.init();
    return schema;
  }

  protected <T> FieldSchema<T> createMapFieldSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
    JavaType javaType = propertyDescriptor.getJavaType();
    if (javaType.isJavaLangObject()) {
      javaType = ProtoConst.MAP_TYPE;
    }

    JavaType entryType = TypeFactory.defaultInstance().constructParametricType(MapEntry.class,
        javaType.getKeyType(),
        javaType.getContentType());
    SchemaEx<Entry<Object, Object>> entrySchema = getOrCreateMessageSchema((Message) protoField.getType(),
        entryType);
    return new MapSchema<>(protoField, propertyDescriptor, entrySchema);
  }

  // normal message write from or read to a map
  public FieldMapEx<Map<Object, Object>> createMapFields(Message message) {
    List<FieldSchema<Map<Object, Object>>> fieldSchemas = new ArrayList<>();
    for (Field protoField : message.getFields()) {
      PropertyDescriptor propertyDescriptor = new PropertyDescriptor();
      propertyDescriptor.setJavaType(ProtoConst.OBJECT_TYPE);
      propertyDescriptor.setGetter(new MapGetter<>(protoField.getName()));
      propertyDescriptor.setSetter(new MapSetter<>(protoField.getName()));

      FieldSchema<Map<Object, Object>> fieldSchema = createSchemaField(protoField, propertyDescriptor);
      fieldSchemas.add(fieldSchema);
    }

    return FieldMapEx.createFieldMap(fieldSchemas);
  }

  public <T> FieldSchema<T> createSchemaField(Field protoField, PropertyDescriptor propertyDescriptor) {
    // map is a special repeated
    if (protoField.isMap()) {
      return createMapFieldSchema(protoField, propertyDescriptor);
    }

    if (protoField.isRepeated()) {
      return createRepeatedSchema(protoField, propertyDescriptor);
    }

    if (isAnyField(protoField)) {
      return new AnySchema<>(protoMapper, protoField, propertyDescriptor);
    }

    if (protoField.getType().isScalar()) {
      return createScalarField(protoField, propertyDescriptor);
    }

    // message
    if (protoField.getType().isMessage()) {
      SchemaEx<Object> messageSchema = getOrCreateMessageSchema((Message) protoField.getType(),
          propertyDescriptor.getJavaType());
      if (isWrapProperty((Message) protoField.getType())) {
        return new PropertyWrapperAsFieldSchema<>(protoField, propertyDescriptor, messageSchema);
      }

      return new MessageAsFieldSchema<>(protoField, propertyDescriptor, messageSchema);
    }

    if (protoField.isOneofPart()) {
      throw new IllegalStateException("not IMPL oneof now.");
    }

    ProtoUtils.throwNotSupportWrite(protoField, propertyDescriptor.getJavaType().getRawClass());
    return null;
  }

  protected abstract <T> FieldSchema<T> createRepeatedSchema(Field protoField, PropertyDescriptor propertyDescriptor);
}
