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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
}
