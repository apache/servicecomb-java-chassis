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
package org.apache.servicecomb.authentication.consumer;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.ConsumerFilter;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.ws.rs.core.Response.Status;

public class ConsumerAuthFilter implements ConsumerFilter {
  private ConsumerTokenManager authenticationTokenManager;

  @Autowired
  public void setConsumerTokenManager(ConsumerTokenManager consumerTokenManager) {
    this.authenticationTokenManager = consumerTokenManager;
  }

  @Override
  public int getOrder(InvocationType invocationType, String application, String serviceName) {
    return Filter.CONSUMER_LOAD_BALANCE_ORDER + 1010;
  }

  @Override
  public String getName() {
    return "consumer-public-key";
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    Optional<String> token = Optional.ofNullable(authenticationTokenManager.getToken());
    if (!token.isPresent()) {
      return CompletableFuture.failedFuture(
          new InvocationException(Status.SERVICE_UNAVAILABLE, "auth token is not properly configured yet."));
    }
    invocation.addContext(CoreConst.AUTH_TOKEN, token.get());
    return nextNode.onFilter(invocation);
  }
}
