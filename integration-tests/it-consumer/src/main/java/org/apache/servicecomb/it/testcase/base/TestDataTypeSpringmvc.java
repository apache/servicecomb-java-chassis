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

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.it.extend.engine.ITClientHttpRequestFactory;
import org.apache.servicecomb.it.extend.engine.ITInvoker;
import org.apache.servicecomb.it.testcase.support.DataTypeRestIntf;
import org.apache.servicecomb.it.testcase.support.ProducerDevMode;
import org.apache.servicecomb.provider.springmvc.reference.CseRestTemplate;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class TestDataTypeSpringmvc {
  private static DataTypeRestIntf dataTypeIntf;

  private static ITClientHttpRequestFactory clientHttpRequestFactory = new ITClientHttpRequestFactory();

  private static RestTemplate restTemplate = new CseRestTemplate();

  private static String urlPrefix;

  private static String transport;

  static {
    restTemplate.setRequestFactory(clientHttpRequestFactory);
  }

  public static void init(String transport, ProducerDevMode producerDevMode) {
    TestDataTypeSpringmvc.transport = transport;
    dataTypeIntf = ITInvoker
        .createProxy("it-producer", "dataType" + producerDevMode.name(), transport, DataTypeRestIntf.class);

    clientHttpRequestFactory.setTransport(transport);

    urlPrefix = "cse://it-producer/v1/dataType" + producerDevMode.name();
  }

  @Test
  public void checkTransport_intf() {
    assertEquals(transport, dataTypeIntf.checkTransport());
  }

  @Test
  public void checkTransport_rt() {
    assertEquals(transport, restTemplate.getForObject(urlPrefix + "/checkTransport", String.class));
  }

  @Test
  public void intPath_intf() {
    int expect = 10;
    assertEquals(expect, dataTypeIntf.intPath(expect));
  }

  @Test
  public void intPath_rt() {
    int expect = 10;
    assertEquals(expect, (int) restTemplate.getForObject(urlPrefix + "/intPath/" + expect, int.class));
  }

  @Test
  public void intQuery_intf() {
    assertEquals(10, dataTypeIntf.intQuery(10));
  }

  @Test
  public void intQuery_rt() {
    int expect = 10;
    assertEquals(expect, (int) restTemplate.getForObject(urlPrefix + "/intQuery?input=" + expect, int.class));
  }

  @Test
  public void intHeader_intf() {
    assertEquals(10, dataTypeIntf.intHeader(10));
  }

  @Test
  public void intHeader_rt() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("input", "10");
    @SuppressWarnings("rawtypes")
    HttpEntity entity = new HttpEntity<>(null, headers);
    ResponseEntity<Integer> response = restTemplate.exchange(urlPrefix + "/intHeader",
        HttpMethod.GET,
        entity,
        int.class);
    assertEquals(10, (int) response.getBody());
  }

  @Test
  public void intCookie_intf() {
    assertEquals(10, dataTypeIntf.intCookie(10));
  }

  @Test
  public void intCookie_rt() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", "input=10");
    @SuppressWarnings("rawtypes")
    HttpEntity entity = new HttpEntity<>(null, headers);
    ResponseEntity<Integer> response = restTemplate.exchange(urlPrefix + "/intCookie",
        HttpMethod.GET,
        entity,
        int.class);
    assertEquals(10, (int) response.getBody());
  }

  @Test
  public void intBody_intf() {
    assertEquals(10, dataTypeIntf.intBody(10));
  }

  @Test
  public void intBody_rt() {
    assertEquals(10, (int) restTemplate.postForObject(urlPrefix + "/intBody", 10, int.class));
  }

  @Test
  public void intForm_intf() {
    assertEquals(10, dataTypeIntf.intForm(10));
  }

  @Test
  public void intForm_rt() {
    HttpHeaders formHeaders = new HttpHeaders();
    formHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    Map<String, Integer> map = new HashMap<>();

    map.put("form1", 10);
    HttpEntity<Map<String, Integer>> formEntiry = new HttpEntity<>(map, formHeaders);

    assertEquals(10, (int) restTemplate.postForEntity(urlPrefix + "/intForm", formEntiry, int.class).getBody());
    //两种调用方式都可以
    assertEquals(10, (int) restTemplate.postForEntity(urlPrefix + "/intForm", map, int.class).getBody());
  }

  @Test
  public void intAttribute_intf() {
    assertEquals(10, dataTypeIntf.intAttribute(10));
  }

  @Test
  public void intAttribute_rt() {
    Map<String, Integer> map = new HashMap<>();
    map.put("a", 10);
    int result = restTemplate.postForObject(urlPrefix + "/intAttribute", map, Integer.class);
    assertEquals(10, result);
  }

  @Test
  public void intQueryWithDefault_rt() {
    int expect = 10;
    assertEquals(expect,
        (int) restTemplate.getForObject(urlPrefix + "/intQueryWithDefault?input=" + expect, int.class));
    assertEquals(13, (int) restTemplate.getForObject(urlPrefix + "/intQueryWithDefault", int.class));
  }

  @Test
  public void intHeaderWithDefault_rt() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("input", "10");
    @SuppressWarnings("rawtypes")
    HttpEntity entity = new HttpEntity<>(null, headers);
    ResponseEntity<Integer> response = restTemplate.exchange(urlPrefix + "/intHeaderWithDefault",
        HttpMethod.GET,
        entity,
        int.class);
    assertEquals(10, (int) response.getBody());
    headers.remove("input");
    @SuppressWarnings("rawtypes")
    HttpEntity<Object> entity1 = new HttpEntity<>(null, headers);
    ResponseEntity<Integer> response1 = restTemplate.exchange(urlPrefix + "/intHeaderWithDefault",
        HttpMethod.GET,
        entity1,
        int.class);
    assertEquals(13, (int) response1.getBody());
  }
