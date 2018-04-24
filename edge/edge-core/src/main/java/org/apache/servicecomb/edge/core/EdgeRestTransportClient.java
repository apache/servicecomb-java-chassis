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

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClientWithContext;
import org.apache.servicecomb.transport.rest.client.RestTransportClient;

import io.vertx.core.Context;

public class EdgeRestTransportClient extends RestTransportClient {
  @Override
  protected HttpClientWithContext findHttpClientPool(Invocation invocation) {
    Context invocationContext = (Context) invocation.getHandlerContext().get(EdgeInvocation.EDGE_INVOCATION_CONTEXT);
    return clientMgr.findClientPool(invocation.isSync(), invocationContext);
  }
}
