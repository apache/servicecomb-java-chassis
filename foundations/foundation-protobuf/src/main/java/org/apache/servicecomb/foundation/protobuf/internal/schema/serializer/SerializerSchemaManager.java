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

import static org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils.isWrapProperty;

import java.lang.reflect.Type;
import java.util.Map;

import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.RootSerializer;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoConst;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;
import org.apache.servicecomb.foundation.protobuf.internal.schema.SchemaManager;
import org.apache.servicecomb.foundation.protobuf.internal.schema.any.AnyEntrySchema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.any.AnySchema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.AnyRepeatedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.BytesRepeatedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.MessagesRepeatedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.PropertyWrapperRepeatedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.StringsRepeatedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.bools.BoolNotPackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.bools.BoolPackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.doubles.DoubleNotPackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.doubles.DoublePackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.enums.EnumNotPackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.enums.EnumPackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.floats.FloatNotPackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.floats.FloatPackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.ints.Fixed32NotPackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.ints.Fixed32PackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.ints.Int32NotPackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.ints.Int32PackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.ints.SFixed32NotPackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.ints.SFixed32PackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.ints.SInt32NotPackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.ints.SInt32PackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.ints.UInt32NotPackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.ints.UInt32PackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.longs.Fixed64NotPackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.longs.Fixed64PackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.longs.Int64NotPackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.longs.Int64PackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.longs.SFixed64NotPackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.longs.SFixed64PackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.longs.SInt64NotPackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.longs.SInt64PackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.longs.UInt64NotPackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.longs.UInt64PackedWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.scalar.BoolWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.scalar.BytesWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.scalar.DoubleWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.scalar.EnumWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.scalar.Fixed32WriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.scalar.Fixed64WriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.scalar.FloatWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.scalar.Int32WriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.scalar.Int64WriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.scalar.SFixed32WriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.scalar.SFixed64WriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.scalar.SInt32WriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.scalar.SInt64WriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.scalar.StringWriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.scalar.UInt32WriteSchemas;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.scalar.UInt64WriteSchemas;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.protostuff.SchemaEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.compiler.model.Message;
import io.protostuff.compiler.model.ScalarFieldType;
import io.protostuff.runtime.FieldSchema;

public class SerializerSchemaManager extends SchemaManager {
  public SerializerSchemaManager(ProtoMapper protoMapper) {
    super(protoMapper);
  }

  public RootSerializer createRootSerializer(Message message, Type type) {
    if (ProtoUtils.isAnyMessage(message)) {
      SchemaEx<Object> messageSchema = new AnyEntrySchema(protoMapper, type);
      return new RootSerializer(messageSchema);
    }
    JavaType javaType = TypeFactory.defaultInstance().constructType(type);
    SchemaEx<Object> messageSchema = getOrCreateMessageSchema(message, javaType);
    return new RootSerializer(messageSchema);
  }

  public RootSerializer createRootSerializer(Message message, Map<String, Type> types) {
    throw new IllegalStateException("not implemented");
  }

  @Override
  protected <T> SchemaEx<T> newMessageSchema(Message message, JavaType javaType) {
    return new MessageWriteSchema<>(protoMapper, message, javaType);
  }

  @Override
  protected <T> SchemaEx<T> newMessageSchema(Message message, Map<String, Type> types) {
    throw new IllegalStateException("not implemented");
  }

  protected <T> FieldSchema<T> createScalarField(Field protoField, PropertyDescriptor propertyDescriptor) {
    if (protoField.getType().isEnum()) {
      return EnumWriteSchemas.create(protoField, propertyDescriptor);
    }

    switch ((ScalarFieldType) protoField.getType()) {
      case INT32:
        return Int32WriteSchemas.create(protoField, propertyDescriptor);
      case UINT32:
        return UInt32WriteSchemas.create(protoField, propertyDescriptor);
      case SINT32:
        return SInt32WriteSchemas.create(protoField, propertyDescriptor);
      case FIXED32:
        return Fixed32WriteSchemas.create(protoField, propertyDescriptor);
      case SFIXED32:
        return SFixed32WriteSchemas.create(protoField, propertyDescriptor);
      case INT64:
        return Int64WriteSchemas.create(protoField, propertyDescriptor);
      case UINT64:
        return UInt64WriteSchemas.create(protoField, propertyDescriptor);
      case SINT64:
        return SInt64WriteSchemas.create(protoField, propertyDescriptor);
      case FIXED64:
        return Fixed64WriteSchemas.create(protoField, propertyDescriptor);
      case SFIXED64:
        return SFixed64WriteSchemas.create(protoField, propertyDescriptor);
      case FLOAT:
        return FloatWriteSchemas.create(protoField, propertyDescriptor);
      case DOUBLE:
        return DoubleWriteSchemas.create(protoField, propertyDescriptor);
      case BOOL:
        return BoolWriteSchemas.create(protoField, propertyDescriptor);
      case STRING:
        return StringWriteSchemas.create(protoField, propertyDescriptor);
      case BYTES:
        return BytesWriteSchemas.create(protoField, propertyDescriptor);
      default:
        throw new IllegalStateException("unknown proto field type: " + protoField.getType());
    }
  }

