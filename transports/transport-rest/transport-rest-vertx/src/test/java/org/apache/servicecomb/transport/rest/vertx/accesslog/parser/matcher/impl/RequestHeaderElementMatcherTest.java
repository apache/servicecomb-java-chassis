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

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.RequestHeaderElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;
import org.junit.Test;

public class RequestHeaderElementMatcherTest {
  private static final RequestHeaderElementMatcher MATCHER = new RequestHeaderElementMatcher();

  @Test
  public void extractElementPlaceholder() {
    List<AccessLogElementExtraction> extractionList = MATCHER
        .extractElementPlaceholder("%{header0}i %h %{yyyyMMdd HH:mm:ss zzz}t %{header1}i %b%b %H %{header2}i");

    assertEquals(3, extractionList.size());
    assertEquals(0, extractionList.get(0).getStart());
    assertEquals(11, extractionList.get(0).getEnd());
    assertEquals(41, extractionList.get(1).getStart());
    assertEquals(52, extractionList.get(1).getEnd());
    assertEquals(61, extractionList.get(2).getStart());
    assertEquals(72, extractionList.get(2).getEnd());

    assertEquals(RequestHeaderElement.class, extractionList.get(0).getAccessLogElement().getClass());
    assertEquals(RequestHeaderElement.class, extractionList.get(1).getAccessLogElement().getClass());
    assertEquals(RequestHeaderElement.class, extractionList.get(2).getAccessLogElement().getClass());

    assertEquals("header0",
        ((RequestHeaderElement) (extractionList.get(0).getAccessLogElement())).getIdentifier());
    assertEquals("header1",
        ((RequestHeaderElement) (extractionList.get(1).getAccessLogElement())).getIdentifier());
    assertEquals("header2",
        ((RequestHeaderElement) (extractionList.get(2).getAccessLogElement())).getIdentifier());
  }

  @Test
  public void extractElementPlaceholderOnNotMatched() {
    List<AccessLogElementExtraction> extractionList = MATCHER
        .extractElementPlaceholder("%{header0}o %h %{yyyyMMdd HH:mm:ss zzz}t %{header1}o %b%b %H %{header2}o");

    assertEquals(0, extractionList.size());
  }
}
