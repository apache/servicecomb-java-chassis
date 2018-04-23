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

package org.apache.servicecomb.transport.rest.vertx.trace;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.tracing.BraveTraceIdGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.impl.headers.VertxHttpHeaders;
import io.vertx.ext.web.RoutingContext;
import mockit.Expectations;

public class TracePrepareHandlerTest {
  private static final TracePrepareHandler HANDLER = new TracePrepareHandler();

  @Test
  public void handleWhenTraceIdExists() {
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    String traceId = "testTraceId";

    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.getHeader(Const.TRACE_ID_NAME)).thenReturn(traceId);

    HANDLER.handle(context);

    Mockito.verify(request, Mockito.times(0)).headers();
  }

  @Test
  public void handleWhenSetNewTraceId() {
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    String traceId = "testTraceId";
    MultiMap multimapHeader = new VertxHttpHeaders();

    new Expectations(BraveTraceIdGenerator.class) {
      {
        BraveTraceIdGenerator.INSTANCE.generateStringId();
        result = traceId;
      }
    };

    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.getHeader(Const.TRACE_ID_NAME)).thenReturn(null);
    Mockito.when(request.headers()).thenReturn(multimapHeader);

    HANDLER.handle(context);

    Mockito.verify(request, Mockito.times(1)).headers();
    Assert.assertEquals(1, multimapHeader.size());
    Assert.assertEquals(traceId, multimapHeader.get(Const.TRACE_ID_NAME));
  }
}
