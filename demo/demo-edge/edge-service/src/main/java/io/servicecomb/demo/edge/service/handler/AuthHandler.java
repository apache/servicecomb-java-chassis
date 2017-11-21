/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.demo.edge.service.handler;

import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.core.Handler;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.provider.pojo.Invoker;
import io.servicecomb.swagger.invocation.AsyncResponse;
import io.servicecomb.swagger.invocation.InvocationType;
import io.servicecomb.swagger.invocation.exception.InvocationException;

public class AuthHandler implements Handler {
  private static Logger LOGGER = LoggerFactory.getLogger(AuthHandler.class);

  private Auth auth;

  public AuthHandler() {
    auth = Invoker.createProxy("auth", "auth", Auth.class);
  }

  @Override
  public void init(MicroserviceMeta microserviceMeta, InvocationType invocationType) {
  }

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    if (!auth.auth("")) {
      asyncResp.consumerFail(new InvocationException(Status.UNAUTHORIZED, (Object) "auth failed"));
      return;
    }

    LOGGER.debug("auth success.");
    invocation.next(asyncResp);
  }
}
