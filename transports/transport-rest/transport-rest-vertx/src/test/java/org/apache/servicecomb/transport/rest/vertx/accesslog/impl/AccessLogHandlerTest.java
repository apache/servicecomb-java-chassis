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

package org.apache.servicecomb.transport.rest.vertx.accesslog.impl;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TimeZone;

import org.apache.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DatetimeConfigurableElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.MethodElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.PlainTextElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import mockit.Deencapsulation;

public class AccessLogHandlerTest {

  private static final AccessLogElement methodElement = new MethodElement();

  private static final AccessLogElement datetimeElement = new DatetimeConfigurableElement();

  private static final AccessLogElement plainTextElement = new PlainTextElement(" - ");

  private static final Logger logger = Mockito.mock(Logger.class);

  private static final AccessLogHandler ACCESS_LOG_HANDLER = new AccessLogHandler("rawPattern", s -> {
    assertEquals("rawPattern", s);
    return Arrays.asList(new AccessLogElementExtraction().setAccessLogElement(methodElement),
        new AccessLogElementExtraction().setAccessLogElement(plainTextElement),
        new AccessLogElementExtraction().setAccessLogElement(datetimeElement));
  });

  @BeforeClass
  public static void init() {
    Deencapsulation.setField(AccessLogHandler.class, "LOGGER", logger);
  }

  @Test
  public void testConstructor() {
    AccessLogElement[] elements = Deencapsulation.getField(ACCESS_LOG_HANDLER, "accessLogElements");
    assertEquals(3, elements.length);
    assertEquals(methodElement, elements[0]);
    assertEquals(plainTextElement, elements[1]);
    assertEquals(datetimeElement, elements[2]);
  }

  @Test
  public void handle() {
    RoutingContext testContext = Mockito.mock(RoutingContext.class);
    ACCESS_LOG_HANDLER.handle(testContext);
  }

  @Test
  public void testLog() {
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    long startMillisecond = 1416863450581L;
    AccessLogParam accessLogParam = new AccessLogParam().setStartMillisecond(startMillisecond)
        .setRoutingContext(context);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DatetimeConfigurableElement.DEFAULT_DATETIME_PATTERN,
        DatetimeConfigurableElement.DEFAULT_LOCALE);
    simpleDateFormat.setTimeZone(TimeZone.getDefault());

    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.method()).thenReturn(HttpMethod.DELETE);

    Deencapsulation.invoke(ACCESS_LOG_HANDLER, "log", accessLogParam);

    Mockito.verify(logger).info("DELETE" + " - " + simpleDateFormat.format(startMillisecond));
  }
}
