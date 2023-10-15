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
import java.util.Map;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.codec.RestClientRequest;
import org.apache.servicecomb.common.rest.codec.query.QueryCodec;
import org.apache.servicecomb.common.rest.codec.query.QueryCodecsUtils;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response.Status;

@SuppressWarnings("unchecked")
public class QueryProcessorCreator implements ParamValueProcessorCreator<Parameter> {
  public static final String PARAMTYPE = "query";

  public static class QueryProcessor extends AbstractParamProcessor {
    // This configuration is used for temporary use only. Do not use it if you are sure how it works. And may be deleted in future.
    private final boolean emptyAsNull = LegacyPropertyFactory
        .getBooleanProperty("servicecomb.rest.parameter.query.emptyAsNull", false);

    // This configuration is used for temporary use only. Do not use it if you are sure how it works. And may be deleted in future.
    private final boolean ignoreDefaultValue = LegacyPropertyFactory
        .getBooleanProperty("servicecomb.rest.parameter.query.ignoreDefaultValue", false);

    // This configuration is used for temporary use only. Do not use it if you are sure how it works. And may be deleted in future.
    private final boolean ignoreRequiredCheck = LegacyPropertyFactory
        .getBooleanProperty("servicecomb.rest.parameter.query.ignoreRequiredCheck", false);

    private final boolean repeatedType;

    private final QueryCodec queryCodec;

    public QueryProcessor(QueryParameter queryParameter, JavaType targetType) {
      super(queryParameter.getName(), targetType, queryParameter.getSchema().getDefault(),
          queryParameter.getRequired() != null && queryParameter.getRequired());

      this.repeatedType = queryParameter.getSchema() instanceof ArraySchema;
      this.queryCodec = QueryCodecsUtils.find(queryParameter.getStyle(), queryParameter.getExplode());
    }

    @Override
    public Object getValue(HttpServletRequest request) {
      return queryCodec.decode(this, request);
    }

    public Object getAndCheckParameter(HttpServletRequest request) {
      Object value = request.getParameter(paramPath);

      // compatible to SpringMVC @RequestParam. BODY_PARAMETER is only set for SpringMVC.
      if (value == null) {
        Map<String, Object> forms = (Map<String, Object>) request.getAttribute(RestConst.BODY_PARAMETER);
        value = (forms == null || forms.get(paramPath) == null)
            ? null : forms.get(paramPath);
      }

      // make some old systems happy
      if (emptyAsNull && "".equals(value)) {
        value = null;
      }

      return value != null ? value : checkRequiredAndDefaultValue();
    }

    private Object checkRequiredAndDefaultValue() {
      if (!ignoreRequiredCheck && isRequired()) {
        throw new InvocationException(Status.BAD_REQUEST,
            String.format("Parameter %s is required.", paramPath));
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

    public QueryCodec getQueryCodec() {
      return queryCodec;
    }

    public boolean isRepeatedType() {
      return repeatedType;
    }

    public Object convertValue(Object value) {
      return convertValue(value, targetType);
    }
  }

  public QueryProcessorCreator() {
    ParamValueProcessorCreatorManager.INSTANCE.register(PARAMTYPE, this);
  }

  @Override
  public ParamValueProcessor create(OperationMeta operationMeta,
      String parameterName, Parameter parameter, Type genericParamType) {
    JavaType targetType =
        genericParamType == null ? null : TypeFactory.defaultInstance().constructType(genericParamType);
    return new QueryProcessor((QueryParameter) parameter, targetType);
  }
}
