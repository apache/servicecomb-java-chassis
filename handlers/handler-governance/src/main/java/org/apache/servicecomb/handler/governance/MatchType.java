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

package org.apache.servicecomb.handler.governance;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.governance.marker.GovernanceRequest;

public final class MatchType {
  public static final String REST = "rest";

  public static final String RPC = "rpc";

  public static GovernanceRequest createGovHttpRequest(Invocation invocation) {
    GovernanceRequest request = new GovernanceRequest();

    if (MatchType.REST.equalsIgnoreCase(invocation.getOperationMeta().getConfig().getGovernanceMatchType())) {
      if (invocation.isConsumer()) {
        request.setUri(
            invocation.getSchemaMeta().getSwagger().getBasePath() + invocation.getOperationMeta().getOperationPath());
        request.setMethod(invocation.getOperationMeta().getHttpMethod());
        request.setHeaders(getHeaderMap(invocation, true));
        return request;
      }
      request.setUri(invocation.getRequestEx().getRequestURI());
      request.setMethod(invocation.getRequestEx().getMethod());
      request.setHeaders(getHeaderMap(invocation, false));
      return request;
    }

    if (invocation.isConsumer()) {
      request.setUri(invocation.getOperationMeta().getMicroserviceQualifiedName());
    } else {
      request.setUri(invocation.getOperationMeta().getSchemaQualifiedName());
    }
    request.setMethod(invocation.getOperationMeta().getHttpMethod());
    request.setHeaders(getHeaderMap(invocation, true));

    return request;
  }

  private static Map<String, String> getHeaderMap(Invocation invocation, boolean fromContext) {
    Map<String, String> headers = new HashMap<>();
    if (fromContext) {
      headers.putAll(invocation.getContext());
    } else {
      Enumeration<String> names = invocation.getRequestEx().getHeaderNames();
      while (names.hasMoreElements()) {
        String name = names.nextElement();
        if (invocation.getRequestEx().getHeader(name) != null) {
          headers.put(name, invocation.getRequestEx().getHeader(name));
        }
      }
    }

    Map<String, Object> arguments = invocation.getSwaggerArguments();
    if (arguments != null) {
      arguments.forEach((k, v) -> {
        if (v != null) {
          headers.put(k, v.toString());
        }
      });
    }
    return headers;
  }
}
