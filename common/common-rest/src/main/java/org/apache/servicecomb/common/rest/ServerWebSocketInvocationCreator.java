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

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static org.apache.servicecomb.core.exception.ExceptionCodes.NOT_DEFINED_ANY_SCHEMA;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.locator.OperationLocator;
import org.apache.servicecomb.common.rest.locator.ServicePathManager;
import org.apache.servicecomb.config.YAMLUtil;
import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.exception.Exceptions;
import org.apache.servicecomb.core.invocation.InvocationCreator;
import org.apache.servicecomb.core.invocation.InvocationFactory;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;

public abstract class ServerWebSocketInvocationCreator implements InvocationCreator {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestVertxProducerInvocationCreator.class);

  protected MicroserviceMeta microserviceMeta;

  protected final Endpoint endpoint;

  protected final ServerWebSocket websocket;

  protected RestOperationMeta restOperationMeta;

  public ServerWebSocketInvocationCreator(MicroserviceMeta microserviceMeta, Endpoint endpoint,
      ServerWebSocket websocket) {
    this.microserviceMeta = microserviceMeta;
    this.endpoint = endpoint;
    this.websocket = websocket;
  }

  @Override
  public CompletableFuture<Invocation> createAsync() {
    initRestOperation();

    Invocation invocation = createInstance();
    initInvocationContext(invocation);
    addParameterContext(invocation);
    initTransportContext(invocation);

    return CompletableFuture.completedFuture(invocation);
  }

  protected Invocation createInstance() {
    return InvocationFactory.forProvider(endpoint, restOperationMeta.getOperationMeta(), null);
  }

  protected void initInvocationContext(Invocation invocation) {
    if (!LegacyPropertyFactory.getBooleanProperty(RestConst.DECODE_INVOCATION_CONTEXT, true)) {
      return;
    }

    String strCseContext = websocket.headers().get(CoreConst.CSE_CONTEXT);
    if (StringUtils.isEmpty(strCseContext)) {
      return;
    }

    @SuppressWarnings("unchecked")
    Map<String, String> invocationContext = Json.decodeValue(strCseContext, Map.class);
    invocation.mergeContext(invocationContext);
  }

  // No queries for websocket
  protected void addParameterContext(Invocation invocation) {
    String headerContextMapper = LegacyPropertyFactory
        .getStringProperty(RestConst.HEADER_CONTEXT_MAPPER);

    Map<String, Object> headerContextMappers;
    if (headerContextMapper != null) {
      headerContextMappers = YAMLUtil.yaml2Properties(headerContextMapper);
    } else {
      headerContextMappers = new HashMap<>();
    }

    headerContextMappers.forEach((k, v) -> {
      if (v instanceof String && websocket.headers().get(k) != null) {
        invocation.addContext((String) v, websocket.headers().get(k));
      }
    });
  }

  protected abstract void initTransportContext(Invocation invocation);

  protected void initRestOperation() {
    OperationLocator locator = locateOperation(microserviceMeta);
    restOperationMeta = locator.getOperation();
  }

  protected OperationLocator locateOperation(MicroserviceMeta microserviceMeta) {
    ServicePathManager servicePathManager = ServicePathManager.getServicePathManager(microserviceMeta);
    if (servicePathManager == null) {
      LOGGER.error("No schema defined for {}:{}.", this.microserviceMeta.getAppId(),
          this.microserviceMeta.getMicroserviceName());
      throw Exceptions.create(NOT_FOUND, NOT_DEFINED_ANY_SCHEMA, NOT_FOUND.getReasonPhrase());
    }

    return locateOperation(servicePathManager);
  }

  protected OperationLocator locateOperation(ServicePathManager servicePathManager) {
    return servicePathManager.producerLocateOperation(websocket.path(), HttpMethod.POST.name());
  }
}
