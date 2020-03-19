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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DatetimeConfigurableItemTest {

  private static final long START_MILLISECOND = 1416863450581L;

  private StringBuilder strBuilder;

  private InvocationFinishEvent finishEvent;

  private ServerAccessLogEvent accessLogEvent;

  private Invocation invocation;

  private InvocationStageTrace invocationStageTrace;

  @Before
  public void initStrBuilder() {
    finishEvent = Mockito.mock(InvocationFinishEvent.class);
    invocation = Mockito.mock(Invocation.class);
    invocationStageTrace = Mockito.mock(InvocationStageTrace.class);

    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getInvocationStageTrace()).thenReturn(invocationStageTrace);
    when(invocationStageTrace.getStartSend()).thenReturn(0L);
    when(invocationStageTrace.getStart()).thenReturn(0L);
    when(invocationStageTrace.getStartCurrentTime()).thenReturn(START_MILLISECOND);

    accessLogEvent = new ServerAccessLogEvent();
    accessLogEvent.setMilliStartTime(START_MILLISECOND);
    strBuilder = new StringBuilder();
  }

  @Test
  public void serverFormattedElement() {
    DatetimeConfigurableItemAccess element = new DatetimeConfigurableItemAccess(
        "EEE, yyyy MMM dd HH:mm:ss zzz|GMT-08|zh-CN");
    element.appendServerFormattedItem(accessLogEvent, strBuilder);
    assertEquals("星期一, 2014 十一月 24 13:10:50 GMT-08:00", strBuilder.toString());
  }

  @Test
  public void clientFormattedElement() {
    DatetimeConfigurableItemAccess element = new DatetimeConfigurableItemAccess(
        "EEE, yyyy MMM dd HH:mm:ss zzz|GMT-08|zh-CN");
    element.appendClientFormattedItem(finishEvent, strBuilder);
    assertEquals("星期一, 2014 十一月 24 13:10:50 GMT-08:00", strBuilder.toString());
  }

  @Test
  public void serverFormattedElementOnNoPattern() {
    DatetimeConfigurableItemAccess element = new DatetimeConfigurableItemAccess(
        "|GMT+08|zh-CN");

    element.appendServerFormattedItem(accessLogEvent, strBuilder);
    assertEquals("星期二, 25 十一月 2014 05:10:50 GMT+08:00", strBuilder.toString());
  }

  @Test
  public void clientFormattedElementOnNoPattern() {
    DatetimeConfigurableItemAccess element = new DatetimeConfigurableItemAccess(
        "|GMT+08|zh-CN");

    element.appendClientFormattedItem(finishEvent, strBuilder);
    assertEquals("星期二, 25 十一月 2014 05:10:50 GMT+08:00", strBuilder.toString());
  }

  @Test
  public void getFormattedElementOnNoTimezone() {
    DatetimeConfigurableItemAccess element = new DatetimeConfigurableItemAccess(
        "yyyy/MM/dd zzz||zh-CN");
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd zzz", Locale.forLanguageTag("zh-CN"));
    simpleDateFormat.setTimeZone(TimeZone.getDefault());

    element.appendServerFormattedItem(accessLogEvent, strBuilder);
    assertEquals(simpleDateFormat.format(START_MILLISECOND), strBuilder.toString());
  }

  @Test
  public void clientFormattedElementOnNoTimezone() {
    DatetimeConfigurableItemAccess element = new DatetimeConfigurableItemAccess(
        "yyyy/MM/dd zzz||zh-CN");
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd zzz", Locale.forLanguageTag("zh-CN"));
    simpleDateFormat.setTimeZone(TimeZone.getDefault());

    element.appendClientFormattedItem(finishEvent, strBuilder);
    assertEquals(simpleDateFormat.format(START_MILLISECOND), strBuilder.toString());
  }

  @Test
  public void serverFormattedElementOnNoLocale() {
    DatetimeConfigurableItemAccess element = new DatetimeConfigurableItemAccess(
        "EEE, dd MMM yyyy HH:mm:ss zzz|GMT+08|");

    element.appendServerFormattedItem(accessLogEvent, strBuilder);
    assertEquals("Tue, 25 Nov 2014 05:10:50 GMT+08:00", strBuilder.toString());
  }

  @Test
  public void clientFormattedElementOnNoLocale() {
    DatetimeConfigurableItemAccess element = new DatetimeConfigurableItemAccess(
        "EEE, dd MMM yyyy HH:mm:ss zzz|GMT+08|");

    element.appendClientFormattedItem(finishEvent, strBuilder);
    assertEquals("Tue, 25 Nov 2014 05:10:50 GMT+08:00", strBuilder.toString());
  }

  @Test
  public void serverFormattedElementOnNoConfig() {
    DatetimeConfigurableItemAccess element = new DatetimeConfigurableItemAccess(
        "||");
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DatetimeConfigurableItemAccess.DEFAULT_DATETIME_PATTERN,
        Locale.US);
    simpleDateFormat.setTimeZone(TimeZone.getDefault());

    element.appendServerFormattedItem(accessLogEvent, strBuilder);
    assertEquals(simpleDateFormat.format(START_MILLISECOND), strBuilder.toString());
  }

  @Test
  public void clientFormattedElementOnNoConfig() {
    DatetimeConfigurableItemAccess element = new DatetimeConfigurableItemAccess(
        "||");
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DatetimeConfigurableItemAccess.DEFAULT_DATETIME_PATTERN,
        Locale.US);
    simpleDateFormat.setTimeZone(TimeZone.getDefault());

    element.appendClientFormattedItem(finishEvent, strBuilder);
    assertEquals(simpleDateFormat.format(START_MILLISECOND), strBuilder.toString());
  }

  @Test
  public void serverConstructorWithNoArg() {
    DatetimeConfigurableItemAccess element = new DatetimeConfigurableItemAccess();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
    simpleDateFormat.setTimeZone(TimeZone.getDefault());

    element.appendServerFormattedItem(accessLogEvent, strBuilder);
    assertEquals("EEE, dd MMM yyyy HH:mm:ss zzz", element.getPattern());
    assertEquals(Locale.US, element.getLocale());
    assertEquals(TimeZone.getDefault(), element.getTimezone());
    assertEquals(simpleDateFormat.format(START_MILLISECOND), strBuilder.toString());
  }

  @Test
  public void clientConstructorWithNoArg() {
    DatetimeConfigurableItemAccess element = new DatetimeConfigurableItemAccess();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
    simpleDateFormat.setTimeZone(TimeZone.getDefault());

    element.appendClientFormattedItem(finishEvent, strBuilder);
    assertEquals("EEE, dd MMM yyyy HH:mm:ss zzz", element.getPattern());
    assertEquals(Locale.US, element.getLocale());
    assertEquals(TimeZone.getDefault(), element.getTimezone());
    assertEquals(simpleDateFormat.format(START_MILLISECOND), strBuilder.toString());
  }

  @Test
  public void serverConstructorWithNoSeparator() {
    DatetimeConfigurableItemAccess element = new DatetimeConfigurableItemAccess("yyyy/MM/dd HH:mm:ss zzz");
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss zzz", Locale.US);
    simpleDateFormat.setTimeZone(TimeZone.getDefault());

    element.appendServerFormattedItem(accessLogEvent, strBuilder);
    assertEquals("yyyy/MM/dd HH:mm:ss zzz", element.getPattern());
    assertEquals(Locale.US, element.getLocale());
    assertEquals(TimeZone.getDefault(), element.getTimezone());
    assertEquals(simpleDateFormat.format(START_MILLISECOND), strBuilder.toString());
  }

  @Test
  public void clientConstructorWithNoSeparator() {
    DatetimeConfigurableItemAccess element = new DatetimeConfigurableItemAccess("yyyy/MM/dd HH:mm:ss zzz");
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss zzz", Locale.US);
    simpleDateFormat.setTimeZone(TimeZone.getDefault());

    element.appendClientFormattedItem(finishEvent, strBuilder);
    assertEquals("yyyy/MM/dd HH:mm:ss zzz", element.getPattern());
    assertEquals(Locale.US, element.getLocale());
    assertEquals(TimeZone.getDefault(), element.getTimezone());
    assertEquals(simpleDateFormat.format(START_MILLISECOND), strBuilder.toString());
  }
}
