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

package org.apache.servicecomb.transport.rest.vertx;

import org.apache.http.HttpHeaders;
import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import mockit.Mocked;

public class TestVertxRestDispatcher {
  @Mocked
  Router mainRouter;

  @Mocked
  TransportManager transportManager;

  VertxRestDispatcher dispatcher;

  Throwable throwable;

  Environment environment = Mockito.mock(Environment.class);

  @Before
  public void setUp() {
    LegacyPropertyFactory.setEnvironment(environment);
    Mockito.when(environment.getProperty(
            RestConst.UPLOAD_MAX_SIZE, long.class, -1L))
        .thenReturn(-1L);
    Mockito.when(environment.getProperty(RestConst.UPLOAD_MAX_FILE_SIZE, long.class, -1L))
        .thenReturn(-1L);
    Mockito.when(environment.getProperty(RestConst.UPLOAD_FILE_SIZE_THRESHOLD, int.class, 0))
        .thenReturn(0);

    dispatcher = new VertxRestDispatcher();
    dispatcher.init(mainRouter);

    SCBBootstrap.createSCBEngineForTest().setTransportManager(transportManager);
  }

  @After
  public void teardown() {
    SCBEngine.getInstance().destroy();
  }

  @Test
  public void getOrder() {
    Mockito.when(environment.getProperty(
            "servicecomb.http.dispatcher.rest.order", int.class, Integer.MAX_VALUE))
        .thenReturn(Integer.MAX_VALUE);
    Assertions.assertEquals(Integer.MAX_VALUE, dispatcher.getOrder());
  }

  @Test
  public void failureHandlerWithNoRestProducerInvocationAndInvocationException() {
    RoutingContext context = Mockito.mock(RoutingContext.class);
    Mockito.when(context.get(RestConst.REST_PRODUCER_INVOCATION)).thenReturn(null);
    InvocationException e = new InvocationException(Status.REQUEST_ENTITY_TOO_LARGE, "testMsg");
    ErrorDataDecoderException edde = new ErrorDataDecoderException(e);
    Mockito.when(context.failure()).thenReturn(edde);
    MockHttpServerResponse response = new MockHttpServerResponse();
    Mockito.when(context.response()).thenReturn(response);

    dispatcher.failureHandler(context);

    MatcherAssert.assertThat(response.responseHeader, Matchers.hasEntry(HttpHeaders.CONTENT_TYPE, MediaType.WILDCARD));
    MatcherAssert.assertThat(response.responseStatusCode, Matchers.is(Status.REQUEST_ENTITY_TOO_LARGE.getStatusCode()));
    MatcherAssert.assertThat(response.responseStatusMessage,
        Matchers.is(Status.REQUEST_ENTITY_TOO_LARGE.getReasonPhrase()));
    MatcherAssert.assertThat(response.responseChunk,
        Matchers.is("{\"message\":\"" + Status.REQUEST_ENTITY_TOO_LARGE.getReasonPhrase() + "\"}"));
    Assertions.assertTrue(response.responseEnded);
  }

  @Test
  public void failureHandlerWithNoRestProducerInvocationAndOtherException() {
    RoutingContext context = Mockito.mock(RoutingContext.class);
    Mockito.when(context.get(RestConst.REST_PRODUCER_INVOCATION)).thenReturn(null);
    String exceptionMessage = "Internal Server Error";
    Exception exception = new Exception(exceptionMessage);
    Mockito.when(context.failure()).thenReturn(exception);
    MockHttpServerResponse response = new MockHttpServerResponse();
    Mockito.when(context.response()).thenReturn(response);

    dispatcher.failureHandler(context);

    MatcherAssert.assertThat(response.responseHeader, Matchers.hasEntry(HttpHeaders.CONTENT_TYPE, MediaType.WILDCARD));
    MatcherAssert.assertThat(response.responseStatusCode, Matchers.is(Status.INTERNAL_SERVER_ERROR.getStatusCode()));
    MatcherAssert.assertThat(response.responseChunk,
        Matchers.is("{\"message\":\"" + exceptionMessage + "\"}"));
    Assertions.assertTrue(response.responseEnded);
  }

