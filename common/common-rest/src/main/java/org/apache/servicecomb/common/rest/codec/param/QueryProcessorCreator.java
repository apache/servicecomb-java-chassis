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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.common.rest.codec.RestClientRequest;
import org.apache.servicecomb.swagger.converter.property.SwaggerParamCollectionFormat;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.netflix.config.DynamicPropertyFactory;

import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;

public class QueryProcessorCreator implements ParamValueProcessorCreator {
  public static final String PARAMTYPE = "query";

  public static class QueryProcessor extends AbstractParamProcessor {
    // This configuration is used for temporary use only. Do not use it if you are sure how it works. And may be deleted in future.
    private boolean emptyAsNull = DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.rest.parameter.query.emptyAsNull", false).get();

    // This configuration is used for temporary use only. Do not use it if you are sure how it works. And may be deleted in future.
    private boolean ignoreDefaultValue = DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.rest.parameter.query.ignoreDefaultValue", false).get();

    private SwaggerParamCollectionFormat collectionFormat;

    public QueryProcessor(String paramPath, JavaType targetType, Object defaultValue, String collectionFormat) {
      super(paramPath, targetType, defaultValue);
      if (StringUtils.isNoneEmpty(collectionFormat)) {
        this.collectionFormat = SwaggerParamCollectionFormat.valueOf(collectionFormat.toUpperCase());
      }
    }

    @Override
    public Object getValue(HttpServletRequest request) throws Exception {
      Object value = null;
      if (targetType.isContainerType()
          && SwaggerParamCollectionFormat.MULTI.equals(collectionFormat)) {
        value = request.getParameterValues(paramPath);
      } else {
        value = request.getParameter(paramPath);
        // make some old systems happy
        if (emptyAsNull) {
          if (StringUtils.isEmpty((String) value)) {
            value = null;
          }
        }
        if (value == null) {
          Object defaultValue = getDefaultValue();
          if (!ignoreDefaultValue && defaultValue != null) {
            value = defaultValue;
          }
        }
        if (null != collectionFormat) {
          value = collectionFormat.splitParam((String) value);
        }
      }

      return convertValue(value, targetType);
    }

    @Override
    public void setValue(RestClientRequest clientRequest, Object arg) throws Exception {
      // query不需要set
    }

    @Override
    public String getProcessorType() {
      return PARAMTYPE;
    }

    public SwaggerParamCollectionFormat getCollectionFormat() {
      return collectionFormat;
    }
  }

  public QueryProcessorCreator() {
    ParamValueProcessorCreatorManager.INSTANCE.register(PARAMTYPE, this);
  }

  @Override
  public ParamValueProcessor create(Parameter parameter, Type genericParamType) {
    QueryParameter queryParameter = (QueryParameter) parameter;
    JavaType targetType = TypeFactory.defaultInstance().constructType(genericParamType);
    return new QueryProcessor(parameter.getName(), targetType, queryParameter.getDefaultValue(),
        queryParameter.getCollectionFormat());
  }
}
