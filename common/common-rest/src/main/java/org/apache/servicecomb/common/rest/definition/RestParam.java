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

import org.apache.servicecomb.common.rest.codec.param.ParamValueProcessor;
import org.apache.servicecomb.common.rest.codec.param.ParamValueProcessorCreator;
import org.apache.servicecomb.common.rest.codec.param.ParamValueProcessorCreatorManager;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.models.parameters.Parameter;

public class RestParam {
  private static final JavaType STRING_ARRAY_TYPE = TypeFactory.defaultInstance().constructArrayType(String.class);

  protected ParamValueProcessor paramProcessor;

  protected String paramName;

  // 在args数组中的下标
  protected int paramIndex;

  public RestParam(int paramIndex, Parameter parameter, Type genericParamType) {
    this.paramIndex = paramIndex;
    this.paramName = parameter.getName();

    init(parameter, genericParamType);
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

  protected void init(Parameter parameter, Type genericParamType) {
    String paramType = parameter.getIn();
    ParamValueProcessorCreator creater =
        ParamValueProcessorCreatorManager.INSTANCE.ensureFindValue(paramType);

    this.setParamProcessor(creater.create(parameter, genericParamType));
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue(Object[] args) {
    return (T) args[paramIndex];
  }

  public String[] getValueAsStrings(Object[] args) {
    Object value = args[paramIndex];
    if (value == null) {
      return null;
    }

    if (value.getClass().isArray() || Collection.class.isInstance(value)) {
      return (String[]) paramProcessor.convertValue(value, STRING_ARRAY_TYPE);
    }

    return new String[] {String.valueOf(value)};
  }
}
