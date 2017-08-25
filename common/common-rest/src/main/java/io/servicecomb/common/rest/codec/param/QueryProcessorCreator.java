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

package io.servicecomb.common.rest.codec.param;

import java.lang.reflect.Type;
import java.util.Collection;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.servicecomb.common.rest.codec.RestClientRequest;
import io.servicecomb.common.rest.codec.RestObjectMapper;
import io.servicecomb.common.rest.codec.RestServerRequest;
import io.swagger.models.parameters.Parameter;

public class QueryProcessorCreator implements ParamValueProcessorCreator {
  public static final String PARAMTYPE = "query";

  public static class QueryProcessor extends AbstractParamProcessor {
    // Query参数可能为数组类型
    protected boolean isArrayOrCollection;

    public QueryProcessor(String paramPath, JavaType targetType, boolean isArrayOrCollection) {
      super(paramPath, targetType);
      this.isArrayOrCollection = isArrayOrCollection;
    }

    @Override
    public Object getValue(RestServerRequest request) throws Exception {
      String[] param = request.getQueryParam(paramPath);
      if (param == null) {
        return null;
      }

      // 处理数组类型query参数
      if (isArrayOrCollection) {
        return convertValue(param, targetType);
      }

      if (param.length == 0) {
        return null;
      }

      return convertValue(param[0], targetType);
    }

    @Override
    public void setValue(RestClientRequest clientRequest, Object arg) throws Exception {
      // query不需要set
    }

    protected <T> T convertValue(Object arg, Class<T> cls) throws Exception {
      return RestObjectMapper.INSTANCE.convertValue(arg, cls);
    }

    @Override
    public String getProcessorType() {
      return PARAMTYPE;
    }
  }

  public QueryProcessorCreator() {
    ParamValueProcessorCreatorManager.INSTANCE.register(PARAMTYPE, this);
  }

  @Override
  public ParamValueProcessor create(Parameter parameter, Type genericParamType) {
    JavaType targetType = TypeFactory.defaultInstance().constructType(genericParamType);
    Class<?> rawCls = targetType.getRawClass();
    boolean isArrayOrCollection = rawCls.isArray() || Collection.class.isAssignableFrom(rawCls);
    return new QueryProcessor(parameter.getName(), targetType, isArrayOrCollection);
  }
}
