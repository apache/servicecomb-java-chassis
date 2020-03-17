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

package org.apache.servicecomb.foundation.log.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.apache.servicecomb.foundation.log.core.element.LogItem;
import org.apache.servicecomb.foundation.log.core.element.impl.DatetimeConfigurableItem;
import org.apache.servicecomb.foundation.log.core.element.impl.HttpMethodItem;
import org.apache.servicecomb.foundation.log.core.element.impl.PlainTextItem;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import mockit.Deencapsulation;

public class LogGeneratorTest {

  private static final LogGenerator LOG_GENERATOR = new LogGenerator("%m - %t");

  @Test
  public void testConstructor() {
    LogItem<RoutingContext>[] elements = Deencapsulation.getField(LOG_GENERATOR, "logItems");
    assertEquals(3, elements.length);
    assertEquals(HttpMethodItem.class, elements[0].getClass());
    assertEquals(PlainTextItem.class, elements[1].getClass());
    assertEquals(DatetimeConfigurableItem.class, elements[2].getClass());
  }

  @Test
  public void testServerLog() {
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    long startMillisecond = 1416863450581L;
    ServerAccessLogEvent serverAccessLogEvent = new ServerAccessLogEvent();
    serverAccessLogEvent.setMilliStartTime(startMillisecond).setRoutingContext(context);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DatetimeConfigurableItem.DEFAULT_DATETIME_PATTERN,
        DatetimeConfigurableItem.DEFAULT_LOCALE);
    simpleDateFormat.setTimeZone(TimeZone.getDefault());

    when(context.request()).thenReturn(request);
    when(request.method()).thenReturn(HttpMethod.DELETE);

    String log = LOG_GENERATOR.generateServerLog(serverAccessLogEvent);
    Assert.assertEquals("DELETE" + " - " + simpleDateFormat.format(startMillisecond), log);
  }

  @Test
  public void testClientLog() {
    Invocation invocation = Mockito.mock(Invocation.class);
    InvocationStageTrace stageTrace = Mockito.mock(InvocationStageTrace.class);
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    long startMillisecond = 1416863450581L;
    when(stageTrace.getStartSend()).thenReturn(0L);
    when(stageTrace.getStart()).thenReturn(0L);
    when(stageTrace.getFinish()).thenReturn(0L);
    when(stageTrace.getStartCurrentTime()).thenReturn(startMillisecond);
    when(invocation.getOperationMeta()).thenReturn(operationMeta);
    when(invocation.getInvocationStageTrace()).thenReturn(stageTrace);

    InvocationFinishEvent finishEvent = new InvocationFinishEvent(invocation, null);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DatetimeConfigurableItem.DEFAULT_DATETIME_PATTERN,
        DatetimeConfigurableItem.DEFAULT_LOCALE);
    simpleDateFormat.setTimeZone(TimeZone.getDefault());
    when(operationMeta.getHttpMethod()).thenReturn(HttpMethod.DELETE.toString());
    String log = LOG_GENERATOR.generateClientLog(finishEvent);
    Assert.assertEquals("DELETE" + " - " + simpleDateFormat.format(startMillisecond), log);
  }
}
