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

package org.apache.servicecomb.tracing.zipkin;

import static com.seanyinx.github.unit.scaffolding.AssertUtils.expectFailing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import brave.http.HttpTracing;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import brave.Span;
import brave.Tracing;

public class ZipkinTracingHandlerTest {

  private final Invocation invocation = Mockito.mock(Invocation.class);

  private final Response response = Mockito.mock(Response.class);

  private final AsyncResponse asyncResponse = Mockito.mock(AsyncResponse.class);

  private final Tracing tracing = Tracing.newBuilder().build();

  private final Span span = Mockito.mock(Span.class);

  private final RuntimeException exception = new RuntimeException("oops");

  private ZipkinTracingHandler tracingHandler;

  @BeforeEach
  public void setUp() throws Exception {
    HttpTracing httpTracing = HttpTracing.create(tracing);
    tracingHandler = new ZipkinTracingHandler(httpTracing);
  }

  @Test
  public void delegatesResponseAndHandleResponse() throws Exception {
    ArgumentCaptor<AsyncResponse> argumentCaptor = ArgumentCaptor.forClass(AsyncResponse.class);

    tracingHandler.handle(invocation, asyncResponse);

    verify(invocation).next(argumentCaptor.capture());

    argumentCaptor.getValue().handle(response);

    verify(asyncResponse).handle(response);
  }

  @Test
  public void delegatesErrorResponseAndHandleResponse() throws Exception {
    when(response.isFailed()).thenReturn(true);
    when(response.getResult()).thenReturn(exception);

    ArgumentCaptor<AsyncResponse> argumentCaptor = ArgumentCaptor.forClass(AsyncResponse.class);

    tracingHandler.handle(invocation, asyncResponse);

    verify(invocation).next(argumentCaptor.capture());

    argumentCaptor.getValue().handle(response);

    verify(asyncResponse).handle(response);
  }

  @Test
  public void delegatesErrorOnInvocationFailure() throws Exception {
    doThrow(exception).when(invocation).next(ArgumentMatchers.any(AsyncResponse.class));

    try {
      tracingHandler.handle(invocation, asyncResponse);
      expectFailing(RuntimeException.class);
    } catch (Exception e) {
      Assertions.assertEquals(exception, e);
    }
  }
}
