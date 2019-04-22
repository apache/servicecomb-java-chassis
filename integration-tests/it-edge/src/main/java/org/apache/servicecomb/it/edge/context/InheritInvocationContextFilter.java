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

package org.apache.servicecomb.it.edge.context;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.springframework.util.StringUtils;

public class InheritInvocationContextFilter implements HttpServerFilter {
  @Override
  public int getOrder() {
    return 0;
  }

  /**
   * Check whether users can inherit specified InvocationContext from outer request
   */
  @Override
  public Response afterReceiveRequest(Invocation invocation, HttpServletRequestEx requestEx) {
    String invocationContextHeader = requestEx.getHeader(Const.CSE_CONTEXT);
    if (StringUtils.isEmpty(invocationContextHeader)) {
      return null;
    }

    try {
      @SuppressWarnings("unchecked")
      Map<String, String> invocationContext =
          JsonUtils.readValue(invocationContextHeader.getBytes(StandardCharsets.UTF_8), Map.class);
      if (!StringUtils.isEmpty(invocationContext.get("allowInherit"))) {
        invocation.addContext("allowInherit", invocationContext.get("allowInherit"));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
