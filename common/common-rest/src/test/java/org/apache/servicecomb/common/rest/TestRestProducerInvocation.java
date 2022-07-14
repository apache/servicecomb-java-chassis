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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.common.rest.definition.RestMetaUtils;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.common.rest.locator.OperationLocator;
import org.apache.servicecomb.common.rest.locator.ServicePathManager;
import org.apache.servicecomb.common.rest.locator.TestPathSchema;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.vertx.http.AbstractHttpServletRequest;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class TestRestProducerInvocation {

  final Transport transport = Mockito.mock(Transport.class);

  HttpServletRequestEx requestEx = Mockito.mock(HttpServletRequestEx.class);

  final HttpServletResponseEx responseEx = Mockito.mock(HttpServletResponseEx.class);

  RestProducerInvocation restProducerInvocation;

  Throwable throwableOfSendFailResponse;

  boolean scheduleInvocation;

  static final List<HttpServerFilter> httpServerFilters = SPIServiceUtils.getSortedService(HttpServerFilter.class);

  static SCBEngine scbEngine;

  static OperationMeta operationMeta;

  static RestOperationMeta restOperationMeta;

  static MicroserviceMeta microserviceMeta;

  @BeforeAll
  public static void classSetup() {
    ConfigUtil.installDynamicConfig();
    scbEngine = SCBBootstrap.createSCBEngineForTest()
        .addProducerMeta("sid1", new TestPathSchema())
        .run();
    operationMeta = scbEngine.getProducerMicroserviceMeta().operationMetas().get("test.sid1.dynamicId");
    restOperationMeta = RestMetaUtils.getRestOperationMeta(operationMeta);
    microserviceMeta = operationMeta.getMicroserviceMeta();
  }

  @AfterAll
  public static void classTeardown() {
    scbEngine.destroy();
    ArchaiusUtils.resetConfig();
  }

  private void initRestProducerInvocation() {
    restProducerInvocation.transport = transport;
    restProducerInvocation.requestEx = requestEx;
    restProducerInvocation.responseEx = responseEx;
    restProducerInvocation.restOperationMeta = restOperationMeta;
    restProducerInvocation.httpServerFilters = httpServerFilters;
  }

  @Test
  public void invokeSendFail() {
    InvocationException expected = new InvocationException(javax.ws.rs.core.Response.Status.BAD_REQUEST, "test");
    restProducerInvocation = Mockito.spy(new RestProducerInvocation());
    Mockito.doThrow(expected).when(restProducerInvocation).findRestOperation();
    Mockito.doAnswer(invocationOnMock -> {
      throwableOfSendFailResponse = expected;
      return null;
    }).when(restProducerInvocation).sendFailResponse(Mockito.any());
    Mockito.doThrow(new IllegalStateException("must not invoke scheduleInvocation")).when(restProducerInvocation).scheduleInvocation();


    restProducerInvocation.invoke(transport, requestEx, responseEx, httpServerFilters);

    Assertions.assertSame(expected, throwableOfSendFailResponse);
  }

  @Test
  public void invokeNormal() {
    restProducerInvocation = Mockito.spy(new RestProducerInvocation());
    Mockito.doAnswer(invocationOnMock -> {
      restProducerInvocation.restOperationMeta = restOperationMeta;
      return null;
    }).when(restProducerInvocation).findRestOperation();
    Mockito.doAnswer(invocationOnMock -> {
      scheduleInvocation = true;
      return null;
    }).when(restProducerInvocation).scheduleInvocation();

    requestEx = new AbstractHttpServletRequest() {
    };
    restProducerInvocation.invoke(transport, requestEx, responseEx, httpServerFilters);

    Assertions.assertTrue(scheduleInvocation);
    Assertions.assertSame(requestEx, requestEx.getAttribute(RestConst.REST_REQUEST));
  }

  @Test
  public void findRestOperationNameFromRegistry() {
    Microservice microservice = new Microservice();
    microservice.setServiceName("ms");

    try (MockedStatic<ServicePathManager> managerMockedStatic = Mockito.mockStatic(ServicePathManager.class)){
      managerMockedStatic.when(() -> ServicePathManager.getServicePathManager(microserviceMeta)).thenReturn(null);
      restProducerInvocation = new RestProducerInvocation();
      initRestProducerInvocation();

      Exception exception = Assertions.assertThrows(Exception.class,
              () -> restProducerInvocation.findRestOperation());
      Assertions.assertTrue(exception.getMessage().contains("[message=Not Found]"));
    }
  }

  @Test
  public void findRestOperationNormal() {
    requestEx = new AbstractHttpServletRequest() {
      @Override
      public String getRequestURI() {
        return "/path";
      }

      @Override
      public String getMethod() {
        return "GET";
      }

      @Override
      public String getHeader(String name) {
        return "ms";
      }
    };
    Map<String, String> pathVars = new HashMap<>();
    try (MockedStatic<ServicePathManager> managerMockedStatic = Mockito.mockStatic(ServicePathManager.class)){
      ServicePathManager servicePathManager = Mockito.mock(ServicePathManager.class);
      OperationLocator locator = Mockito.mock(OperationLocator.class);
      managerMockedStatic.when(() -> ServicePathManager.getServicePathManager(microserviceMeta)).thenReturn(servicePathManager);
      Mockito.when(servicePathManager.producerLocateOperation(Mockito.any(), Mockito.any())).thenReturn(locator);
      Mockito.when(locator.getPathVarMap()).thenReturn(pathVars);
      Mockito.when(locator.getOperation()).thenReturn(restOperationMeta);
      restProducerInvocation = new RestProducerInvocation();
      initRestProducerInvocation();

      restProducerInvocation.findRestOperation();
      Assertions.assertSame(restOperationMeta, restProducerInvocation.restOperationMeta);
      Assertions.assertSame(pathVars, requestEx.getAttribute(RestConst.PATH_PARAMETERS));
    }
  }
}
