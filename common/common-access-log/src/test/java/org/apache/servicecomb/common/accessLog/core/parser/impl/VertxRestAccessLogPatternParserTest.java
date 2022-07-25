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

package org.apache.servicecomb.common.accessLog.core.parser.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.servicecomb.common.accessLog.core.element.AccessLogItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.CookieAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.ConfigurableDatetimeAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.DurationMillisecondAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.DurationSecondAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.FirstLineOfRequestAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.HttpMethodAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.HttpStatusAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.InvocationContextAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.LocalHostAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.LocalPortAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.QueryStringAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.RemoteHostAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.RequestHeaderAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.RequestProtocolAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.ResponseHeaderAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.ResponseSizeAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.TraceIdAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.TransportAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.UrlPathAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.UrlPathWithQueryAccessItem;
import org.apache.servicecomb.common.accessLog.core.parser.CompositeVertxRestAccessLogItemMeta;
import org.apache.servicecomb.common.accessLog.core.parser.VertxRestAccessLogItemMeta;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.vertx.ext.web.RoutingContext;

public class VertxRestAccessLogPatternParserTest {
  private static final String ROW_PATTERN = "[cs-method] %m %s%T%D%h%v%p%B%b%r%U%q"
      + "cs-uri-stemcs-uri-querycs-uri%H%t%{yyyy MM dd HH:mm:ss zzz}t"
      + "%{yyyy MM dd HH:mm:ss|GMT+0|en-US}t"
      + "%{incoming-header}i"
      + "%{outgoing-header}o"
      + "%{cookie}C"
      + "%SCB-traceId"
      + "%{ctx}SCB-ctx"
      + "%SCB-transport";

  private static final VertxRestAccessLogPatternParser logPatternParser = new VertxRestAccessLogPatternParser();

  private ServerAccessLogEvent accessLogEvent;

  private RoutingContext routingContext;


  @BeforeEach
  public void initStrBuilder() {
    routingContext = Mockito.mock(RoutingContext.class);
    accessLogEvent = new ServerAccessLogEvent();
    accessLogEvent.setRoutingContext(routingContext);
  }

  @Test
  public void testParsePatternFullTest() {
    List<AccessLogItem<RoutingContext>> result = logPatternParser.parsePattern(ROW_PATTERN);
    Assertions.assertEquals(29, result.size());
    StringBuilder builder = new StringBuilder();
    result.get(0).appendServerFormattedItem(accessLogEvent, builder);
    Assertions.assertEquals("[", builder.toString());
    Assertions.assertEquals(HttpMethodAccessItem.class, result.get(1).getClass());
    builder = new StringBuilder();
    result.get(2).appendServerFormattedItem(accessLogEvent, builder);
    Assertions.assertEquals("] ", builder.toString());
    Assertions.assertEquals(HttpMethodAccessItem.class, result.get(3).getClass());
    builder = new StringBuilder();
    result.get(4).appendServerFormattedItem(accessLogEvent, builder);
    Assertions.assertEquals(" ", builder.toString());
    Assertions.assertEquals(HttpStatusAccessItem.class, result.get(5).getClass());
    Assertions.assertEquals(DurationSecondAccessItem.class, result.get(6).getClass());
    Assertions.assertEquals(DurationMillisecondAccessItem.class, result.get(7).getClass());
    Assertions.assertEquals(RemoteHostAccessItem.class, result.get(8).getClass());
    Assertions.assertEquals(LocalHostAccessItem.class, result.get(9).getClass());
    Assertions.assertEquals(LocalPortAccessItem.class, result.get(10).getClass());
    Assertions.assertEquals(ResponseSizeAccessItem.class, result.get(11).getClass());
    Assertions.assertEquals("0", ((ResponseSizeAccessItem) result.get(11)).getZeroBytes());
    Assertions.assertEquals(ResponseSizeAccessItem.class, result.get(12).getClass());
    Assertions.assertEquals("-", ((ResponseSizeAccessItem) result.get(12)).getZeroBytes());
    Assertions.assertEquals(FirstLineOfRequestAccessItem.class, result.get(13).getClass());
    Assertions.assertEquals(UrlPathAccessItem.class, result.get(14).getClass());
    Assertions.assertEquals(QueryStringAccessItem.class, result.get(15).getClass());
    Assertions.assertEquals(UrlPathAccessItem.class, result.get(16).getClass());
    Assertions.assertEquals(QueryStringAccessItem.class, result.get(17).getClass());
    Assertions.assertEquals(UrlPathWithQueryAccessItem.class, result.get(18).getClass());
    Assertions.assertEquals(RequestProtocolAccessItem.class, result.get(19).getClass());
    Assertions.assertEquals(ConfigurableDatetimeAccessItem.class, result.get(20).getClass());
    Assertions.assertEquals(ConfigurableDatetimeAccessItem.DEFAULT_DATETIME_PATTERN,
        ((ConfigurableDatetimeAccessItem) result.get(20)).getPattern());
    Assertions.assertEquals(ConfigurableDatetimeAccessItem.DEFAULT_LOCALE, ((ConfigurableDatetimeAccessItem) result.get(20)).getLocale());
    Assertions.assertEquals(TimeZone.getDefault(), ((ConfigurableDatetimeAccessItem) result.get(20)).getTimezone());
    Assertions.assertEquals("yyyy MM dd HH:mm:ss zzz", ((ConfigurableDatetimeAccessItem) result.get(21)).getPattern());
    Assertions.assertEquals(ConfigurableDatetimeAccessItem.DEFAULT_LOCALE, ((ConfigurableDatetimeAccessItem) result.get(21)).getLocale());
    Assertions.assertEquals(TimeZone.getDefault(), ((ConfigurableDatetimeAccessItem) result.get(21)).getTimezone());
    Assertions.assertEquals("yyyy MM dd HH:mm:ss", ((ConfigurableDatetimeAccessItem) result.get(22)).getPattern());
    Assertions.assertEquals(Locale.forLanguageTag("en-US"), ((ConfigurableDatetimeAccessItem) result.get(22)).getLocale());
    Assertions.assertEquals(TimeZone.getTimeZone("GMT+0"), ((ConfigurableDatetimeAccessItem) result.get(22)).getTimezone());
    Assertions.assertEquals(RequestHeaderAccessItem.class, result.get(23).getClass());
    Assertions.assertEquals("incoming-header", ((RequestHeaderAccessItem) result.get(23)).getVarName());
    Assertions.assertEquals(ResponseHeaderAccessItem.class, result.get(24).getClass());
    Assertions.assertEquals("outgoing-header", ((ResponseHeaderAccessItem) result.get(24)).getVarName());
    Assertions.assertEquals(CookieAccessItem.class, result.get(25).getClass());
    Assertions.assertEquals("cookie", ((CookieAccessItem) result.get(25)).getVarName());
    Assertions.assertEquals(TraceIdAccessItem.class, result.get(26).getClass());
    Assertions.assertEquals(InvocationContextAccessItem.class, result.get(27).getClass());
    Assertions.assertEquals("ctx", ((InvocationContextAccessItem) result.get(27)).getVarName());
    Assertions.assertEquals(TransportAccessItem.class, result.get(28).getClass());
  }

