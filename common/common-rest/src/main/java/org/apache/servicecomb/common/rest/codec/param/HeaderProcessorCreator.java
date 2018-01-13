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

import org.apache.servicecomb.common.rest.codec.RestClientRequest;
import org.apache.servicecomb.common.rest.codec.RestObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.models.parameters.Parameter;

public class HeaderProcessorCreator implements ParamValueProcessorCreator {
  private static final Logger LOGGER = LoggerFactory.getLogger(HeaderProcessorCreator.class);

  public static final String PARAMTYPE = "header";

  public static class HeaderProcessor extends AbstractParamProcessor {
    public HeaderProcessor(String paramPath, JavaType targetType) {
      super(paramPath, targetType);
    }

    @Override
    public Object getValue(HttpServletRequest request) throws Exception {
      Object value = null;
      if (targetType.isContainerType()) {
        Enumeration<String> headerValues = request.getHeaders(paramPath);
        if (headerValues == null) {
          return null;
        }

        value = Collections.list(headerValues);
      } else {
        value = request.getHeader(paramPath);
      }

      return convertValue(value, targetType);
    }

    @Override
    public void setValue(RestClientRequest clientRequest, Object arg) throws Exception {
      if (null == arg) {
        // null header should not be set to clientRequest to avoid NullPointerException in Netty.
        LOGGER.debug("Header arg is null, will not be set into clientRequest. paramPath = [{}]", paramPath);
        return;
      }
      clientRequest.putHeader(paramPath, RestObjectMapper.INSTANCE.convertToString(arg));
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
    JavaType targetType = TypeFactory.defaultInstance().constructType(genericParamType);
    return new HeaderProcessor(parameter.getName(), targetType);
  }
}
