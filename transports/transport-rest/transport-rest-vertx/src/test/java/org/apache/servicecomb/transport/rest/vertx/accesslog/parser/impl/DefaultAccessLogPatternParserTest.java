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
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.BytesWrittenV1Element;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.BytesWrittenV2Element;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.CookieElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DatetimeConfigurableElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DurationMillisecondElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DurationSecondElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.FirstLineOfRequestElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.LocalHostElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.LocalPortElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.MethodElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.PlainTextElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.QueryOnlyElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.RemoteHostElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.RequestHeaderElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.ResponseHeaderElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.StatusElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.UriPathIncludeQueryElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.UriPathOnlyElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.VersionOrProtocolElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;
import org.junit.Test;

import mockit.Deencapsulation;

public class DefaultAccessLogPatternParserTest {
  private static final String ROW_PATTERN = "[cs-method] %m %s%T%D%h%v%p%B%b%r%U%q"
      + "cs-uri-stemcs-uri-querycs-uri%H%t%{yyyy MM dd HH:mm:ss zzz}t"
      + "%{yyyy MM dd HH:mm:ss|GMT+0|en-US}t"
      + "%{incoming-header}i"
      + "%{outgoing-header}o"
      + "%{cookie}c";

  private static DefaultAccessLogPatternParser accessLogPatternParser = new DefaultAccessLogPatternParser();

  @Test
  public void testParsePattern() {
    List<AccessLogElementExtraction> result = accessLogPatternParser.parsePattern(ROW_PATTERN);
    assertEquals(26, result.size());
    assertEquals(PlainTextElement.class, result.get(0).getAccessLogElement().getClass());
    assertEquals(MethodElement.class, result.get(1).getAccessLogElement().getClass());
    assertEquals(PlainTextElement.class, result.get(2).getAccessLogElement().getClass());
    assertEquals(MethodElement.class, result.get(3).getAccessLogElement().getClass());
    assertEquals(PlainTextElement.class, result.get(4).getAccessLogElement().getClass());
    assertEquals(StatusElement.class, result.get(5).getAccessLogElement().getClass());
    assertEquals(DurationSecondElement.class, result.get(6).getAccessLogElement().getClass());
    assertEquals(DurationMillisecondElement.class, result.get(7).getAccessLogElement().getClass());
    assertEquals(RemoteHostElement.class, result.get(8).getAccessLogElement().getClass());
    assertEquals(LocalHostElement.class, result.get(9).getAccessLogElement().getClass());
    assertEquals(LocalPortElement.class, result.get(10).getAccessLogElement().getClass());
    assertEquals(BytesWrittenV1Element.class, result.get(11).getAccessLogElement().getClass());
    assertEquals(BytesWrittenV2Element.class, result.get(12).getAccessLogElement().getClass());
    assertEquals(FirstLineOfRequestElement.class, result.get(13).getAccessLogElement().getClass());
    assertEquals(UriPathOnlyElement.class, result.get(14).getAccessLogElement().getClass());
    assertEquals(QueryOnlyElement.class, result.get(15).getAccessLogElement().getClass());
    assertEquals(UriPathOnlyElement.class, result.get(16).getAccessLogElement().getClass());
    assertEquals(QueryOnlyElement.class, result.get(17).getAccessLogElement().getClass());
    assertEquals(UriPathIncludeQueryElement.class, result.get(18).getAccessLogElement().getClass());
    assertEquals(VersionOrProtocolElement.class, result.get(19).getAccessLogElement().getClass());
    assertEquals(DatetimeConfigurableElement.class, result.get(20).getAccessLogElement().getClass());
    assertEquals(DatetimeConfigurableElement.class, result.get(21).getAccessLogElement().getClass());
    assertEquals(DatetimeConfigurableElement.class, result.get(22).getAccessLogElement().getClass());
    assertEquals(RequestHeaderElement.class, result.get(23).getAccessLogElement().getClass());
    assertEquals(ResponseHeaderElement.class, result.get(24).getAccessLogElement().getClass());
    assertEquals(CookieElement.class, result.get(25).getAccessLogElement().getClass());
  }

  @Test
  public void testCheckExtractionList() {
    List<AccessLogElementExtraction> extractionList = new ArrayList<>(3);
    extractionList.add(new AccessLogElementExtraction().setStart(0).setEnd(3));
    extractionList.add(new AccessLogElementExtraction().setStart(3).setEnd(6));
    extractionList.add(new AccessLogElementExtraction().setStart(5).setEnd(9));

    try {
      Deencapsulation.invoke(new DefaultAccessLogPatternParser(), "checkExtractionList", extractionList);
      fail("expect an exception");
    } catch (Exception e) {
      assertEquals(IllegalArgumentException.class, e.getClass());
      assertEquals("access log pattern contains illegal placeholder, please check it.", e.getMessage());
    }
  }
}
