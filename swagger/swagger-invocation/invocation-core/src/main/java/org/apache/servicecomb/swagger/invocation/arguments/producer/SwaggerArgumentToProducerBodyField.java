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

package org.apache.servicecomb.swagger.invocation.arguments.producer;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentMapper;
import org.apache.servicecomb.swagger.invocation.arguments.FieldInfo;

/**
 * 透明RPC的典型场景
 * 因为没有标注指明RESTful映射方式
 * 所以，所有参数被包装为一个class，每个参数是一个field
 * producer在处理时，需要将这些field取出来当作参数使用
 */
public class SwaggerArgumentToProducerBodyField implements ArgumentMapper {
  // key为producerArgs的下标
  private Map<Integer, FieldInfo> fieldMap;

  public SwaggerArgumentToProducerBodyField(Map<Integer, FieldInfo> fieldMap) {
    this.fieldMap = fieldMap;
  }

  @Override
  public void mapArgument(SwaggerInvocation invocation, Object[] producerArguments) {
    Object body = invocation.getSwaggerArgument(0);

    try {
      for (Entry<Integer, FieldInfo> entry : fieldMap.entrySet()) {
        FieldInfo info = entry.getValue();

        Object fieldValue = info.getField().get(body);
        Object producerParam = info.getConverter().convert(fieldValue);
        producerArguments[entry.getKey()] = producerParam;
      }
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Throwable e) {
      throw new Error(e);
    }
  }
}
