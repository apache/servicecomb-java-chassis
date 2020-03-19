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
package org.apache.servicecomb.it.testcase;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.it.extend.engine.GateRestTemplate;
import org.apache.servicecomb.it.schema.Generic;
import org.apache.servicecomb.it.schema.User;
import org.junit.Test;

public class TestGenericEdge {
  private static GateRestTemplate client = GateRestTemplate.createEdgeRestTemplate("generic");

  @Test
  @SuppressWarnings("unchecked")
  public void testGenericMap() {
    Generic<Map<String, String>> mapGeneric = new Generic<>();
    Map<String, String> map = new HashMap<>();
    map.put("test", "hello");
    mapGeneric.value = map;
    Generic<Map<String, String>> result = client.postForObject("/genericMap", mapGeneric, Generic.class);
    String test = result.value.get("test");
    assertEquals(test, "hello");
    result = client.postForObject("/genericMap", mapGeneric, Generic.class);
    test = result.value.get("test");
    assertEquals(test, "hello");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testGenericMapList() {
    Generic<Map<String, List<String>>> mapListGeneric = new Generic<>();
    Map<String, List<String>> map = new HashMap<>();
    List<String> list = new ArrayList<>();
    list.add("hello");
    map.put("test", list);
    mapListGeneric.value = map;
    Generic<Map<String, List<String>>> result = client.postForObject("/genericMapList", mapListGeneric, Generic.class);
    String test = result.value.get("test").get(0);
    assertEquals("hello", test);
    result = client.postForObject("/genericMapList", mapListGeneric, Generic.class);
    test = result.value.get("test").get(0);
    assertEquals("hello", test);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testGenericUser() {
    Generic<User> generic = new Generic<>();
    generic.value = new User();

    Generic<Map<String, Object>> result = client.postForObject("/genericUser", generic, Generic.class);
    assertEquals("nameA", result.value.get("name"));
    assertEquals(100, result.value.get("age"));
    result = client.postForObject("/genericUser", generic, Generic.class);
    assertEquals("nameA", result.value.get("name"));
    assertEquals(100, result.value.get("age"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testGenericMapListUser() {
    Generic<Map<String, List<User>>> mapListUserGeneric = new Generic<>();
    Map<String, List<User>> map = new HashMap<>();
    List<User> list = new ArrayList<>();
    list.add(new User());
    map.put("test", list);
    mapListUserGeneric.value = map;

    Generic<Map<String, List<Map<String, Object>>>> result = client
        .postForObject("/genericMapListUser", mapListUserGeneric, Generic.class);
    Map<String, Object> resultUser = result.value.get("test").get(0);
    assertEquals("nameA", resultUser.get("name"));
    assertEquals(100, resultUser.get("age"));

    result = client.postForObject("/genericMapListUser", mapListUserGeneric, Generic.class);
    resultUser = result.value.get("test").get(0);
    assertEquals("nameA", resultUser.get("name"));
    assertEquals(100, resultUser.get("age"));
  }
}
