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
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicecomb.codec.protobuf.utils.schema.WrapSchemaFactory;
import org.apache.servicecomb.common.javassist.JavassistUtils;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.springframework.util.ClassUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Pipe;
import io.protostuff.Schema;
import io.protostuff.WireFormat.FieldType;
import io.protostuff.runtime.DefaultIdStrategy;
import io.protostuff.runtime.Delegate;
import io.protostuff.runtime.ProtobufCompatibleUtils;
import io.protostuff.runtime.RuntimeEnv;
import io.protostuff.runtime.RuntimeSchema;

public final class ProtobufSchemaUtils {
  private static volatile Map<String, WrapSchema> schemaCache = new ConcurrentHashMap<>();

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
      public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated) throws IOException {
        throw new IllegalStateException("not support.");
      }

      @Override
      public Class<?> typeClass() {
        return Object.class;
      }
    });
  }

  private interface SchemaCreator {
    WrapSchema create() throws Exception;
  }

  private ProtobufSchemaUtils() {
  }

  private static WrapSchema getOrCreateSchema(String className, SchemaCreator creator) {
    WrapSchema schema = schemaCache.get(className);
    if (schema != null) {
      return schema;
    }

    synchronized (ProtobufSchemaUtils.class) {
      schema = schemaCache.get(className);
      if (schema != null) {
        return schema;
      }

      try {
        schema = creator.create();
      } catch (Exception e) {
        throw new Error(e);
      }
      schemaCache.put(className, schema);
      return schema;
    }
  }

  private static boolean isArgsNeedWrap(Method method) {
    if (method.getParameterCount() != 1) {
      return true;
    }

    // 单参数时，需要根据实际情况判断
    return isNeedWrap(method.getParameterTypes()[0]);
  }

  private static boolean isNeedWrap(Class<?> cls) {
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

  // 为了支持method args的场景，全部实现ProtobufMessageWrapper接口，有的场景有点浪费，不过无关紧要
  private static WrapSchema createWrapSchema(WrapClassConfig config) throws Exception {
    Class<?> cls = JavassistUtils.createClass(config);
    Schema<?> schema = RuntimeSchema.createFrom(cls);
    return WrapSchemaFactory.createSchema(schema, config.getType());
  }

  // 适用于将单个类型包装的场景
  // 比如return
  public static WrapSchema getOrCreateSchema(Type type) {
    JavaType javaType = TypeFactory.defaultInstance().constructType(type);
    // List<String> -> java.util.List<java.lang.String>
    // List<List<String>> -> java.util.List<java.util.List<java.lang.String>>
    String key = javaType.toCanonical();
    return getOrCreateSchema(key, () -> {
      if (!isNeedWrap(javaType.getRawClass())) {
        // 可以直接使用
        Schema<?> schema = RuntimeSchema.createFrom(javaType.getRawClass());
        return WrapSchemaFactory.createSchema(schema, WrapType.NOT_WRAP);
      }

      // 需要包装
      WrapClassConfig config = new WrapClassConfig();
      config.setType(WrapType.NORMAL_WRAP);

      config.setClassName("gen.wrap.protobuf." + key.replaceAll("[<>]", "_").replace("[", "array_"));
      if (!Void.TYPE.isAssignableFrom(javaType.getRawClass())) {
        config.addField("field0", javaType);
      }

      JavassistUtils.genSingleWrapperInterface(config);

      return createWrapSchema(config);
    });
  }

  public static WrapSchema getOrCreateArgsSchema(OperationMeta operationMeta) {
    Method method = operationMeta.getMethod();
    String type = "gen." + method.getDeclaringClass().getName() + "." + method.getName() + ".Args";

    return getOrCreateSchema(type, () -> {
      if (!isArgsNeedWrap(method)) {
        // 可以直接使用
        Class<?> cls = (Class<?>) method.getParameterTypes()[0];
        Schema<?> schema = RuntimeSchema.createFrom(cls);
        return WrapSchemaFactory.createSchema(schema, WrapType.ARGS_NOT_WRAP);
      }

      // 需要包装
      WrapClassConfig config = new WrapClassConfig();
      config.setType(WrapType.ARGS_WRAP);
      config.setClassName(type);

      Parameter[] params = method.getParameters();
      for (int idx = 0; idx < params.length; idx++) {
        Parameter param = params[idx];
        String paramName = org.apache.servicecomb.swagger.generator.core.utils.ClassUtils
            .correctMethodParameterName(operationMeta.getParamName(idx));
        config.addField(paramName, param.getParameterizedType());
      }

      JavassistUtils.genMultiWrapperInterface(config);

      return createWrapSchema(config);
    });
  }
}