  @Test
  public void failureHandlerWithNoExceptionAndStatusCodeIsSet() {
    RoutingContext context = Mockito.mock(RoutingContext.class);
    Mockito.when(context.get(RestConst.REST_PRODUCER_INVOCATION)).thenReturn(null);
    Mockito.when(context.failure()).thenReturn(null);
    MockHttpServerResponse response = new MockHttpServerResponse();
    Mockito.when(context.response()).thenReturn(response);
    Mockito.when(context.statusCode()).thenReturn(Status.REQUEST_ENTITY_TOO_LARGE.getStatusCode());

    dispatcher.failureHandler(context);

    MatcherAssert.assertThat(response.responseHeader, Matchers.hasEntry(HttpHeaders.CONTENT_TYPE, MediaType.WILDCARD));
    MatcherAssert.assertThat(response.responseStatusCode, Matchers.is(Status.REQUEST_ENTITY_TOO_LARGE.getStatusCode()));
    Assertions.assertTrue(response.responseEnded);
  }

  @Test
  public void failureHandlerWithNoExceptionAndStatusCodeIsNotSet() {
    RoutingContext context = Mockito.mock(RoutingContext.class);
    Mockito.when(context.get(RestConst.REST_PRODUCER_INVOCATION)).thenReturn(null);
    Mockito.when(context.failure()).thenReturn(null);
    MockHttpServerResponse response = new MockHttpServerResponse();
    Mockito.when(context.response()).thenReturn(response);
    Mockito.when(context.statusCode()).thenReturn(Status.OK.getStatusCode());

    dispatcher.failureHandler(context);

    MatcherAssert.assertThat(response.responseHeader, Matchers.hasEntry(HttpHeaders.CONTENT_TYPE, MediaType.WILDCARD));
    MatcherAssert.assertThat(response.responseStatusCode, Matchers.is(Status.INTERNAL_SERVER_ERROR.getStatusCode()));
    MatcherAssert.assertThat(response.responseStatusMessage,
        Matchers.is(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()));
    MatcherAssert.assertThat(response.responseChunk,
        Matchers.is("{\"message\":\"" + Status.INTERNAL_SERVER_ERROR.getReasonPhrase() + "\"}"));
    Assertions.assertTrue(response.responseEnded);
  }

  @Test
  public void testWrapResponseBody() {
    VertxRestDispatcher vertxRestDispatcher = new VertxRestDispatcher();
    String message = "abcd";
    String bodyString = vertxRestDispatcher.wrapResponseBody(message);
    Assertions.assertNotNull(bodyString);
    Assertions.assertEquals("{\"message\":\"abcd\"}", bodyString);

    message = "\"abcd\"";
    bodyString = vertxRestDispatcher.wrapResponseBody(message);
    Assertions.assertNotNull(bodyString);
    Assertions.assertEquals("{\"message\":\"\\\"abcd\\\"\"}", bodyString);

    message = ".01ab\"!@#$%^&*()'\\cd";
    bodyString = vertxRestDispatcher.wrapResponseBody(message);
    Assertions.assertNotNull(bodyString);
    Assertions.assertEquals("{\"message\":\".01ab\\\"!@#$%^&*()'\\\\cd\"}", bodyString);

    message = new JsonObject().put("key", new JsonObject().put("k2", "value")).toString();
    bodyString = vertxRestDispatcher.wrapResponseBody(message);
    Assertions.assertNotNull(bodyString);
    Assertions.assertEquals("{\"key\":{\"k2\":\"value\"}}", bodyString);

    message = "ab\"23\n@!#cd";
    bodyString = vertxRestDispatcher.wrapResponseBody(message);
    Assertions.assertNotNull(bodyString);
    Assertions.assertEquals("{\"message\":\"ab\\\"23\\n@!#cd\"}", bodyString);

    message = "ab\"23\r\n@!#cd";
    bodyString = vertxRestDispatcher.wrapResponseBody(message);
    Assertions.assertNotNull(bodyString);
    Assertions.assertEquals("{\"message\":\"ab\\\"23\\r\\n@!#cd\"}", bodyString);
  }
}
