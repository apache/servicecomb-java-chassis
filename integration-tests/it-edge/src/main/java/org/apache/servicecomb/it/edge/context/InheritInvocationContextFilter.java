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

/**
 * By default, the EdgeService does not inherit invocation context from outer request.
 * Users can implement a {@link HttpServerFilter} like this to inherit specific invocation context.
 * <br/>
 * <em>Caution: For security reason, it't better not to inherit all of the invocation context
 * without checking or filtering.</em>
 */
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
      // Here only the specific invocation context "allowInherit" can be inherited,
      // and other context key-value pairs are ignored.
      // If you want to inherit invocation context from outer requests,
      // it's better to implement such a white-list logic to filter the invocation context.
      // CAUTION: to avoid potential security problem, please do not add all invocation context key-value pairs
      // into InvocationContext without checking or filtering.
      if (!StringUtils.isEmpty(invocationContext.get("allowInherit"))) {
        invocation.addContext("allowInherit", invocationContext.get("allowInherit"));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
