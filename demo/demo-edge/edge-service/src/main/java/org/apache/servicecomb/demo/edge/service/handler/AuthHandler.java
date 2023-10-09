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

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.ConsumerFilter;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.demo.edge.service.EdgeConst;
import org.apache.servicecomb.provider.pojo.Invoker;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.core.Response.Status;

@Component
public class AuthHandler implements ConsumerFilter {
  private static Logger LOGGER = LoggerFactory.getLogger(AuthHandler.class);

  private static Auth auth;

  static {
    auth = Invoker.createProxy("auth", "auth", Auth.class);
  }

  @Override
  public int getOrder(InvocationType invocationType, String application, String serviceName) {
    return Filter.CONSUMER_LOAD_BALANCE_ORDER - 1980;
  }

  @Override
  public boolean enabledForMicroservice(String application, String serviceName) {
    if ("auth".equals(serviceName)) {
      return false;
    }
    return true;
  }

  @Override
  public String getName() {
    return "test-auth";
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    if (invocation.getHandlerContext().get(EdgeConst.ENCRYPT_CONTEXT) != null) {
      return nextNode.onFilter(invocation);
    }

    return auth.auth("").thenCompose(result -> doHandle(invocation, nextNode, result));
  }

  protected CompletableFuture<Response> doHandle(Invocation invocation, FilterNode nextNode, Boolean authSucc) {
    if (!authSucc) {
      return CompletableFuture.failedFuture(new InvocationException(Status.UNAUTHORIZED, (Object) "auth failed"));
    }

    LOGGER.debug("auth success.");
    return nextNode.onFilter(invocation);
  }
}
