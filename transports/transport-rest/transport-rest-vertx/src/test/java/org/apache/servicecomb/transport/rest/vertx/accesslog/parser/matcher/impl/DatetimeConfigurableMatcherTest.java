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

package org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DatetimeConfigurableElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;
import org.junit.Test;

import mockit.Deencapsulation;

public class DatetimeConfigurableMatcherTest {
  private static final DatetimeConfigurableMatcher MATCHER = new DatetimeConfigurableMatcher();

  @Test
  public void extractElementPlaceholderOnOnlyV1() {
    List<AccessLogElementExtraction> extractionList = MATCHER
        .extractElementPlaceholder("%{yyyyMMdd}t %h %{yyyyMMdd HH:mm:ss zzz}t %b%b %H %{yyyyMMdd}t");

    assertEquals(3, extractionList.size());
    assertEquals(0, extractionList.get(0).getStart());
    assertEquals(12, extractionList.get(0).getEnd());
    assertEquals(16, extractionList.get(1).getStart());
    assertEquals(41, extractionList.get(1).getEnd());
    assertEquals(50, extractionList.get(2).getStart());
    assertEquals(62, extractionList.get(2).getEnd());

    assertEquals(DatetimeConfigurableElement.class, extractionList.get(0).getAccessLogElement().getClass());
    assertEquals(DatetimeConfigurableElement.class, extractionList.get(1).getAccessLogElement().getClass());
    assertEquals(DatetimeConfigurableElement.class, extractionList.get(2).getAccessLogElement().getClass());

    assertEquals("yyyyMMdd",
        ((DatetimeConfigurableElement) (extractionList.get(0).getAccessLogElement())).getPattern());
    assertEquals("yyyyMMdd HH:mm:ss zzz",
        ((DatetimeConfigurableElement) (extractionList.get(1).getAccessLogElement())).getPattern());
    assertEquals("yyyyMMdd",
        ((DatetimeConfigurableElement) (extractionList.get(2).getAccessLogElement())).getPattern());
  }

  @Test
  public void extractElementPlaceholderOnOnlyV2() {
    List<AccessLogElementExtraction> extractionList = MATCHER
        .extractElementPlaceholder(
            "%{EEE, dd MMM yyyy HH:mm:ss zzz||zh-CN}t %h %{EEE, yy/MM/dd HH:mm:ss zzz|GMT+08|zh-CN}t %b%b %H %{EEE, dd MMM yyyy HH:mm:ss zzz|GMT-08|}t");

    assertEquals(3, extractionList.size());
    assertEquals(0, extractionList.get(0).getStart());
    assertEquals(40, extractionList.get(0).getEnd());
    assertEquals(44, extractionList.get(1).getStart());
    assertEquals(87, extractionList.get(1).getEnd());
    assertEquals(96, extractionList.get(2).getStart());
    assertEquals(137, extractionList.get(2).getEnd());

    assertEquals(DatetimeConfigurableElement.class, extractionList.get(0).getAccessLogElement().getClass());
    assertEquals(DatetimeConfigurableElement.class, extractionList.get(1).getAccessLogElement().getClass());
    assertEquals(DatetimeConfigurableElement.class, extractionList.get(2).getAccessLogElement().getClass());

    assertEquals("EEE, dd MMM yyyy HH:mm:ss zzz",
        Deencapsulation.getField(extractionList.get(0).getAccessLogElement(), "pattern"));
    assertEquals(TimeZone.getDefault(),
        Deencapsulation.getField(extractionList.get(0).getAccessLogElement(), "timezone"));
    assertEquals(Locale.SIMPLIFIED_CHINESE,
        Deencapsulation.getField(extractionList.get(0).getAccessLogElement(), "locale"));

    assertEquals("EEE, yy/MM/dd HH:mm:ss zzz",
        Deencapsulation.getField(extractionList.get(1).getAccessLogElement(), "pattern"));
    assertEquals(TimeZone.getTimeZone("GMT+08"),
        Deencapsulation.getField(extractionList.get(1).getAccessLogElement(), "timezone"));
    assertEquals(Locale.SIMPLIFIED_CHINESE,
        Deencapsulation.getField(extractionList.get(1).getAccessLogElement(), "locale"));

    assertEquals("EEE, dd MMM yyyy HH:mm:ss zzz",
        Deencapsulation.getField(extractionList.get(2).getAccessLogElement(), "pattern"));
    assertEquals(TimeZone.getTimeZone("GMT-08"),
        Deencapsulation.getField(extractionList.get(2).getAccessLogElement(), "timezone"));
    assertEquals(Locale.US, Deencapsulation.getField(extractionList.get(2).getAccessLogElement(), "locale"));
  }

  @Test
  public void extractElementPlaceholderOnV1V2Mixed() {
    List<AccessLogElementExtraction> extractionList = MATCHER
        .extractElementPlaceholder(
            "%{yyyyMMdd}t %h %{yyyyMMdd HH:mm:ss zzz}t %{EEE, dd MMM yyyy HH:mm:ss zzz|GMT+08|zh-CN}t %b%b %H %{yyyyMMdd}t");

    assertEquals(4, extractionList.size());
    assertEquals(0, extractionList.get(0).getStart());
    assertEquals(12, extractionList.get(0).getEnd());
    assertEquals(16, extractionList.get(1).getStart());
    assertEquals(41, extractionList.get(1).getEnd());
    assertEquals(42, extractionList.get(2).getStart());
    assertEquals(88, extractionList.get(2).getEnd());
    assertEquals(97, extractionList.get(3).getStart());
    assertEquals(109, extractionList.get(3).getEnd());

    assertEquals(DatetimeConfigurableElement.class, extractionList.get(0).getAccessLogElement().getClass());
    assertEquals(DatetimeConfigurableElement.class, extractionList.get(1).getAccessLogElement().getClass());
    assertEquals(DatetimeConfigurableElement.class, extractionList.get(2).getAccessLogElement().getClass());
    assertEquals(DatetimeConfigurableElement.class, extractionList.get(3).getAccessLogElement().getClass());
    assertEquals("yyyyMMdd",
        ((DatetimeConfigurableElement) (extractionList.get(0).getAccessLogElement())).getPattern());
    assertEquals("yyyyMMdd HH:mm:ss zzz",
        ((DatetimeConfigurableElement) (extractionList.get(1).getAccessLogElement())).getPattern());
    assertEquals("EEE, dd MMM yyyy HH:mm:ss zzz",
        ((DatetimeConfigurableElement) (extractionList.get(2).getAccessLogElement())).getPattern());
    assertEquals("yyyyMMdd",
        ((DatetimeConfigurableElement) (extractionList.get(3).getAccessLogElement())).getPattern());
  }

  @Test
  public void extractElementPlaceholderOnNotFound() {
    List<AccessLogElementExtraction> extractionList = MATCHER
        .extractElementPlaceholder(
            "%{identifier}i %h %b%b %H %{identifier}o");

    assertEquals(0, extractionList.size());
  }
}
