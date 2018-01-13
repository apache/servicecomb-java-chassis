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
package org.apache.servicecomb.swagger.invocation.converter.impl;

import java.lang.reflect.Type;

import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.swagger.invocation.converter.Converter;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ConverterCommon implements Converter {
  private JavaType targetJavaType;

  public ConverterCommon(Type targetType) {
    targetJavaType = TypeFactory.defaultInstance().constructType(targetType);
  }

  @Override
  public Object convert(Object value) {
    return JsonUtils.OBJ_MAPPER.convertValue(value, targetJavaType);
  }
}
