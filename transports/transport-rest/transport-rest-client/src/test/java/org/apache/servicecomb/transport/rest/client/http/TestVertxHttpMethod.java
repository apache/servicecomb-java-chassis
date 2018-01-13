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

package org.apache.servicecomb.transport.rest.client.http;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessor;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.definition.path.URLPathBuilder;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClientWithContext;
import org.apache.servicecomb.serviceregistry.api.Const;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestVertxHttpMethod extends VertxHttpMethod {

  HttpClientRequest request;

  @Before
  public void setup() {
    request = mock(HttpClientRequest.class);
  }

  @Test
  public void testDoMethodNullPointerException(@Mocked HttpClient httpClient) throws Exception {
    Context context = new MockUp<Context>() {
      @Mock
      public void runOnContext(Handler<Void> action) {
        action.handle(null);
      }
    }.getMockInstance();
    HttpClientWithContext httpClientWithContext = new HttpClientWithContext(httpClient, context);

    Invocation invocation = mock(Invocation.class);
    AsyncResponse asyncResp = mock(AsyncResponse.class);

    try {
      this.doMethod(httpClientWithContext, invocation, asyncResp);
      fail("Expect to throw NullPointerException, but got none");
    } catch (NullPointerException e) {
    }
  }

  @Test
  public void testDoMethod(@Mocked HttpClient httpClient, @Injectable URIEndpointObject address) throws Exception {
    Context context = new MockUp<Context>() {
      @Mock
      public void runOnContext(Handler<Void> action) {
        action.handle(null);
      }
    }.getMockInstance();
    HttpClientWithContext httpClientWithContext = new HttpClientWithContext(httpClient, context);

    Invocation invocation = mock(Invocation.class);
    AsyncResponse asyncResp = mock(AsyncResponse.class);
    OperationMeta operationMeta = mock(OperationMeta.class);
    RestOperationMeta swaggerRestOperation = mock(RestOperationMeta.class);

    Endpoint endpoint = mock(Endpoint.class);
    when(invocation.getOperationMeta()).thenReturn(operationMeta);
    URLPathBuilder urlPathBuilder = mock(URLPathBuilder.class);
    when(swaggerRestOperation.getPathBuilder()).thenReturn(urlPathBuilder);
    operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
    when(operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION)).thenReturn(swaggerRestOperation);
    when(invocation.getEndpoint()).thenReturn(endpoint);
    when(endpoint.getAddress()).thenReturn(address);

    when(request.exceptionHandler(Mockito.any())).then(answer -> null);

    this.doMethod(httpClientWithContext, invocation, asyncResp);
    Assert.assertTrue(true);
  }

  @Test
  public void testCreateRequest() {
    HttpClient client = mock(HttpClient.class);
    Invocation invocation = mock(Invocation.class);
    OperationMeta operationMeta = mock(OperationMeta.class);
    Endpoint endpoint = mock(Endpoint.class);
    URIEndpointObject address = mock(URIEndpointObject.class);
    when(invocation.getEndpoint()).thenReturn(endpoint);
    when(endpoint.getAddress()).thenReturn(address);
    when(address.isSslEnabled()).thenReturn(false);
    when(invocation.getOperationMeta()).thenReturn(operationMeta);
    RestOperationMeta swaggerRestOperation = mock(RestOperationMeta.class);
    when(operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION)).thenReturn(swaggerRestOperation);
    IpPort ipPort = mock(IpPort.class);
    when(ipPort.getPort()).thenReturn(10);
    when(ipPort.getHostOrIp()).thenReturn("ever");
    AsyncResponse asyncResp = mock(AsyncResponse.class);
    List<HttpMethod> methods = new ArrayList<>(
        Arrays.asList(HttpMethod.GET, HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE, HttpMethod.PATCH));
    for (HttpMethod method : methods) {
      when(swaggerRestOperation.getHttpMethod()).thenReturn(method.toString());
      HttpClientRequest obj =
          VertxHttpMethod.INSTANCE.createRequest(client, invocation, ipPort, "good", asyncResp);
      Assert.assertNull(obj);
    }
  }

  @Test
  public void testSetCseContext() {
    boolean status = false;
    try {
      Invocation invocation = mock(Invocation.class);
      HttpClientResponse httpResponse = mock(HttpClientResponse.class);
      OperationMeta operationMeta = mock(OperationMeta.class);
      RestOperationMeta swaggerRestOperation = mock(RestOperationMeta.class);
      HttpClientRequest request = mock(HttpClientRequest.class);

      Endpoint endpoint = mock(Endpoint.class);
      when(invocation.getOperationMeta()).thenReturn(operationMeta);
      URLPathBuilder urlPathBuilder = mock(URLPathBuilder.class);
      when(swaggerRestOperation.getPathBuilder()).thenReturn(urlPathBuilder);
      operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
      when(operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION)).thenReturn(swaggerRestOperation);
      when(invocation.getEndpoint()).thenReturn(endpoint);
      String contentType = httpResponse.getHeader("Content-Type");
      ProduceProcessor produceProcessor = mock(ProduceProcessor.class);
      when(swaggerRestOperation.findProduceProcessor(contentType)).thenReturn(produceProcessor);
      this.setCseContext(invocation, request);
    } catch (Exception ex) {
      status = true;
    }
    Assert.assertFalse(status);
  }

  @Test
  public void testHandleResponse() {
    boolean status = false;
    try {
      Invocation invocation = mock(Invocation.class);
      AsyncResponse asyncResp = mock(AsyncResponse.class);
      HttpClientResponse httpResponse = mock(HttpClientResponse.class);
      OperationMeta operationMeta = mock(OperationMeta.class);
      RestOperationMeta swaggerRestOperation = mock(RestOperationMeta.class);

      Endpoint endpoint = mock(Endpoint.class);
      when(invocation.getOperationMeta()).thenReturn(operationMeta);
      URLPathBuilder urlPathBuilder = mock(URLPathBuilder.class);
      when(swaggerRestOperation.getPathBuilder()).thenReturn(urlPathBuilder);
      operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
      when(operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION)).thenReturn(swaggerRestOperation);
      when(invocation.getEndpoint()).thenReturn(endpoint);

      String contentType = httpResponse.getHeader("Content-Type");
      ProduceProcessor produceProcessor = mock(ProduceProcessor.class);
      when(swaggerRestOperation.findProduceProcessor(contentType)).thenReturn(produceProcessor);
      this.handleResponse(invocation, httpResponse, asyncResp);
    } catch (Exception ex) {
      status = true;
    }
    Assert.assertFalse(status);
  }

  @Override
  protected HttpClientRequest createRequest(HttpClient client, Invocation invocation, IpPort ipPort, String path,
      AsyncResponse asyncResp) {
    return request;
  }

  @Test
  public void testCreateRequestPathNoUrlPrefixNoPath(@Injectable Invocation invocation,
      @Injectable RestOperationMeta swaggerRestOperation, @Injectable Endpoint endpoint,
      @Injectable URIEndpointObject address, @Injectable URLPathBuilder builder) throws Exception {
    new Expectations() {
      {
        endpoint.getAddress();
        result = address;
        builder.createRequestPath((Object[]) any);
        result = "/path";
      }
    };
    String path = this.createRequestPath(invocation, swaggerRestOperation);
    Assert.assertEquals("/path", path);
  }

  @Test
  public void testCreateRequestPathNoUrlPrefixHavePath(@Injectable Invocation invocation,
      @Injectable RestOperationMeta swaggerRestOperation, @Injectable Endpoint endpoint,
      @Injectable URIEndpointObject address, @Injectable URLPathBuilder builder) throws Exception {
    Map<String, Object> contextMap = new HashMap<>();
    contextMap.put(RestConst.REST_CLIENT_REQUEST_PATH, "/client/path");

    new Expectations() {
      {
        endpoint.getAddress();
        result = address;
        invocation.getHandlerContext();
        result = contextMap;
      }
    };
    String path = this.createRequestPath(invocation, swaggerRestOperation);
    Assert.assertEquals("/client/path", path);
  }

  @Test
  public void testCreateRequestPathHaveUrlPrefixNoPath(@Injectable Invocation invocation,
      @Injectable RestOperationMeta swaggerRestOperation, @Injectable Endpoint endpoint,
      @Injectable URIEndpointObject address, @Injectable URLPathBuilder builder) throws Exception {
    new Expectations() {
      {
        endpoint.getAddress();
        result = address;
        address.getFirst(Const.URL_PREFIX);
        result = "/root";
        builder.createRequestPath((Object[]) any);
        result = "/path";
      }
    };
    String path = this.createRequestPath(invocation, swaggerRestOperation);
    Assert.assertEquals("/root/path", path);
  }

  @Test
  public void testCreateRequestPathHaveUrlPrefixHavePath(@Injectable Invocation invocation,
      @Injectable RestOperationMeta swaggerRestOperation, @Injectable Endpoint endpoint,
      @Injectable URIEndpointObject address, @Injectable URLPathBuilder builder) throws Exception {
    Map<String, Object> contextMap = new HashMap<>();
    contextMap.put(RestConst.REST_CLIENT_REQUEST_PATH, "/client/path");

    new Expectations() {
      {
        endpoint.getAddress();
        result = address;
        address.getFirst(Const.URL_PREFIX);
        result = "/root";
        invocation.getHandlerContext();
        result = contextMap;
      }
    };
    String path = this.createRequestPath(invocation, swaggerRestOperation);
    Assert.assertEquals("/root/client/path", path);
  }

  @Test
  public void testCreateRequestPathHaveUrlPrefixHavePathAndStartWith(@Injectable Invocation invocation,
      @Injectable RestOperationMeta swaggerRestOperation, @Injectable Endpoint endpoint,
      @Injectable URIEndpointObject address, @Injectable URLPathBuilder builder) throws Exception {
    Map<String, Object> contextMap = new HashMap<>();
    contextMap.put(RestConst.REST_CLIENT_REQUEST_PATH, "/client/path");

    new Expectations() {
      {
        endpoint.getAddress();
        result = address;
        address.getFirst(Const.URL_PREFIX);
        result = "/client";
        invocation.getHandlerContext();
        result = contextMap;
      }
    };
    String path = this.createRequestPath(invocation, swaggerRestOperation);
    Assert.assertEquals("/client/path", path);
  }
}