//暂时不支持 cookie 设置默认值,不过以后会支持,先放这里
//  @Test
//  public void intCookieWithDefault_rt() {
//    HttpHeaders headers = new HttpHeaders();
////    headers.add("Cookie", "input=10");
////    @SuppressWarnings("rawtypes")
////    HttpEntity entity = new HttpEntity<>( headers);
////    ResponseEntity<Integer> response = restTemplate.exchange(urlPrefix + "/intCookieWithDefault",
////        HttpMethod.GET,
////        entity,
////        int.class);
////    assertEquals(10, (int) response.getBody());
////    headers.remove("Cookie");
//    headers.add("Cookie", "input=10");
//
//    @SuppressWarnings("rawtypes")
//    HttpEntity entity1 = new HttpEntity<>( headers);
//    ResponseEntity<Integer> response1 = restTemplate.exchange(urlPrefix + "/intCookieWithDefault",
//        HttpMethod.GET,
//        entity1,
//        Integer.class);
//    assertEquals(10, (int) response1.getBody());
//  }

  @Test
  public void intAttributeWithDefault_rt() {
    Map<String, Integer> map = new HashMap<>();
    map.put("a", 10);
    int result = restTemplate.postForObject(urlPrefix + "/intAttributeWithDefault", map, Integer.class);
    assertEquals(10, result);

    map.remove("a");
    int result1 = restTemplate.postForObject(urlPrefix + "/intAttributeWithDefault", map, Integer.class);
    assertEquals(13, result1);
  }

  @Test
  public void intAdd_intf() {
    int i = dataTypeIntf.intAdd(2, 3);
    assertEquals(5, i);
  }

  @Test
  public void intAdd_rt() {
    Map<String, Integer> map = new HashMap<>();
    map.put("a", 10);
    map.put("b", 10);
    int result = restTemplate.postForObject(urlPrefix + "/add", map, Integer.class);
    assertEquals(20, result);
  }

  @Test
  public void intMulti_intf() {
    int a = 1, b = 1, c = 1, d = 1, e = 1;
    assertEquals(String.format("a=%s,b=%s,c=%s,d=%s,e=%s", a, b, c, d, e), dataTypeIntf.intMulti(a, b, c, d, e));
  }

  @Test
  public void intMulti_rt() {
    Map<String, String> params = new HashMap<>();
    params.put("a", "1");
    params.put("e", "1");
    params.put("c", "1");
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.COOKIE, "b=1");
    headers.add("d", "1");
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    ResponseEntity<String> result = restTemplate.exchange(
        urlPrefix + "/intMulti/{e}?a={a}&c={c}",
        HttpMethod.POST,
        requestEntity,
        String.class,
        params);
    assertEquals(String.format("a=%s,b=%s,c=%s,d=%s,e=%s", 1, 1, 1, 1, 1), result.getBody());
  }

  @Test
  public void intRequestQuery_rt() {
    int expect = 10;
    Map<String, String> params = new HashMap<>();
    params.put("a", "10");
    assertEquals(expect, (int) restTemplate.postForObject(urlPrefix + "/queryRequest?a=" + expect, null, int.class));
    assertEquals(expect, (int) restTemplate.postForObject(urlPrefix + "/queryRequest?a={a}", null, int.class, params));
  }

  @Test
  public void intRequestForm_rt() {
    HttpHeaders formHeaders = new HttpHeaders();
    formHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    Map<String, Integer> map = new HashMap<>();

    map.put("form1", 10);
    map.put("form2", 10);
    HttpEntity<Map<String, Integer>> formEntiry = new HttpEntity<>(map, formHeaders);

    assertEquals(String.format("form1=%s,form2=%s", 10, 10),
        restTemplate.postForEntity(urlPrefix + "/formRequest", formEntiry, String.class).getBody());
    //other method
    assertEquals(String.format("form1=%s,form2=%s", 10, 10),
        restTemplate.postForEntity(urlPrefix + "/formRequest", map, String.class).getBody());
  }
}
