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

import java.util.HashSet;

import org.apache.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.CookieImpl;

public class CookieItemTest {

  public static final String COOKIE_NAME = "cookieName";

  private static final CookieItem ELEMENT = new CookieItem(COOKIE_NAME);

  @Test
  public void getFormattedElement() {
    AccessLogParam<RoutingContext> param = new AccessLogParam<>();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);
    HashSet<Cookie> cookieSet = new HashSet<>();
    String cookieValue = "cookieValue";
    CookieImpl cookie = new CookieImpl(COOKIE_NAME, cookieValue);

    cookieSet.add(cookie);
    Mockito.when(mockContext.cookieCount()).thenReturn(1);
    Mockito.when(mockContext.cookies()).thenReturn(cookieSet);
    param.setContextData(mockContext);

    String result = ELEMENT.getFormattedItem(param);

    Assert.assertEquals(cookieValue, result);
  }

  @Test
  public void getFormattedElementOnCookieCountIsZero() {
    AccessLogParam<RoutingContext> param = new AccessLogParam<>();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);
    HashSet<Cookie> cookieSet = new HashSet<>();

    Mockito.when(mockContext.cookieCount()).thenReturn(0);
    Mockito.when(mockContext.cookies()).thenReturn(cookieSet);
    param.setContextData(mockContext);

    String result = ELEMENT.getFormattedItem(param);

    Assert.assertEquals("-", result);
  }

  @Test
  public void getFormattedElementOnCookieSetIsNull() {
    AccessLogParam<RoutingContext> param = new AccessLogParam<>();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);

    Mockito.when(mockContext.cookieCount()).thenReturn(1);
    Mockito.when(mockContext.cookies()).thenReturn(null);
    param.setContextData(mockContext);

    String result = ELEMENT.getFormattedItem(param);

    Assert.assertEquals("-", result);
  }

  @Test
  public void getFormattedElementOnNotFound() {
    AccessLogParam<RoutingContext> param = new AccessLogParam<>();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);
    HashSet<Cookie> cookieSet = new HashSet<>();
    String cookieValue = "cookieValue";
    CookieImpl cookie = new CookieImpl("anotherCookieName", cookieValue);

    cookieSet.add(cookie);
    Mockito.when(mockContext.cookieCount()).thenReturn(1);
    Mockito.when(mockContext.cookies()).thenReturn(cookieSet);
    param.setContextData(mockContext);

    String result = ELEMENT.getFormattedItem(param);

    Assert.assertEquals("-", result);
  }
}
