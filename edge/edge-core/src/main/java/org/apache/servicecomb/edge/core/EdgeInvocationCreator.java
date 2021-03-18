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

import static org.apache.servicecomb.edge.core.EdgeInvocation.EDGE_INVOCATION_CONTEXT;

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.common.rest.RestVertxProducerInvocationCreator;
import org.apache.servicecomb.common.rest.locator.OperationLocator;
import org.apache.servicecomb.common.rest.locator.ServicePathManager;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.invocation.InvocationFactory;
import org.apache.servicecomb.core.provider.consumer.MicroserviceReferenceConfig;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public class EdgeInvocationCreator extends RestVertxProducerInvocationCreator {
  protected final String microserviceName;

  protected final String versionRule;

  protected final String path;

  protected MicroserviceReferenceConfig microserviceReferenceConfig;

  public EdgeInvocationCreator(RoutingContext routingContext,
      HttpServletRequestEx requestEx, HttpServletResponseEx responseEx,
      String microserviceName, String versionRule, String path) {
    super(routingContext, null, null, requestEx, responseEx);

    this.microserviceName = microserviceName;
    this.versionRule = versionRule;
    this.path = path;
  }

  @Override
  public CompletableFuture<Invocation> createAsync() {
    return createMicroserviceReferenceConfig()
        .thenCompose(v -> super.createAsync());
  }

  protected CompletableFuture<Void> createMicroserviceReferenceConfig() {
    return SCBEngine.getInstance()
        .createMicroserviceReferenceConfigAsync(microserviceName, versionRule)
        .thenAccept(mrc -> {
          this.microserviceReferenceConfig = mrc;
          this.microserviceMeta = mrc.getLatestMicroserviceMeta();
        });
  }

  @Override
  protected OperationLocator locateOperation(ServicePathManager servicePathManager) {
    return servicePathManager.consumerLocateOperation(path, requestEx.getMethod());
  }

  @Override
  protected void initInvocationContext(Invocation invocation) {
    // do not read InvocationContext from HTTP header, for security reason
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
    invocation.setEdge(true);
    invocation.addLocalContext(EDGE_INVOCATION_CONTEXT, Vertx.currentContext());

    return invocation;
  }
}
