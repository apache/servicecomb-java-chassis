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
import static org.apache.servicecomb.core.SCBEngine.CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC;
import static org.apache.servicecomb.core.SCBEngine.DEFAULT_TURN_DOWN_STATUS_WAIT_SEC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.locator.OperationLocator;
import org.apache.servicecomb.common.rest.locator.ServicePathManager;
import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.SCBStatus;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.core.definition.InvocationRuntimeType;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

public class RestProducerInvocationCreatorTest {

  final RoutingContext routingContext = Mockito.mock(RoutingContext.class);

  final MicroserviceMeta microserviceMeta = Mockito.mock(MicroserviceMeta.class);

  final ServicePathManager servicePathManager = Mockito.mock(ServicePathManager.class);

  final RestOperationMeta restOperationMeta = Mockito.mock(RestOperationMeta.class);

  final Endpoint endpoint = Mockito.mock(Endpoint.class);

  final HttpServletRequestEx requestEx = Mockito.mock(HttpServletRequestEx.class);

  final HttpServletResponseEx responseEx = Mockito.mock(HttpServletResponseEx.class);

  final OperationLocator locator = Mockito.mock(OperationLocator.class);

  final InvocationRuntimeType invocationRuntimeType = Mockito.mock(InvocationRuntimeType.class);

  final OperationMeta operationMeta = Mockito.mock(OperationMeta.class);

  final SchemaMeta schemaMeta = Mockito.mock(SchemaMeta.class);

  RestProducerInvocationCreator creator;

  static SCBEngine engine;

  @BeforeAll
  public static void beforeClass() {
    engine = SCBBootstrap.createSCBEngineForTest();
    engine.setStatus(SCBStatus.UP);
    Environment environment = Mockito.mock(Environment.class);
    LegacyPropertyFactory.setEnvironment(environment);
    Mockito.when(environment.getProperty("servicecomb.rest.parameter.decodeAsObject", boolean.class, false))
        .thenReturn(false);
    Mockito.when(environment.getProperty(CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC,
        long.class, DEFAULT_TURN_DOWN_STATUS_WAIT_SEC)).thenReturn(DEFAULT_TURN_DOWN_STATUS_WAIT_SEC);
    engine.setEnvironment(environment);
  }

  @AfterAll
  public static void afterClass() {
    engine.destroy();
  }

  @BeforeEach
  public void setUp() {
    creator = new RestVertxProducerInvocationCreator(routingContext, microserviceMeta, endpoint,
        requestEx, responseEx);
    creator = Mockito.spy(creator);
  }

  @Test
  public void should_failed_when_not_defined_any_schema() {
    try (MockedStatic<ServicePathManager> mockedStatic = Mockito.mockStatic(ServicePathManager.class)) {
      mockedStatic.when(() -> ServicePathManager.getServicePathManager(null)).thenReturn(servicePathManager);

      InvocationException throwable = (InvocationException) catchThrowable(() -> creator.createAsync().join());
      CommonExceptionData data = (CommonExceptionData) throwable.getErrorData();

      assertThat(throwable.getStatusCode()).isEqualTo(NOT_FOUND.getStatusCode());
      assertThat(Json.encode(data)).isIn("{\"code\":\"SCB.00000002\",\"message\":\"Not Found\"}",
          "{\"message\":\"Not Found\",\"code\":\"SCB.00000002\"}");
    }
  }

  @Test
  public void should_save_requestEx_in_invocation_context() {
    try (MockedStatic<ServicePathManager> mockedStatic = Mockito.mockStatic(ServicePathManager.class)) {
      mockedStatic.when(() -> ServicePathManager.getServicePathManager(microserviceMeta))
          .thenReturn(servicePathManager);
      Mockito.when(creator.locateOperation(microserviceMeta)).thenReturn(locator);
      Mockito.when(locator.getOperation()).thenReturn(restOperationMeta);
      Mockito.when(restOperationMeta.getOperationMeta()).thenReturn(operationMeta);
      Mockito.when(operationMeta.buildBaseProviderRuntimeType()).thenReturn(invocationRuntimeType);
      Mockito.when(operationMeta.getSchemaMeta()).thenReturn(schemaMeta);
      Mockito.when(schemaMeta.getMicroserviceMeta()).thenReturn(microserviceMeta);

      Invocation invocation = creator.createAsync().join();

      Object request = invocation.getLocalContext(RestConst.REST_REQUEST);
      assertThat(request).isSameAs(requestEx);
    }
  }

  @Test
  public void should_save_path_var_map_in_requestEx() {
    try (MockedStatic<ServicePathManager> mockedStatic = Mockito.mockStatic(ServicePathManager.class)) {
      mockedStatic.when(() -> ServicePathManager.getServicePathManager(microserviceMeta))
          .thenReturn(servicePathManager);
      Mockito.when(creator.locateOperation(microserviceMeta)).thenReturn(locator);
      Mockito.when(locator.getOperation()).thenReturn(restOperationMeta);
      Mockito.when(restOperationMeta.getOperationMeta()).thenReturn(operationMeta);
      Mockito.when(operationMeta.buildBaseProviderRuntimeType()).thenReturn(invocationRuntimeType);
      Mockito.when(operationMeta.getSchemaMeta()).thenReturn(schemaMeta);
      Mockito.when(schemaMeta.getMicroserviceMeta()).thenReturn(microserviceMeta);

      creator.createAsync().join();

      Mockito.verify(requestEx, Mockito.times(1)).setAttribute(Mockito.eq(RestConst.PATH_PARAMETERS), Mockito.any());
    }
  }

  @Test
  public void should_merge_invocation_context_from_request() {
    try (MockedStatic<ServicePathManager> mockedStatic = Mockito.mockStatic(ServicePathManager.class)) {
      mockedStatic.when(() -> ServicePathManager.getServicePathManager(microserviceMeta))
          .thenReturn(servicePathManager);
      Mockito.when(creator.locateOperation(microserviceMeta)).thenReturn(locator);
      Mockito.when(locator.getOperation()).thenReturn(restOperationMeta);
      Mockito.when(restOperationMeta.getOperationMeta()).thenReturn(operationMeta);
      Mockito.when(operationMeta.buildBaseProviderRuntimeType()).thenReturn(invocationRuntimeType);
      Mockito.when(operationMeta.getSchemaMeta()).thenReturn(schemaMeta);
      Mockito.when(schemaMeta.getMicroserviceMeta()).thenReturn(microserviceMeta);
      Mockito.when(requestEx.getHeader(CoreConst.CSE_CONTEXT)).thenReturn("{\"k\":\"v\"}");

      Invocation invocation = creator.createAsync().join();

      assertThat(invocation.getContext("k")).isEqualTo("v");
    }
  }
}
