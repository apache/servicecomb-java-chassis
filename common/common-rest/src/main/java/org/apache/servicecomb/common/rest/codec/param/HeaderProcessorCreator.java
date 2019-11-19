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
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.common.rest.codec.RestClientRequest;
import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.netflix.config.DynamicPropertyFactory;

import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.ArrayProperty;

public class HeaderProcessorCreator implements ParamValueProcessorCreator {
  private static final Logger LOGGER = LoggerFactory.getLogger(HeaderProcessorCreator.class);

  public static final String PARAMTYPE = "header";

  public static class HeaderProcessor extends AbstractParamProcessor {
    // This configuration is used for temporary use only. Do not use it if you are sure how it works. And may be deleted in future.
    private boolean ignoreRequiredCheck = DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.rest.parameter.header.ignoreRequiredCheck", false).get();

    private boolean repeatedType;

    public HeaderProcessor(HeaderParameter headerParameter, JavaType targetType) {
      super(headerParameter.getName(), targetType, headerParameter.getDefaultValue(), headerParameter.getRequired());

      this.repeatedType = ArrayProperty.isType(headerParameter.getType());
    }

    @Override
    public Object getValue(HttpServletRequest request) {
      if (repeatedType) {
        Enumeration<String> headerValues = request.getHeaders(paramPath);
        if (headerValues == null) {
          //Even if the paramPath does not exist, headerValues won't be null at now
          return null;
        }
        return convertValue(Collections.list(headerValues), targetType);
      }

      Object value = request.getHeader(paramPath);
      if (value == null) {
        value = checkRequiredAndDefaultValue();
      }
      return convertValue(value, targetType);
    }

    private Object checkRequiredAndDefaultValue() {
      if (!ignoreRequiredCheck && isRequired()) {
        throw new InvocationException(Status.BAD_REQUEST, "Parameter is required.");
      }
      return getDefaultValue();
    }

    @Override
    public void setValue(RestClientRequest clientRequest, Object arg) throws Exception {
      if (null == arg) {
        // null header should not be set to clientRequest to avoid NullPointerException in Netty.
        LOGGER.debug("Header arg is null, will not be set into clientRequest. paramPath = [{}]", paramPath);
        return;
      }
      clientRequest.putHeader(paramPath,
          RestObjectMapperFactory.getConsumerWriterMapper().convertToString(arg));
    }

    @Override
    public String getProcessorType() {
      return PARAMTYPE;
    }
  }

  public HeaderProcessorCreator() {
    ParamValueProcessorCreatorManager.INSTANCE.register(PARAMTYPE, this);
  }

  @Override
  public ParamValueProcessor create(Parameter parameter, Type genericParamType) {
    JavaType targetType =
        genericParamType == null ? null : TypeFactory.defaultInstance().constructType(genericParamType);
    return new HeaderProcessor((HeaderParameter) parameter, targetType);
  }
}
