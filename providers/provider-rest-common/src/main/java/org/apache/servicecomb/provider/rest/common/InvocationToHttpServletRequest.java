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

package org.apache.servicecomb.provider.rest.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.definition.RestParam;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.vertx.http.AbstractHttpServletRequest;

import io.vertx.core.net.SocketAddress;

/**
 * when transport is not over http, mock an HttpServletRequest from Invocation
 */
public class InvocationToHttpServletRequest extends AbstractHttpServletRequest {
  private final RestOperationMeta swaggerOperation;

  private final Invocation invocation;

  public InvocationToHttpServletRequest(Invocation invocation) {
    this.swaggerOperation = invocation.getOperationMeta().getExtData(RestConst.SWAGGER_REST_OPERATION);
    this.invocation = invocation;
  }

  private SocketAddress getSockerAddress() {
    return (SocketAddress) invocation.getHandlerContext().get(Const.REMOTE_ADDRESS);
  }

  @Override
  public String getParameter(String name) {
    RestParam param = swaggerOperation.getParamByName(name);
    if (param == null) {
      return null;
    }

    Object value = param.getValue(invocation.getSwaggerArguments());
    if (value == null) {
      return null;
    }

    return String.valueOf(value);
  }

  @Override
  public String[] getParameterValues(String name) {
    RestParam param = swaggerOperation.getParamByName(name);
    if (param == null) {
      return null;
    }

    return param.getValueAsStrings(invocation.getSwaggerArguments());
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    Map<String, String[]> paramMap = new HashMap<>();
    for (RestParam param : swaggerOperation.getParamList()) {
      String[] value = param.getValueAsStrings(invocation.getSwaggerArguments());
      paramMap.put(param.getParamName(), value);
    }
    return paramMap;
  }

  @Override
  public String getHeader(String name) {
    return getParameter(name);
  }

  @Override
  public int getIntHeader(String name) {
    String header = getHeader(name);
    if (header == null) {
      return -1;
    }

    return Integer.parseInt(header);
  }

  @Override
  public String getMethod() {
    return this.swaggerOperation.getHttpMethod();
  }

  @Override
  public String getPathInfo() {
    try {
      return this.swaggerOperation.getPathBuilder().createPathString(invocation.getSwaggerArguments());
    } catch (Exception e) {
      throw new ServiceCombException("Failed to get path info.", e);
    }
  }

  @Override
  public String getRemoteAddr() {
    return this.getSockerAddress() == null ? "" : this.getSockerAddress().host();
  }

  @Override
  public String getRemoteHost() {
    return this.getSockerAddress() == null ? "" : this.getSockerAddress().host();
  }

  @Override
  public int getRemotePort() {
    return this.getSockerAddress() == null ? 0 : this.getSockerAddress().port();
  }

  @Override
  public String getContextPath() {
    return "";
  }

  /**
   * it's a mock httpServletRequest, contentType is unknown
   * @return contentType
   */
  @Override
  public String getContentType() {
    return null;
  }

  /**
   * it's a mock httpServletRequest, characterEncoding is unknown
   * @return characterEncoding
   */
  @Override
  public String getCharacterEncoding() {
    return null;
  }
}
