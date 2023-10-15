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
package org.apache.servicecomb.transport.rest.client;

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.ConsumerFilter;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;

public class RestClientSenderFilter implements ConsumerFilter {
  public static final String NAME = "rest-client-sender";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean enabledForTransport(String transport) {
    return CoreConst.RESTFUL.equals(transport);
  }

  @Override
  public int getOrder(InvocationType invocationType, String application, String serviceName) {
    return Filter.CONSUMER_LOAD_BALANCE_ORDER + 2000;
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    CompletableFuture<Response> future = new RestClientSender(invocation)
        .send();

    return invocation.optimizeSyncConsumerThread(future);
  }
}
