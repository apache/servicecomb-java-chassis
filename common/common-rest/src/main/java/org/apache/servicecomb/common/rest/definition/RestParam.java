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

package org.apache.servicecomb.common.rest.definition;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import org.apache.servicecomb.common.rest.codec.param.BodyProcessorCreator;
import org.apache.servicecomb.common.rest.codec.param.FormProcessorCreator;
import org.apache.servicecomb.common.rest.codec.param.ParamValueProcessor;
import org.apache.servicecomb.common.rest.codec.param.ParamValueProcessorCreator;
import org.apache.servicecomb.common.rest.codec.param.ParamValueProcessorCreatorManager;
import org.apache.servicecomb.core.definition.OperationMeta;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RestParam {
  private static final JavaType STRING_ARRAY_TYPE = TypeFactory.defaultInstance().constructArrayType(String.class);

  protected ParamValueProcessor paramProcessor;

  protected String paramName;

  public RestParam(OperationMeta operationMeta, Parameter parameter, Type genericParamType) {
    this.paramName = parameter.getName();

    init(operationMeta, parameter, genericParamType);
  }

  public RestParam(OperationMeta operationMeta,
      String paramName, RequestBody parameter, boolean isForm, Type genericParamType) {
    this.paramName = paramName;

    init(operationMeta, parameter, isForm, genericParamType);
  }


  public ParamValueProcessor getParamProcessor() {
    return this.paramProcessor;
  }

  public void setParamProcessor(ParamValueProcessor paramProcessor) {
    this.paramProcessor = paramProcessor;
  }

  public String getParamName() {
    return paramName;
  }

  protected void init(OperationMeta operationMeta, Parameter parameter, Type genericParamType) {
    String paramType = parameter.getIn();
    ParamValueProcessorCreator creator =
        ParamValueProcessorCreatorManager.INSTANCE.ensureFindValue(paramType);

    this.setParamProcessor(creator.create(operationMeta, parameter.getName(), parameter, genericParamType));
  }

  protected void init(OperationMeta operationMeta, RequestBody parameter, boolean isForm, Type genericParamType) {
    ParamValueProcessorCreator creator;
    if (isForm) {
      creator =
          ParamValueProcessorCreatorManager.INSTANCE.ensureFindValue(FormProcessorCreator.PARAMTYPE);
    } else {
      creator =
          ParamValueProcessorCreatorManager.INSTANCE.ensureFindValue(BodyProcessorCreator.PARAM_TYPE);
    }

    this.setParamProcessor(creator.create(operationMeta, this.paramName,
        parameter, genericParamType));
  }

  public <T> T getValue(Map<String, Object> args) {
    return (T) args.get(paramName);
  }

  public String[] getValueAsStrings(Map<String, Object> args) {
    Object value = args.get(paramName);
    if (value == null) {
      return null;
    }

    if (value.getClass().isArray() || value instanceof Collection) {
      return (String[]) paramProcessor.convertValue(value, STRING_ARRAY_TYPE);
    }

    return new String[] {String.valueOf(value)};
  }
}
