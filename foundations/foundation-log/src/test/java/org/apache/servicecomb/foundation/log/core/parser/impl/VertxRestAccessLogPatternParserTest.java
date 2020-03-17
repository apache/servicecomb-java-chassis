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

package org.apache.servicecomb.foundation.log.core.parser.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.servicecomb.core.event.ServerAccessLogEvent;
import org.apache.servicecomb.foundation.log.core.element.LogItem;
import org.apache.servicecomb.foundation.log.core.element.impl.CookieItem;
import org.apache.servicecomb.foundation.log.core.element.impl.DatetimeConfigurableItem;
import org.apache.servicecomb.foundation.log.core.element.impl.DurationMillisecondItem;
import org.apache.servicecomb.foundation.log.core.element.impl.DurationSecondItem;
import org.apache.servicecomb.foundation.log.core.element.impl.FirstLineOfRequestItem;
import org.apache.servicecomb.foundation.log.core.element.impl.HttpMethodItem;
import org.apache.servicecomb.foundation.log.core.element.impl.HttpStatusItem;
import org.apache.servicecomb.foundation.log.core.element.impl.InvocationContextItem;
import org.apache.servicecomb.foundation.log.core.element.impl.LocalHostItem;
import org.apache.servicecomb.foundation.log.core.element.impl.LocalPortItem;
import org.apache.servicecomb.foundation.log.core.element.impl.QueryStringItem;
import org.apache.servicecomb.foundation.log.core.element.impl.RemoteHostItem;
import org.apache.servicecomb.foundation.log.core.element.impl.RequestHeaderItem;
import org.apache.servicecomb.foundation.log.core.element.impl.RequestProtocolItem;
import org.apache.servicecomb.foundation.log.core.element.impl.ResponseHeaderItem;
import org.apache.servicecomb.foundation.log.core.element.impl.ResponseSizeItem;
import org.apache.servicecomb.foundation.log.core.element.impl.TraceIdItem;
import org.apache.servicecomb.foundation.log.core.element.impl.TransportItem;
import org.apache.servicecomb.foundation.log.core.element.impl.UrlPathItem;
import org.apache.servicecomb.foundation.log.core.element.impl.UrlPathWithQueryItem;
import org.apache.servicecomb.foundation.log.core.parser.CompositeVertxRestLogItemMeta;
import org.apache.servicecomb.foundation.log.core.parser.VertxRestLogItemMeta;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.ext.web.RoutingContext;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

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

  private static VertxRestLogPatternParser logPatternParser = new VertxRestLogPatternParser();

  private ServerAccessLogEvent accessLogEvent;

  private RoutingContext routingContext;


  @Before
  public void initStrBuilder() {
    routingContext = Mockito.mock(RoutingContext.class);
    accessLogEvent = new ServerAccessLogEvent();
    accessLogEvent.setRoutingContext(routingContext);
  }

  @Test
  public void testParsePatternFullTest() {
    List<LogItem<RoutingContext>> result = logPatternParser.parsePattern(ROW_PATTERN);
    assertEquals(29, result.size());
    StringBuilder builder = new StringBuilder();
    result.get(0).appendFormattedItem(accessLogEvent, builder);
    assertEquals("[", builder.toString());
    assertEquals(HttpMethodItem.class, result.get(1).getClass());
    builder = new StringBuilder();
    result.get(2).appendFormattedItem(accessLogEvent, builder);
    assertEquals("] ", builder.toString());
    assertEquals(HttpMethodItem.class, result.get(3).getClass());
    builder = new StringBuilder();
    result.get(4).appendFormattedItem(accessLogEvent, builder);
    assertEquals(" ", builder.toString());
    assertEquals(HttpStatusItem.class, result.get(5).getClass());
    assertEquals(DurationSecondItem.class, result.get(6).getClass());
    assertEquals(DurationMillisecondItem.class, result.get(7).getClass());
    assertEquals(RemoteHostItem.class, result.get(8).getClass());
    assertEquals(LocalHostItem.class, result.get(9).getClass());
    assertEquals(LocalPortItem.class, result.get(10).getClass());
    assertEquals(ResponseSizeItem.class, result.get(11).getClass());
    assertEquals("0", ((ResponseSizeItem) result.get(11)).getZeroBytes());
    assertEquals(ResponseSizeItem.class, result.get(12).getClass());
    assertEquals("-", ((ResponseSizeItem) result.get(12)).getZeroBytes());
    assertEquals(FirstLineOfRequestItem.class, result.get(13).getClass());
    assertEquals(UrlPathItem.class, result.get(14).getClass());
    assertEquals(QueryStringItem.class, result.get(15).getClass());
    assertEquals(UrlPathItem.class, result.get(16).getClass());
    assertEquals(QueryStringItem.class, result.get(17).getClass());
    assertEquals(UrlPathWithQueryItem.class, result.get(18).getClass());
    assertEquals(RequestProtocolItem.class, result.get(19).getClass());
    assertEquals(DatetimeConfigurableItem.class, result.get(20).getClass());
    assertEquals(DatetimeConfigurableItem.DEFAULT_DATETIME_PATTERN,
        ((DatetimeConfigurableItem) result.get(20)).getPattern());
    assertEquals(DatetimeConfigurableItem.DEFAULT_LOCALE, ((DatetimeConfigurableItem) result.get(20)).getLocale());
    assertEquals(TimeZone.getDefault(), ((DatetimeConfigurableItem) result.get(20)).getTimezone());
    assertEquals("yyyy MM dd HH:mm:ss zzz", ((DatetimeConfigurableItem) result.get(21)).getPattern());
    assertEquals(DatetimeConfigurableItem.DEFAULT_LOCALE, ((DatetimeConfigurableItem) result.get(21)).getLocale());
    assertEquals(TimeZone.getDefault(), ((DatetimeConfigurableItem) result.get(21)).getTimezone());
    assertEquals("yyyy MM dd HH:mm:ss", ((DatetimeConfigurableItem) result.get(22)).getPattern());
    assertEquals(Locale.forLanguageTag("en-US"), ((DatetimeConfigurableItem) result.get(22)).getLocale());
    assertEquals(TimeZone.getTimeZone("GMT+0"), ((DatetimeConfigurableItem) result.get(22)).getTimezone());
    assertEquals(RequestHeaderItem.class, result.get(23).getClass());
    assertEquals("incoming-header", ((RequestHeaderItem) result.get(23)).getVarName());
    assertEquals(ResponseHeaderItem.class, result.get(24).getClass());
    assertEquals("outgoing-header", ((ResponseHeaderItem) result.get(24)).getVarName());
    assertEquals(CookieItem.class, result.get(25).getClass());
    assertEquals("cookie", ((CookieItem) result.get(25)).getVarName());
    assertEquals(TraceIdItem.class, result.get(26).getClass());
    assertEquals(InvocationContextItem.class, result.get(27).getClass());
    assertEquals("ctx", ((InvocationContextItem) result.get(27)).getVarName());
    assertEquals(TransportItem.class, result.get(28).getClass());
  }

  @Test
  public void testParsePattern() {
    String pattern = " %m  cs-uri-stem %{response-header}o ";
    List<LogItem<RoutingContext>> result = logPatternParser.parsePattern(pattern);
    assertEquals(7, result.size());
    StringBuilder stringBuilder = new StringBuilder();
    result.get(0).appendFormattedItem(accessLogEvent, stringBuilder);
    assertEquals(" ", stringBuilder.toString());
    assertEquals(HttpMethodItem.class, result.get(1).getClass());
    stringBuilder = new StringBuilder();
    result.get(2).appendFormattedItem(accessLogEvent, stringBuilder);
    assertEquals("  ", stringBuilder.toString());
    assertEquals(UrlPathItem.class, result.get(3).getClass());
    stringBuilder = new StringBuilder();
    result.get(4).appendFormattedItem(accessLogEvent, stringBuilder);
    assertEquals(" ", stringBuilder.toString());
    assertEquals(ResponseHeaderItem.class, result.get(5).getClass());
    assertEquals("response-header", ((ResponseHeaderItem) result.get(5)).getVarName());
    stringBuilder = new StringBuilder();
    result.get(6).appendFormattedItem(accessLogEvent, stringBuilder);
    assertEquals(" ", stringBuilder.toString());
  }

  @Test
  public void testParsePatternWithNoBlank() {
    String pattern = "%mcs-uri-stem%{response-header}o";
    List<LogItem<RoutingContext>> result = logPatternParser.parsePattern(pattern);
    assertEquals(3, result.size());

    assertEquals(HttpMethodItem.class, result.get(0).getClass());
    assertEquals(UrlPathItem.class, result.get(1).getClass());
    assertEquals(ResponseHeaderItem.class, result.get(2).getClass());
    assertEquals("response-header", ((ResponseHeaderItem) result.get(2)).getVarName());
  }

  @Test
  public void testParsePatternComplex() {
    String pattern = "%m  cs-uri-stem %{response-header}o abc cs-uri-query %s%{request} header}i plain cs-uri";
    List<LogItem<RoutingContext>> result = logPatternParser.parsePattern(pattern);
    assertEquals(12, result.size());

    assertEquals(HttpMethodItem.class, result.get(0).getClass());
    StringBuilder stringBuilder = new StringBuilder();
    result.get(1).appendFormattedItem(accessLogEvent, stringBuilder);
    assertEquals("  ", stringBuilder.toString());
    assertEquals(UrlPathItem.class, result.get(2).getClass());
    stringBuilder = new StringBuilder();
    result.get(3).appendFormattedItem(accessLogEvent, stringBuilder);
    assertEquals(" ", stringBuilder.toString());
    assertEquals(ResponseHeaderItem.class, result.get(4).getClass());
    assertEquals("response-header", ((ResponseHeaderItem) result.get(4)).getVarName());
    stringBuilder = new StringBuilder();
    result.get(5).appendFormattedItem(accessLogEvent, stringBuilder);
    assertEquals(" abc ", stringBuilder.toString());
    assertEquals(QueryStringItem.class, result.get(6).getClass());
    stringBuilder = new StringBuilder();
    result.get(7).appendFormattedItem(accessLogEvent, stringBuilder);
    assertEquals(" ", stringBuilder.toString());
    assertEquals(HttpStatusItem.class, result.get(8).getClass());
    assertEquals(RequestHeaderItem.class, result.get(9).getClass());
    assertEquals("request} header", ((RequestHeaderItem) result.get(9)).getVarName());
    stringBuilder = new StringBuilder();
    result.get(10).appendFormattedItem(accessLogEvent, stringBuilder);
    assertEquals(" plain ", stringBuilder.toString());
    assertEquals(UrlPathWithQueryItem.class, result.get(11).getClass());
  }

  Comparator<VertxRestLogItemMeta> comparator = VertxRestLogPatternParser.accessLogItemMetaComparator;

  /**
   * one factor test
   */
  @Test
  public void testCompareMetaSimple() {
    Assert.assertTrue(
        comparator.compare(
            new VertxRestLogItemMeta(null, null, null, 0),
            new VertxRestLogItemMeta(null, null, null, 1)
        ) < 0
    );
    Assert.assertTrue(
        comparator.compare(
            new VertxRestLogItemMeta(null, "}abc", null, 0),
            new VertxRestLogItemMeta(null, null, null, 0)
        ) < 0
    );
    Assert.assertTrue(
        comparator.compare(
            new VertxRestLogItemMeta(null, "}abc", null, 0),
            new VertxRestLogItemMeta(null, "}de", null, 0)
        ) < 0
    );
    Assert.assertTrue(
        comparator.compare(
            new VertxRestLogItemMeta(null, "}abc", null, 0),
            new VertxRestLogItemMeta(null, "}ab", null, 0)
        ) < 0
    );
    Assert.assertTrue(
        comparator.compare(
            new VertxRestLogItemMeta("%abc", null, null, 0),
            new VertxRestLogItemMeta("%de", null, null, 0)
        ) < 0
    );
    Assert.assertTrue(
        comparator.compare(
            new VertxRestLogItemMeta("%abc", null, null, 0),
            new VertxRestLogItemMeta("%ab", null, null, 0)
        ) < 0
    );
    Assert.assertEquals(0, comparator.compare(
        new VertxRestLogItemMeta("%abc", null, null, 0),
        new VertxRestLogItemMeta("%abc", null, null, 0)
    ));
  }

  /**
   * multiple factors test
   */
  @Test
  public void testCompareMetaComplex() {
    Assert.assertTrue(
        comparator.compare(
            new VertxRestLogItemMeta("%bcd", "}ab", null, 0),
            new VertxRestLogItemMeta("%abc", "}abc", null, 0)
        ) > 0
    );
    Assert.assertTrue(
        comparator.compare(
            new VertxRestLogItemMeta("%abc", null, null, 0),
            new VertxRestLogItemMeta("%bcd", "}ab", null, 0)
        ) > 0
    );
    Assert.assertTrue(
        comparator.compare(
            new VertxRestLogItemMeta("%bcd", "}abc", null, 0),
            new VertxRestLogItemMeta("%abc", "}abc", null, 0)
        ) > 0
    );
    Assert.assertTrue(
        comparator.compare(
            new VertxRestLogItemMeta("%abc", "}abc", null, 1),
            new VertxRestLogItemMeta("%ab", "}ab", null, 0)
        ) > 0
    );
  }

  @Test
  public void testComparePlaceholderString() {
    Assert.assertTrue(
        VertxRestLogPatternParser.comparePlaceholderString("abc", "bbc") < 0
    );
    Assert.assertTrue(
        VertxRestLogPatternParser.comparePlaceholderString("abc", "ab") < 0
    );
    Assert.assertEquals(0, VertxRestLogPatternParser.comparePlaceholderString("abc", "abc"));
    Assert.assertTrue(
        VertxRestLogPatternParser.comparePlaceholderString("bbc", "abc") > 0
    );
    Assert.assertTrue(
        VertxRestLogPatternParser.comparePlaceholderString("ab", "abc") > 0
    );
  }

  @Test
  public void testExtendedVertxRestAccessLogItemCreator() {
    final List<VertxRestLogItemMeta> metaList0 = new ArrayList<>();
    metaList0.add(new VertxRestLogItemMeta("%{", "}abc", null));
    metaList0.add(new VertxRestLogItemMeta("%{", "}a", null));
    metaList0.add(new VertxRestLogItemMeta("%_", null, null, -1));

    final List<VertxRestLogItemMeta> metaList1 = new ArrayList<>();
    metaList0.add(new VertxRestLogItemMeta("%a", "}abc", null));
    metaList0.add(new VertxRestLogItemMeta("%0", "}abc", null, 1));
    metaList0.add(new VertxRestLogItemMeta("%m", null, null));

    new MockUp<VertxRestLogPatternParser>() {
      @Mock
      List<VertxRestLogItemMeta> loadVertxRestLogItemMeta() {
        List<VertxRestLogItemMeta> metaList = new ArrayList<>(1);
        CompositeVertxRestLogItemMeta compositeMeta0 = new CompositeVertxRestLogItemMeta() {
          @Override
          public List<VertxRestLogItemMeta> getAccessLogItemMetas() {
            return metaList0;
          }
        };
        CompositeVertxRestLogItemMeta compositeMeta1 = new CompositeVertxRestLogItemMeta() {
          @Override
          public List<VertxRestLogItemMeta> getAccessLogItemMetas() {
            return metaList1;
          }
        };
        metaList.add(compositeMeta0);
        metaList.add(compositeMeta1);
        metaList.add(new VertxRestLogItemMeta("%{", null, null));
        return metaList;
      }
    };

    VertxRestLogPatternParser parser = new VertxRestLogPatternParser();

    List<VertxRestLogItemMeta> accessLogItemMetaList =
        Deencapsulation.getField(parser, "metaList");

    assertEquals(7, accessLogItemMetaList.size());
    assertEquals("%_", accessLogItemMetaList.get(0).getPrefix());
    assertEquals("%a", accessLogItemMetaList.get(1).getPrefix());
    assertEquals("}abc", accessLogItemMetaList.get(1).getSuffix());
    assertEquals("%{", accessLogItemMetaList.get(2).getPrefix());
    assertEquals("}abc", accessLogItemMetaList.get(2).getSuffix());
    assertEquals("%{", accessLogItemMetaList.get(3).getPrefix());
    assertEquals("}a", accessLogItemMetaList.get(3).getSuffix());
    assertEquals("%m", accessLogItemMetaList.get(4).getPrefix());
    assertNull(accessLogItemMetaList.get(4).getSuffix());
    assertEquals("%{", accessLogItemMetaList.get(5).getPrefix());
    assertNull(accessLogItemMetaList.get(5).getSuffix());
    assertEquals("%0", accessLogItemMetaList.get(6).getPrefix());
    assertEquals("}abc", accessLogItemMetaList.get(6).getSuffix());
  }
}
