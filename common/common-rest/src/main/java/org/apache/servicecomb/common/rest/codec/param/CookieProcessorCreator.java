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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.servicecomb.common.rest.codec.RestClientRequest;
import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.models.parameters.CookieParameter;
import io.swagger.models.parameters.Parameter;

public class CookieProcessorCreator implements ParamValueProcessorCreator {
  public static final String PARAMTYPE = "cookie";

  public static class CookieProcessor extends AbstractParamProcessor {
    public CookieProcessor(String paramPath, JavaType targetType, Object defaultValue) {
      super(paramPath, targetType, defaultValue);
    }

    @Override
    public Object getValue(HttpServletRequest request) throws Exception {
      Cookie[] cookies = request.getCookies();
      if (cookies == null) {
        return null;
      }

      String value = null;
      for (Cookie cookie : cookies) {
        if (paramPath.equals(cookie.getName())) {
          value = cookie.getValue();
          if (value == null) {
            Object defaultValue = getDefaultValue();
            if (defaultValue != null) {
              value = defaultValue.toString();
            }
          }
        }
      }

      return convertValue(value, targetType);
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
  public ParamValueProcessor create(Parameter parameter, Type genericParamType) {
    JavaType targetType = TypeFactory.defaultInstance().constructType(genericParamType);
    return new CookieProcessor(parameter.getName(), targetType, ((CookieParameter) parameter).getDefaultValue());
  }
}
