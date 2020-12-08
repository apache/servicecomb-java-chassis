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

import static org.apache.servicecomb.core.provider.consumer.InvokerUtils.isInEventLoop;
import static org.apache.servicecomb.swagger.invocation.InvocationType.CONSUMER;

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterMeta;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.foundation.common.utils.AsyncUtils;
import org.apache.servicecomb.swagger.invocation.Response;

@FilterMeta(name = "rest-client-sender", invocationType = CONSUMER)
public class RestClientSenderFilter implements Filter {
  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    CompletableFuture<Response> future = new RestClientSender(invocation)
        .send();

    if (invocation.isSync() && !isInEventLoop()) {
      AsyncUtils.toSync(future);
    }
    return future;
  }
}
