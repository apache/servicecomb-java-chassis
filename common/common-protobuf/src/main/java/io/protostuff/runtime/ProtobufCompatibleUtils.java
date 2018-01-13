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

import java.io.IOException;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.ReflectUtils;

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Pipe;
import io.protostuff.WireFormat.FieldType;

/**
 * protostuff实现protobuf逻辑时，关于map的编码与protobuf不兼容
 * 这里修改map的编码逻辑
 */
public final class ProtobufCompatibleUtils {
  private static boolean inited = false;

  private ProtobufCompatibleUtils() {
  }

  public static void init() {
    if (inited) {
      return;
    }

    replaceRuntimeFieldFactoryMap();

    inited = true;
  }

  protected static void replaceRuntimeFieldFactoryMap() {
    RuntimeFieldFactory<Map<?, ?>> org = RuntimeMapFieldFactory.MAP;
    RuntimeFieldFactory<Map<?, ?>> map = new RuntimeFieldFactory<Map<?, ?>>(
        RuntimeFieldFactory.ID_MAP) {

      @Override
      public FieldType getFieldType() {
        return org.getFieldType();
      }

      @Override
      public Map<?, ?> readFrom(Input input) throws IOException {
        return org.readFrom(input);
      }

      @Override
      public void writeTo(Output output, int number, Map<?, ?> value,
          boolean repeated) throws IOException {
        org.writeTo(output, number, value, repeated);
      }

      @Override
      public void transfer(Pipe pipe, Input input, Output output, int number,
          boolean repeated) throws IOException {
        org.transfer(pipe, input, output, number, repeated);
      }

      @Override
      public Class<?> typeClass() {
        return org.typeClass();
      }

      @Override
      public <T> Field<T> create(int number, String name, java.lang.reflect.Field field, IdStrategy strategy) {
        @SuppressWarnings("unchecked")
        RuntimeMapField<T, Object, Object> runtimeMapField =
            (RuntimeMapField<T, Object, Object>) org.create(number, name, field, strategy);

        return new RuntimeMapFieldProtobuf<>(runtimeMapField, field);
      }
    };

    ReflectUtils.setField(RuntimeMapFieldFactory.class, null, "MAP", map);
  }
}
