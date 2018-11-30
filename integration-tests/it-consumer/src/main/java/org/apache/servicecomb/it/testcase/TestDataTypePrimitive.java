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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.test.scaffolding.model.Color;
import org.apache.servicecomb.it.Consumers;
import org.apache.servicecomb.it.extend.engine.ITSCBRestTemplate;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

public class TestDataTypePrimitive {
  interface DataTypePojoIntf {
    int intBody(int input);

    int intAdd(int num1, int num2);

    String stringBody(String input);

    String stringConcat(String str1, String str2);

    double doubleBody(double input);

    double doubleAdd(double num1, double num2);

    float floatBody(float input);

    float floatAdd(float num1, float num2);

    Color enumBody(Color color);
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

    //float
    float floatPath(float input);

    float floatQuery(float input);

    float floatHeader(float input);

    float floatCookie(float input);

    float floatBody(float input);

    float floatForm(float input);

    float floatAdd(float num1, float num2);

    // enum
    Color enumBody(Color color);

    // query array
    String queryArr(String[] queryArr);

    String queryArrCSV(String[] queryArr);

    String queryArrSSV(String[] queryArr);

    String queryArrTSV(String[] queryArr);

    String queryArrPIPES(String[] queryArr);

    String queryArrMULTI(String[] queryArr);
  }

  private static Consumers<DataTypePojoIntf> consumersPojo = new Consumers<>("dataTypePojo", DataTypePojoIntf.class);

  private static Consumers<DataTypeRestIntf> consumersJaxrs =
      new Consumers<>("dataTypeJaxrs", DataTypeRestIntf.class);

