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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.common.rest.locator.OperationLocator;
import org.apache.servicecomb.common.rest.locator.ServicePathManager;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.foundation.vertx.http.AbstractHttpServletRequest;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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

  @Mocked
  RestOperationMeta restOperationMeta;

  @Mocked
  MicroserviceMeta microserviceMeta;

  List<HttpServerFilter> httpServerFilters = Collections.emptyList();

  RestProducerInvocation restProducerInvocation;

  Throwable throwableOfSendFailResponse;

  boolean scheduleInvocation;

  boolean runOnExecutor;

  boolean invokeNoParam;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private void initRestProducerInvocation() {
    restProducerInvocation.transport = transport;
    restProducerInvocation.requestEx = requestEx;
    restProducerInvocation.responseEx = responseEx;
    restProducerInvocation.restOperationMeta = restOperationMeta;
    restProducerInvocation.httpServerFilters = httpServerFilters;
  }

  @Before
  public void setup() {
    SCBEngine.getInstance().setProducerMicroserviceMeta(microserviceMeta);
  }

  @After
  public void teardown() {
    SCBEngine.getInstance().setProducerMicroserviceMeta(null);
  }

  @Test
  public void invokeSendFail(@Mocked InvocationException expected) {
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

    Assert.assertSame(expected, throwableOfSendFailResponse);
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

    Assert.assertTrue(scheduleInvocation);
    Assert.assertSame(requestEx, requestEx.getAttribute(RestConst.REST_REQUEST));
  }

  @Test
  public void findRestOperationNameFromRegistry() {
    Microservice microservice = new Microservice();
    microservice.setServiceName("ms");

    new Expectations(RegistryUtils.class) {
      {
        requestEx.getHeader(Const.TARGET_MICROSERVICE);
        result = null;
        RegistryUtils.getMicroservice();
        result = microservice;
      }
    };
    new Expectations(ServicePathManager.class) {
      {
        //just make the method throw Exception
        ServicePathManager.getServicePathManager(microserviceMeta);
        result = null;
      }
    };
    restProducerInvocation = new RestProducerInvocation();
    initRestProducerInvocation();

    expectedException.expect(Exception.class);
    expectedException.expectMessage("[message=Not Found]");
    restProducerInvocation.findRestOperation();
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
    Assert.assertSame(restOperationMeta, restProducerInvocation.restOperationMeta);
    Assert.assertSame(pathVars, requestEx.getAttribute(RestConst.PATH_PARAMETERS));
  }
}
