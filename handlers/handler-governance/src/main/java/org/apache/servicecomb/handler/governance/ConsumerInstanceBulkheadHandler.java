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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.governance.MatchType;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.governance.handler.InstanceBulkheadHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCompletionStage;

public class ConsumerInstanceBulkheadHandler implements Handler {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerInstanceBulkheadHandler.class);

  private final InstanceBulkheadHandler instanceBulkheadHandler = BeanUtils.getBean(InstanceBulkheadHandler.class);

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    if (invocation.getEndpoint() == null) {
      invocation.next(asyncResp);
      return;
    }
    Supplier<CompletionStage<Response>> next = createBusinessCompletionStageSupplier(invocation);
    DecorateCompletionStage<Response> dcs = Decorators.ofCompletionStage(next);
    GovernanceRequest request = MatchType.createGovHttpRequest(invocation);
    request.setServiceName(invocation.getMicroserviceName());
    request.setInstanceId(invocation.getEndpoint().getMicroserviceInstance().getInstanceId());

    addBulkhead(dcs, request);

    dcs.get().whenComplete((r, e) -> {
      if (e == null) {
        asyncResp.complete(r);
        return;
      }

      if (e instanceof BulkheadFullException) {
        // return 503 so that consumer can retry
        asyncResp.complete(
            Response.failResp(new InvocationException(503, "bulkhead is full and does not permit further calls.",
                new CommonExceptionData("bulkhead is full and does not permit further calls."))));
        LOGGER.warn("bulkhead is full and does not permit further calls by policy : {}", e.getMessage());
      } else {
        asyncResp.complete(Response.createProducerFail(e));
      }
    });
  }

  private void addBulkhead(DecorateCompletionStage<Response> dcs, GovernanceRequest request) {
    Bulkhead bulkhead = instanceBulkheadHandler.getActuator(request);
    if (bulkhead != null) {
      dcs.withBulkhead(bulkhead);
    }
  }

  private Supplier<CompletionStage<Response>> createBusinessCompletionStageSupplier(Invocation invocation) {
    return () -> {
      CompletableFuture<Response> result = new CompletableFuture<>();
      try {
        invocation.next(result::complete);
      } catch (Exception e) {
        result.completeExceptionally(e);
      }
      return result;
    };
  }
}
