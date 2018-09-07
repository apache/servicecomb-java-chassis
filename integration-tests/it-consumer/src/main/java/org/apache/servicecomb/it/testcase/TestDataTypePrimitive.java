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

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.it.Consumers;
import org.apache.servicecomb.it.junit.ITJUnitUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class TestDataTypePrimitive {
  interface DataTypePojoIntf {
    int intBody(int input);

    int add(int num1, int num2);

    String stringBody(String input);

    String stringConcat(String str1, String str2);
  }

  public interface DataTypeRestIntf {
    int intPath(int input);

    int intQuery(int input);

    int intHeader(int input);

    int intCookie(int input);

    int intBody(int input);

    int intForm(int input);

    int add(int num1, int num2);

    //string
    String stringPath(String input);

    String stringQuery(String input);

    String stringHeader(String input);

    String stringCookie(String input);

    String stringForm(String input);

    String stringBody(String input);

    String stringConcat(String str1, String str2);
  }

  private static Consumers<DataTypePojoIntf> consumersPojo = new Consumers<>("dataTypePojo", DataTypePojoIntf.class);

  private static Consumers<DataTypeRestIntf> consumersJaxrs =
      new Consumers<>("dataTypeJaxrs", DataTypeRestIntf.class);

  private static Consumers<DataTypeRestIntf> consumersSpringmvc = new Consumers<>("dataTypeSpringmvc",
      DataTypeRestIntf.class);

  @BeforeClass
  public static void classSetup() {
    consumersPojo.init(ITJUnitUtils.getTransport());
    consumersJaxrs.init(ITJUnitUtils.getTransport());
    consumersSpringmvc.init(ITJUnitUtils.getTransport());
  }

  @Test
  public void int_pojo_intf() {
    assertEquals(10, consumersPojo.getIntf().intBody(10));
  }

  @Test
  public void string_pojo_intf() {
    String expect = "serviceComb";
    assertEquals(expect, consumersPojo.getIntf().stringBody(expect));
  }

  @Test
  public void int_pojo_rt() {
    Map<String, Integer> map = new HashMap<>();
    map.put("input", 10);
    assertEquals(10, (int) consumersPojo.getSCBRestTemplate().postForObject("/intBody", map, int.class));
  }

  @Test
  public void string_pojo_rt() {
    String expect = "serviceComb";
    Map<String, String> map = new HashMap<>();
    map.put("input", expect);
    assertEquals(expect, (String) consumersPojo.getSCBRestTemplate().postForObject("/stringBody", map, String.class));
  }

  @Test
  public void add_pojo_intf() {
    assertEquals(12, consumersPojo.getIntf().add(10, 2));
  }

  @Test
  public void string_concat_pojo_intf() {
    assertEquals("serviceComb", consumersPojo.getIntf().stringConcat("service", "Comb"));
  }

  @Test
  public void add_pojo_rt() {
    Map<String, Integer> map = new HashMap<>();
    map.put("num1", 10);
    map.put("num2", 2);
    assertEquals(12, (int) consumersPojo.getSCBRestTemplate().postForObject("/add", map, int.class));
  }

  @Test
  public void string_concat_pojo_rt() {
    Map<String, String> map = new HashMap<>();
    map.put("str1", "service");
    map.put("str2", "Comb");
    assertEquals("serviceComb",
        (String) consumersPojo.getSCBRestTemplate().postForObject("/stringConcat", map, String.class));
  }

  @Test
  public void intPath_jaxrs_intf() {
    assertEquals(10, consumersJaxrs.getIntf().intPath(10));
  }

  @Test
  public void stringPath_jaxrs_intf() {
    String expect = "serviceComb";
    assertEquals(expect, consumersJaxrs.getIntf().stringPath(expect));
  }

  @Test
  public void intPath_jaxrs_rt() {
    assertEquals(10, (int) consumersJaxrs.getSCBRestTemplate().getForObject("/intPath/10", int.class));
  }

  @Test
  public void stringPath_jaxrs_rt() {
    String expect = "serviceComb";
    assertEquals(expect,
        (String) consumersJaxrs.getSCBRestTemplate().getForObject("/stringPath/" + expect, String.class));
  }

  @Test
  public void intQuery_jaxrs_intf() {
    assertEquals(10, consumersJaxrs.getIntf().intQuery(10));
  }

  @Test
  public void stringQuery_jaxrs_intf() {
    String expect = "serviceComb";
    assertEquals(expect, consumersJaxrs.getIntf().stringQuery(expect));
  }

  @Test
  public void intQuery_jaxrs_rt() {
    assertEquals(10, (int) consumersJaxrs.getSCBRestTemplate().getForObject("/intQuery?input=10", int.class));
  }

  @Test
  public void stringQuery_jaxrs_rt() {
    String expect = "serviceComb";
    assertEquals(expect,
        (String) consumersJaxrs.getSCBRestTemplate().getForObject("/stringQuery?input=" + expect, String.class));
  }

  @Test
  public void intHeader_jaxrs_intf() {
    assertEquals(10, consumersJaxrs.getIntf().intHeader(10));
  }

  @Test
  public void stringHeader_jaxrs_intf() {
    String expect = "serviceComb";
    assertEquals(expect, consumersJaxrs.getIntf().stringHeader(expect));
  }

  @Test
  public void intHeader_jaxrs_rt() {
    intHeader_rt(consumersJaxrs);
  }

  protected void intHeader_rt(Consumers<DataTypeRestIntf> consumers) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("input", "10");

    @SuppressWarnings("rawtypes")
    HttpEntity entity = new HttpEntity<>(null, headers);
    ResponseEntity<Integer> response = consumers.getSCBRestTemplate()
        .exchange("/intHeader",
            HttpMethod.GET,
            entity,
            int.class);
    assertEquals(10, (int) response.getBody());
  }

  @Test
  public void stringHeader_jaxrs_rt() {
    stringHeader_rt(consumersJaxrs);
  }

  protected void stringHeader_rt(Consumers<DataTypeRestIntf> consumers) {
    String expect = "serviceComb";
    HttpHeaders headers = new HttpHeaders();
    headers.add("input", expect);

    @SuppressWarnings("rawtypes")
    HttpEntity entity = new HttpEntity<>(null, headers);
    ResponseEntity<String> response = consumers.getSCBRestTemplate()
        .exchange("/stringHeader",
            HttpMethod.GET,
            entity,
            String.class);
    assertEquals(expect, (String) response.getBody());
  }

  @Test
  public void intCookie_jaxrs_intf() {
    assertEquals(10, consumersJaxrs.getIntf().intCookie(10));
  }

  @Test
  public void stringCookie_jaxrs_intf() {
    String expect = "serviceComb";
    assertEquals(expect, consumersJaxrs.getIntf().stringCookie(expect));
  }

  @Test
  public void intCookie_jaxrs_rt() {
    intCookie_rt(consumersJaxrs);
  }

  void intCookie_rt(Consumers<DataTypeRestIntf> consumers) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", "input=10");

    @SuppressWarnings("rawtypes")
    HttpEntity entity = new HttpEntity<>(null, headers);
    ResponseEntity<Integer> response = consumers.getSCBRestTemplate()
        .exchange("/intCookie",
            HttpMethod.GET,
            entity,
            int.class);
    assertEquals(10, (int) response.getBody());
  }

  @Test
  public void stringCookie_jaxrs_rt() {
    stringCookie_rt(consumersJaxrs);
  }

  void stringCookie_rt(Consumers<DataTypeRestIntf> consumers) {
    String expect = "serviceComb";
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", "input=" + expect);

    @SuppressWarnings("rawtypes")
    HttpEntity entity = new HttpEntity<>(null, headers);
    ResponseEntity<String> response = consumers.getSCBRestTemplate()
        .exchange("/stringCookie",
            HttpMethod.GET,
            entity,
            String.class);
    assertEquals(expect, (String) response.getBody());
  }

  @Test
  public void intForm_jaxrs_intf() {
    assertEquals(10, consumersJaxrs.getIntf().intForm(10));
  }

  @Test
  public void stringForm_jaxrs_intf() {
    String expect = "serviceComb";
    assertEquals(expect, consumersJaxrs.getIntf().stringForm(expect));
  }

  @Test
  public void intForm_jaxrs_rt() {
    Map<String, Integer> map = new HashMap<>();
    map.put("input", 10);
    HttpEntity<Map<String, Integer>> formEntiry = new HttpEntity<>(map);

    assertEquals(10,
        (int) consumersJaxrs.getSCBRestTemplate().postForEntity("/intForm", formEntiry, int.class).getBody());
    //just use map is ok
    assertEquals(10,
        (int) consumersJaxrs.getSCBRestTemplate().postForEntity("/intForm", map, int.class).getBody());
  }

  @Test
  public void stringForm_jaxrs_rt() {
    String expect = "serviceComb";
    Map<String, String> map = new HashMap<>();
    map.put("input", expect);
    HttpEntity<Map<String, String>> formEntiry = new HttpEntity<>(map);

    assertEquals(expect,
        (String) consumersJaxrs.getSCBRestTemplate()
            .postForEntity("/stringForm", formEntiry, String.class)
            .getBody());

    //you can use another method to invoke it
    assertEquals(expect,
        (String) consumersJaxrs.getSCBRestTemplate().postForEntity("/stringForm", map, String.class).getBody());
  }

  @Test
  public void intBody_jaxrs_intf() {
    assertEquals(10, consumersJaxrs.getIntf().intBody(10));
  }

  @Test
  public void stringBody_jaxrs_intf() {
    String expect = "serviceComb";
    assertEquals(expect, consumersJaxrs.getIntf().stringBody(expect));
  }

  @Test
  public void intBody_jaxrs_rt() {
    assertEquals(10, (int) consumersJaxrs.getSCBRestTemplate().postForObject("/intBody", 10, int.class));
  }

  @Test
  public void stringBody_jaxrs_rt() {
    String expect = "serviceComb";
    assertEquals(expect,
        (String) consumersJaxrs.getSCBRestTemplate().postForObject("/stringBody", expect, String.class));
  }

  @Test
  public void add_jaxrs_intf() {
    assertEquals(12, consumersJaxrs.getIntf().add(10, 2));
  }

  @Test
  public void string_concat_jaxrs_intf() {
    assertEquals("serviceComb", consumersJaxrs.getIntf().stringConcat("service", "Comb"));
  }

  @Test
  public void add_jaxrs_rt() {
    assertEquals(12, (int) consumersJaxrs.getSCBRestTemplate().getForObject("/add?num1=10&num2=2", int.class));
  }

  @Test
  public void string_concat_jaxrs_rt() {
    assertEquals("serviceComb", (String) consumersJaxrs.getSCBRestTemplate()
        .getForObject("/stringConcat?str1=service&str2=Comb", String.class));
  }

  @Test
  public void intPath_springmvc_intf() {
    assertEquals(10, consumersSpringmvc.getIntf().intPath(10));
  }

  @Test
  public void stringPath_springmvc_intf() {
    String expect = "serviceComb";
    assertEquals(expect, consumersSpringmvc.getIntf().stringPath(expect));
  }

  @Test
  public void intPath_springmvc_rt() {
    assertEquals(10, (int) consumersSpringmvc.getSCBRestTemplate().getForObject("/intPath/10", int.class));
  }

  @Test
  public void stringPath_springmvc_rt() {
    String expect = "serviceComb";
    assertEquals(expect,
        (String) consumersSpringmvc.getSCBRestTemplate().getForObject("/stringPath/" + expect, String.class));
  }

  @Test
  public void intQuery_springmvc_intf() {
    assertEquals(10, consumersSpringmvc.getIntf().intQuery(10));
  }

  @Test
  public void stringQuery_springmvc_intf() {
    String expect = "serviceComb";
    assertEquals(expect, consumersSpringmvc.getIntf().stringQuery(expect));
  }

  @Test
  public void intQuery_springmvc_rt() {
    assertEquals(10, (int) consumersSpringmvc.getSCBRestTemplate().getForObject("/intQuery?input=10", int.class));
  }

  @Test
  public void stringQuery_springmvc_rt() {
    String expect = "serviceComb";
    assertEquals(expect,
        (String) consumersSpringmvc.getSCBRestTemplate().getForObject("/stringQuery?input=" + expect, String.class));
  }

  @Test
  public void intHeader_springmvc_intf() {
    assertEquals(10, consumersSpringmvc.getIntf().intHeader(10));
  }

  @Test
  public void stringHeader_springmvc_intf() {
    String expect = "serviceComb";
    assertEquals(expect, consumersSpringmvc.getIntf().stringHeader(expect));
  }

  @Test
  public void intHeader_springmvc_rt() {
    intHeader_rt(consumersSpringmvc);
  }

  @Test
  public void stringHeader_springmvc_rt() {
    stringHeader_rt(consumersSpringmvc);
  }

  @Test
  public void intCookie_springmvc_intf() {
    assertEquals(10, consumersSpringmvc.getIntf().intCookie(10));
  }

  @Test
  public void stringCookie_springmvc_intf() {
    String expect = "serviceComb";
    assertEquals(expect, consumersSpringmvc.getIntf().stringCookie(expect));
  }

  @Test
  public void intCookie_springmvc_rt() {
    intCookie_rt(consumersSpringmvc);
  }

  @Test
  public void stringCookie_springmvc_rt() {
    stringCookie_rt(consumersSpringmvc);
  }

  @Test
  public void intForm_springmvc_intf() {
    assertEquals(10, consumersSpringmvc.getIntf().intForm(10));
  }

  @Test
  public void stringForm_springmvc_intf() {
    String expect = "serviceComb";
    assertEquals(expect, consumersSpringmvc.getIntf().stringForm(expect));
  }

  @Test
  public void intForm_springmvc_rt() {
    Map<String, Integer> map = new HashMap<>();
    map.put("input", 10);
    HttpEntity<Map<String, Integer>> formEntiry = new HttpEntity<>(map);

    assertEquals(10,
        (int) consumersSpringmvc.getSCBRestTemplate().postForEntity("/intForm", formEntiry, int.class).getBody());
  }

  @Test
  public void stringForm_springmvc_rt() {
    String expect = "serviceComb";
    Map<String, String> map = new HashMap<>();
    map.put("input", expect);
    HttpEntity<Map<String, String>> formEntiry = new HttpEntity<>(map);

    assertEquals(expect,
        (String) consumersSpringmvc.getSCBRestTemplate().postForEntity("/stringForm", formEntiry, String.class)
            .getBody());

    assertEquals(expect,
        (String) consumersSpringmvc.getSCBRestTemplate().postForEntity("/stringForm", map, String.class)
            .getBody());
  }

  @Test
  public void intBody_springmvc_intf() {
    assertEquals(10, consumersSpringmvc.getIntf().intBody(10));
  }

  @Test
  public void stringBody_springmvc_intf() {
    String expect = "serviceComb";
    assertEquals(expect, consumersSpringmvc.getIntf().stringBody(expect));
  }

  @Test
  public void intBody_springmvc_rt() {
    assertEquals(10, (int) consumersSpringmvc.getSCBRestTemplate().postForObject("/intBody", 10, int.class));
  }

  @Test
  public void stringBody_springmvc_rt() {
    String expect = "serviceComb";
    assertEquals(expect,
        (String) consumersSpringmvc.getSCBRestTemplate().postForObject("/stringBody", expect, String.class));
  }

  @Test
  public void add_springmvc_intf() {
    assertEquals(12, consumersSpringmvc.getIntf().add(10, 2));
  }

  @Test
  public void string_concat_springmvc_intf() {
    assertEquals("serviceComb", consumersSpringmvc.getIntf().stringConcat("service", "Comb"));
  }

  @Test
  public void add_springmvc_rt() {
    assertEquals(12, (int) consumersSpringmvc.getSCBRestTemplate().getForObject("/add?num1=10&num2=2", int.class));
  }

  @Test
  public void string_concat_springmvc_rt() {
    assertEquals("serviceComb",
        (String) consumersSpringmvc.getSCBRestTemplate()
            .getForObject("/stringConcat?str1=service&str2=Comb", String.class));
  }
}
