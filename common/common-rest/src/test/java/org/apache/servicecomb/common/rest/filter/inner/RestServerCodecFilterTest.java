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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.databind.JavaType;
import org.apache.servicecomb.common.rest.HttpTransportContext;
import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.codec.produce.ProduceJsonProcessor;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.SCBStatus;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.core.definition.InvocationRuntimeType;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.core.invocation.InvocationFactory;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.test.scaffolding.exception.RuntimeExceptionWithoutStackTrace;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.Response;

import com.fasterxml.jackson.databind.type.TypeFactory;

import io.vertx.core.MultiMap;
import io.vertx.core.json.Json;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.servlet.http.Part;

public class RestServerCodecFilterTest {
  final RestServerCodecFilter codecFilter = new RestServerCodecFilter();

  Invocation invocation;

  final Endpoint endpoint = Mockito.mock(Endpoint.class);

  final OperationMeta operationMeta = Mockito.mock(OperationMeta.class);

  final SchemaMeta schemaMeta = Mockito.mock(SchemaMeta.class);

  final MicroserviceMeta microserviceMeta = Mockito.mock(MicroserviceMeta.class);

  final InvocationRuntimeType invocationRuntimeType = Mockito.mock(InvocationRuntimeType.class);

  final RestOperationMeta restOperationMeta = Mockito.mock(RestOperationMeta.class);

  final HttpTransportContext transportContext = Mockito.mock(HttpTransportContext.class);

  final HttpServletResponseEx responseEx = Mockito.mock(HttpServletResponseEx.class);

  final MultiMap headers = MultiMap.caseInsensitiveMultiMap();

  final FilterNode nextNode = new FilterNode((invocation, next) -> {
    Response response = Response.ok("ok");
    response.setHeaders(headers);
    return CompletableFuture.completedFuture(response);
  });

  static SCBEngine engine;

  @BeforeAll
  public static void beforeClass() {
    ArchaiusUtils.resetConfig();
    ConfigUtil.installDynamicConfig();

    engine = SCBBootstrap.createSCBEngineForTest();
    engine.setStatus(SCBStatus.UP);
  }

  @AfterAll
  public static void afterClass() {
    engine.destroy();
    ArchaiusUtils.resetConfig();
  }

  @BeforeEach
  public void setUp() {
    Mockito.when(operationMeta.getSchemaMeta()).thenReturn(schemaMeta);
    Mockito.when(schemaMeta.getMicroserviceMeta()).thenReturn(microserviceMeta);
    Mockito.when(microserviceMeta.getHandlerChain()).thenReturn(new ArrayList<>());
    Mockito.when(operationMeta.buildBaseProviderRuntimeType()).thenReturn(invocationRuntimeType);
    Mockito.when(transportContext.getProduceProcessor()).thenReturn(Mockito.mock(ProduceJsonProcessor.class));
    invocation = Mockito.spy(InvocationFactory.forProvider(endpoint, operationMeta, null));
  }

  private void mockDecodeRequestFail() {
    Mockito.when(invocation.getTransportContext()).thenReturn(transportContext);
    Mockito.when(transportContext.getResponseEx()).thenReturn(responseEx);
    Mockito.when(invocation.getRequestEx()).thenThrow(new RuntimeExceptionWithoutStackTrace("mock encode request failed"));
  }

  @Test
  public void should_not_invoke_filter_when_decode_request_failed() {
    FilterNode nextNode = Mockito.mock(FilterNode.class);
    mockDecodeRequestFail();

    codecFilter.onFilter(invocation, nextNode);

    Mockito.verify(nextNode, Mockito.times(0)).onFilter(invocation);
  }

  @Test
  public void should_convert_exception_to_response_when_decode_request_failed()
      throws ExecutionException, InterruptedException {
    mockDecodeRequestFail();
    Mockito.when(invocation.findResponseType(INTERNAL_SERVER_ERROR.getStatusCode()))
            .thenReturn(TypeFactory.defaultInstance().constructType(String.class));

    Response response = codecFilter.onFilter(invocation, nextNode).get();

    assertThat(response.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
    assertThat(Json.encode(response.getResult())).
            isIn("{\"code\":\"SCB.50000000\",\"message\":\"mock encode request failed\"}",
                    "{\"message\":\"mock encode request failed\",\"code\":\"SCB.50000000\"}");
  }

  private void success_invocation() throws InterruptedException, ExecutionException {
    Mockito.when(invocation.getTransportContext()).thenReturn(transportContext);
    Mockito.when(operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION)).thenReturn(restOperationMeta);
    Mockito.when(invocation.findResponseType(INTERNAL_SERVER_ERROR.getStatusCode())).thenReturn(TypeFactory.defaultInstance().constructType(String.class));
    JavaType javaType = Mockito.mock(JavaType.class);
    Mockito.when(invocationRuntimeType.findResponseType(200)).thenReturn(javaType);
    Mockito.when(javaType.getRawClass()).thenAnswer(invocationOnMock -> Part.class);
    Mockito.when(invocation.getTransportContext()).thenReturn(transportContext);
    Mockito.when(transportContext.getResponseEx()).thenReturn(responseEx);

    codecFilter.onFilter(invocation, nextNode).get();
  }

  @Test
  public void should_encode_response_header() throws ExecutionException, InterruptedException {
    headers.add("h1", "v1");
    success_invocation();

   Mockito.verify(responseEx).addHeader("h1", "v1");
  }

  @Test
  public void should_not_encode_content_length_header() throws ExecutionException, InterruptedException {
    headers.add("h1", "v1")
        .add("h2", "v2")
        .add(CONTENT_LENGTH, "10");
    success_invocation();

    Mockito.verify(responseEx, Mockito.times(1)).addHeader("h1", "v1");
    Mockito.verify(responseEx, Mockito.times(1)).addHeader("h2", "v2");
  }

  @Test
  public void should_not_encode_transfer_encoding_header() throws ExecutionException, InterruptedException {
    headers.add("h1", "v1")
        .add("h2", "v2")
        .add(TRANSFER_ENCODING, "test");
    success_invocation();

    Mockito.verify(responseEx, Mockito.times(1)).addHeader("h1", "v1");
    Mockito.verify(responseEx, Mockito.times(1)).addHeader("h2", "v2");
  }
}
