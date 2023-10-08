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
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.apache.servicecomb.core.SCBEngine.CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC;
import static org.apache.servicecomb.core.SCBEngine.DEFAULT_TURN_DOWN_STATUS_WAIT_SEC;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.servicecomb.common.rest.HttpTransportContext;
import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
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
import org.apache.servicecomb.foundation.test.scaffolding.exception.RuntimeExceptionWithoutStackTrace;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.vertx.core.MultiMap;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.core.MediaType;

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
    engine = SCBBootstrap.createSCBEngineForTest();
    Environment environment = Mockito.mock(Environment.class);
    engine.setEnvironment(environment);
    Mockito.when(environment.getProperty(CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC,
        long.class, DEFAULT_TURN_DOWN_STATUS_WAIT_SEC)).thenReturn(DEFAULT_TURN_DOWN_STATUS_WAIT_SEC);
    engine.setStatus(SCBStatus.UP);
  }

  @AfterAll
  public static void afterClass() {
    engine.destroy();
  }

  @BeforeEach
  public void setUp() {
    Mockito.when(operationMeta.getSchemaMeta()).thenReturn(schemaMeta);
    Mockito.when(schemaMeta.getMicroserviceMeta()).thenReturn(microserviceMeta);
    Mockito.when(operationMeta.buildBaseProviderRuntimeType()).thenReturn(invocationRuntimeType);
    invocation = Mockito.spy(InvocationFactory.forProvider(endpoint, operationMeta, null));
  }

  private void mockDecodeRequestFail() {
    Mockito.when(invocation.getTransportContext()).thenReturn(transportContext);
    Mockito.when(transportContext.getResponseEx()).thenReturn(responseEx);
    Mockito.when(invocation.getRequestEx())
        .thenThrow(new RuntimeExceptionWithoutStackTrace("mock encode request failed"));
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

    Assertions.assertThrows(ExecutionException.class,
        () -> codecFilter.onFilter(invocation, nextNode).get());
  }

  private void success_invocation() throws InterruptedException, ExecutionException {
    Mockito.when(invocation.getTransportContext()).thenReturn(transportContext);
    HttpServletRequestEx requestExt = Mockito.mock(HttpServletRequestEx.class);
    Mockito.when(invocation.getRequestEx()).thenReturn(requestExt);
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
    Operation swaggerOperation = Mockito.mock(Operation.class);
    Mockito.when(operationMeta.getSwaggerOperation()).thenReturn(swaggerOperation);
    ApiResponses apiResponses = Mockito.mock(ApiResponses.class);
    Mockito.when(swaggerOperation.getResponses()).thenReturn(apiResponses);
    ApiResponse apiResponse = Mockito.mock(ApiResponse.class);
    Mockito.when(apiResponses.get("200")).thenReturn(apiResponse);
    Content content = new Content();
    content.addMediaType(MediaType.APPLICATION_JSON, new io.swagger.v3.oas.models.media.MediaType());
    content.get(MediaType.APPLICATION_JSON).setSchema(new StringSchema());
    Mockito.when(apiResponse.getContent()).thenReturn(content);
    Mockito.when(operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION)).thenReturn(restOperationMeta);
    Mockito.when(invocation.findResponseType(INTERNAL_SERVER_ERROR.getStatusCode()))
        .thenReturn(TypeFactory.defaultInstance().constructType(String.class));
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
