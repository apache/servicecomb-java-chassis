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
import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessor;
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
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.Json;

public abstract class RestProducerInvocationCreator implements InvocationCreator {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestVertxProducerInvocationCreator.class);

  protected MicroserviceMeta microserviceMeta;

  protected final Endpoint endpoint;

  protected final HttpServletRequestEx requestEx;

  protected final HttpServletResponseEx responseEx;

  protected RestOperationMeta restOperationMeta;

  protected ProduceProcessor produceProcessor;

  public RestProducerInvocationCreator(MicroserviceMeta microserviceMeta, Endpoint endpoint,
      HttpServletRequestEx requestEx, HttpServletResponseEx responseEx) {
    this.microserviceMeta = microserviceMeta;
    this.endpoint = endpoint;
    this.requestEx = requestEx;
    this.responseEx = responseEx;
  }

  @Override
  public CompletableFuture<Invocation> createAsync() {
    initRestOperation();

    Invocation invocation = createInstance();
    initInvocationContext(invocation);
    addParameterContext(invocation);
    initTransportContext(invocation);

    invocation.addLocalContext(RestConst.REST_REQUEST, requestEx);

    return CompletableFuture.completedFuture(invocation);
  }

  protected Invocation createInstance() {
    return InvocationFactory.forProvider(endpoint, restOperationMeta.getOperationMeta(), null);
  }

  protected void initInvocationContext(Invocation invocation) {
    String strCseContext = requestEx.getHeader(CoreConst.CSE_CONTEXT);
    if (StringUtils.isEmpty(strCseContext)) {
      return;
    }

    @SuppressWarnings("unchecked")
    Map<String, String> invocationContext = Json.decodeValue(strCseContext, Map.class);
    invocation.mergeContext(invocationContext);
  }

  protected void addParameterContext(Invocation invocation) {
    String headerContextMapper = LegacyPropertyFactory
        .getStringProperty(RestConst.HEADER_CONTEXT_MAPPER);
    String queryContextMapper = LegacyPropertyFactory
        .getStringProperty(RestConst.QUERY_CONTEXT_MAPPER);

    Map<String, Object> headerContextMappers;
    if (headerContextMapper != null) {
      headerContextMappers = YAMLUtil.yaml2Properties(headerContextMapper);
    } else {
      headerContextMappers = new HashMap<>();
    }

    Map<String, Object> queryContextMappers;
    if (queryContextMapper != null) {
      queryContextMappers = YAMLUtil.yaml2Properties(queryContextMapper);
    } else {
      queryContextMappers = new HashMap<>();
    }

    headerContextMappers.forEach((k, v) -> {
      if (v instanceof String && requestEx.getHeader(k) != null) {
        invocation.addContext((String) v, requestEx.getHeader(k));
      }
    });
    queryContextMappers.forEach((k, v) -> {
      if (v instanceof String && requestEx.getParameter(k) != null) {
        invocation.addContext((String) v, requestEx.getParameter(k));
      }
    });
  }

  protected abstract void initTransportContext(Invocation invocation);

  protected void initRestOperation() {
    OperationLocator locator = locateOperation(microserviceMeta);
    requestEx.setAttribute(RestConst.PATH_PARAMETERS, locator.getPathVarMap());
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
    return servicePathManager.producerLocateOperation(requestEx.getRequestURI(), requestEx.getMethod());
  }
}
