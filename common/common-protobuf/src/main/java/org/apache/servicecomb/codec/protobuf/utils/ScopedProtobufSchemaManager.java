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

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Map;

import org.apache.servicecomb.codec.protobuf.utils.schema.WrapSchemaFactory;
import org.apache.servicecomb.common.javassist.JavassistUtils;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.utils.JvmUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class ScopedProtobufSchemaManager {
  private ClassLoader classLoader;

  private Map<String, WrapSchema> schemaCache = new ConcurrentHashMapEx<>();

  public ScopedProtobufSchemaManager(ClassLoader classLoader) {
    this.classLoader = JvmUtils.correctClassLoader(classLoader);
  }

  // 为了支持method args的场景，全部实现ProtobufMessageWrapper接口，有的场景有点浪费，不过无关紧要
  private WrapSchema createWrapSchema(WrapClassConfig config) {
    Class<?> cls = JavassistUtils.createClass(classLoader, config);
    Schema<?> schema = RuntimeSchema.createFrom(cls);
    return WrapSchemaFactory.createSchema(schema, config.getType());
  }

  // 适用于将单个类型包装的场景
  // 比如return
  public WrapSchema getOrCreateSchema(Type type) {
    JavaType javaType = TypeFactory.defaultInstance().constructType(type);
    // List<String> -> java.util.List<java.lang.String>
    // List<List<String>> -> java.util.List<java.util.List<java.lang.String>>
    String key = javaType.toCanonical();
    return schemaCache.computeIfAbsent(key, k -> {
      if (!ProtobufSchemaUtils.isNeedWrap(javaType.getRawClass())) {
        // 可以直接使用
        Schema<?> schema = RuntimeSchema.createFrom(javaType.getRawClass());
        return WrapSchemaFactory.createSchema(schema, WrapType.NOT_WRAP);
      }

      // 需要包装
      WrapClassConfig config = new WrapClassConfig();
      config.setType(WrapType.NORMAL_WRAP);

      config.setClassName(
          "gen.wrap.protobuf." +
              org.apache.servicecomb.swagger.generator.core.utils.ClassUtils.correctClassName(key));
      if (!Void.TYPE.isAssignableFrom(javaType.getRawClass())) {
        config.addField("field0", javaType);
      }

      JavassistUtils.genSingleWrapperInterface(config);

      return createWrapSchema(config);
    });
  }

  public WrapSchema getOrCreateArgsSchema(OperationMeta operationMeta) {
    Method method = operationMeta.getMethod();
    String type = "gen." + method.getDeclaringClass().getName() + "." + method.getName() + ".Args";

    return schemaCache.computeIfAbsent(type, (t) -> {
      if (!ProtobufSchemaUtils.isArgsNeedWrap(method)) {
        // 可以直接使用
        Class<?> cls = method.getParameterTypes()[0];
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
