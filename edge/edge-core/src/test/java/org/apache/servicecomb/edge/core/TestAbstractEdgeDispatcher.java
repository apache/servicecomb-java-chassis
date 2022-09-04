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

package org.apache.servicecomb.edge.core;

import org.apache.servicecomb.foundation.test.scaffolding.exception.RuntimeExceptionWithoutStackTrace;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestAbstractEdgeDispatcher {
  static class AbstractEdgeDispatcherForTest extends AbstractEdgeDispatcher {
    @Override
    public int getOrder() {
      return 0;
    }

    @Override
    public void init(Router router) {
    }
  }

  @Test
  public void onFailure() {
    RoutingContext context = Mockito.mock(RoutingContext.class);

    HttpServerResponse response = Mockito.mock(HttpServerResponse.class);

    Mockito.when(context.response()).thenReturn(response);
    Mockito.when(context.failure()).thenReturn(new RuntimeExceptionWithoutStackTrace("failed"));

    AbstractEdgeDispatcherForTest dispatcher = new AbstractEdgeDispatcherForTest();
    dispatcher.onFailure(context);

    Mockito.verify(response).setStatusMessage("Bad Gateway");
    Mockito.verify(response).setStatusCode(502);

    Mockito.when(context.failure()).thenReturn(new InvocationException(401, "unauthorized", "unauthorized"));

    dispatcher = new AbstractEdgeDispatcherForTest();
    dispatcher.onFailure(context);
    Mockito.verify(response).setStatusMessage("unauthorized");
    Mockito.verify(response).setStatusCode(401);
  }
}
