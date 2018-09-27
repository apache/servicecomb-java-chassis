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

package io.protostuff.runtime;

import static io.protostuff.runtime.RuntimeSchema.MIN_TAG_FOR_HASH_FIELD_MAP;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.internal.bean.BeanDescriptor;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;
import org.apache.servicecomb.foundation.protobuf.internal.schema.FieldSchema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.PojoFieldSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Schema;
import io.protostuff.compiler.model.Message;
import io.protostuff.runtime.RuntimeEnv.Instantiator;

public class MessageSchema implements Schema<Object>, FieldMap<Object> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MessageSchema.class);

  private ProtoMapper protoMapper;

  private FieldMap<Object> fieldMap;

  private Class<Object> typeClass;

  private Instantiator<Object> instantiator;

  private Message message;

  // one class can bind to different proto message (almost different version)
  // so save the information only in message, not global
  private final Map<Type, List<PojoFieldSerializer>> pojoFieldSerializers = new ConcurrentHashMapEx<>();

  public void init(ProtoMapper protoMapper, Collection<Field<Object>> fields, Message message) {
    init(protoMapper, null, fields, null, message);
  }

  public void init(ProtoMapper protoMapper, Class<Object> typeClass, Collection<Field<Object>> fields,
      Instantiator<Object> instantiator,
      Message message) {
    this.protoMapper = protoMapper;
    this.fieldMap = createFieldMap(fields);
    this.instantiator = instantiator;
    this.typeClass = typeClass;
    this.message = message;
  }

  public Message getMessage() {
    return message;
  }

  private FieldMap<Object> createFieldMap(Collection<Field<Object>> fields) {
    int lastFieldNumber = 0;
    for (Field<Object> field : fields) {
      if (field.number > lastFieldNumber) {
        lastFieldNumber = field.number;
      }
    }
    if (preferHashFieldMap(fields, lastFieldNumber)) {
      return new HashFieldMap<>(fields);
    }
    // array field map should be more efficient
    return new ArrayFieldMap<>(fields, lastFieldNumber);
  }

  private boolean preferHashFieldMap(Collection<Field<Object>> fields, int lastFieldNumber) {
    return lastFieldNumber > MIN_TAG_FOR_HASH_FIELD_MAP && lastFieldNumber >= 2 * fields.size();
  }

  @Override
  public String getFieldName(int number) {
    // only called on writes
    final Field<Object> field = fieldMap.getFieldByNumber(number);
    return field == null ? null : field.name;
  }

  @Override
  public int getFieldNumber(String name) {
    final Field<Object> field = fieldMap.getFieldByName(name);
    return field == null ? null : field.number;
  }

  @Override
  public boolean isInitialized(Object message) {
    return true;
  }

  @Override
  public Object newMessage() {
    return instantiator.newInstance();
  }

  @Override
  public String messageName() {
    return message.getName();
  }

  @Override
  public String messageFullName() {
    return message.getCanonicalName();
  }

  @Override
  public Class<? super Object> typeClass() {
    return typeClass;
  }

  @Override
  public void mergeFrom(Input input, Object message) throws IOException {
    Field<Object> field = null;
    try {
      for (int n = input.readFieldNumber(this); n != 0; n = input.readFieldNumber(this)) {
        field = fieldMap.getFieldByNumber(n);
        if (field == null) {
          input.handleUnknownField(n, this);
        } else {
          field.mergeFrom(input, message);
        }
      }
    } catch (IOException e) {
      logError((FieldSchema) field, "deserialize", e);
      throw e;
    } catch (RuntimeException e) {
      logError((FieldSchema) field, "deserialize", e);
      throw e;
    }
  }

  protected void logError(FieldSchema fieldSchema, String action, Throwable e) {
    if (fieldSchema == null) {
      return;
    }

    io.protostuff.compiler.model.Field protoField = fieldSchema.getProtoField();
    LOGGER.error("Failed to {}, field={}:{}, type={}",
        action,
        protoField.getType().getCanonicalName(),
        protoField.getName(),
        protoField.getTypeName(),
        e.getMessage());
  }

  @SuppressWarnings("unchecked")
  @Override
  public void writeTo(Output output, Object message) throws IOException {
    if (message == null) {
      return;
    }

    if (message instanceof Map) {
      writeFromMap(output, (Map<String, Object>) message);
      return;
    }

    writeFromPojo(output, message);
  }

  /**
   * <pre>
   * when use with generic
   * each time serialize the value field, will run with the real type
   * so there is no problem
   *
   * eg: CustomeGeneric&lt;User&gt; someMethod(CustomGeneric&lt;People&gt; input)
   *   input: {
   *     People value
   *   }
   *   output: {
   *     User value
   *   }
   * </pre>
   * @param output
   * @param value
   * @throws Throwable
   */
  protected void writeFromPojo(Output output, Object value) throws IOException {
    List<PojoFieldSerializer> serializers = pojoFieldSerializers
        .computeIfAbsent(value.getClass(), this::createFieldSerializers);
    for (PojoFieldSerializer serializer : serializers) {
      serializer.writeTo(output, value);
    }
  }

  protected List<PojoFieldSerializer> createFieldSerializers(Type type) {
    BeanDescriptor beanDescriptor = protoMapper.getBeanDescriptorManager().getOrCreateBeanDescriptor(type);
    List<PojoFieldSerializer> pojoFieldSerializers = new ArrayList<>();
    for (Field<Object> f : fieldMap.getFields()) {
      PropertyDescriptor propertyDescriptor = beanDescriptor.getPropertyDescriptors().get(f.name);
      if (propertyDescriptor == null) {
        continue;
      }

      Getter getter = propertyDescriptor.getGetter();
      if (getter == null) {
        continue;
      }

      pojoFieldSerializers.add(new PojoFieldSerializer(getter, (FieldSchema) f));
    }

    return pojoFieldSerializers;
  }

  protected void writeFromMap(Output output, Map<String, Object> map) throws IOException {
    for (Entry<String, Object> entry : map.entrySet()) {
      if (entry.getValue() == null) {
        continue;
      }

      Field<Object> field = fieldMap.getFieldByName(entry.getKey());
      if (field == null) {
        // not defined in proto, ignore it.
        continue;
      }

      field.writeTo(output, entry.getValue());
    }
  }

  @Override
  public Field<Object> getFieldByNumber(int n) {
    return fieldMap.getFieldByNumber(n);
  }

  @Override
  public Field<Object> getFieldByName(String fieldName) {
    return fieldMap.getFieldByName(fieldName);
  }

  @Override
  public int getFieldCount() {
    return fieldMap.getFieldCount();
  }

  @Override
  public List<Field<Object>> getFields() {
    return fieldMap.getFields();
  }
}
