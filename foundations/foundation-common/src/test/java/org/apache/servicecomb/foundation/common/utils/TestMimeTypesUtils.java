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

package org.apache.servicecomb.foundation.common.utils;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestMimeTypesUtils {
  @Test
  public void testSortedAcceptableMimeTypes1() {
    String accept = "text/html";
    List<String> types = MimeTypesUtils.getSortedAcceptableMimeTypes(accept);
    Assertions.assertEquals(1, types.size());
    Assertions.assertEquals("text/html", types.get(0));
  }

  @Test
  public void testSortedAcceptableMimeTypes2() {
    String accept = "text/html, application/json";
    List<String> types = MimeTypesUtils.getSortedAcceptableMimeTypes(accept);
    Assertions.assertEquals(2, types.size());
    Assertions.assertEquals("text/html", types.get(0));
    Assertions.assertEquals("application/json", types.get(1));
  }

  @Test
  public void testSortedAcceptableMimeTypes3() {
    String accept = "text/html,application/json";
    List<String> types = MimeTypesUtils.getSortedAcceptableMimeTypes(accept);
    Assertions.assertEquals(2, types.size());
    Assertions.assertEquals("text/html", types.get(0));
    Assertions.assertEquals("application/json", types.get(1));
  }

  @Test
  public void testSortedAcceptableMimeTypes4() {
    String accept = "text/html; q=0.8,application/json; q=0.9";
    List<String> types = MimeTypesUtils.getSortedAcceptableMimeTypes(accept);
    Assertions.assertEquals(2, types.size());
    Assertions.assertEquals("application/json", types.get(0));
    Assertions.assertEquals("text/html", types.get(1));
  }

  @Test
  public void testSortedAcceptableMimeTypes5() {
    String accept = "text/html;q=0.8,application/json;q=0.9";
    List<String> types = MimeTypesUtils.getSortedAcceptableMimeTypes(accept);
    Assertions.assertEquals(2, types.size());
    Assertions.assertEquals("application/json", types.get(0));
    Assertions.assertEquals("text/html", types.get(1));
  }

  @Test
  public void testSortedAcceptableMimeTypes6() {
    String accept = "text/html; q=0.8,application/json; q=0.9, text/plain";
    List<String> types = MimeTypesUtils.getSortedAcceptableMimeTypes(accept);
    Assertions.assertEquals(3, types.size());
    Assertions.assertEquals("text/plain", types.get(0));
    Assertions.assertEquals("application/json", types.get(1));
    Assertions.assertEquals("text/html", types.get(2));
  }

  @Test
  public void testSortedAcceptableMimeTypes7() {
    String accept = "text/html;q=0.8,application/json;q=0.9,text/plain";
    List<String> types = MimeTypesUtils.getSortedAcceptableMimeTypes(accept);
    Assertions.assertEquals(3, types.size());
    Assertions.assertEquals("text/plain", types.get(0));
    Assertions.assertEquals("application/json", types.get(1));
    Assertions.assertEquals("text/html", types.get(2));
  }

  @Test
  public void getSortedAcceptableMimeTypesNull() {
    List<String> types = MimeTypesUtils.getSortedAcceptableMimeTypes(null);
    Assertions.assertSame(Collections.emptyList(), types);
  }
}
