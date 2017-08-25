/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicecomb.swagger.invocation.converter.impl;

import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.servicecomb.foundation.common.utils.JsonUtils;
import io.servicecomb.swagger.invocation.converter.Converter;

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
