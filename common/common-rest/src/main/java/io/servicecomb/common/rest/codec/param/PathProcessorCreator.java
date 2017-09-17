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
import java.net.URLDecoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.servicecomb.common.rest.RestConst;
import io.servicecomb.common.rest.codec.RestClientRequest;
import io.swagger.models.parameters.Parameter;

public class PathProcessorCreator implements ParamValueProcessorCreator {
  public static final String PARAMTYPE = "path";

  public static class PathProcessor extends AbstractParamProcessor {
    public PathProcessor(String paramPath, JavaType targetType) {
      super(paramPath, targetType);
    }

    @Override
    public Object getValue(HttpServletRequest request) throws Exception {
      @SuppressWarnings("unchecked")
      Map<String, String> pathVarMap = (Map<String, String>) request.getAttribute(RestConst.PATH_PARAMETERS);
      if (pathVarMap == null) {
        return null;
      }

      String value = pathVarMap.get(paramPath);
      if (value == null) {
        return null;
      }
      return convertValue(URLDecoder.decode(value, "UTF-8"), targetType);
    }

    @Override
    public void setValue(RestClientRequest clientRequest, Object arg) throws Exception {
      // path不需要set
    }

    @Override
    public String getProcessorType() {
      return PARAMTYPE;
    }
  }

  public PathProcessorCreator() {
    ParamValueProcessorCreatorManager.INSTANCE.register(PARAMTYPE, this);
  }

  @Override
  public ParamValueProcessor create(Parameter parameter, Type genericParamType) {
    JavaType targetType = TypeFactory.defaultInstance().constructType(genericParamType);
    return new PathProcessor(parameter.getName(), targetType);
  }
}
