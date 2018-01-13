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

package org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl;

import static org.junit.Assert.assertEquals;

import org.apache.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.RoutingContext;

public class FirstLineOfRequestElementTest {

  public static final FirstLineOfRequestElement ELEMENT = new FirstLineOfRequestElement();

  @Test
  public void getFormattedElement() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    String uri = "/test/uri";

    param.setRoutingContext(mockContext);
    Mockito.when(mockContext.request()).thenReturn(request);
    Mockito.when(request.method()).thenReturn(HttpMethod.DELETE);
    Mockito.when(request.path()).thenReturn(uri);
    Mockito.when(request.version()).thenReturn(HttpVersion.HTTP_1_1);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals("\"DELETE " + uri + " HTTP/1.1\"", result);
  }
}
