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

public class InvocationToHttpServletRequest extends AbstractHttpServletRequest {
  private RestOperationMeta swaggerOperation;

  private Object[] args;

  private SocketAddress sockerAddress;

  public InvocationToHttpServletRequest(Invocation invocation) {
    this.swaggerOperation = invocation.getOperationMeta().getExtData(RestConst.SWAGGER_REST_OPERATION);
    this.args = invocation.getArgs();
    this.sockerAddress = (SocketAddress) invocation.getHandlerContext().get(Const.REMOTE_ADDRESS);
  }

  @Override
  public String getParameter(String name) {
    RestParam param = swaggerOperation.getParamByName(name);
    if (param == null) {
      return null;
    }

    Object value = param.getValue(args);
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

    return param.getValueAsStrings(args);
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    Map<String, String[]> paramMap = new HashMap<>();
    for (RestParam param : swaggerOperation.getParamList()) {
      String[] value = param.getValueAsStrings(args);
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
      return this.swaggerOperation.getPathBuilder().createPathString(args);
    } catch (Exception e) {
      throw new ServiceCombException("Failed to get path info.", e);
    }
  }

  @Override
  public String getRemoteAddr() {
    return this.sockerAddress == null ? "" : this.sockerAddress.host();
  }

  @Override
  public String getRemoteHost() {
    return this.sockerAddress == null ? "" : this.sockerAddress.host();
  }

  @Override
  public int getRemotePort() {
    return this.sockerAddress == null ? 0 : this.sockerAddress.port();
  }

  @Override
  public String getContextPath() {
    return "";
  }
}
