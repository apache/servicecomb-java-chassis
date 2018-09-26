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
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class TestDataTypePrimitive {
  interface DataTypePojoIntf {
    int intBody(int input);

    int intAdd(int num1, int num2);

    String stringBody(String input);

    String stringConcat(String str1, String str2);

    double doubleBody(double input);

    double doubleAdd(double num1, double num2);
  }

  public interface DataTypeRestIntf {
    int intPath(int input);

    int intQuery(int input);

    int intHeader(int input);

    int intCookie(int input);

    int intBody(int input);

    int intForm(int input);

    int intAdd(int num1, int num2);

    //string
    String stringPath(String input);

    String stringQuery(String input);

    String stringHeader(String input);

    String stringCookie(String input);

    String stringForm(String input);

    String stringBody(String input);

    String stringConcat(String str1, String str2);

    //double
    double doublePath(double input);

    double doubleQuery(double input);

    double doubleHeader(double input);

    double doubleCookie(double input);

    double doubleBody(double input);

    double doubleForm(double input);

    double doubleAdd(double num1, double num2);
  }

  private static Consumers<DataTypePojoIntf> consumersPojo;

  private static Consumers<DataTypeRestIntf> consumersJaxrs;

  private static Consumers<DataTypeRestIntf> consumersSpringmvc;

  private static String producerName;

  @Before
  public void prepare() {
    if (!ITJUnitUtils.getProducerName().equals(producerName)) {
      producerName = ITJUnitUtils.getProducerName();
      consumersPojo = new Consumers<>(producerName, "dataTypePojo", DataTypePojoIntf.class);
      consumersJaxrs = new Consumers<>(producerName, "dataTypeJaxrs", DataTypeRestIntf.class);
      consumersSpringmvc = new Consumers<>(producerName, "dataTypeSpringmvc", DataTypeRestIntf.class);
      consumersPojo.init(ITJUnitUtils.getTransport());
      consumersJaxrs.init(ITJUnitUtils.getTransport());
      consumersSpringmvc.init(ITJUnitUtils.getTransport());
    }
  }

  @Test
  public void int_pojo_intf() {
    assertEquals(10, consumersPojo.getIntf().intBody(10));
  }

  @Test
  public void double_pojo_intf() {
    assertEquals(10.2, consumersPojo.getIntf().doubleBody(10.2), 0.0);
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
  public void double_pojo_rt() {
    Map<String, Double> map = new HashMap<>();
    map.put("input", 10.2);
    assertEquals(10.2, (double) consumersPojo.getSCBRestTemplate().postForObject("/doubleBody", map, double.class),
        0.0);
  }


  @Test
  public void string_pojo_rt() {
    String expect = "serviceComb";
    Map<String, String> map = new HashMap<>();
    map.put("input", expect);
    assertEquals(expect, (String) consumersPojo.getSCBRestTemplate().postForObject("/stringBody", map, String.class));
  }

  @Test
  public void intAdd_pojo_intf() {
    assertEquals(12, consumersPojo.getIntf().intAdd(10, 2));
  }

  @Test
  public void doubleAdd_pojo_intf() {
    assertEquals(20.5, consumersPojo.getIntf().doubleAdd(10.2, 10.3), 0.0);
  }

  @Test
  public void string_concat_pojo_intf() {
    assertEquals("serviceComb", consumersPojo.getIntf().stringConcat("service", "Comb"));
  }

  @Test
  public void intAdd_pojo_rt() {
    Map<String, Integer> map = new HashMap<>();
    map.put("num1", 10);
    map.put("num2", 2);
    assertEquals(12, (int) consumersPojo.getSCBRestTemplate().postForObject("/intAdd", map, int.class));
  }

  @Test
  public void doubleAdd_pojo_rt() {
    Map<String, Double> map = new HashMap<>();
    map.put("num1", 10.2);
    map.put("num2", 10.3);
    assertEquals(20.5, (double) consumersPojo.getSCBRestTemplate().postForObject("/doubleAdd", map, double.class), 0.0);
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
  public void doublePath_jaxrs_intf() {
    assertEquals(10.2, consumersJaxrs.getIntf().doublePath(10.2), 0.0);
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
  public void doublePath_jaxrs_rt() {
    assertEquals(10.2, (double) consumersJaxrs.getSCBRestTemplate().getForObject("/doublePath/10.2", double.class),
        0.0);
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
  public void doubleQuery_jaxrs_intf() {
    assertEquals(10.2, consumersJaxrs.getIntf().doubleQuery(10.2), 0.0);
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
  public void doubleQuery_jaxrs_rt() {
    assertEquals(10.2,
        (double) consumersJaxrs.getSCBRestTemplate().getForObject("/doubleQuery?input=10.2", double.class), 0.0);
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
  public void doubleHeader_jaxrs_intf() {
    assertEquals(10.2, consumersJaxrs.getIntf().doubleHeader(10.2), 0.0);
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

  @Test
  public void doubleHeader_jaxrs_rt() {
    doubleHeader_rt(consumersJaxrs);
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

  protected void doubleHeader_rt(Consumers<DataTypeRestIntf> consumers) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("input", "10.2");

    @SuppressWarnings("rawtypes")
    HttpEntity entity = new HttpEntity<>(null, headers);
    ResponseEntity<Double> response = consumers.getSCBRestTemplate()
        .exchange("/doubleHeader",
            HttpMethod.GET,
            entity,
            double.class);
    assertEquals(10.2, (double) response.getBody(), 0.0);
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
  public void doubleCookie_jaxrs_intf() {
    assertEquals(10.2, consumersJaxrs.getIntf().doubleCookie(10.2), 0.0);
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

  @Test
  public void doubleCookie_jaxrs_rt() {
    doubleCookie_rt(consumersJaxrs);
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

  void doubleCookie_rt(Consumers<DataTypeRestIntf> consumers) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", "input=10.2");

    @SuppressWarnings("rawtypes")
    HttpEntity entity = new HttpEntity<>(null, headers);
    ResponseEntity<Double> response = consumers.getSCBRestTemplate()
        .exchange("/doubleCookie",
            HttpMethod.GET,
            entity,
            double.class);
    assertEquals(10.2, (double) response.getBody(), 0.0);
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
  public void doubleForm_jaxrs_intf() {
    assertEquals(10.2, consumersJaxrs.getIntf().doubleForm(10.2), 0.0);
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
  public void doubleForm_jaxrs_rt() {
    Map<String, Double> map = new HashMap<>();
    map.put("input", 10.2);
    HttpEntity<Map<String, Double>> formEntiry = new HttpEntity<>(map);

    assertEquals(10.2,
        (double) consumersJaxrs.getSCBRestTemplate().postForEntity("/doubleForm", formEntiry, double.class).getBody(),
        0.0);
    //just use map is ok
    assertEquals(10.2,
        (double) consumersJaxrs.getSCBRestTemplate().postForEntity("/doubleForm", map, double.class).getBody(), 0.0);
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
  public void doubleBody_jaxrs_intf() {
    assertEquals(10.2, consumersJaxrs.getIntf().doubleBody(10.2), 0.0);
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
  public void doubleBody_jaxrs_rt() {
    assertEquals(10.2, (double) consumersJaxrs.getSCBRestTemplate().postForObject("/doubleBody", 10.2, double.class),
        0.0);
  }

  @Test
  public void stringBody_jaxrs_rt() {
    String expect = "serviceComb";
    assertEquals(expect,
        (String) consumersJaxrs.getSCBRestTemplate().postForObject("/stringBody", expect, String.class));
  }

  @Test
  public void intAdd_jaxrs_intf() {
    assertEquals(12, consumersJaxrs.getIntf().intAdd(10, 2));
  }
  @Test
  public void doubleAdd_jaxrs_intf() {
    assertEquals(20.5, consumersJaxrs.getIntf().doubleAdd(10.2, 10.3), 0.0);
  }

  @Test
  public void string_concat_jaxrs_intf() {
    assertEquals("serviceComb", consumersJaxrs.getIntf().stringConcat("service", "Comb"));
  }

  @Test
  public void intAdd_jaxrs_rt() {
    assertEquals(12, (int) consumersJaxrs.getSCBRestTemplate().getForObject("/intAdd?num1=10&num2=2", int.class));
  }

  @Test
  public void doubleAdd_jaxrs_rt() {
    assertEquals(20.5,
        (double) consumersJaxrs.getSCBRestTemplate().getForObject("/doubleAdd?num1=10.2&num2=10.3", double.class), 0.0);
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
  public void doublePath_springmvc_intf() {
    assertEquals(10.2, consumersSpringmvc.getIntf().doublePath(10.2), 0.0);
  }

  @Test
  public void stringPath_springmvc_intf() {
    String expect = "serviceComb";
    assertEquals(expect, consumersSpringmvc.getIntf().stringPath(expect));
  }

  @Test
  public void doublePath_springmvc_rt() {
    assertEquals(10.2, (double) consumersSpringmvc.getSCBRestTemplate().getForObject("/doublePath/10.2", double.class),
        0.0);
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
  public void doubleQuery_springmvc_intf() {
    assertEquals(10.2, consumersSpringmvc.getIntf().doubleQuery(10.2), 0.0);
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
  public void doubleQuery_springmvc_rt() {
    assertEquals(10.2,
        (double) consumersSpringmvc.getSCBRestTemplate().getForObject("/doubleQuery?input=10.2", double.class), 0.0);
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
  public void doubleHeader_springmvc_intf() {
    assertEquals(10.2, consumersSpringmvc.getIntf().doubleHeader(10.2), 0.0);
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
  public void doubleHeader_springmvc_rt() {
    doubleHeader_rt(consumersSpringmvc);
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
  public void doubleCookie_springmvc_intf() {
    assertEquals(10.2, consumersSpringmvc.getIntf().doubleCookie(10.2), 0.0);
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
  public void doubleCookie_springmvc_rt() {
    doubleCookie_rt(consumersSpringmvc);
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
  public void doubleForm_springmvc_intf() {
    assertEquals(10.2, consumersSpringmvc.getIntf().doubleForm(10.2), 0.0);

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
  public void doubleForm_springmvc_rt() {
    Map<String, Double> map = new HashMap<>();
    map.put("input", 10.2);
    HttpEntity<Map<String, Double>> formEntiry = new HttpEntity<>(map);

    assertEquals(10.2,
        (double) consumersSpringmvc.getSCBRestTemplate().postForEntity("/doubleForm", formEntiry, double.class)
            .getBody(), 0.0);
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
  public void doubleBody_springmvc_intf() {
    assertEquals(10.2, consumersSpringmvc.getIntf().doubleBody(10.2), 0.0);
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
  public void doubleBody_springmvc_rt() {
    assertEquals(10.2,
        (double) consumersSpringmvc.getSCBRestTemplate().postForObject("/doubleBody", 10.2, double.class), 0.0);
  }

  @Test
  public void stringBody_springmvc_rt() {
    String expect = "serviceComb";
    assertEquals(expect,
        (String) consumersSpringmvc.getSCBRestTemplate().postForObject("/stringBody", expect, String.class));
  }

  @Test
  public void intAdd_springmvc_intf() {
    assertEquals(12, consumersSpringmvc.getIntf().intAdd(10, 2));
  }

  @Test
  public void doubleAdd_springmvc_intf() {
    assertEquals(20.5, consumersSpringmvc.getIntf().doubleAdd(10.2, 10.3), 0.0);

  }

  @Test
  public void string_concat_springmvc_intf() {
    assertEquals("serviceComb", consumersSpringmvc.getIntf().stringConcat("service", "Comb"));
  }

  @Test
  public void intAdd_springmvc_rt() {
    assertEquals(12, (int) consumersSpringmvc.getSCBRestTemplate().getForObject("/intAdd?num1=10&num2=2", int.class));
  }

  @Test
  public void doubleAdd_springmvc_rt() {
    assertEquals(20.5,
        (double) consumersSpringmvc.getSCBRestTemplate().getForObject("/doubleAdd?num1=10.2&num2=10.3", double.class),
        0.0);
  }

  @Test
  public void string_concat_springmvc_rt() {
    assertEquals("serviceComb",
        (String) consumersSpringmvc.getSCBRestTemplate()
            .getForObject("/stringConcat?str1=service&str2=Comb", String.class));
  }
}