  @Test
  public void testParsePattern() {
    String pattern = " %m  cs-uri-stem %{response-header}o ";
    List<AccessLogItem<RoutingContext>> result = logPatternParser.parsePattern(pattern);
    Assertions.assertEquals(7, result.size());
    StringBuilder stringBuilder = new StringBuilder();
    result.get(0).appendServerFormattedItem(accessLogEvent, stringBuilder);
    Assertions.assertEquals(" ", stringBuilder.toString());
    Assertions.assertEquals(HttpMethodAccessItem.class, result.get(1).getClass());
    stringBuilder = new StringBuilder();
    result.get(2).appendServerFormattedItem(accessLogEvent, stringBuilder);
    Assertions.assertEquals("  ", stringBuilder.toString());
    Assertions.assertEquals(UrlPathAccessItem.class, result.get(3).getClass());
    stringBuilder = new StringBuilder();
    result.get(4).appendServerFormattedItem(accessLogEvent, stringBuilder);
    Assertions.assertEquals(" ", stringBuilder.toString());
    Assertions.assertEquals(ResponseHeaderAccessItem.class, result.get(5).getClass());
    Assertions.assertEquals("response-header", ((ResponseHeaderAccessItem) result.get(5)).getVarName());
    stringBuilder = new StringBuilder();
    result.get(6).appendServerFormattedItem(accessLogEvent, stringBuilder);
    Assertions.assertEquals(" ", stringBuilder.toString());
  }

  @Test
  public void testParsePatternWithNoBlank() {
    String pattern = "%mcs-uri-stem%{response-header}o";
    List<AccessLogItem<RoutingContext>> result = logPatternParser.parsePattern(pattern);
    Assertions.assertEquals(3, result.size());

    Assertions.assertEquals(HttpMethodAccessItem.class, result.get(0).getClass());
    Assertions.assertEquals(UrlPathAccessItem.class, result.get(1).getClass());
    Assertions.assertEquals(ResponseHeaderAccessItem.class, result.get(2).getClass());
    Assertions.assertEquals("response-header", ((ResponseHeaderAccessItem) result.get(2)).getVarName());
  }