  @Override
  protected <T> FieldSchema<T> createRepeatedSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
    boolean packed = ProtoUtils.isPacked(protoField);
    if (protoField.getType().isEnum()) {
      return packed ? EnumPackedWriteSchemas.create(protoField, propertyDescriptor) :
          EnumNotPackedWriteSchemas.create(protoField, propertyDescriptor);
    }

    if (protoField.getType().isScalar()) {
      switch ((ScalarFieldType) protoField.getType()) {
        case INT32:
          return packed ? Int32PackedWriteSchemas.create(protoField, propertyDescriptor) :
              Int32NotPackedWriteSchemas.create(protoField, propertyDescriptor);
        case UINT32:
          return packed ? UInt32PackedWriteSchemas.create(protoField, propertyDescriptor) :
              UInt32NotPackedWriteSchemas.create(protoField, propertyDescriptor);
        case SINT32:
          return packed ? SInt32PackedWriteSchemas.create(protoField, propertyDescriptor) :
              SInt32NotPackedWriteSchemas.create(protoField, propertyDescriptor);
        case FIXED32:
          return packed ? Fixed32PackedWriteSchemas.create(protoField, propertyDescriptor) :
              Fixed32NotPackedWriteSchemas.create(protoField, propertyDescriptor);
        case SFIXED32:
          return packed ? SFixed32PackedWriteSchemas.create(protoField, propertyDescriptor) :
              SFixed32NotPackedWriteSchemas.create(protoField, propertyDescriptor);
        case INT64:
          return packed ? Int64PackedWriteSchemas.create(protoField, propertyDescriptor) :
              Int64NotPackedWriteSchemas.create(protoField, propertyDescriptor);
        case UINT64:
          return packed ? UInt64PackedWriteSchemas.create(protoField, propertyDescriptor) :
              UInt64NotPackedWriteSchemas.create(protoField, propertyDescriptor);
        case SINT64:
          return packed ? SInt64PackedWriteSchemas.create(protoField, propertyDescriptor) :
              SInt64NotPackedWriteSchemas.create(protoField, propertyDescriptor);
        case FIXED64:
          return packed ? Fixed64PackedWriteSchemas.create(protoField, propertyDescriptor) :
              Fixed64NotPackedWriteSchemas.create(protoField, propertyDescriptor);
        case SFIXED64:
          return packed ? SFixed64PackedWriteSchemas.create(protoField, propertyDescriptor) :
              SFixed64NotPackedWriteSchemas.create(protoField, propertyDescriptor);
        case FLOAT:
          return packed ? FloatPackedWriteSchemas.create(protoField, propertyDescriptor) :
              FloatNotPackedWriteSchemas.create(protoField, propertyDescriptor);
        case DOUBLE:
          return packed ? DoublePackedWriteSchemas.create(protoField, propertyDescriptor) :
              DoubleNotPackedWriteSchemas.create(protoField, propertyDescriptor);
        case BOOL:
          return packed ? BoolPackedWriteSchemas.create(protoField, propertyDescriptor) :
              BoolNotPackedWriteSchemas.create(protoField, propertyDescriptor);
        case STRING:
          return StringsRepeatedWriteSchemas.create(protoField, propertyDescriptor);
        case BYTES:
          return BytesRepeatedWriteSchemas.create(protoField, propertyDescriptor);
      }
    }

    if (ProtoUtils.isAnyField(protoField)) {
      FieldSchema<T> anySchema = new AnySchema<>(protoMapper, protoField, propertyDescriptor);
      return AnyRepeatedWriteSchemas.create(protoField, propertyDescriptor, anySchema);
    }

    if (protoField.getType().isMessage()) {
      JavaType contentType = propertyDescriptor.getJavaType().getContentType();
      if (contentType == null) {
        contentType = ProtoConst.OBJECT_TYPE;
      }
      SchemaEx<Object> contentSchema = getOrCreateMessageSchema((Message) protoField.getType(), contentType);
      if (isWrapProperty((Message) protoField.getType())) {
        return PropertyWrapperRepeatedWriteSchemas.create(protoField, propertyDescriptor, contentSchema);
      }

      return MessagesRepeatedWriteSchemas.create(protoField, propertyDescriptor, contentSchema);
    }

    ProtoUtils.throwNotSupportWrite(protoField, propertyDescriptor.getJavaType().getRawClass());
    return null;
  }
}
