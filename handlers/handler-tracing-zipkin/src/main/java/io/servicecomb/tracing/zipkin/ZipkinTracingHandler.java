/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.tracing.zipkin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import brave.Span;
import brave.Tracer.SpanInScope;
import brave.Tracing;
import io.servicecomb.core.Handler;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.swagger.invocation.AsyncResponse;
import io.servicecomb.swagger.invocation.InvocationType;

class ZipkinTracingHandler implements Handler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZipkinTracingHandler.class);

  private final Tracing tracer;

  private final ZipkinTracingDelegate tracingDelegate;

  ZipkinTracingHandler(ZipkinTracingDelegate tracingDelegate) {
    this.tracer = tracingDelegate.tracer();
    this.tracingDelegate = tracingDelegate;
  }

  @Override
  public void init(MicroserviceMeta microserviceMeta, InvocationType invocationType) {

  }

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    Span span = tracingDelegate.createSpan(invocation);
    try (SpanInScope scope = tracer.tracer().withSpanInScope(span)) {
      LOGGER.debug("{}: Generated tracing span for {}",
          tracingDelegate.name(),
          invocation.getOperationName());

      invocation.next(onResponse(invocation, asyncResp, span));
    } catch (Exception e) {
      LOGGER.debug("{}: Failed invocation on {}",
          tracingDelegate.name(),
          invocation.getOperationName(),
          e);

      tracingDelegate.onResponse(span, null, e);
      throw e;
    }
  }

  private AsyncResponse onResponse(Invocation invocation, AsyncResponse asyncResp, Span span) {
    return response -> {
      Throwable error = response.isFailed() ? response.getResult() : null;
      tracingDelegate.onResponse(span, response, error);

      LOGGER.debug("{}: Completed invocation on {}",
          tracingDelegate.name(),
          invocation.getOperationName(),
          error);

      asyncResp.handle(response);
    };
  }
}
