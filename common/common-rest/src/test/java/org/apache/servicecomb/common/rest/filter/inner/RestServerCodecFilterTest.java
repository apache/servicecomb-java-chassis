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

package org.apache.servicecomb.common.rest.filter.inner;

import static com.google.common.net.HttpHeaders.CONTENT_LENGTH;
import static com.google.common.net.HttpHeaders.TRANSFER_ENCODING;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.servicecomb.common.rest.HttpTransportContext;
import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.SCBStatus;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.core.invocation.InvocationFactory;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.test.scaffolding.exception.RuntimeExceptionWithoutStackTrace;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.type.TypeFactory;

import io.vertx.core.MultiMap;
import io.vertx.core.json.Json;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;

public class RestServerCodecFilterTest {
  RestServerCodecFilter codecFilter = new RestServerCodecFilter();

  Invocation invocation;

  @Mocked
  Endpoint endpoint;

  @Mocked
  OperationMeta operationMeta;

  @Mocked
  RestOperationMeta restOperationMeta;

  @Mocked
  HttpTransportContext transportContext;

  @Mocked
  HttpServletResponseEx responseEx;

  MultiMap headers = MultiMap.caseInsensitiveMultiMap();

  FilterNode nextNode = new FilterNode((invocation, next) -> {
    Response response = Response.ok("ok");
    response.setHeaders(headers);
    return CompletableFuture.completedFuture(response);
  });

  static SCBEngine engine;

  @BeforeClass
  public static void beforeClass() {
    ArchaiusUtils.resetConfig();
    ConfigUtil.installDynamicConfig();

    engine = SCBBootstrap.createSCBEngineForTest();
    engine.setStatus(SCBStatus.UP);
  }

  @AfterClass
  public static void afterClass() {
    engine.destroy();
    ArchaiusUtils.resetConfig();
  }

  @Before
  public void setUp() {
    invocation = InvocationFactory.forProvider(endpoint, operationMeta, null);
  }

  private void mockDecodeRequestFail() {
    new Expectations(invocation) {
      {
        invocation.getTransportContext();
        result = transportContext;

        transportContext.getRequestEx();
        result = new RuntimeExceptionWithoutStackTrace("encode request failed");
      }
    };
  }

  @Test
  public void should_not_invoke_filter_when_decode_request_failed(@Mocked FilterNode nextNode) {
    mockDecodeRequestFail();

    codecFilter.onFilter(invocation, nextNode);

    new Verifications() {
      {
        nextNode.onFilter(invocation);
        times = 0;
      }
    };
  }

  @Test
  public void should_convert_exception_to_response_when_decode_request_failed()
      throws ExecutionException, InterruptedException {
    mockDecodeRequestFail();
    new Expectations(invocation) {
      {
        invocation.findResponseType(anyInt);
        result = TypeFactory.defaultInstance().constructType(String.class);
      }
    };

    Response response = codecFilter.onFilter(invocation, nextNode).get();

    assertThat(response.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
    assertThat(Json.encode(response.getResult()))
        .isEqualTo("{\"code\":\"SCB.50000000\",\"message\":\"encode request failed\"}");
  }

  private void success_invocation() throws InterruptedException, ExecutionException {
    new Expectations(invocation) {
      {
        invocation.getTransportContext();
        result = transportContext;

        operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
        result = restOperationMeta;

        invocation.findResponseType(anyInt);
        result = TypeFactory.defaultInstance().constructType(String.class);
      }
    };

    codecFilter.onFilter(invocation, nextNode).get();
  }

  @Test
  public void should_encode_response_header() throws ExecutionException, InterruptedException {
    headers.add("h1", "v1");
    success_invocation();

    new Verifications() {
      {
        String name;
        String value;
        responseEx.addHeader(name = withCapture(), value = withCapture());
        assertThat(name).isEqualTo("h1");
        assertThat(value).isEqualTo("v1");
      }
    };
  }

  @Test
  public void should_not_encode_content_length_header() throws ExecutionException, InterruptedException {
    headers.add("h1", "v1")
        .add("h2", "v2")
        .add(CONTENT_LENGTH, "10");
    success_invocation();

    new Verifications() {
      {
        List<String> names = new ArrayList<>();
        List<String> values = new ArrayList<>();
        responseEx.addHeader(withCapture(names), withCapture(values));
        assertThat(names).containsExactly("h1", "h2");
        assertThat(values).containsExactly("v1", "v2");
      }
    };
  }

  @Test
  public void should_not_encode_transfer_encoding_header() throws ExecutionException, InterruptedException {
    headers.add("h1", "v1")
        .add("h2", "v2")
        .add(TRANSFER_ENCODING, "test");
    success_invocation();

    new Verifications() {
      {
        List<String> names = new ArrayList<>();
        List<String> values = new ArrayList<>();
        responseEx.addHeader(withCapture(names), withCapture(values));
        assertThat(names).containsExactly("h1", "h2");
        assertThat(values).containsExactly("v1", "v2");
      }
    };
  }
}