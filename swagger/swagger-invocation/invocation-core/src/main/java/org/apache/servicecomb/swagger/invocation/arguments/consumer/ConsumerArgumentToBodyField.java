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

package org.apache.servicecomb.swagger.invocation.arguments.consumer;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentMapper;
import org.apache.servicecomb.swagger.invocation.arguments.FieldInfo;

/**
 * 透明RPC的典型场景
 * 因为没有标注指明RESTful映射方式
 * 所以，所有参数被包装为一个class，每个参数是一个field
 */
public class ConsumerArgumentToBodyField implements ArgumentMapper {
  private Class<?> swaggerParamType;

  // key为consumerArgs的下标
  private Map<Integer, FieldInfo> fieldMap;

  public ConsumerArgumentToBodyField(Class<?> swaggerParamType, Map<Integer, FieldInfo> fieldMap) {
    this.swaggerParamType = correctType(swaggerParamType);
    this.fieldMap = fieldMap;
  }

  protected Class<?> correctType(Class<?> swaggerParamType) {
    if (!Modifier.isAbstract(swaggerParamType.getModifiers())) {
      return swaggerParamType;
    }

    if (List.class.isAssignableFrom(swaggerParamType)) {
      return ArrayList.class;
    }

    if (Set.class.isAssignableFrom(swaggerParamType)) {
      return HashSet.class;
    }

    throw new Error("not support " + swaggerParamType.getName());
  }

  public Class<?> getSwaggerParamType() {
    return swaggerParamType;
  }

  @Override
  public void mapArgument(SwaggerInvocation invocation, Object[] consumerArguments) {
    try {
      Object body = swaggerParamType.newInstance();
      for (Entry<Integer, FieldInfo> entry : fieldMap.entrySet()) {
        FieldInfo info = entry.getValue();

        Object consumerParam = consumerArguments[entry.getKey()];
        Object swaggerParam = info.getConverter().convert(consumerParam);
        info.getField().set(body, swaggerParam);
      }
      invocation.setSwaggerArgument(0, body);
    } catch (Throwable e) {
      throw new Error(e);
    }
  }
}
