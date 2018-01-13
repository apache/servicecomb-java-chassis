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

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.UriPathIncludeQueryElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;
import org.junit.Test;

public class UriPathIncludeQueryMatcherTest {
  private static final UriPathIncludeQueryMatcher MATCHER = new UriPathIncludeQueryMatcher();

  private static final String RAW_PATTERN = "cs-uri %h %{PATTERN}t cs-urics-uri %H cs-uri-query cs-uri";

  @Test
  public void testExtractElementPlaceHolder() {
    List<AccessLogElementExtraction> extractionList = MATCHER.extractElementPlaceholder(RAW_PATTERN);

    assertEquals(4, extractionList.size());
    assertEquals(0, extractionList.get(0).getStart());
    assertEquals(6, extractionList.get(0).getEnd());
    assertEquals(UriPathIncludeQueryElement.class, extractionList.get(0).getAccessLogElement().getClass());
    assertEquals(22, extractionList.get(1).getStart());
    assertEquals(28, extractionList.get(1).getEnd());
    assertEquals(UriPathIncludeQueryElement.class, extractionList.get(1).getAccessLogElement().getClass());
    assertEquals(28, extractionList.get(2).getStart());
    assertEquals(34, extractionList.get(2).getEnd());
    assertEquals(UriPathIncludeQueryElement.class, extractionList.get(2).getAccessLogElement().getClass());
    assertEquals(51, extractionList.get(3).getStart());
    assertEquals(57, extractionList.get(3).getEnd());
    assertEquals(UriPathIncludeQueryElement.class, extractionList.get(3).getAccessLogElement().getClass());
  }
}
