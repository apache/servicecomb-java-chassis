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

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.condition.OS;
import org.mockito.Mockito;

public class DatetimeConfigurableItemTest {

  private static final long START_MILLISECOND = 1416863450581L;

  private StringBuilder strBuilder;

  private InvocationFinishEvent finishEvent;

  private ServerAccessLogEvent accessLogEvent;

  private Invocation invocation;

  private InvocationStageTrace invocationStageTrace;

  @BeforeEach
  public void initStrBuilder() {
    finishEvent = Mockito.mock(InvocationFinishEvent.class);
    invocation = Mockito.mock(Invocation.class);
    invocationStageTrace = Mockito.mock(InvocationStageTrace.class);

    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getInvocationStageTrace()).thenReturn(invocationStageTrace);
    when(invocationStageTrace.getStartSend()).thenReturn(0L);
    when(invocationStageTrace.getStart()).thenReturn(0L);
    when(invocationStageTrace.getStartTimeMillis()).thenReturn(START_MILLISECOND);

    accessLogEvent = new ServerAccessLogEvent();
    accessLogEvent.setMilliStartTime(START_MILLISECOND);
    strBuilder = new StringBuilder();
  }

  @Test
  @EnabledOnOs({OS.LINUX, OS.WINDOWS})
  @EnabledOnJre(JRE.JAVA_8)
  public void serverFormattedElement() {
    ConfigurableDatetimeAccessItem element = new ConfigurableDatetimeAccessItem(
            "EEE, yyyy MMM dd HH:mm:ss zzz|GMT-08|zh-CN");
    element.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals("星期一, 2014 十一月 24 13:10:50 GMT-08:00", strBuilder.toString());
  }

  @Test
  @EnabledOnOs({OS.LINUX, OS.WINDOWS})
  @EnabledOnJre(JRE.JAVA_8)
  public void clientFormattedElement() {
    ConfigurableDatetimeAccessItem element = new ConfigurableDatetimeAccessItem(
            "EEE, yyyy MMM dd HH:mm:ss zzz|GMT-08|zh-CN");
    element.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals("星期一, 2014 十一月 24 13:10:50 GMT-08:00", strBuilder.toString());
  }

  @Test
  @EnabledOnOs({OS.LINUX, OS.WINDOWS})
  @EnabledOnJre(JRE.JAVA_8)
  public void serverFormattedElementOnNoPattern() {
    ConfigurableDatetimeAccessItem element = new ConfigurableDatetimeAccessItem(
            "|GMT+08|zh-CN");

    element.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals("星期二, 25 十一月 2014 05:10:50 GMT+08:00", strBuilder.toString());
  }

  @Test
  @EnabledOnOs({OS.LINUX, OS.WINDOWS})
  @EnabledOnJre(JRE.JAVA_8)
  public void clientFormattedElementOnNoPattern() {
    ConfigurableDatetimeAccessItem element = new ConfigurableDatetimeAccessItem(
            "|GMT+08|zh-CN");

    element.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals("星期二, 25 十一月 2014 05:10:50 GMT+08:00", strBuilder.toString());
  }

  @Test
  public void getFormattedElementOnNoTimezone() {
    ConfigurableDatetimeAccessItem element = new ConfigurableDatetimeAccessItem(
            "yyyy/MM/dd zzz||zh-CN");
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd zzz", Locale.forLanguageTag("zh-CN"));
    simpleDateFormat.setTimeZone(TimeZone.getDefault());

    element.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals(simpleDateFormat.format(START_MILLISECOND), strBuilder.toString());
  }

  @Test
  public void clientFormattedElementOnNoTimezone() {
    ConfigurableDatetimeAccessItem element = new ConfigurableDatetimeAccessItem(
            "yyyy/MM/dd zzz||zh-CN");
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd zzz", Locale.forLanguageTag("zh-CN"));
    simpleDateFormat.setTimeZone(TimeZone.getDefault());

    element.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals(simpleDateFormat.format(START_MILLISECOND), strBuilder.toString());
  }

  @Test
  public void serverFormattedElementOnNoLocale() {
    ConfigurableDatetimeAccessItem element = new ConfigurableDatetimeAccessItem(
            "EEE, dd MMM yyyy HH:mm:ss zzz|GMT+08|");

    element.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals("Tue, 25 Nov 2014 05:10:50 GMT+08:00", strBuilder.toString());
  }

  @Test
  public void clientFormattedElementOnNoLocale() {
    ConfigurableDatetimeAccessItem element = new ConfigurableDatetimeAccessItem(
            "EEE, dd MMM yyyy HH:mm:ss zzz|GMT+08|");

    element.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals("Tue, 25 Nov 2014 05:10:50 GMT+08:00", strBuilder.toString());
  }

  @Test
  public void serverFormattedElementOnNoConfig() {
    ConfigurableDatetimeAccessItem element = new ConfigurableDatetimeAccessItem(
            "||");
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ConfigurableDatetimeAccessItem.DEFAULT_DATETIME_PATTERN,
            Locale.US);
    simpleDateFormat.setTimeZone(TimeZone.getDefault());

    element.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals(simpleDateFormat.format(START_MILLISECOND), strBuilder.toString());
  }

  @Test
  public void clientFormattedElementOnNoConfig() {
    ConfigurableDatetimeAccessItem element = new ConfigurableDatetimeAccessItem(
            "||");
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ConfigurableDatetimeAccessItem.DEFAULT_DATETIME_PATTERN,
            Locale.US);
    simpleDateFormat.setTimeZone(TimeZone.getDefault());

    element.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals(simpleDateFormat.format(START_MILLISECOND), strBuilder.toString());
  }

  @Test
  public void serverConstructorWithNoArg() {
    ConfigurableDatetimeAccessItem element = new ConfigurableDatetimeAccessItem();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
    simpleDateFormat.setTimeZone(TimeZone.getDefault());

    element.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals("EEE, dd MMM yyyy HH:mm:ss zzz", element.getPattern());
    Assertions.assertEquals(Locale.US, element.getLocale());
    Assertions.assertEquals(TimeZone.getDefault(), element.getTimezone());
    Assertions.assertEquals(simpleDateFormat.format(START_MILLISECOND), strBuilder.toString());
  }

  @Test
  public void clientConstructorWithNoArg() {
    ConfigurableDatetimeAccessItem element = new ConfigurableDatetimeAccessItem();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
    simpleDateFormat.setTimeZone(TimeZone.getDefault());

    element.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals("EEE, dd MMM yyyy HH:mm:ss zzz", element.getPattern());
    Assertions.assertEquals(Locale.US, element.getLocale());
    Assertions.assertEquals(TimeZone.getDefault(), element.getTimezone());
    Assertions.assertEquals(simpleDateFormat.format(START_MILLISECOND), strBuilder.toString());
  }

  @Test
  public void serverConstructorWithNoSeparator() {
    ConfigurableDatetimeAccessItem element = new ConfigurableDatetimeAccessItem("yyyy/MM/dd HH:mm:ss zzz");
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss zzz", Locale.US);
    simpleDateFormat.setTimeZone(TimeZone.getDefault());

    element.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals("yyyy/MM/dd HH:mm:ss zzz", element.getPattern());
    Assertions.assertEquals(Locale.US, element.getLocale());
    Assertions.assertEquals(TimeZone.getDefault(), element.getTimezone());
    Assertions.assertEquals(simpleDateFormat.format(START_MILLISECOND), strBuilder.toString());
  }

  @Test
  public void clientConstructorWithNoSeparator() {
    ConfigurableDatetimeAccessItem element = new ConfigurableDatetimeAccessItem("yyyy/MM/dd HH:mm:ss zzz");
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss zzz", Locale.US);
    simpleDateFormat.setTimeZone(TimeZone.getDefault());

    element.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals("yyyy/MM/dd HH:mm:ss zzz", element.getPattern());
    Assertions.assertEquals(Locale.US, element.getLocale());
    Assertions.assertEquals(TimeZone.getDefault(), element.getTimezone());
    Assertions.assertEquals(simpleDateFormat.format(START_MILLISECOND), strBuilder.toString());
  }
}
