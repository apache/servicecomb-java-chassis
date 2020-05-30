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

import static javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import javax.ws.rs.core.HttpHeaders;

import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.locator.ServicePathManager;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.SCBStatus;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Verifications;

public class RestProducerInvocationCreatorTest {
  @Injectable
  RoutingContext routingContext;

  @Injectable
  MicroserviceMeta microserviceMeta;

  @Injectable
  ServicePathManager servicePathManager;

  @Mocked
  RestOperationMeta restOperationMeta;

  @Injectable
  Endpoint endpoint;

  @Injectable
  HttpServletRequestEx requestEx;

  @Injectable
  HttpServletResponseEx responseEx;

  RestProducerInvocationCreator creator;

  static SCBEngine engine;

  @BeforeClass
  public static void beforeClass() throws Exception {
    ArchaiusUtils.resetConfig();
    ConfigUtil.installDynamicConfig();

    engine = SCBBootstrap.createSCBEngineForTest();
    engine.setStatus(SCBStatus.UP);
  }

  @AfterClass
  public static void afterClass() throws Exception {
    engine.destroy();
    ArchaiusUtils.resetConfig();
  }

  @Before
  public void setUp() {
    creator = new RestVertxProducerInvocationCreator(routingContext, microserviceMeta, endpoint,
        requestEx, responseEx);
  }

  private void mockGetServicePathManager() {
    mockGetServicePathManager(servicePathManager);
  }

  private void mockGetServicePathManager(final ServicePathManager servicePathManager) {
    new Expectations(ServicePathManager.class) {
      {
        ServicePathManager.getServicePathManager(microserviceMeta);
        result = servicePathManager;
      }
    };
  }

  @Test
  public void should_failed_when_not_defined_any_schema() {
    mockGetServicePathManager(null);

    InvocationException throwable = (InvocationException) catchThrowable(() -> creator.create());
    CommonExceptionData data = (CommonExceptionData) throwable.getErrorData();

    assertThat(throwable.getStatusCode()).isEqualTo(NOT_FOUND.getStatusCode());
    assertThat(Json.encode(data)).isEqualTo("{\"code\":\"SCB.0002\",\"message\":\"Not Found\"}");
  }

  @Test
  public void should_failed_when_accept_is_not_support() {
    mockGetServicePathManager();
    new Expectations() {
      {
        requestEx.getHeader(HttpHeaders.ACCEPT);
        result = "test-type";

        restOperationMeta.ensureFindProduceProcessor(requestEx);
        result = null;
      }
    };

    InvocationException throwable = (InvocationException) catchThrowable(() -> creator.create());
    CommonExceptionData data = (CommonExceptionData) throwable.getErrorData();

    assertThat(throwable.getStatusCode()).isEqualTo(NOT_ACCEPTABLE.getStatusCode());
    assertThat(Json.encode(data))
        .isEqualTo("{\"code\":\"SCB.0000\",\"message\":\"Accept test-type is not supported\"}");
  }

  @Test
  public void should_save_requestEx_in_invocation_context() {
    mockGetServicePathManager();

    Invocation invocation = creator.create();

    Object request = invocation.getLocalContext(RestConst.REST_REQUEST);
    assertThat(request).isSameAs(requestEx);
  }

  @Test
  public void should_save_path_var_map_in_requestEx() {
    mockGetServicePathManager();

    creator.create();

    new Verifications() {
      {
        requestEx.setAttribute(RestConst.PATH_PARAMETERS, any);
        times = 1;
      }
    };
  }

  @Test
  public void should_merge_invocation_context_from_request() {
    mockGetServicePathManager();
    new Expectations() {
      {
        requestEx.getHeader(Const.CSE_CONTEXT);
        result = "{\"k\":\"v\"}";
      }
    };

    Invocation invocation = creator.create();

    assertThat(invocation.getContext("k")).isEqualTo("v");
  }
}