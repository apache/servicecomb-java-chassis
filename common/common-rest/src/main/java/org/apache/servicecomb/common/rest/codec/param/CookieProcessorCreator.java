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
import java.util.Objects;

import org.apache.servicecomb.common.rest.codec.RestClientRequest;
import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.oas.models.parameters.Parameter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response.Status;

public class CookieProcessorCreator implements ParamValueProcessorCreator<Parameter> {
  public static final String PARAMTYPE = "cookie";

  public static class CookieProcessor extends AbstractParamProcessor {
    public CookieProcessor(String paramPath, JavaType targetType, Object defaultValue, boolean required) {
      super(paramPath, targetType, defaultValue, required);
    }

    @Override
    public Object getValue(HttpServletRequest request) {
      Cookie[] cookies = request.getCookies();
      Object value = null;
      if (cookies == null || cookies.length == 0) {
        value = checkRequiredAndDefaultValue();
        return convertValue(value, targetType);
      }

      for (Cookie cookie : cookies) {
        if (Objects.equals(paramPath, cookie.getName())) {
          value = cookie.getValue();
          break;
        }
      }
      if (value == null) {
        value = checkRequiredAndDefaultValue();
      }
      return convertValue(value, targetType);
    }

    private Object checkRequiredAndDefaultValue() {
      if (isRequired()) {
        throw new InvocationException(Status.BAD_REQUEST, "Parameter is required.");
      }
      return getDefaultValue();
    }

    @Override
    public void setValue(RestClientRequest clientRequest, Object arg) throws Exception {
      clientRequest.addCookie(paramPath,
          RestObjectMapperFactory.getConsumerWriterMapper().convertToString(arg));
    }

    @Override
    public String getProcessorType() {
      return PARAMTYPE;
    }
  }

  public CookieProcessorCreator() {
    ParamValueProcessorCreatorManager.INSTANCE.register(PARAMTYPE, this);
  }

  @Override
  public ParamValueProcessor create(OperationMeta operationMeta,
      String parameterName, Parameter parameter, Type genericParamType) {
    JavaType targetType =
        genericParamType == null ? null : TypeFactory.defaultInstance().constructType(genericParamType);
    return new CookieProcessor(parameterName, targetType, parameter.getSchema().getDefault(),
        parameter.getRequired() != null && parameter.getRequired());
  }
}
