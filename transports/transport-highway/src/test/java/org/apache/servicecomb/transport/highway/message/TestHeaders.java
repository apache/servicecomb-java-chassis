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
package org.apache.servicecomb.transport.highway.message;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestHeaders {
  @Test
  public void test1() {
    Headers headers = new Headers();
    Assertions.assertNull(headers.getFirst("h1"));
    Assertions.assertNull(headers.getHeader("h1"));

    Map<String, List<Object>> headerMap = new HashMap<>();
    List<Object> h1Value = Arrays.asList("h1v1", "h1v2");
    headerMap.put("h1", h1Value);
    headerMap.put("h2", null);
    headerMap.put("h3", Arrays.asList());
    headers.setHeaderMap(headerMap);

    Assertions.assertEquals(headerMap, headers.getHeaderMap());
    Assertions.assertEquals("h1v1", headers.getFirst("h1"));
    Assertions.assertNull(headers.getFirst("h2"));
    Assertions.assertNull(headers.getFirst("h3"));
    Assertions.assertEquals(h1Value, headers.getHeader("h1"));
  }

  @Test
  public void test2() {
    Headers headers = new Headers();
    headers.addHeader("h1", "h1v1");
    headers.addHeader("h1", "h1v2");

    Assertions.assertEquals("h1v1", headers.getFirst("h1"));
  }

  @Test
  public void addHeader_list() {
    Headers headers = new Headers();
    headers.addHeader("h", Arrays.asList("v1", "v2"));
    headers.addHeader("h", Arrays.asList("v3"));

    MatcherAssert.assertThat(headers.getHeader("h"), Matchers.contains("v1", "v2", "v3"));
  }
}
