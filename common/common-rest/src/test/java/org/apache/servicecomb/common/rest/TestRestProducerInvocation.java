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
import org.junit.AfterClass;
import org.junit.jupiter.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestRestProducerInvocation {
  @Mocked
  Transport transport;

  @Mocked
  HttpServletRequestEx requestEx;

  @Mocked
  HttpServletResponseEx responseEx;

  RestProducerInvocation restProducerInvocation;

  Throwable throwableOfSendFailResponse;

  boolean scheduleInvocation;

  static List<HttpServerFilter> httpServerFilters = SPIServiceUtils.getSortedService(HttpServerFilter.class);

  static SCBEngine scbEngine;

  static OperationMeta operationMeta;

  static RestOperationMeta restOperationMeta;

  static MicroserviceMeta microserviceMeta;

  @BeforeClass
  public static void classSetup() {
    ConfigUtil.installDynamicConfig();
    scbEngine = SCBBootstrap.createSCBEngineForTest()
        .addProducerMeta("sid1", new TestPathSchema())
        .run();
    operationMeta = scbEngine.getProducerMicroserviceMeta().operationMetas().get("test.sid1.dynamicId");
    restOperationMeta = RestMetaUtils.getRestOperationMeta(operationMeta);
    microserviceMeta = operationMeta.getMicroserviceMeta();
  }

  @AfterClass
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
    restProducerInvocation = new MockUp<RestProducerInvocation>() {
      @Mock
      void sendFailResponse(Throwable throwable) {
        throwableOfSendFailResponse = throwable;
      }

      @Mock
      void findRestOperation() {
        throw expected;
      }

      @Mock
      void scheduleInvocation() {
        throw new IllegalStateException("must not invoke scheduleInvocation");
      }
    }.getMockInstance();

    restProducerInvocation.invoke(transport, requestEx, responseEx, httpServerFilters);

    Assertions.assertSame(expected, throwableOfSendFailResponse);
  }

  @Test
  public void invokeNormal() {
    restProducerInvocation = new MockUp<RestProducerInvocation>() {
      @Mock
      void findRestOperation() {
        restProducerInvocation.restOperationMeta = restOperationMeta;
      }

      @Mock
      void scheduleInvocation() {
        scheduleInvocation = true;
      }
    }.getMockInstance();

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

    new Expectations(ServicePathManager.class) {
      {
        //just make the method throw Exception
        ServicePathManager.getServicePathManager(microserviceMeta);
        result = null;
      }
    };
    restProducerInvocation = new RestProducerInvocation();
    initRestProducerInvocation();

    Exception exception = Assertions.assertThrows(Exception.class,
            () -> restProducerInvocation.findRestOperation());
    Assertions.assertTrue(exception.getMessage().contains("[message=Not Found]"));
  }

  @Test
  public void findRestOperationNormal(@Mocked ServicePathManager servicePathManager,
      @Mocked OperationLocator locator) {
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
    new Expectations(ServicePathManager.class) {
      {
        ServicePathManager.getServicePathManager(microserviceMeta);
        result = servicePathManager;
        servicePathManager.producerLocateOperation(anyString, anyString);
        result = locator;
        locator.getPathVarMap();
        result = pathVars;
        locator.getOperation();
        result = restOperationMeta;
      }
    };
    restProducerInvocation = new RestProducerInvocation();
    initRestProducerInvocation();

    restProducerInvocation.findRestOperation();
    Assertions.assertSame(restOperationMeta, restProducerInvocation.restOperationMeta);
    Assertions.assertSame(pathVars, requestEx.getAttribute(RestConst.PATH_PARAMETERS));
  }
}
