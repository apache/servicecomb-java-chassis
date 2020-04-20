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

import static org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils.isWrapProperty;

import java.lang.reflect.Type;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.TypesUtil;
import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.RootDeserializer;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoConst;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyWrapper;
import org.apache.servicecomb.foundation.protobuf.internal.schema.SchemaManager;
import org.apache.servicecomb.foundation.protobuf.internal.schema.any.AnyEntrySchema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.AnyRepeatedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.BytesRepeatedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.MessageRepeatedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.PropertyWrapperRepeatedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.StringRepeatedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.bools.impl.BoolNotPackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.bools.impl.BoolPackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.doubles.impl.DoubleNotPackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.doubles.impl.DoublePackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.enums.EnumNotPackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.enums.EnumPackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.floats.impl.FloatNotPackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.floats.impl.FloatPackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.ints.impl.Fixed32NotPackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.ints.impl.Fixed32PackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.ints.impl.Int32NotPackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.ints.impl.Int32PackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.ints.impl.SFixed32NotPackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.ints.impl.SFixed32PackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.ints.impl.SInt32NotPackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.ints.impl.SInt32PackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.ints.impl.UInt32NotPackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.ints.impl.UInt32PackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.longs.impl.Fixed64NotPackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.longs.impl.Fixed64PackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.longs.impl.Int64NotPackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.longs.impl.Int64PackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.longs.impl.SFixed64NotPackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.longs.impl.SFixed64PackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.longs.impl.SInt64NotPackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.longs.impl.SInt64PackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.longs.impl.UInt64NotPackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.longs.impl.UInt64PackedReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.scalar.BoolReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.scalar.BytesReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.scalar.DoubleReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.scalar.EnumsReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.scalar.Fixed32ReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.scalar.Fixed64ReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.scalar.FloatReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.scalar.Int32ReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.scalar.Int64ReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.scalar.SFixed32ReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.scalar.SFixed64ReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.scalar.SInt32ReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.scalar.SInt64ReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.scalar.StringReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.scalar.UInt32ReadSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.scalar.UInt64ReadSchemas;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.protostuff.SchemaEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.compiler.model.Message;
import io.protostuff.compiler.model.ScalarFieldType;
import io.protostuff.runtime.FieldSchema;

public class DeserializerSchemaManager extends SchemaManager {
  public DeserializerSchemaManager(ProtoMapper protoMapper) {
    super(protoMapper);
  }

