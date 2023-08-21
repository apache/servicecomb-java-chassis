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

package org.apache.servicecomb.common.rest.codec.param;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.codec.RestClientRequest;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.oas.models.parameters.Parameter;
import jakarta.servlet.http.HttpServletRequest;

public class PathProcessorCreator implements ParamValueProcessorCreator<Parameter> {
  public static final String PARAMTYPE = "path";

  public static class PathProcessor extends AbstractParamProcessor {
    public PathProcessor(String paramPath, JavaType targetType, Object defaultValue, boolean required) {
      super(paramPath, targetType, defaultValue, required);
    }

    @Override
    public Object getValue(HttpServletRequest request) {
      @SuppressWarnings("unchecked")
      Map<String, String> pathVarMap = (Map<String, String>) request.getAttribute(RestConst.PATH_PARAMETERS);
      if (pathVarMap == null) {
        return null;
      }

      String value = pathVarMap.get(paramPath);
      if (value == null) {
        return null;
      }
      return convertValue(StringUtils.uriDecode(value, StandardCharsets.UTF_8), targetType);
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
  public ParamValueProcessor create(OperationMeta operationMeta,
      String parameterName, Parameter parameter, Type genericParamType) {
    JavaType targetType =
        genericParamType == null ? null : TypeFactory.defaultInstance().constructType(genericParamType);
    return new PathProcessor(parameterName, targetType, parameter.getSchema().getDefault(), true);
  }
}
