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

package org.apache.servicecomb.transport.highway;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.apache.servicecomb.core.SCBEngine.CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC;
import static org.apache.servicecomb.core.SCBEngine.DEFAULT_TURN_DOWN_STATUS_WAIT_SEC;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.servicecomb.codec.protobuf.definition.OperationProtobuf;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.SCBStatus;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.core.invocation.InvocationFactory;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.test.scaffolding.exception.RuntimeExceptionWithoutStackTrace;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.transport.highway.message.RequestHeader;
import org.apache.servicecomb.transport.highway.message.ResponseHeader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.impl.FileResolverImpl;
import io.vertx.core.json.Json;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;

public class HighwayServerCodecFilterTest {
  HighwayServerCodecFilter codecFilter = new HighwayServerCodecFilter();

  Invocation invocation;

  @Mocked
  Endpoint endpoint;

  @Mocked
  OperationMeta operationMeta;

  @Mocked
  HighwayTransportContext transportContext;

  MultiMap headers = MultiMap.caseInsensitiveMultiMap();

  FilterNode nextNode = new FilterNode((invocation, next) -> {
    Response response = Response.ok("ok");
    response.setHeaders(headers);
    return CompletableFuture.completedFuture(response);
  });

  static SCBEngine engine;

  Environment environment = Mockito.mock(Environment.class);

  @Before
  public void setUp() {
    Mockito.when(environment.getProperty(CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC,
        long.class, DEFAULT_TURN_DOWN_STATUS_WAIT_SEC)).thenReturn(DEFAULT_TURN_DOWN_STATUS_WAIT_SEC);
    Mockito.when(environment.getProperty("servicecomb.transport.eventloop.size", int.class, -1))
        .thenReturn(-1);
    Mockito.when(environment.getProperty(FileResolverImpl.DISABLE_CP_RESOLVING_PROP_NAME, boolean.class, true))
        .thenReturn(true);
    LegacyPropertyFactory.setEnvironment(environment);

    engine = SCBBootstrap.createSCBEngineForTest(environment);
    engine.setStatus(SCBStatus.UP);
    invocation = InvocationFactory.forProvider(endpoint, operationMeta, null);
  }

  @After
  public void tearDown() {
    engine.destroy();
  }

  private void mockDecodeRequestFail() throws Exception {
    new Expectations(invocation) {
      {
        invocation.getTransportContext();
        result = transportContext;
      }
    };
    new Expectations(HighwayCodec.class) {
      {
        HighwayCodec.decodeRequest(invocation, (RequestHeader) any, (OperationProtobuf) any, (Buffer) any);
        result = new RuntimeExceptionWithoutStackTrace("encode request failed");
      }
    };
  }

  @Test
  public void should_not_invoke_filter_when_decode_request_failed(@Mocked FilterNode nextNode) throws Exception {
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
      throws Exception {
    mockDecodeRequestFail();

    Response response = codecFilter.onFilter(invocation, nextNode).get();

    assertThat(response.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
    assertThat(Json.encode(response.getResult()))
        .isEqualTo("{\"code\":\"SCB.50000000\",\"message\":\"Unexpected "
            + "exception when processing null. encode request failed\"}");
  }

  private void success_invocation() throws InterruptedException, ExecutionException {
    new Expectations(invocation) {
      {
        invocation.getTransportContext();
        result = transportContext;
      }
    };

    codecFilter.onFilter(invocation, nextNode).get();
  }

  @Test
  public void should_encode_response_header(@Mocked ResponseHeader responseHeader)
      throws ExecutionException, InterruptedException {
    success_invocation();

    new Verifications() {
      {
        MultiMap captureHeaders;
        responseHeader.fromMultiMap(captureHeaders = withCapture());
        assertThat(captureHeaders).isSameAs(headers);
      }
    };
  }

  @Test
  public void should_encode_response() throws ExecutionException, InterruptedException {
    success_invocation();

    new Verifications() {
      {
        Buffer captureBuffer;
        transportContext.setResponseBuffer(captureBuffer = withCapture());
        assertThat(captureBuffer).isNotNull();
      }
    };
  }
}
