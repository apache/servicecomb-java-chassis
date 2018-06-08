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

package org.apache.servicecomb.transport.rest.vertx.accesslog.parser.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.CookieItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DatetimeConfigurableItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DurationMillisecondItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DurationSecondItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.FirstLineOfRequestItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.HttpMethodItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.HttpStatusItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.InvocationContextItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.LocalHostItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.LocalPortItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.QueryStringItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.RemoteHostItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.RequestHeaderItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.RequestProtocolItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.ResponseHeaderItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.ResponseSizeItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.TraceIdItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.UrlPathItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.UrlPathWithQueryItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.CompositeVertxRestAccessLogItemMeta;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.VertxRestAccessLogItemMeta;
import org.junit.Assert;
import org.junit.Test;

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
      + "%{ctx}SCB-ctx";

  private static VertxRestAccessLogPatternParser accessLogPatternParser = new VertxRestAccessLogPatternParser();

  @Test
  public void testParsePatternFullTest() {
    List<AccessLogItem<RoutingContext>> result = accessLogPatternParser.parsePattern(ROW_PATTERN);
    assertEquals(28, result.size());

    assertEquals("[", result.get(0).getFormattedItem(null));
    assertEquals(HttpMethodItem.class, result.get(1).getClass());
    assertEquals("] ", result.get(2).getFormattedItem(null));
    assertEquals(HttpMethodItem.class, result.get(3).getClass());
    assertEquals(" ", result.get(4).getFormattedItem(null));
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
  }

  @Test
  public void testParsePattern() {
    String pattern = " %m  cs-uri-stem %{response-header}o ";
    List<AccessLogItem<RoutingContext>> result = accessLogPatternParser.parsePattern(pattern);
    assertEquals(7, result.size());

    assertEquals(" ", result.get(0).getFormattedItem(null));
    assertEquals(HttpMethodItem.class, result.get(1).getClass());
    assertEquals("  ", result.get(2).getFormattedItem(null));
    assertEquals(UrlPathItem.class, result.get(3).getClass());
    assertEquals(" ", result.get(4).getFormattedItem(null));
    assertEquals(ResponseHeaderItem.class, result.get(5).getClass());
    assertEquals("response-header", ((ResponseHeaderItem) result.get(5)).getVarName());
    assertEquals(" ", result.get(6).getFormattedItem(null));
  }

  @Test
  public void testParsePatternWithNoBlank() {
    String pattern = "%mcs-uri-stem%{response-header}o";
    List<AccessLogItem<RoutingContext>> result = accessLogPatternParser.parsePattern(pattern);
    assertEquals(3, result.size());

    assertEquals(HttpMethodItem.class, result.get(0).getClass());
    assertEquals(UrlPathItem.class, result.get(1).getClass());
    assertEquals(ResponseHeaderItem.class, result.get(2).getClass());
    assertEquals("response-header", ((ResponseHeaderItem) result.get(2)).getVarName());
  }

  @Test
  public void testParsePatternComplex() {
    String pattern = "%m  cs-uri-stem %{response-header}o abc cs-uri-query %s%{request} header}i plain cs-uri";
    List<AccessLogItem<RoutingContext>> result = accessLogPatternParser.parsePattern(pattern);
    assertEquals(12, result.size());

    assertEquals(HttpMethodItem.class, result.get(0).getClass());
    assertEquals("  ", result.get(1).getFormattedItem(null));
    assertEquals(UrlPathItem.class, result.get(2).getClass());
    assertEquals(" ", result.get(3).getFormattedItem(null));
    assertEquals(ResponseHeaderItem.class, result.get(4).getClass());
    assertEquals("response-header", ((ResponseHeaderItem) result.get(4)).getVarName());
    assertEquals(" abc ", result.get(5).getFormattedItem(null));
    assertEquals(QueryStringItem.class, result.get(6).getClass());
    assertEquals(" ", result.get(7).getFormattedItem(null));
    assertEquals(HttpStatusItem.class, result.get(8).getClass());
    assertEquals(RequestHeaderItem.class, result.get(9).getClass());
    assertEquals("request} header", ((RequestHeaderItem) result.get(9)).getVarName());
    assertEquals(" plain ", result.get(10).getFormattedItem(null));
    assertEquals(UrlPathWithQueryItem.class, result.get(11).getClass());
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
      List<VertxRestAccessLogItemMeta> loadVertxRestAccessLogItemMeta() {
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
