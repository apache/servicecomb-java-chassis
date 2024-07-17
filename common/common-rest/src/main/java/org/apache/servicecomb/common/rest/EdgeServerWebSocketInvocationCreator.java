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

package org.apache.servicecomb.common.rest;

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.common.rest.locator.OperationLocator;
import org.apache.servicecomb.common.rest.locator.ServicePathManager;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.invocation.InvocationFactory;
import org.apache.servicecomb.core.provider.consumer.MicroserviceReferenceConfig;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.ServerWebSocket;

public class EdgeServerWebSocketInvocationCreator extends ProviderServerWebSocketInvocationCreator {
  private final String microserviceName;

  protected MicroserviceReferenceConfig microserviceReferenceConfig;

  protected final String path;

  public EdgeServerWebSocketInvocationCreator(String microserviceName, String path,
      Endpoint endpoint, ServerWebSocket webSocket) {
    super(null, endpoint, webSocket);
    this.microserviceName = microserviceName;
    this.path = path;
  }

  @Override
  public CompletableFuture<Invocation> createAsync() {
    return createMicroserviceReferenceConfig()
        .thenCompose(v -> super.createAsync());
  }

  protected CompletableFuture<Void> createMicroserviceReferenceConfig() {
    return SCBEngine.getInstance()
        .getOrCreateReferenceConfigAsync(microserviceName)
        .thenAccept(mrc -> {
          this.microserviceReferenceConfig = mrc;
          this.microserviceMeta = mrc.getMicroserviceMeta();
        });
  }

  @Override
  protected OperationLocator locateOperation(ServicePathManager servicePathManager) {
    return servicePathManager.consumerLocateOperation(path, HttpMethod.POST.name());
  }

  @Override
  protected Invocation createInstance() {
    ReferenceConfig referenceConfig = microserviceReferenceConfig
        .createReferenceConfig(restOperationMeta.getOperationMeta());

    Invocation invocation = InvocationFactory.forConsumer(referenceConfig,
        restOperationMeta.getOperationMeta(),
        restOperationMeta.getOperationMeta().buildBaseConsumerRuntimeType(),
        null);
    invocation.setSync(false);
    invocation.setEdge();
    invocation.setEndpoint(endpoint); // ensure transport name is correct

    return invocation;
  }
}