  @Test
  public void testParsePatternComplex() {
    String pattern = "%m  cs-uri-stem %{response-header}o abc cs-uri-query %s%{request} header}i plain cs-uri";
    List<AccessLogItem<RoutingContext>> result = logPatternParser.parsePattern(pattern);
    Assertions.assertEquals(12, result.size());

    Assertions.assertEquals(HttpMethodAccessItem.class, result.get(0).getClass());
    StringBuilder stringBuilder = new StringBuilder();
    result.get(1).appendServerFormattedItem(accessLogEvent, stringBuilder);
    Assertions.assertEquals("  ", stringBuilder.toString());
    Assertions.assertEquals(UrlPathAccessItem.class, result.get(2).getClass());
    stringBuilder = new StringBuilder();
    result.get(3).appendServerFormattedItem(accessLogEvent, stringBuilder);
    Assertions.assertEquals(" ", stringBuilder.toString());
    Assertions.assertEquals(ResponseHeaderAccessItem.class, result.get(4).getClass());
    Assertions.assertEquals("response-header", ((ResponseHeaderAccessItem) result.get(4)).getVarName());
    stringBuilder = new StringBuilder();
    result.get(5).appendServerFormattedItem(accessLogEvent, stringBuilder);
    Assertions.assertEquals(" abc ", stringBuilder.toString());
    Assertions.assertEquals(QueryStringAccessItem.class, result.get(6).getClass());
    stringBuilder = new StringBuilder();
    result.get(7).appendServerFormattedItem(accessLogEvent, stringBuilder);
    Assertions.assertEquals(" ", stringBuilder.toString());
    Assertions.assertEquals(HttpStatusAccessItem.class, result.get(8).getClass());
    Assertions.assertEquals(RequestHeaderAccessItem.class, result.get(9).getClass());
    Assertions.assertEquals("request} header", ((RequestHeaderAccessItem) result.get(9)).getVarName());
    stringBuilder = new StringBuilder();
    result.get(10).appendServerFormattedItem(accessLogEvent, stringBuilder);
    Assertions.assertEquals(" plain ", stringBuilder.toString());
    Assertions.assertEquals(UrlPathWithQueryAccessItem.class, result.get(11).getClass());
  }

  final Comparator<VertxRestAccessLogItemMeta> comparator = VertxRestAccessLogPatternParser.accessLogItemMetaComparator;

  /**
   * one factor test
   */
  @Test
  public void testCompareMetaSimple() {
    Assertions.assertTrue(
        comparator.compare(
            new VertxRestAccessLogItemMeta(null, null, null, 0),
            new VertxRestAccessLogItemMeta(null, null, null, 1)
        ) < 0
    );
    Assertions.assertTrue(
        comparator.compare(
            new VertxRestAccessLogItemMeta(null, "}abc", null, 0),
            new VertxRestAccessLogItemMeta(null, null, null, 0)
        ) < 0
    );
    Assertions.assertTrue(
        comparator.compare(
            new VertxRestAccessLogItemMeta(null, "}abc", null, 0),
            new VertxRestAccessLogItemMeta(null, "}de", null, 0)
        ) < 0
    );
    Assertions.assertTrue(
        comparator.compare(
            new VertxRestAccessLogItemMeta(null, "}abc", null, 0),
            new VertxRestAccessLogItemMeta(null, "}ab", null, 0)
        ) < 0
    );
    Assertions.assertTrue(
        comparator.compare(
            new VertxRestAccessLogItemMeta("%abc", null, null, 0),
            new VertxRestAccessLogItemMeta("%de", null, null, 0)
        ) < 0
    );
    Assertions.assertTrue(
        comparator.compare(
            new VertxRestAccessLogItemMeta("%abc", null, null, 0),
            new VertxRestAccessLogItemMeta("%ab", null, null, 0)
        ) < 0
    );
    Assertions.assertEquals(0, comparator.compare(
        new VertxRestAccessLogItemMeta("%abc", null, null, 0),
        new VertxRestAccessLogItemMeta("%abc", null, null, 0)
    ));
  }

  /**
   * multiple factors test
   */
  @Test
  public void testCompareMetaComplex() {
    Assertions.assertTrue(
        comparator.compare(
            new VertxRestAccessLogItemMeta("%bcd", "}ab", null, 0),
            new VertxRestAccessLogItemMeta("%abc", "}abc", null, 0)
        ) > 0
    );
    Assertions.assertTrue(
        comparator.compare(
            new VertxRestAccessLogItemMeta("%abc", null, null, 0),
            new VertxRestAccessLogItemMeta("%bcd", "}ab", null, 0)
        ) > 0
    );
    Assertions.assertTrue(
        comparator.compare(
            new VertxRestAccessLogItemMeta("%bcd", "}abc", null, 0),
            new VertxRestAccessLogItemMeta("%abc", "}abc", null, 0)
        ) > 0
    );
    Assertions.assertTrue(
        comparator.compare(
            new VertxRestAccessLogItemMeta("%abc", "}abc", null, 1),
            new VertxRestAccessLogItemMeta("%ab", "}ab", null, 0)
        ) > 0
    );
  }

