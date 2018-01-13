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

import java.util.Collections;
import java.util.List;

import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.impl.DefaultAccessLogPatternParser;
import org.junit.Test;

public class MethodMatcherTest {
  private static final MethodMatcher MATCHER = new MethodMatcher();

  private static final String RAW_PATTERN = "cs-method %s cs-method %T %m";

  @Test
  public void extractElementPlaceHolder() {
    List<AccessLogElementExtraction> extractionList = MATCHER.extractElementPlaceholder(RAW_PATTERN);
    Collections.sort(extractionList, DefaultAccessLogPatternParser.ACCESS_LOG_ELEMENT_EXTRACTION_COMPARATOR);
    assertEquals(3, extractionList.size());
    assertEquals(0, extractionList.get(0).getStart());
    assertEquals(9, extractionList.get(0).getEnd());
    assertEquals(13, extractionList.get(1).getStart());
    assertEquals(22, extractionList.get(1).getEnd());
    assertEquals(26, extractionList.get(2).getStart());
    assertEquals(28, extractionList.get(2).getEnd());
  }
}
