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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.servicecomb.common.accessLog.core.element.AccessLogItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.CookieItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.DatetimeConfigurableItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.DurationMillisecondItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.DurationSecondItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.FirstLineOfRequestItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.HttpMethodItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.HttpStatusItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.InvocationContextItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.LocalHostItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.LocalPortItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.QueryStringItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.RemoteHostItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.RequestHeaderItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.RequestProtocolItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.ResponseHeaderItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.ResponseSizeItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.TraceIdItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.TransportItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.UrlPathItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.UrlPathWithQueryItemAccess;
import org.apache.servicecomb.common.accessLog.core.parser.CompositeVertxRestAccessLogItemMeta;
import org.apache.servicecomb.common.accessLog.core.parser.VertxRestAccessLogItemMeta;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;
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

  private static VertxRestAccessLogPatternParser logPatternParser = new VertxRestAccessLogPatternParser();

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
    List<AccessLogItem<RoutingContext>> result = logPatternParser.parsePattern(ROW_PATTERN);
    assertEquals(29, result.size());
    StringBuilder builder = new StringBuilder();
    result.get(0).appendServerFormattedItem(accessLogEvent, builder);
    assertEquals("[", builder.toString());
    assertEquals(HttpMethodItemAccess.class, result.get(1).getClass());
    builder = new StringBuilder();
    result.get(2).appendServerFormattedItem(accessLogEvent, builder);
    assertEquals("] ", builder.toString());
    assertEquals(HttpMethodItemAccess.class, result.get(3).getClass());
    builder = new StringBuilder();
    result.get(4).appendServerFormattedItem(accessLogEvent, builder);
    assertEquals(" ", builder.toString());
    assertEquals(HttpStatusItemAccess.class, result.get(5).getClass());
    assertEquals(DurationSecondItemAccess.class, result.get(6).getClass());
    assertEquals(DurationMillisecondItemAccess.class, result.get(7).getClass());
    assertEquals(RemoteHostItemAccess.class, result.get(8).getClass());
    assertEquals(LocalHostItemAccess.class, result.get(9).getClass());
    assertEquals(LocalPortItemAccess.class, result.get(10).getClass());
    assertEquals(ResponseSizeItemAccess.class, result.get(11).getClass());
    assertEquals("0", ((ResponseSizeItemAccess) result.get(11)).getZeroBytes());
    assertEquals(ResponseSizeItemAccess.class, result.get(12).getClass());
    assertEquals("-", ((ResponseSizeItemAccess) result.get(12)).getZeroBytes());
    assertEquals(FirstLineOfRequestItemAccess.class, result.get(13).getClass());
    assertEquals(UrlPathItemAccess.class, result.get(14).getClass());
    assertEquals(QueryStringItemAccess.class, result.get(15).getClass());
    assertEquals(UrlPathItemAccess.class, result.get(16).getClass());
    assertEquals(QueryStringItemAccess.class, result.get(17).getClass());
    assertEquals(UrlPathWithQueryItemAccess.class, result.get(18).getClass());
    assertEquals(RequestProtocolItemAccess.class, result.get(19).getClass());
    assertEquals(DatetimeConfigurableItemAccess.class, result.get(20).getClass());
    assertEquals(DatetimeConfigurableItemAccess.DEFAULT_DATETIME_PATTERN,
        ((DatetimeConfigurableItemAccess) result.get(20)).getPattern());
    assertEquals(DatetimeConfigurableItemAccess.DEFAULT_LOCALE, ((DatetimeConfigurableItemAccess) result.get(20)).getLocale());
    assertEquals(TimeZone.getDefault(), ((DatetimeConfigurableItemAccess) result.get(20)).getTimezone());
    assertEquals("yyyy MM dd HH:mm:ss zzz", ((DatetimeConfigurableItemAccess) result.get(21)).getPattern());
    assertEquals(DatetimeConfigurableItemAccess.DEFAULT_LOCALE, ((DatetimeConfigurableItemAccess) result.get(21)).getLocale());
    assertEquals(TimeZone.getDefault(), ((DatetimeConfigurableItemAccess) result.get(21)).getTimezone());
    assertEquals("yyyy MM dd HH:mm:ss", ((DatetimeConfigurableItemAccess) result.get(22)).getPattern());
    assertEquals(Locale.forLanguageTag("en-US"), ((DatetimeConfigurableItemAccess) result.get(22)).getLocale());
    assertEquals(TimeZone.getTimeZone("GMT+0"), ((DatetimeConfigurableItemAccess) result.get(22)).getTimezone());
    assertEquals(RequestHeaderItemAccess.class, result.get(23).getClass());
    assertEquals("incoming-header", ((RequestHeaderItemAccess) result.get(23)).getVarName());
    assertEquals(ResponseHeaderItemAccess.class, result.get(24).getClass());
    assertEquals("outgoing-header", ((ResponseHeaderItemAccess) result.get(24)).getVarName());
    assertEquals(CookieItemAccess.class, result.get(25).getClass());
    assertEquals("cookie", ((CookieItemAccess) result.get(25)).getVarName());
    assertEquals(TraceIdItemAccess.class, result.get(26).getClass());
    assertEquals(InvocationContextItemAccess.class, result.get(27).getClass());
    assertEquals("ctx", ((InvocationContextItemAccess) result.get(27)).getVarName());
    assertEquals(TransportItemAccess.class, result.get(28).getClass());
  }

  @Test
  public void testParsePattern() {
    String pattern = " %m  cs-uri-stem %{response-header}o ";
    List<AccessLogItem<RoutingContext>> result = logPatternParser.parsePattern(pattern);
    assertEquals(7, result.size());
    StringBuilder stringBuilder = new StringBuilder();
    result.get(0).appendServerFormattedItem(accessLogEvent, stringBuilder);
    assertEquals(" ", stringBuilder.toString());
    assertEquals(HttpMethodItemAccess.class, result.get(1).getClass());
    stringBuilder = new StringBuilder();
    result.get(2).appendServerFormattedItem(accessLogEvent, stringBuilder);
    assertEquals("  ", stringBuilder.toString());
    assertEquals(UrlPathItemAccess.class, result.get(3).getClass());
    stringBuilder = new StringBuilder();
    result.get(4).appendServerFormattedItem(accessLogEvent, stringBuilder);
    assertEquals(" ", stringBuilder.toString());
    assertEquals(ResponseHeaderItemAccess.class, result.get(5).getClass());
    assertEquals("response-header", ((ResponseHeaderItemAccess) result.get(5)).getVarName());
    stringBuilder = new StringBuilder();
    result.get(6).appendServerFormattedItem(accessLogEvent, stringBuilder);
    assertEquals(" ", stringBuilder.toString());
  }

  @Test
  public void testParsePatternWithNoBlank() {
    String pattern = "%mcs-uri-stem%{response-header}o";
    List<AccessLogItem<RoutingContext>> result = logPatternParser.parsePattern(pattern);
    assertEquals(3, result.size());

    assertEquals(HttpMethodItemAccess.class, result.get(0).getClass());
    assertEquals(UrlPathItemAccess.class, result.get(1).getClass());
    assertEquals(ResponseHeaderItemAccess.class, result.get(2).getClass());
    assertEquals("response-header", ((ResponseHeaderItemAccess) result.get(2)).getVarName());
  }

  @Test
  public void testParsePatternComplex() {
    String pattern = "%m  cs-uri-stem %{response-header}o abc cs-uri-query %s%{request} header}i plain cs-uri";
    List<AccessLogItem<RoutingContext>> result = logPatternParser.parsePattern(pattern);
    assertEquals(12, result.size());

    assertEquals(HttpMethodItemAccess.class, result.get(0).getClass());
    StringBuilder stringBuilder = new StringBuilder();
    result.get(1).appendServerFormattedItem(accessLogEvent, stringBuilder);
    assertEquals("  ", stringBuilder.toString());
    assertEquals(UrlPathItemAccess.class, result.get(2).getClass());
    stringBuilder = new StringBuilder();
    result.get(3).appendServerFormattedItem(accessLogEvent, stringBuilder);
    assertEquals(" ", stringBuilder.toString());
    assertEquals(ResponseHeaderItemAccess.class, result.get(4).getClass());
    assertEquals("response-header", ((ResponseHeaderItemAccess) result.get(4)).getVarName());
    stringBuilder = new StringBuilder();
    result.get(5).appendServerFormattedItem(accessLogEvent, stringBuilder);
    assertEquals(" abc ", stringBuilder.toString());
    assertEquals(QueryStringItemAccess.class, result.get(6).getClass());
    stringBuilder = new StringBuilder();
    result.get(7).appendServerFormattedItem(accessLogEvent, stringBuilder);
    assertEquals(" ", stringBuilder.toString());
    assertEquals(HttpStatusItemAccess.class, result.get(8).getClass());
    assertEquals(RequestHeaderItemAccess.class, result.get(9).getClass());
    assertEquals("request} header", ((RequestHeaderItemAccess) result.get(9)).getVarName());
    stringBuilder = new StringBuilder();
    result.get(10).appendServerFormattedItem(accessLogEvent, stringBuilder);
    assertEquals(" plain ", stringBuilder.toString());
    assertEquals(UrlPathWithQueryItemAccess.class, result.get(11).getClass());
  }

  Comparator<VertxRestAccessLogItemMeta> comparator = VertxRestAccessLogPatternParser.accessLogItemMetaComparator;

  /**
   * one factor test
   */
  @Test
  public void testCompareMetaSimple() {
    Assert.assertTrue(
        comparator.compare(
            new VertxRestAccessLogItemMeta(null, null, null, 0),
            new VertxRestAccessLogItemMeta(null, null, null, 1)
        ) < 0
    );
    Assert.assertTrue(
        comparator.compare(
            new VertxRestAccessLogItemMeta(null, "}abc", null, 0),
            new VertxRestAccessLogItemMeta(null, null, null, 0)
        ) < 0
    );
    Assert.assertTrue(
        comparator.compare(
            new VertxRestAccessLogItemMeta(null, "}abc", null, 0),
            new VertxRestAccessLogItemMeta(null, "}de", null, 0)
        ) < 0
    );
    Assert.assertTrue(
        comparator.compare(
            new VertxRestAccessLogItemMeta(null, "}abc", null, 0),
            new VertxRestAccessLogItemMeta(null, "}ab", null, 0)
        ) < 0
    );
    Assert.assertTrue(
        comparator.compare(
            new VertxRestAccessLogItemMeta("%abc", null, null, 0),
            new VertxRestAccessLogItemMeta("%de", null, null, 0)
        ) < 0
    );
    Assert.assertTrue(
        comparator.compare(
            new VertxRestAccessLogItemMeta("%abc", null, null, 0),
            new VertxRestAccessLogItemMeta("%ab", null, null, 0)
        ) < 0
    );
    Assert.assertEquals(0, comparator.compare(
        new VertxRestAccessLogItemMeta("%abc", null, null, 0),
        new VertxRestAccessLogItemMeta("%abc", null, null, 0)
    ));
  }

  /**
   * multiple factors test
   */
  @Test
  public void testCompareMetaComplex() {
    Assert.assertTrue(
        comparator.compare(
            new VertxRestAccessLogItemMeta("%bcd", "}ab", null, 0),
            new VertxRestAccessLogItemMeta("%abc", "}abc", null, 0)
        ) > 0
    );
    Assert.assertTrue(
        comparator.compare(
            new VertxRestAccessLogItemMeta("%abc", null, null, 0),
            new VertxRestAccessLogItemMeta("%bcd", "}ab", null, 0)
        ) > 0
    );
    Assert.assertTrue(
        comparator.compare(
            new VertxRestAccessLogItemMeta("%bcd", "}abc", null, 0),
            new VertxRestAccessLogItemMeta("%abc", "}abc", null, 0)
        ) > 0
    );
    Assert.assertTrue(
        comparator.compare(
            new VertxRestAccessLogItemMeta("%abc", "}abc", null, 1),
            new VertxRestAccessLogItemMeta("%ab", "}ab", null, 0)
        ) > 0
    );
  }

  @Test
  public void testComparePlaceholderString() {
    Assert.assertTrue(
        VertxRestAccessLogPatternParser.comparePlaceholderString("abc", "bbc") < 0
    );
    Assert.assertTrue(
        VertxRestAccessLogPatternParser.comparePlaceholderString("abc", "ab") < 0
    );
    Assert.assertEquals(0, VertxRestAccessLogPatternParser.comparePlaceholderString("abc", "abc"));
    Assert.assertTrue(
        VertxRestAccessLogPatternParser.comparePlaceholderString("bbc", "abc") > 0
    );
    Assert.assertTrue(
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

    new MockUp<VertxRestAccessLogPatternParser>() {
      @Mock
      List<VertxRestAccessLogItemMeta> loadVertxRestLogItemMeta() {
        List<VertxRestAccessLogItemMeta> metaList = new ArrayList<>(1);
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
        metaList.add(compositeMeta0);
        metaList.add(compositeMeta1);
        metaList.add(new VertxRestAccessLogItemMeta("%{", null, null));
        return metaList;
      }
    };

    VertxRestAccessLogPatternParser parser = new VertxRestAccessLogPatternParser();

    List<VertxRestAccessLogItemMeta> accessLogItemMetaList =
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
