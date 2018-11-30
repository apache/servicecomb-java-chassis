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
package org.apache.servicecomb.demo.edge.service.encrypt.filter;

import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.demo.edge.service.EdgeConst;
import org.apache.servicecomb.demo.edge.service.encrypt.EncryptContext;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.swagger.invocation.Response;

public class UserIdFilter implements HttpServerFilter {
  @Override
  public int getOrder() {
    return -8000;
  }

  @Override
  public Response afterReceiveRequest(Invocation invocation, HttpServletRequestEx requestEx) {
    EncryptContext encryptContext = (EncryptContext) invocation.getHandlerContext().get(EdgeConst.ENCRYPT_CONTEXT);
    if (encryptContext == null) {
      return null;
    }

    String userId = encryptContext.getUserId();
    if (userId != null) {
      requestEx.setParameter("userId", userId);
    }

    return null;
  }
}
