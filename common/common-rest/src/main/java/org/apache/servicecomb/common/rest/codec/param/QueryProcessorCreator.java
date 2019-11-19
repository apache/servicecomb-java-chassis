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
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.common.rest.codec.RestClientRequest;
import org.apache.servicecomb.swagger.converter.property.SwaggerParamCollectionFormat;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.netflix.config.DynamicPropertyFactory;

import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;

public class QueryProcessorCreator implements ParamValueProcessorCreator {
  public static final String PARAMTYPE = "query";

  public static class QueryProcessor extends AbstractParamProcessor {
    // This configuration is used for temporary use only. Do not use it if you are sure how it works. And may be deleted in future.
    private boolean emptyAsNull = DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.rest.parameter.query.emptyAsNull", false).get();

    // This configuration is used for temporary use only. Do not use it if you are sure how it works. And may be deleted in future.
    private boolean ignoreDefaultValue = DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.rest.parameter.query.ignoreDefaultValue", false).get();

    // This configuration is used for temporary use only. Do not use it if you are sure how it works. And may be deleted in future.
    private boolean ignoreRequiredCheck = DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.rest.parameter.query.ignoreRequiredCheck", false).get();

    private boolean repeatedType;

    private SwaggerParamCollectionFormat collectionFormat;

    public QueryProcessor(QueryParameter queryParameter, JavaType targetType) {
      super(queryParameter.getName(), targetType, queryParameter.getDefaultValue(), queryParameter.getRequired());

      this.repeatedType = ArrayProperty.isType(queryParameter.getType());
      if (StringUtils.isNotEmpty(queryParameter.getCollectionFormat())) {
        this.collectionFormat = SwaggerParamCollectionFormat
            .valueOf(queryParameter.getCollectionFormat().toUpperCase());
      }
    }

    @Override
    public Object getValue(HttpServletRequest request) {
      if (repeatedType
          && SwaggerParamCollectionFormat.MULTI.equals(collectionFormat)) {
        //Even if the paramPath does not exist, value won't be null at now
        String[] value = request.getParameterValues(paramPath);
        return convertValue(value, targetType);
      }

      Object value = request.getParameter(paramPath);
      // make some old systems happy
      if (emptyAsNull && StringUtils.isEmpty((String) value)) {
        value = null;
      }
      if (value == null) {
        value = checkRequiredAndDefaultValue();
      }
      if (null != collectionFormat) {
        value = collectionFormat.splitParam((String) value);
      }

      return convertValue(value, targetType);
    }

    private Object checkRequiredAndDefaultValue() {
      if (!ignoreRequiredCheck && isRequired()) {
        throw new InvocationException(Status.BAD_REQUEST, "Parameter is required.");
      }
      Object defaultValue = getDefaultValue();
      if (!ignoreDefaultValue && defaultValue != null) {
        return defaultValue;
      }

      return null;
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
    JavaType targetType =
        genericParamType == null ? null : TypeFactory.defaultInstance().constructType(genericParamType);
    return new QueryProcessor((QueryParameter) parameter, targetType);
  }
}
