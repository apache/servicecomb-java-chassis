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

package org.apache.servicecomb.demo.edge.service.handler;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.demo.edge.service.EdgeConst;
import org.apache.servicecomb.provider.pojo.Invoker;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthHandler implements Handler {
  private static Logger LOGGER = LoggerFactory.getLogger(AuthHandler.class);

  private static Auth auth;

  static {
    auth = Invoker.createProxy("auth", "auth", Auth.class);
  }

  @Override
  public void init(MicroserviceMeta microserviceMeta, InvocationType invocationType) {
  }

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    if (invocation.getHandlerContext().get(EdgeConst.ENCRYPT_CONTEXT) != null) {
      invocation.next(asyncResp);
      return;
    }

    auth.auth("").whenComplete((succ, e) -> doHandle(invocation, asyncResp, succ, e));
  }

  protected void doHandle(Invocation invocation, AsyncResponse asyncResp, Boolean authSucc, Throwable authException) {
    if (authException != null) {
      asyncResp.consumerFail(new InvocationException(Status.UNAUTHORIZED, (Object) authException.getMessage()));
      return;
    }

    if (!authSucc) {
      asyncResp.consumerFail(new InvocationException(Status.UNAUTHORIZED, (Object) "auth failed"));
    }

    LOGGER.debug("auth success.");
    try {
      invocation.next(asyncResp);
    } catch (Throwable e) {
      asyncResp.consumerFail(e);
    }
  }
}
