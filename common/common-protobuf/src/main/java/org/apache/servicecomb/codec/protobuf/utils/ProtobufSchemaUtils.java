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

package org.apache.servicecomb.codec.protobuf.utils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.springframework.util.ClassUtils;

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Pipe;
import io.protostuff.WireFormat.FieldType;
import io.protostuff.runtime.DefaultIdStrategy;
import io.protostuff.runtime.Delegate;
import io.protostuff.runtime.ProtobufCompatibleUtils;
import io.protostuff.runtime.RuntimeEnv;

public final class ProtobufSchemaUtils {
  static {
    initProtobufObjectCodec();
    ProtobufCompatibleUtils.init();
  }

  protected static void initProtobufObjectCodec() {
    ((DefaultIdStrategy) RuntimeEnv.ID_STRATEGY).registerDelegate(new Delegate<Object>() {
      @Override
      public FieldType getFieldType() {
        return FieldType.BYTES;
      }

      @Override
      public Object readFrom(Input input) throws IOException {
        return JsonUtils.readValue(input.readByteArray(), Object.class);
      }

      @Override
      public void writeTo(Output output, int number, Object value, boolean repeated) throws IOException {
        output.writeByteArray(number, JsonUtils.writeValueAsBytes(value), false);
      }

      @Override
      public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated) {
        throw new IllegalStateException("not support.");
      }

      @Override
      public Class<?> typeClass() {
        return Object.class;
      }
    });
  }

  private ProtobufSchemaUtils() {
  }

  public static boolean isArgsNeedWrap(Method method) {
    if (method.getParameterCount() != 1) {
      return true;
    }

    // 单参数时，需要根据实际情况判断
    return isNeedWrap(method.getParameterTypes()[0]);
  }

  public static boolean isNeedWrap(Class<?> cls) {
    // protobuf不支持原子类型、enum、string、数组、collection等等作为msg
    // 只有pojo类型才可以
    // java.lang.Object也不可以，因为这可以是任意类型，结果不确定
    return ClassUtils.isPrimitiveOrWrapper(cls) || cls.isArray() || cls.isEnum()
        || String.class.isAssignableFrom(cls)
        || Collection.class.isAssignableFrom(cls)
        || Map.class.isAssignableFrom(cls)
        || Date.class.isAssignableFrom(cls)
        || Object.class.equals(cls);
  }
}