  @Test
  public void testComparePlaceholderString() {
    Assertions.assertTrue(
        VertxRestAccessLogPatternParser.comparePlaceholderString("abc", "bbc") < 0
    );
    Assertions.assertTrue(
        VertxRestAccessLogPatternParser.comparePlaceholderString("abc", "ab") < 0
    );
    Assertions.assertEquals(0, VertxRestAccessLogPatternParser.comparePlaceholderString("abc", "abc"));
    Assertions.assertTrue(
        VertxRestAccessLogPatternParser.comparePlaceholderString("bbc", "abc") > 0
    );
    Assertions.assertTrue(
        VertxRestAccessLogPatternParser.comparePlaceholderString("ab", "abc") > 0
    );
  }

  @Test
  public void testExtendedVertxRestAccessLogItemCreator() {
    final List<VertxRestAccessLogItemMeta> metaList0 = new ArrayList<>();
    metaList0.add(new VertxRestAccessLogItemMeta("%{", "}abc", null));
    metaList0.add(new VertxRestAccessLogItemMeta("%{", "}a", null));
    metaList0.add(new VertxRestAccessLogItemMeta("%_", null, null, -1));

    final List<VertxRestAccessLogItemMeta> metaList1 = new ArrayList<>();
    metaList0.add(new VertxRestAccessLogItemMeta("%a", "}abc", null));
    metaList0.add(new VertxRestAccessLogItemMeta("%0", "}abc", null, 1));
    metaList0.add(new VertxRestAccessLogItemMeta("%m", null, null));

    CompositeVertxRestAccessLogItemMeta compositeMeta0 = new CompositeVertxRestAccessLogItemMeta() {
      @Override
      public List<VertxRestAccessLogItemMeta> getAccessLogItemMetas() {
        return metaList0;
      }
    };
    CompositeVertxRestAccessLogItemMeta compositeMeta1 = new CompositeVertxRestAccessLogItemMeta() {
      @Override
      public List<VertxRestAccessLogItemMeta> getAccessLogItemMetas() {
        return metaList1;
      }
    };

    List<VertxRestAccessLogItemMeta> metaList = new ArrayList<>(1);
    metaList.add(compositeMeta0);
    metaList.add(compositeMeta1);
    metaList.add(new VertxRestAccessLogItemMeta("%{", null, null));

    VertxRestAccessLogPatternParser parser = Mockito.mock(VertxRestAccessLogPatternParser.class);
    Mockito.when(parser.getMetaList()).thenAnswer(invocation -> {
      List<VertxRestAccessLogItemMeta> resultMetaList = new ArrayList<>();
      for (VertxRestAccessLogItemMeta meta : metaList) {
        if (CompositeVertxRestAccessLogItemMeta.class.isAssignableFrom(meta.getClass())) {
          resultMetaList.addAll(((CompositeVertxRestAccessLogItemMeta) meta).getAccessLogItemMetas());
        } else {
          resultMetaList.add(meta);
        }
      }
      VertxRestAccessLogPatternParser.sortAccessLogItemMeta(resultMetaList);
      return resultMetaList;
    });

    List<VertxRestAccessLogItemMeta> accessLogItemMetaList = parser.getMetaList();

    Assertions.assertEquals(7, accessLogItemMetaList.size());
    Assertions.assertEquals("%_", accessLogItemMetaList.get(0).getPrefix());
    Assertions.assertEquals("%a", accessLogItemMetaList.get(1).getPrefix());
    Assertions.assertEquals("}abc", accessLogItemMetaList.get(1).getSuffix());
    Assertions.assertEquals("%{", accessLogItemMetaList.get(2).getPrefix());
    Assertions.assertEquals("}abc", accessLogItemMetaList.get(2).getSuffix());
    Assertions.assertEquals("%{", accessLogItemMetaList.get(3).getPrefix());
    Assertions.assertEquals("}a", accessLogItemMetaList.get(3).getSuffix());
    Assertions.assertEquals("%m", accessLogItemMetaList.get(4).getPrefix());
    Assertions.assertNull(accessLogItemMetaList.get(4).getSuffix());
    Assertions.assertEquals("%{", accessLogItemMetaList.get(5).getPrefix());
    Assertions.assertNull(accessLogItemMetaList.get(5).getSuffix());
    Assertions.assertEquals("%0", accessLogItemMetaList.get(6).getPrefix());
    Assertions.assertEquals("}abc", accessLogItemMetaList.get(6).getSuffix());
  }
}
