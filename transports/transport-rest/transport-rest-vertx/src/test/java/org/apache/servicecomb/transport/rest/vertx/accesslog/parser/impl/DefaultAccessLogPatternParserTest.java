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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.BytesWrittenElement;
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
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.TraceIdElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.UriPathIncludeQueryElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.UriPathOnlyElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.VersionOrProtocolElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;
import org.hamcrest.Matchers;
import org.junit.Test;

import mockit.Deencapsulation;

public class DefaultAccessLogPatternParserTest {
  private static final String ROW_PATTERN = "[cs-method] %m %s%T%D%h%v%p%B%b%r%U%q"
      + "cs-uri-stemcs-uri-querycs-uri%H%t%{yyyy MM dd HH:mm:ss zzz}t"
      + "%{yyyy MM dd HH:mm:ss|GMT+0|en-US}t"
      + "%{incoming-header}i"
      + "%{outgoing-header}o"
      + "%{cookie}c"
      + "%SCB-traceId";

  private static DefaultAccessLogPatternParser accessLogPatternParser = new DefaultAccessLogPatternParser();

  @Test
  @SuppressWarnings(value = "unchecked")
  public void testParsePattern() {
    List<AccessLogElementExtraction> result = accessLogPatternParser.parsePattern(ROW_PATTERN);
    assertEquals(27, result.size());

    assertThat(result.stream().map(AccessLogElementExtraction::getAccessLogElement)
            .filter(Objects::nonNull).map(AccessLogElement::getClass)
            .collect(Collectors.toList()),
        Matchers.contains(
            PlainTextElement.class,
            MethodElement.class,
            PlainTextElement.class,
            MethodElement.class,
            PlainTextElement.class,
            StatusElement.class,
            DurationSecondElement.class,
            DurationMillisecondElement.class,
            RemoteHostElement.class,
            LocalHostElement.class,
            LocalPortElement.class,
            BytesWrittenElement.class,
            BytesWrittenElement.class,
            FirstLineOfRequestElement.class,
            UriPathOnlyElement.class,
            QueryOnlyElement.class,
            UriPathOnlyElement.class,
            QueryOnlyElement.class,
            UriPathIncludeQueryElement.class,
            VersionOrProtocolElement.class,
            DatetimeConfigurableElement.class,
            DatetimeConfigurableElement.class,
            DatetimeConfigurableElement.class,
            RequestHeaderElement.class,
            ResponseHeaderElement.class,
            CookieElement.class,
            TraceIdElement.class));
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
