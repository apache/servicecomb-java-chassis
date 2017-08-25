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
package io.servicecomb.swagger.invocation.response;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.servicecomb.swagger.invocation.converter.Converter;
import io.servicecomb.swagger.invocation.converter.ConverterMgr;

public abstract class ResponseMapperFactory<MAPPER> {
  @Inject
  protected ConverterMgr converterMgr;

  // 特殊的应答，比如ResponseEntity/cse Response之类
  protected Map<Class<?>, MAPPER> mappers = new HashMap<>();

  public void setConverterMgr(ConverterMgr converterMgr) {
    this.converterMgr = converterMgr;
  }

  public MAPPER createResponseMapper(Type src, Type target) {
    Type type = choose(src, target);
    if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
      type = ((ParameterizedType) type).getRawType();
    }
    MAPPER mapper = mappers.get(type);
    if (mapper != null) {
      return mapper;
    }

    Converter converter = converterMgr.findConverter(src, target);
    return doCreateResponseMapper(converter);
  }

  protected abstract Type choose(Type src, Type target);

  protected abstract MAPPER doCreateResponseMapper(Converter converter);
}