  public <T> RootDeserializer<T> createRootDeserializer(Message message, Map<String, Type> types) {
    SchemaEx<T> messageSchema = getOrCreateMessageSchema(message, types);
    return new RootDeserializer<>(messageSchema);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public <T> RootDeserializer<T> createRootDeserializer(Message message, Type type) {
    if (ProtoUtils.isAnyMessage(message)) {
      SchemaEx<Object> messageSchema = new AnyEntrySchema(protoMapper, type);
      return new RootDeserializer(messageSchema);
    }
    JavaType javaType = TypeFactory.defaultInstance().constructType(type);
    SchemaEx<T> messageSchema = getOrCreateMessageSchema(message, javaType);
    return new RootDeserializer<>(messageSchema);
  }

  @Override
  protected <T> SchemaEx<T> newMessageSchema(Message message, Map<String, Type> types) {
    return new MessageReadSchema<>(protoMapper, message, types);
  }

  @Override
  protected <T> SchemaEx<T> newMessageSchema(Message message, JavaType javaType) {
    if (ProtoUtils.isWrapProperty(message) && javaType.getRawClass() != PropertyWrapper.class) {
      Field protoField = message.getField(1);
      if (javaType.isJavaLangObject()) {
        javaType =
            protoField.isRepeated() && !protoField.isMap() ? ProtoConst.LIST_TYPE
                : ProtoConst.MAP_TYPE;
      }

      if (javaType.isPrimitive()) {
        javaType = TypeFactory.defaultInstance()
            .constructParametricType(PropertyWrapper.class, TypesUtil.primitiveJavaTypeToWrapper(javaType));
      } else {
        javaType = TypeFactory.defaultInstance().constructParametricType(PropertyWrapper.class, javaType);
      }
    }

    if (javaType.isJavaLangObject()) {
      javaType = ProtoConst.MAP_TYPE;
    }

    return new MessageReadSchema<>(protoMapper, message, javaType);
  }

  protected <T> FieldSchema<T> createScalarField(Field protoField, PropertyDescriptor propertyDescriptor) {
    if (protoField.getType().isEnum()) {
      return EnumsReadSchemas.create(protoField, propertyDescriptor);
    }

    switch ((ScalarFieldType) protoField.getType()) {
      case INT32:
        return Int32ReadSchemas.create(protoField, propertyDescriptor);
      case UINT32:
        return UInt32ReadSchemas.create(protoField, propertyDescriptor);
      case SINT32:
        return SInt32ReadSchemas.create(protoField, propertyDescriptor);
      case FIXED32:
        return Fixed32ReadSchemas.create(protoField, propertyDescriptor);
      case SFIXED32:
        return SFixed32ReadSchemas.create(protoField, propertyDescriptor);
      case INT64:
        return Int64ReadSchemas.create(protoField, propertyDescriptor);
      case UINT64:
        return UInt64ReadSchemas.create(protoField, propertyDescriptor);
      case SINT64:
        return SInt64ReadSchemas.create(protoField, propertyDescriptor);
      case FIXED64:
        return Fixed64ReadSchemas.create(protoField, propertyDescriptor);
      case SFIXED64:
        return SFixed64ReadSchemas.create(protoField, propertyDescriptor);
      case FLOAT:
        return FloatReadSchemas.create(protoField, propertyDescriptor);
      case DOUBLE:
        return DoubleReadSchemas.create(protoField, propertyDescriptor);
      case BOOL:
        return BoolReadSchemas.create(protoField, propertyDescriptor);
      case STRING:
        return StringReadSchemas.create(protoField, propertyDescriptor);
      case BYTES:
        return BytesReadSchemas.create(protoField, propertyDescriptor);
      default:
        throw new IllegalStateException("unknown proto field type: " + protoField.getType());
    }
  }

  @Override
  protected <T> FieldSchema<T> createRepeatedSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
    boolean packed = ProtoUtils.isPacked(protoField);
    if (protoField.getType().isEnum()) {
      return packed ? EnumPackedReadSchemas.create(protoField, propertyDescriptor) :
          EnumNotPackedReadSchemas.create(protoField, propertyDescriptor);
    }

    if (protoField.getType().isScalar()) {
      switch ((ScalarFieldType) protoField.getType()) {
        case INT32:
          return packed ? Int32PackedReadSchemas.create(protoField, propertyDescriptor) :
              Int32NotPackedReadSchemas.create(protoField, propertyDescriptor);
        case UINT32:
          return packed ? UInt32PackedReadSchemas.create(protoField, propertyDescriptor) :
              UInt32NotPackedReadSchemas.create(protoField, propertyDescriptor);
        case SINT32:
          return packed ? SInt32PackedReadSchemas.create(protoField, propertyDescriptor) :
              SInt32NotPackedReadSchemas.create(protoField, propertyDescriptor);
        case FIXED32:
          return packed ? Fixed32PackedReadSchemas.create(protoField, propertyDescriptor) :
              Fixed32NotPackedReadSchemas.create(protoField, propertyDescriptor);
        case SFIXED32:
          return packed ? SFixed32PackedReadSchemas.create(protoField, propertyDescriptor) :
              SFixed32NotPackedReadSchemas.create(protoField, propertyDescriptor);
        case INT64:
          return packed ? Int64PackedReadSchemas.create(protoField, propertyDescriptor) :
              Int64NotPackedReadSchemas.create(protoField, propertyDescriptor);
        case UINT64:
          return packed ? UInt64PackedReadSchemas.create(protoField, propertyDescriptor) :
              UInt64NotPackedReadSchemas.create(protoField, propertyDescriptor);
        case SINT64:
          return packed ? SInt64PackedReadSchemas.create(protoField, propertyDescriptor) :
              SInt64NotPackedReadSchemas.create(protoField, propertyDescriptor);
        case FIXED64:
          return packed ? Fixed64PackedReadSchemas.create(protoField, propertyDescriptor) :
              Fixed64NotPackedReadSchemas.create(protoField, propertyDescriptor);
        case SFIXED64:
          return packed ? SFixed64PackedReadSchemas.create(protoField, propertyDescriptor) :
              SFixed64NotPackedReadSchemas.create(protoField, propertyDescriptor);
        case FLOAT:
          return packed ? FloatPackedReadSchemas.create(protoField, propertyDescriptor) :
              FloatNotPackedReadSchemas.create(protoField, propertyDescriptor);
        case DOUBLE:
          return packed ? DoublePackedReadSchemas.create(protoField, propertyDescriptor) :
              DoubleNotPackedReadSchemas.create(protoField, propertyDescriptor);
        case BOOL:
          return packed ? BoolPackedReadSchemas.create(protoField, propertyDescriptor) :
              BoolNotPackedReadSchemas.create(protoField, propertyDescriptor);
        case STRING:
          return StringRepeatedReadSchemas.create(protoField, propertyDescriptor);
        case BYTES:
          return BytesRepeatedReadSchemas.create(protoField, propertyDescriptor);
      }
    }

    if (ProtoUtils.isAnyField(protoField)) {
      AnyEntrySchema anyEntrySchema = new AnyEntrySchema(protoMapper, null);
      return AnyRepeatedReadSchemas.create(protoField, propertyDescriptor, anyEntrySchema);
    }

    if (protoField.getType().isMessage()) {
      JavaType contentType = propertyDescriptor.getJavaType().getContentType();
      if (contentType == null) {
        contentType = ProtoConst.OBJECT_TYPE;
      }
      SchemaEx<Object> contentSchema = getOrCreateMessageSchema((Message) protoField.getType(), contentType);
      if (isWrapProperty((Message) protoField.getType())) {
        return PropertyWrapperRepeatedReadSchemas.create(protoField, propertyDescriptor, contentSchema);
      }

      return MessageRepeatedReadSchemas.create(protoField, propertyDescriptor, contentSchema);
    }
    ProtoUtils.throwNotSupportMerge(protoField, propertyDescriptor.getJavaType());
    return null;
  }
}