  private static Consumers<DataTypeRestIntf> consumersSpringmvc = new Consumers<>("dataTypeSpringmvc",
      DataTypeRestIntf.class);

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
    assertEquals(10.2, consumersPojo.getSCBRestTemplate().postForObject("/doubleBody", map, double.class),
        0.0);
  }

  @Test
  public void string_pojo_rt() {
    String expect = "serviceComb";
    Map<String, String> map = new HashMap<>();
    map.put("input", expect);
    assertEquals(expect, consumersPojo.getSCBRestTemplate().postForObject("/stringBody", map, String.class));
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
    assertEquals(20.5, consumersPojo.getSCBRestTemplate().postForObject("/doubleAdd", map, double.class), 0.0);
  }

  @Test
  public void string_concat_pojo_rt() {
    Map<String, String> map = new HashMap<>();
    map.put("str1", "service");
    map.put("str2", "Comb");
    assertEquals("serviceComb",
        consumersPojo.getSCBRestTemplate().postForObject("/stringConcat", map, String.class));
  }

  @Test
  public void enumBody_pojo_intf() {
    assertEquals(Color.BLUE, consumersPojo.getIntf().enumBody(Color.BLUE));
  }

  @Test
  public void enumBody_pojo_rt() {
    Map<String, Color> body = new HashMap<>();
    body.put("color", Color.BLUE);
    assertEquals(Color.BLUE,
        consumersPojo.getSCBRestTemplate().postForObject("/enumBody", body, Color.class));
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
    String expect = "serviceComb/serviceComb";
    assertEquals(expect, consumersJaxrs.getIntf().stringPath(expect));
  }

  @Test
  public void intPath_jaxrs_rt() {
    assertEquals(10, (int) consumersJaxrs.getSCBRestTemplate().getForObject("/intPath/10", int.class));
  }

  @Test
  public void doublePath_jaxrs_rt() {
    assertEquals(10.2, consumersJaxrs.getSCBRestTemplate().getForObject("/doublePath/10.2", double.class),
        0.0);
  }

  @Test
  public void stringPath_jaxrs_rt() {
    String expect = "serviceComb";
    assertEquals(expect,
        consumersJaxrs.getSCBRestTemplate().getForObject("/stringPath/" + expect, String.class));
  }

  @Test
  public void stringPath_jaxrs_rt_with_encoded_slash() {
    String requestPathParam = "serviceComb%2FserviceComb";
    String expectResponse = "serviceComb/serviceComb";
    // build request uri to avoid Spring's encoding path
    URI requestUri = UriComponentsBuilder
        .fromUriString(((ITSCBRestTemplate) consumersJaxrs.getSCBRestTemplate()).getUrlPrefix()
            + "/stringPath/" + requestPathParam)
        .build(true).toUri();
    assertEquals(expectResponse,
        consumersJaxrs.getSCBRestTemplate().getForObject(requestUri, String.class));
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
        consumersJaxrs.getSCBRestTemplate().getForObject("/doubleQuery?input=10.2", double.class), 0.0);
  }

  @Test
  public void stringQuery_jaxrs_rt() {
    String expect = "serviceComb";
    assertEquals(expect,
        consumersJaxrs.getSCBRestTemplate().getForObject("/stringQuery?input=" + expect, String.class));
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

    HttpEntity<?> entity = new HttpEntity<>(headers);
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

    HttpEntity<?> entity = new HttpEntity<>(headers);
    ResponseEntity<Double> response = consumers.getSCBRestTemplate()
        .exchange("/doubleHeader",
            HttpMethod.GET,
            entity,
            double.class);
    assertEquals(10.2, response.getBody(), 0.0);
  }

  @Test
  public void stringHeader_jaxrs_rt() {
    stringHeader_rt(consumersJaxrs);
  }

  protected void stringHeader_rt(Consumers<DataTypeRestIntf> consumers) {
    String expect = "serviceComb";
    HttpHeaders headers = new HttpHeaders();
    headers.add("input", expect);

    HttpEntity<?> entity = new HttpEntity<>(headers);
    ResponseEntity<String> response = consumers.getSCBRestTemplate()
        .exchange("/stringHeader",
            HttpMethod.GET,
            entity,
            String.class);
    assertEquals(expect, response.getBody());
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

    HttpEntity<?> entity = new HttpEntity<>(headers);
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

    HttpEntity<?> entity = new HttpEntity<>(headers);
    ResponseEntity<Double> response = consumers.getSCBRestTemplate()
        .exchange("/doubleCookie",
            HttpMethod.GET,
            entity,
            double.class);
    assertEquals(10.2, response.getBody(), 0.0);
  }

  @Test
  public void stringCookie_jaxrs_rt() {
    stringCookie_rt(consumersJaxrs);
  }

  void stringCookie_rt(Consumers<DataTypeRestIntf> consumers) {
    String expect = "serviceComb";
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", "input=" + expect);

    HttpEntity<?> entity = new HttpEntity<>(headers);
    ResponseEntity<String> response = consumers.getSCBRestTemplate()
        .exchange("/stringCookie",
            HttpMethod.GET,
            entity,
            String.class);
    assertEquals(expect, response.getBody());
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
    HttpEntity<Map<String, Integer>> formEntity = new HttpEntity<>(map);

    assertEquals(10,
        (int) consumersJaxrs.getSCBRestTemplate().postForEntity("/intForm", formEntity, int.class).getBody());
    //just use map is ok
    assertEquals(10,
        (int) consumersJaxrs.getSCBRestTemplate().postForEntity("/intForm", map, int.class).getBody());
  }

  @Test
  public void doubleForm_jaxrs_rt() {
    Map<String, Double> map = new HashMap<>();
    map.put("input", 10.2);
    HttpEntity<Map<String, Double>> formEntity = new HttpEntity<>(map);

    assertEquals(10.2,
        consumersJaxrs.getSCBRestTemplate().postForEntity("/doubleForm", formEntity, double.class).getBody(),
        0.0);
    //just use map is ok
    assertEquals(10.2,
        consumersJaxrs.getSCBRestTemplate().postForEntity("/doubleForm", map, double.class).getBody(), 0.0);
  }

  @Test
  public void stringForm_jaxrs_rt() {
    String expect = "serviceComb";
    Map<String, String> map = new HashMap<>();
    map.put("input", expect);
    HttpEntity<Map<String, String>> formEntity = new HttpEntity<>(map);

    assertEquals(expect,
        consumersJaxrs.getSCBRestTemplate()
            .postForEntity("/stringForm", formEntity, String.class)
            .getBody());

    //you can use another method to invoke it
    assertEquals(expect,
        consumersJaxrs.getSCBRestTemplate().postForEntity("/stringForm", map, String.class).getBody());
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
    assertEquals(10.2, consumersJaxrs.getSCBRestTemplate().postForObject("/doubleBody", 10.2, double.class),
        0.0);
  }

  @Test
  public void stringBody_jaxrs_rt() {
    String expect = "serviceComb";
    assertEquals(expect,
        consumersJaxrs.getSCBRestTemplate().postForObject("/stringBody", expect, String.class));
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
        consumersJaxrs.getSCBRestTemplate().getForObject("/doubleAdd?num1=10.2&num2=10.3", double.class), 0.0);
  }

  @Test
  public void string_concat_jaxrs_rt() {
    assertEquals("serviceComb", consumersJaxrs.getSCBRestTemplate()
        .getForObject("/stringConcat?str1=service&str2=Comb", String.class));
  }

  @Test
  public void enumBody_jaxrs_intf() {
    assertEquals(Color.BLUE, consumersJaxrs.getIntf().enumBody(Color.BLUE));
  }

  @Test
  public void enumBody_jaxrs_rt() {
    assertEquals(Color.BLUE,
        consumersJaxrs.getSCBRestTemplate().postForObject("/enumBody", Color.BLUE, Color.class));
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
    String expect = "serviceComb/serviceComb";
    assertEquals(expect, consumersSpringmvc.getIntf().stringPath(expect));
  }

  @Test
  public void doublePath_springmvc_rt() {
    assertEquals(10.2, consumersSpringmvc.getSCBRestTemplate().getForObject("/doublePath/10.2", double.class),
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
        consumersSpringmvc.getSCBRestTemplate().getForObject("/stringPath/" + expect, String.class));
  }

  @Test
  public void stringPath_springmvc_rt_with_encoded_slash() {
    String requestPathParam = "serviceComb%2FserviceComb";
    String expectResponse = "serviceComb/serviceComb";
    URI requestUri = UriComponentsBuilder
        .fromUriString(((ITSCBRestTemplate) consumersSpringmvc.getSCBRestTemplate()).getUrlPrefix()
            + "/stringPath/" + requestPathParam)
        .build(true).toUri();
    assertEquals(expectResponse,
        consumersSpringmvc.getSCBRestTemplate().getForObject(requestUri, String.class));
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
        consumersSpringmvc.getSCBRestTemplate().getForObject("/doubleQuery?input=10.2", double.class), 0.0);
  }

  @Test
  public void stringQuery_springmvc_rt() {
    String expect = "serviceComb";
    assertEquals(expect,
        consumersSpringmvc.getSCBRestTemplate().getForObject("/stringQuery?input=" + expect, String.class));
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
    HttpEntity<Map<String, Integer>> formEntity = new HttpEntity<>(map);

    assertEquals(10,
        (int) consumersSpringmvc.getSCBRestTemplate().postForEntity("/intForm", formEntity, int.class).getBody());
  }

  @Test
  public void doubleForm_springmvc_rt() {
    Map<String, Double> map = new HashMap<>();
    map.put("input", 10.2);
    HttpEntity<Map<String, Double>> formEntity = new HttpEntity<>(map);

    assertEquals(10.2,
        consumersSpringmvc.getSCBRestTemplate().postForEntity("/doubleForm", formEntity, double.class)
            .getBody(), 0.0);
  }

  @Test
  public void stringForm_springmvc_rt() {
    String expect = "serviceComb";
    Map<String, String> map = new HashMap<>();
    map.put("input", expect);
    HttpEntity<Map<String, String>> formEntity = new HttpEntity<>(map);

    assertEquals(expect,
        consumersSpringmvc.getSCBRestTemplate().postForEntity("/stringForm", formEntity, String.class)
            .getBody());

    assertEquals(expect,
        consumersSpringmvc.getSCBRestTemplate().postForEntity("/stringForm", map, String.class)
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
        consumersSpringmvc.getSCBRestTemplate().postForObject("/doubleBody", 10.2, double.class), 0.0);
  }

  @Test
  public void stringBody_springmvc_rt() {
    String expect = "serviceComb";
    assertEquals(expect,
        consumersSpringmvc.getSCBRestTemplate().postForObject("/stringBody", expect, String.class));
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
        consumersSpringmvc.getSCBRestTemplate().getForObject("/doubleAdd?num1=10.2&num2=10.3", double.class),
        0.0);
  }

  @Test
  public void string_concat_springmvc_rt() {
    assertEquals("serviceComb",
        consumersSpringmvc.getSCBRestTemplate()
            .getForObject("/stringConcat?str1=service&str2=Comb", String.class));
  }

  //float
  @Test
  public void float_pojo_intf() {
    assertEquals(10.2f, consumersPojo.getIntf().floatBody(10.2f), 0.0f);
  }

  @Test
  public void float_pojo_rt() {
    Map<String, Float> map = new HashMap<>();
    map.put("input", 10.2f);
    assertEquals(10.2f, consumersPojo.getSCBRestTemplate().postForObject("/floatBody", map, float.class),
        0.0f);
  }

  @Test
  public void floatAdd_pojo_intf() {
    assertEquals(20.5f, consumersPojo.getIntf().floatAdd(10.2f, 10.3f), 0.0f);
  }

  @Test
  public void floatAdd_pojo_rt() {
    Map<String, Float> map = new HashMap<>();
    map.put("num1", 10.2f);
    map.put("num2", 10.3f);
    assertEquals(20.5f, consumersPojo.getSCBRestTemplate().postForObject("/floatAdd", map, float.class), 0.0f);
  }

  @Test
  public void floatPath_jaxrs_intf() {
    assertEquals(10.2f, consumersJaxrs.getIntf().floatPath(10.2f), 0.0f);
  }

  @Test
  public void floatPath_jaxrs_rt() {
    assertEquals(10.2f, consumersJaxrs.getSCBRestTemplate().getForObject("/floatPath/10.2f", float.class),
        0.0f);
  }

  @Test
  public void floatQuery_jaxrs_intf() {
    assertEquals(10.2f, consumersJaxrs.getIntf().floatQuery(10.2f), 0.0f);
  }

  @Test
  public void floatQuery_jaxrs_rt() {
    assertEquals(10.2f,
        consumersJaxrs.getSCBRestTemplate().getForObject("/floatQuery?input=10.2f", float.class), 0.0f);
  }

  @Test
  public void floatHeader_jaxrs_intf() {
    assertEquals(10.2f, consumersJaxrs.getIntf().floatHeader(10.2f), 0.0f);
  }

  @Test
  public void floatHeader_jaxrs_rt() {
    floatHeader_rt(consumersJaxrs);
  }

  protected void floatHeader_rt(Consumers<DataTypeRestIntf> consumers) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("input", "10.2f");

    HttpEntity<?> entity = new HttpEntity<>(headers);
    ResponseEntity<Float> response = consumers.getSCBRestTemplate()
        .exchange("/floatHeader",
            HttpMethod.GET,
            entity,
            float.class);
    assertEquals(10.2f, response.getBody(), 0.0f);
  }

  @Test
  public void floatCookie_jaxrs_intf() {
    assertEquals(10.2f, consumersJaxrs.getIntf().floatCookie(10.2f), 0.0f);
  }

  @Test
  public void floatCookie_jaxrs_rt() {
    floatCookie_rt(consumersJaxrs);
  }

  void floatCookie_rt(Consumers<DataTypeRestIntf> consumers) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", "input=10.2f");

    HttpEntity<?> entity = new HttpEntity<>(headers);
    ResponseEntity<Float> response = consumers.getSCBRestTemplate()
        .exchange("/floatCookie",
            HttpMethod.GET,
            entity,
            float.class);
    assertEquals(10.2f, response.getBody(), 0.0f);
  }

  @Test
  public void floatForm_jaxrs_intf() {
    assertEquals(10.2f, consumersJaxrs.getIntf().floatForm(10.2f), 0.0f);
  }

  @Test
  public void floatForm_jaxrs_rt() {
    Map<String, Float> map = new HashMap<>();
    map.put("input", 10.2f);
    HttpEntity<Map<String, Float>> formEntity = new HttpEntity<>(map);

    assertEquals(10.2f,
        consumersJaxrs.getSCBRestTemplate().postForEntity("/floatForm", formEntity, float.class).getBody(),
        0.0f);
    //just use map is ok
    assertEquals(10.2f,
        consumersJaxrs.getSCBRestTemplate().postForEntity("/floatForm", map, float.class).getBody(), 0.0f);
  }

  @Test
  public void floatBody_jaxrs_intf() {
    assertEquals(10.2f, consumersJaxrs.getIntf().floatBody(10.2f), 0.0f);
  }

  @Test
  public void floatBody_jaxrs_rt() {
    assertEquals(10.2f, consumersJaxrs.getSCBRestTemplate().postForObject("/floatBody", 10.2f, float.class),
        0.0f);
  }

  @Test
  public void floatAdd_jaxrs_intf() {
    assertEquals(20.5f, consumersJaxrs.getIntf().floatAdd(10.2f, 10.3f), 0.0f);
  }

  @Test
  public void floatAdd_jaxrs_rt() {
    assertEquals(20.5f,
        consumersJaxrs.getSCBRestTemplate().getForObject("/floatAdd?num1=10.2f&num2=10.3f", float.class), 0.0f);
  }

  @Test
  public void floatPath_springmvc_intf() {
    assertEquals(10.2f, consumersSpringmvc.getIntf().floatPath(10.2f), 0.0f);
  }

  @Test
  public void floatPath_springmvc_rt() {
    assertEquals(10.2f, consumersSpringmvc.getSCBRestTemplate().getForObject("/floatPath/10.2f", float.class),
        0.0f);
  }

  @Test
  public void floatQuery_springmvc_intf() {
    assertEquals(10.2f, consumersSpringmvc.getIntf().floatQuery(10.2f), 0.0f);
  }

  @Test
  public void floatQuery_springmvc_rt() {
    assertEquals(10.2f,
        consumersSpringmvc.getSCBRestTemplate().getForObject("/floatQuery?input=10.2f", float.class), 0.0f);
  }

  @Test
  public void floatHeader_springmvc_intf() {
    assertEquals(10.2f, consumersSpringmvc.getIntf().floatHeader(10.2f), 0.0f);
  }

  @Test
  public void floatHeader_springmvc_rt() {
    floatHeader_rt(consumersSpringmvc);
  }

  @Test
  public void floatCookie_springmvc_intf() {
    assertEquals(10.2f, consumersSpringmvc.getIntf().floatCookie(10.2f), 0.0f);
  }

  @Test
  public void floatCookie_springmvc_rt() {
    floatCookie_rt(consumersSpringmvc);
  }

  @Test
  public void floatForm_springmvc_intf() {
    assertEquals(10.2f, consumersSpringmvc.getIntf().floatForm(10.2f), 0.0f);
  }

  @Test
  public void floatForm_springmvc_rt() {
    Map<String, Float> map = new HashMap<>();
    map.put("input", 10.2f);
    HttpEntity<Map<String, Float>> formEntity = new HttpEntity<>(map);

    assertEquals(10.2f,
        consumersSpringmvc.getSCBRestTemplate().postForEntity("/floatForm", formEntity, float.class)
            .getBody(), 0.0f);
  }

  @Test
  public void floatBody_springmvc_intf() {
    assertEquals(10.2f, consumersSpringmvc.getIntf().floatBody(10.2f), 0.0f);
  }

  @Test
  public void floatBody_springmvc_rt() {
    assertEquals(10.2f,
        consumersSpringmvc.getSCBRestTemplate().postForObject("/floatBody", 10.2f, float.class), 0.0f);
  }

  @Test
  public void floatAdd_springmvc_intf() {
    assertEquals(20.5f, consumersSpringmvc.getIntf().floatAdd(10.2f, 10.3f), 0.0f);
  }

  @Test
  public void floatAdd_springmvc_rt() {
    assertEquals(20.5f,
        consumersSpringmvc.getSCBRestTemplate().getForObject("/floatAdd?num1=10.2f&num2=10.3f", float.class),
        0.0f);
  }

  @Test
  public void enumBody_springmvc_intf() {
    assertEquals(Color.BLUE, consumersSpringmvc.getIntf().enumBody(Color.BLUE));
  }

  @Test
  public void enumBody_springmvc_rt() {
    assertEquals(Color.BLUE,
        consumersSpringmvc.getSCBRestTemplate().postForObject("/enumBody", Color.BLUE, Color.class));
  }

  // query array
  @Test
  public void queryArr_springmvc_intf() {
    // default
    assertEquals("[a, b, c]3",
        consumersSpringmvc.getIntf().queryArr(new String[] {"a", "b", "c"}));
    assertEquals("[a, ,  , b, c]5",
        consumersSpringmvc.getIntf().queryArr(new String[] {"a", "", " ", "b", "c"}));
    // CSV
    assertEquals("[a, b, c]3",
        consumersSpringmvc.getIntf().queryArrCSV(new String[] {"a", "b", "c"}));
    assertEquals("[a, b, , c]4",
        consumersSpringmvc.getIntf().queryArrCSV(new String[] {null, "a", null, null, "b", null, "", null, "c", null}));
    assertEquals("[a, ,  , b, c]5",
        consumersSpringmvc.getIntf().queryArrCSV(new String[] {"a", "", " ", "b", "c"}));
    // SSV
    assertEquals("[a, b, c]3",
        consumersSpringmvc.getIntf().queryArrSSV(new String[] {"a", "b", "c"}));
    assertEquals("[a, b, , c]4",
        consumersSpringmvc.getIntf().queryArrSSV(new String[] {null, "a", null, null, "b", null, "", null, "c", null}));
    // TSV
    assertEquals("[a, b, c]3",
        consumersSpringmvc.getIntf().queryArrTSV(new String[] {"a", "b", "c"}));
    assertEquals("[a, b, , c]4",
        consumersSpringmvc.getIntf().queryArrTSV(new String[] {null, "a", null, null, "b", null, "", null, "c", null}));
    assertEquals("[a, ,  , b, c]5",
        consumersSpringmvc.getIntf().queryArrTSV(new String[] {"a", "", " ", "b", "c"}));
    // PIPES
    assertEquals("[a, b, c]3",
        consumersSpringmvc.getIntf().queryArrPIPES(new String[] {"a", "b", "c"}));
    assertEquals("[a, b, , c]4",
        consumersSpringmvc.getIntf()
            .queryArrPIPES(new String[] {null, "a", null, null, "b", null, "", null, "c", null}));
    assertEquals("[a, ,  , b, c]5",
        consumersSpringmvc.getIntf().queryArrPIPES(new String[] {"a", "", " ", "b", "c"}));
    // MULTI
    assertEquals("[a, b, c]3",
        consumersSpringmvc.getIntf().queryArrMULTI(new String[] {"a", "b", "c"}));
    assertEquals("[a, b, , c]4",
        consumersSpringmvc.getIntf()
            .queryArrMULTI(new String[] {null, "a", null, null, "b", null, "", null, "c", null}));
    assertEquals("[a, ,  , b, c]5",
        consumersSpringmvc.getIntf().queryArrMULTI(new String[] {"a", "", " ", "b", "c"}));
  }

  @Test
  public void queryArr_springmvc_rt() {
    // default
    assertEquals("[a, b, c]3",
        consumersSpringmvc.getSCBRestTemplate()
            .getForObject("/queryArr?queryArr=a&queryArr=b&queryArr=c", String.class));
    assertEquals("[a, ,  , b, c]5",
        consumersSpringmvc.getSCBRestTemplate()
            .getForObject("/queryArr?queryArr=a&queryArr=&queryArr= &queryArr=b&queryArr=c", String.class));
    // csv
    assertEquals("[a, b, c]3",
        consumersSpringmvc.getSCBRestTemplate()
            .getForObject("/queryArrCSV?queryArr=a,b,c", String.class));
    assertEquals("[a, ,  , b, c]5",
        consumersSpringmvc.getSCBRestTemplate()
            .getForObject("/queryArrCSV?queryArr=a,, ,b,c", String.class));
    // ssv
    assertEquals("[a, b, c]3",
        consumersSpringmvc.getSCBRestTemplate()
            .getForObject("/queryArrSSV?queryArr=a b c", String.class));
    // tsv
    assertEquals("[a, b, c]3",
        consumersSpringmvc.getSCBRestTemplate()
            .getForObject("/queryArrTSV?queryArr=a\tb\tc", String.class));
    assertEquals("[a, ,  , b, c]5",
        consumersSpringmvc.getSCBRestTemplate()
            .getForObject("/queryArrTSV?queryArr=a\t\t \tb\tc", String.class));
    // pipes
    assertEquals("[a, b, c]3",
        consumersSpringmvc.getSCBRestTemplate()
            .getForObject("/queryArrPIPES?queryArr=a|b|c", String.class));
    assertEquals("[a, ,  , b, c]5",
        consumersSpringmvc.getSCBRestTemplate()
            .getForObject("/queryArrPIPES?queryArr=a|| |b|c", String.class));
    // multi
    assertEquals("[a, b, c]3",
        consumersSpringmvc.getSCBRestTemplate()
            .getForObject("/queryArrMULTI?queryArr=a&queryArr=b&queryArr=c", String.class));
    assertEquals("[a, ,  , b, c]5",
        consumersSpringmvc.getSCBRestTemplate()
            .getForObject("/queryArrMULTI?queryArr=a&queryArr=&queryArr= &queryArr=b&queryArr=c", String.class));
  }

  @Test
  public void queryArr_jaxrs_intf() {
    assertEquals("[a, b, c]3",
        consumersJaxrs.getIntf().queryArr(new String[] {"a", "b", "c"}));
    assertEquals("[a, b, , c]4",
        consumersJaxrs.getIntf().queryArr(new String[] {null, "a", null, null, "b", null, "", null, "c", null}));
    assertEquals("[a, ,  , b, c]5",
        consumersJaxrs.getIntf().queryArr(new String[] {"a", "", " ", "b", "c"}));

    assertEquals("[a, b, c]3",
        consumersJaxrs.getIntf().queryArrCSV(new String[] {"a", "b", "c"}));
    assertEquals("[a, b, , c]4",
        consumersJaxrs.getIntf().queryArrCSV(new String[] {null, "a", null, null, "b", null, "", null, "c", null}));
    assertEquals("[a, ,  , b, c]5",
        consumersJaxrs.getIntf().queryArrCSV(new String[] {"a", "", " ", "b", "c"}));

    assertEquals("[a, b, c]3",
        consumersJaxrs.getIntf().queryArrSSV(new String[] {"a", "b", "c"}));
    assertEquals("[a, b, , c]4",
        consumersJaxrs.getIntf().queryArrSSV(new String[] {null, "a", null, null, "b", null, "", null, "c", null}));

    assertEquals("[a, b, c]3",
        consumersJaxrs.getIntf().queryArrTSV(new String[] {"a", "b", "c"}));
    assertEquals("[a, b, , c]4",
        consumersJaxrs.getIntf().queryArrTSV(new String[] {null, "a", null, null, "b", null, "", null, "c", null}));
    assertEquals("[a, ,  , b, c]5",
        consumersJaxrs.getIntf().queryArrTSV(new String[] {"a", "", " ", "b", "c"}));

    assertEquals("[a, b, c]3",
        consumersJaxrs.getIntf().queryArrPIPES(new String[] {"a", "b", "c"}));
    assertEquals("[a, b, , c]4",
        consumersJaxrs.getIntf().queryArrPIPES(new String[] {null, "a", null, null, "b", null, "", null, "c", null}));
    assertEquals("[a, ,  , b, c]5",
        consumersJaxrs.getIntf().queryArrPIPES(new String[] {"a", "", " ", "b", "c"}));

    assertEquals("[a, b, c]3",
        consumersJaxrs.getIntf().queryArrMULTI(new String[] {"a", "b", "c"}));
    assertEquals("[a, b, , c]4",
        consumersJaxrs.getIntf().queryArrMULTI(new String[] {null, "a", null, null, "b", null, "", null, "c", null}));
    assertEquals("[a, ,  , b, c]5",
        consumersJaxrs.getIntf().queryArrMULTI(new String[] {"a", "", " ", "b", "c"}));
  }

  @Test
  public void queryArr_jaxrs_rt() {
    // default
    assertEquals("[a, b, c]3",
        consumersJaxrs.getSCBRestTemplate()
            .getForObject("/queryArr?queryArr=a&queryArr=b&queryArr=c", String.class));
    assertEquals("[a, ,  , b, c]5",
        consumersJaxrs.getSCBRestTemplate()
            .getForObject("/queryArr?queryArr=a&queryArr=&queryArr= &queryArr=b&queryArr=c", String.class));
    // csv
    assertEquals("[a, b, c]3",
        consumersJaxrs.getSCBRestTemplate()
            .getForObject("/queryArrCSV?queryArr=a,b,c", String.class));
    assertEquals("[a, ,  , b, c]5",
        consumersJaxrs.getSCBRestTemplate()
            .getForObject("/queryArrCSV?queryArr=a,, ,b,c", String.class));
    // ssv
    assertEquals("[a, b, c]3",
        consumersJaxrs.getSCBRestTemplate()
            .getForObject("/queryArrSSV?queryArr=a b c", String.class));
    // tsv
    assertEquals("[a, b, c]3",
        consumersJaxrs.getSCBRestTemplate()
            .getForObject("/queryArrTSV?queryArr=a\tb\tc", String.class));
    assertEquals("[a, ,  , b, c]5",
        consumersJaxrs.getSCBRestTemplate()
            .getForObject("/queryArrTSV?queryArr=a\t\t \tb\tc", String.class));
    // pipes
    assertEquals("[a, b, c]3",
        consumersJaxrs.getSCBRestTemplate()
            .getForObject("/queryArrPIPES?queryArr=a|b|c", String.class));
    assertEquals("[a, ,  , b, c]5",
        consumersJaxrs.getSCBRestTemplate()
            .getForObject("/queryArrPIPES?queryArr=a|| |b|c", String.class));
    // multi
    assertEquals("[a, b, c]3",
        consumersJaxrs.getSCBRestTemplate()
            .getForObject("/queryArrMULTI?queryArr=a&queryArr=b&queryArr=c", String.class));
    assertEquals("[a, ,  , b, c]5",
        consumersJaxrs.getSCBRestTemplate()
            .getForObject("/queryArrMULTI?queryArr=a&queryArr=&queryArr= &queryArr=b&queryArr=c", String.class));
  }
}
