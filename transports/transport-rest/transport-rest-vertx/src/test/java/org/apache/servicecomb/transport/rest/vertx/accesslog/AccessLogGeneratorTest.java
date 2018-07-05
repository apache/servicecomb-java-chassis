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

package org.apache.servicecomb.transport.rest.vertx.accesslog;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DatetimeConfigurableItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.HttpMethodItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.PlainTextItem;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import mockit.Deencapsulation;

public class AccessLogGeneratorTest {

  private static final AccessLogGenerator ACCESS_LOG_GENERATOR = new AccessLogGenerator("%m - %t");

  @Test
  public void testConstructor() {
    AccessLogItem<RoutingContext>[] elements = Deencapsulation.getField(ACCESS_LOG_GENERATOR, "accessLogItems");
    assertEquals(3, elements.length);
    assertEquals(HttpMethodItem.class, elements[0].getClass());
    assertEquals(PlainTextItem.class, elements[1].getClass());
    assertEquals(DatetimeConfigurableItem.class, elements[2].getClass());
  }

  @Test
  public void testLog() {
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    long startMillisecond = 1416863450581L;
    AccessLogParam<RoutingContext> accessLogParam = new AccessLogParam<>();
    accessLogParam.setStartMillisecond(startMillisecond).setContextData(context);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DatetimeConfigurableItem.DEFAULT_DATETIME_PATTERN,
        DatetimeConfigurableItem.DEFAULT_LOCALE);
    simpleDateFormat.setTimeZone(TimeZone.getDefault());

    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.method()).thenReturn(HttpMethod.DELETE);

    String log = ACCESS_LOG_GENERATOR.generateLog(accessLogParam);

    Assert.assertEquals("DELETE" + " - " + simpleDateFormat.format(startMillisecond), log);
  }
}
