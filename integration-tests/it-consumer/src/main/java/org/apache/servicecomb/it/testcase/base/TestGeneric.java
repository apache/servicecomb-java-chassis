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
package org.apache.servicecomb.it.testcase.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.ws.Holder;

import org.apache.servicecomb.it.Consumers;
import org.apache.servicecomb.it.schema.Generic;
import org.apache.servicecomb.it.schema.User;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class TestGeneric {
  interface GenericIntf {
    Holder<User> holderUser(Holder<User> input);

    Generic<User> genericUser(Generic<User> input);

    Generic<Long> genericLong(Generic<Long> input);

    Generic<Date> genericDate(Generic<Date> input);

    Generic<HttpStatus> genericEnum(Generic<HttpStatus> input);

    Generic<Generic<User>> genericGenericUser(Generic<Generic<User>> input);

    Generic<Map<String, String>> genericMap(Generic<Map<String, String>> mapGeneric);

    Generic<Map<String, List<String>>> genericMapList(Generic<Map<String, List<String>>> mapListGeneric);

    Generic<Map<String, List<User>>> genericMapListUser(Generic<Map<String, List<User>>> mapListUserGeneric);

    List<List<String>> genericNestedListString(List<List<String>> nestedListString);

    List<List<User>> genericNestedListUser(List<List<User>> nestedListUser);
  }

  private static Consumers<GenericIntf> consumers = new Consumers<>("generic", GenericIntf.class);

  private String expectUserStr = "{\"name\":\"nameA\",\"age\":100,\"index\":0,\"names\":null}";

  private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

  @Test
  public void testHolderUser_intf() {
    Holder<User> holder = new Holder<>(new User());
    Holder<User> result = consumers.getIntf().holderUser(holder);
    assertEquals(result.value.jsonString(), expectUserStr);
  }

  @Test
  public void testHolderUser_rt() {
    Holder<User> holder = new Holder<>(new User());
    @SuppressWarnings("unchecked")
    Holder<User> result = consumers.getSCBRestTemplate().postForObject("/holderUser", holder, Holder.class);
    assertEquals(result.value.jsonString(), expectUserStr);
  }

  @Test
  public void testGenericUser_intf() {
    Generic<User> generic = new Generic<>();
    generic.value = new User();

    Generic<User> result = consumers.getIntf().genericUser(generic);
    assertEquals(result.value.jsonString(), expectUserStr);
  }

  @Test
  public void testGenericUser_rt() {
    Generic<User> generic = new Generic<>();
    generic.value = new User();
    @SuppressWarnings("unchecked")
    Generic<User> result = consumers.getSCBRestTemplate().postForObject("/genericUser", generic, Generic.class);
    assertEquals(result.value.jsonString(), expectUserStr);
  }

  @Test
  public void testGenericLong_intf() {
    Generic<Long> generic = new Generic<>();
    generic.value = 100L;
    Generic<Long> result = consumers.getIntf().genericLong(generic);
    assertEquals(Long.class, result.value.getClass());
    assertEquals(100L, (long) result.value);
  }

  @Test
  public void testGenericLong_rt() {
    Generic<Long> generic = new Generic<>();
    generic.value = 100L;
    @SuppressWarnings("unchecked")
    Generic<Long> result = consumers.getSCBRestTemplate().postForObject("/genericLong", generic, Generic.class);
    assertEquals(Long.class, result.value.getClass());
    assertEquals(100L, (long) result.value);
  }

  @Test
  public void testGenericDate_intf() {
    Generic<Date> generic = new Generic<>();
    generic.value = new Date(1001);
    Generic<Date> result = consumers.getIntf().genericDate(generic);
    assertEquals(result.value.getClass(), Date.class);
    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    assertEquals("1970-01-01T00:00:01.001Z", simpleDateFormat.format(result.value));
  }

  @Test
  public void testGenericDate_rt() {
    Generic<Date> generic = new Generic<>();
    generic.value = new Date(1001);
    @SuppressWarnings("unchecked")
    Generic<Date> result = consumers.getSCBRestTemplate().postForObject("/genericDate", generic, Generic.class);
    assertEquals(result.value.getClass(), Date.class);
    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    assertEquals("1970-01-01T00:00:01.001Z", simpleDateFormat.format(result.value));
  }

  @Test
  public void testGenericEnum_intf() {
    Generic<HttpStatus> generic = new Generic<>();
    generic.value = HttpStatus.OK;

    Generic<HttpStatus> result = consumers.getIntf().genericEnum(generic);
    assertEquals(HttpStatus.class, result.value.getClass());
    assertEquals(HttpStatus.OK, result.value);
  }

  @Test
  public void testGenericEnum_rt() {
    Generic<HttpStatus> generic = new Generic<>();
    generic.value = HttpStatus.OK;
    @SuppressWarnings("unchecked")
    Generic<HttpStatus> result = consumers.getSCBRestTemplate().postForObject("/genericEnum", generic, Generic.class);
    assertEquals(HttpStatus.class, result.value.getClass());
    assertEquals(HttpStatus.OK, result.value);
  }

  @Test
  public void testGenericGenericUser_intf() {
    Generic<Generic<User>> generic = new Generic<>();
    generic.value = new Generic<>();
    generic.value.value = new User();

    Generic<Generic<User>> result = consumers.getIntf().genericGenericUser(generic);
    assertEquals(result.value.value.jsonString(), expectUserStr);
  }

  @Test
  public void testGenericGenericUser_rt() {
    Generic<Generic<User>> generic = new Generic<>();
    generic.value = new Generic<>();
    generic.value.value = new User();
    @SuppressWarnings("unchecked")
    Generic<Generic<User>> result = consumers.getSCBRestTemplate()
        .postForObject("/genericGenericUser", generic, Generic.class);
    assertEquals(result.value.value.jsonString(), expectUserStr);
  }

  @Test
  public void testGenericMap_intf() {
    Generic<Map<String, String>> mapGeneric = new Generic<>();
    Map<String, String> map = new HashMap<>();
    map.put("test", "hello");
    mapGeneric.value = map;
    Generic<Map<String, String>> result = consumers.getIntf().genericMap(mapGeneric);
    String test = result.value.get("test");
    assertEquals(test, "hello");
  }

  @Test
  public void testGenericMap_rt() {
    Generic<Map<String, String>> mapGeneric = new Generic<>();
    Map<String, String> map = new HashMap<>();
    map.put("test", "hello");
    mapGeneric.value = map;
    @SuppressWarnings("unchecked")
    Generic<Map<String, String>> result = consumers.getSCBRestTemplate()
        .postForObject("/genericMap", mapGeneric, Generic.class);
    String test = result.value.get("test");
    assertEquals(test, "hello");
  }

  @Test
  public void testGenericListMap_intf() {
    Generic<Map<String, List<String>>> mapListGeneric = new Generic<>();
    Map<String, List<String>> map = new HashMap<>();
    List<String> list = new ArrayList<>();
    list.add("hello");
    map.put("test", list);
    mapListGeneric.value = map;
    Generic<Map<String, List<String>>> result = consumers.getIntf().genericMapList(mapListGeneric);
    String test = result.value.get("test").get(0);
    assertEquals(test, "hello");
  }

  @Test
  public void testGenericListMap_rt() {
    Generic<Map<String, List<String>>> mapListGeneric = new Generic<>();
    Map<String, List<String>> map = new HashMap<>();
    List<String> list = new ArrayList<>();
    list.add("hello");
    map.put("test", list);
    mapListGeneric.value = map;
    @SuppressWarnings("unchecked")
    Generic<Map<String, List<String>>> result = consumers.getSCBRestTemplate()
        .postForObject("/genericMapList", mapListGeneric, Generic.class);
    String test = result.value.get("test").get(0);
    assertEquals(test, "hello");
  }

  @Test
  public void testGenericListUserMap_intf() {
    Generic<Map<String, List<User>>> mapListUserGeneric = new Generic<>();
    Map<String, List<User>> map = new HashMap<>();
    List<User> list = new ArrayList<>();
    list.add(new User());
    map.put("test", list);
    mapListUserGeneric.value = map;
    Generic<Map<String, List<User>>> result = consumers.getIntf().genericMapListUser(mapListUserGeneric);
    String test = result.value.get("test").get(0).jsonString();
    assertEquals(test, expectUserStr);
  }

  @Test
  public void testGenericListUserMap_rt() {
    Generic<Map<String, List<User>>> mapListUserGeneric = new Generic<>();
    Map<String, List<User>> map = new HashMap<>();
    List<User> list = new ArrayList<>();
    list.add(new User());
    map.put("test", list);
    mapListUserGeneric.value = map;
    @SuppressWarnings("unchecked")
    Generic<Map<String, List<User>>> result = consumers.getSCBRestTemplate()
        .postForObject("/genericMapListUser", mapListUserGeneric, Generic.class);
    String test = result.value.get("test").get(0).jsonString();
    assertEquals(test, expectUserStr);
  }

  @Test
  public void testGenericNestedListString_intfAndRt() {
    ArrayList<List<String>> nestedListString = new ArrayList<>();
    nestedListString.add(Arrays.asList("abc", "def"));
    nestedListString.add(Arrays.asList("ghi", "jkl"));

    List<List<String>> response = consumers.getIntf().genericNestedListString(nestedListString);
    assertEquals(2, response.size());
    assertThat(response.get(0), Matchers.contains("abc", "def"));
    assertThat(response.get(1), Matchers.contains("ghi", "jkl"));

    @SuppressWarnings("unchecked")
    List<List<String>> response2 = consumers.getSCBRestTemplate()
        .postForObject("/genericNestedListString", nestedListString, List.class);
    assertEquals(2, response2.size());
    assertThat(response2.get(0), Matchers.contains("abc", "def"));
    assertThat(response2.get(1), Matchers.contains("ghi", "jkl"));
  }

  @Test
  public void testGenericNestedListUser_intfAndRt() {
    User user1 = new User();
    user1.setAge(1);
    user1.setIndex(1);
    user1.setName("abc");
    user1.setNames(new String[] {"1", "2", "3"});
    User user2 = new User();
    user2.setAge(2);
    user2.setIndex(2);
    user2.setName("def");
    user2.setNames(new String[] {"4", "5"});
    User user3 = new User();
    user3.setAge(3);
    user3.setIndex(3);
    user3.setName("ghi");
    user3.setNames(new String[] {"6", "7"});
    User user4 = new User();
    user4.setAge(4);
    user4.setIndex(4);
    user4.setName("jkl");
    user4.setNames(new String[] {"8", "9", "10"});
    ArrayList<List<User>> nestedListUser = new ArrayList<>();
    nestedListUser.add(Arrays.asList(user1, user2));
    nestedListUser.add(Arrays.asList(user3, user4));

    List<List<User>> response = consumers.getIntf().genericNestedListUser(nestedListUser);
    assertEquals(2, response.size());
    assertThat(response.get(0), Matchers.contains(user1, user2));
    assertThat(response.get(1), Matchers.contains(user3, user4));

    @SuppressWarnings("unchecked")
    List<List<User>> response2 = consumers.getSCBRestTemplate()
        .postForObject("/genericNestedListUser", nestedListUser, List.class);
    assertEquals(2, response2.size());
    assertThat(response2.get(0), Matchers.contains(user1, user2));
    assertThat(response2.get(1), Matchers.contains(user3, user4));
  }
}
