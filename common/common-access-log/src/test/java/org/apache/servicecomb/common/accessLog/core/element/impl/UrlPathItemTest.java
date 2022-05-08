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

package org.apache.servicecomb.common.accessLog.core.element.impl;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.codec.param.RestClientRequestImpl;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.swagger.models.Swagger;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

public class UrlPathItemTest {
  private static final UrlPathAccessItem ITEM = new UrlPathAccessItem();

  private StringBuilder strBuilder;

  private InvocationFinishEvent finishEvent;

  private ServerAccessLogEvent accessLogEvent;

  private RoutingContext routingContext;

  private Invocation invocation;

  private HttpServerRequest serverRequest;

  private OperationMeta operationMeta;

  private SchemaMeta schemaMeta;

  private Swagger swagger;

  private RestClientRequestImpl restClientRequest;

  private HttpClientRequest clientRequest;

  @BeforeEach
  public void initStrBuilder() {
    accessLogEvent = new ServerAccessLogEvent();
    routingContext = Mockito.mock(RoutingContext.class);
    finishEvent = Mockito.mock(InvocationFinishEvent.class);
    invocation = Mockito.mock(Invocation.class);
    serverRequest = Mockito.mock(HttpServerRequest.class);
    operationMeta = Mockito.mock(OperationMeta.class);
    schemaMeta = Mockito.mock(SchemaMeta.class);
    swagger = Mockito.mock(Swagger.class);
    restClientRequest = Mockito.mock(RestClientRequestImpl.class);
    clientRequest = Mockito.mock(HttpClientRequest.class);

    accessLogEvent.setRoutingContext(routingContext);
    strBuilder = new StringBuilder();
  }

  @Test
  public void clientFormattedElement() {
    String uri = "/base/test";
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getOperationMeta()).thenReturn(operationMeta);
    when(invocation.getSchemaMeta()).thenReturn(schemaMeta);
    when(schemaMeta.getSwagger()).thenReturn(swagger);
    when(swagger.getBasePath()).thenReturn("/base");
    when(operationMeta.getOperationPath()).thenReturn("/test");

    ITEM.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals(uri, strBuilder.toString());

    strBuilder = new StringBuilder();
    Map<String, Object> handlerContext = new HashMap<>();
    handlerContext.put(RestConst.INVOCATION_HANDLER_REQUESTCLIENT, restClientRequest);
    when(invocation.getOperationMeta()).thenReturn(null);
    when(invocation.getSchemaMeta()).thenReturn(null);
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getHandlerContext()).thenReturn(handlerContext);
    when(restClientRequest.getRequest()).thenReturn(clientRequest);
    when(clientRequest.path()).thenReturn(uri);
    ITEM.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals(uri, strBuilder.toString());
  }

  @Test
  public void serverFormattedElement() {
    String uri = "/test";
    when(routingContext.request()).thenReturn(serverRequest);
    when(serverRequest.path()).thenReturn(uri);
    ITEM.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals(uri, strBuilder.toString());
  }

  @Test
  public void serverFormattedElementOnRequestIsNull() {
    when(routingContext.request()).thenReturn(null);
    ITEM.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals("-", strBuilder.toString());
  }

  @Test
  public void clientFormattedElementOnRequestIsNull() {
    Map<String, Object> handlerContext = new HashMap<>();
    handlerContext.put(RestConst.INVOCATION_HANDLER_REQUESTCLIENT, restClientRequest);
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getHandlerContext()).thenReturn(handlerContext);
    when(restClientRequest.getRequest()).thenReturn(null);
    ITEM.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals("-", strBuilder.toString());
  }

  @Test
  public void serverFormattedElementOnMethodIsNull() {
    when(routingContext.request()).thenReturn(serverRequest);
    when(serverRequest.path()).thenReturn(null);
    ITEM.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals("-", strBuilder.toString());
  }

  @Test
  public void clientFormattedElementOnMethodIsNull() {
    Map<String, Object> handlerContext = new HashMap<>();
    handlerContext.put(RestConst.INVOCATION_HANDLER_REQUESTCLIENT, restClientRequest);
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getHandlerContext()).thenReturn(handlerContext);
    when(restClientRequest.getRequest()).thenReturn(clientRequest);
    when(clientRequest.path()).thenReturn(null);
    ITEM.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals("-", strBuilder.toString());
  }
}
