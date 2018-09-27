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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.servicecomb.foundation.protobuf.internal.schema.FieldSchema;

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.compiler.model.EnumConstant;
import io.protostuff.compiler.model.Field;
import io.protostuff.compiler.model.Type;

public class EnumSerializerSchema extends FieldSchema {
  private Map<String, Integer> enumNameToValueMap = new HashMap<>();

  private Set<Integer> enumValueSet = new HashSet<>();

  public EnumSerializerSchema(Field field) {
    super(field);
    io.protostuff.compiler.model.Enum enumType = (io.protostuff.compiler.model.Enum) field.getType();
    for (EnumConstant enumConstant : enumType.getConstants()) {
      enumNameToValueMap.put(enumConstant.getName(), enumConstant.getValue());
      enumValueSet.add(enumConstant.getValue());
    }
  }

  @Override
  public void writeTo(Output output, Object value) throws IOException {
    if (value == null) {
      return;
    }

    if (value instanceof Enum) {
      // already be a Enum, need to check if it is a valid Enum?
      // wrong case:
      //   expect a Color enum, but be a Sharp enum?, who will do this?
      // for safe, check it......
      serializeFromString(output, ((Enum<?>) value).name());
      return;
    }

    if (value instanceof Number) {
      // need to check if it is a valid number
      // because maybe come from http request
      serializeFromNumber(output, ((Number) value).intValue());
      return;
    }

    if (value instanceof String[]) {
      if (((String[]) value).length == 0) {
        return;
      }

      serializeFromString(output, ((String[]) value)[0]);
      return;
    }

    if (value instanceof String) {
      serializeFromString(output, (String) value);
      return;
    }

    throwNotSupportValue(value);
  }


  protected void serializeFromNumber(Output output, int enumValue) throws IOException {
    if (!enumValueSet.contains(enumValue)) {
      throw new IllegalStateException(
          String.format("invalid enum value %d for proto %s, field=%s:%s",
              enumValue,
              protoField.getTypeName(),
              ((Type) protoField.getParent()).getCanonicalName(),
              protoField.getName()));
    }

    output.writeInt32(number, enumValue, repeated);
  }

  protected void serializeFromString(Output output, String enumName) throws IOException {
    Integer v = enumNameToValueMap.get(enumName);
    if (v == null) {
      throw new IllegalStateException(
          String.format("invalid enum name %s for proto %s, field=%s:%s",
              enumName,
              protoField.getTypeName(),
              ((Type) protoField.getParent()).getCanonicalName(),
              protoField.getName()));
    }
    output.writeInt32(number, v, repeated);
  }

  @Override
  public Object readFrom(Input input) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void mergeFrom(Input input, Object message) {
    throw new UnsupportedOperationException();
  }
}
