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

import org.apache.servicecomb.core.Const;
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

public class TestDataTypeJaxrs {
  private static DataTypeRestIntf dataTypeIntf;

  private static ITClientHttpRequestFactory clientHttpRequestFactory = new ITClientHttpRequestFactory();

  private static RestTemplate restTemplate = new CseRestTemplate();

  private static String urlPrefix;

  private static String transport;

  static {
    restTemplate.setRequestFactory(clientHttpRequestFactory);
  }

  public static void init(String transport, ProducerDevMode producerDevMode) {
    TestDataTypeJaxrs.transport = transport;
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

    map.put("a", 10);
    HttpEntity<Map<String, Integer>> formEntiry = new HttpEntity<>(map, formHeaders);

    assertEquals(10, (int) restTemplate.postForEntity(urlPrefix + "/intForm", formEntiry, int.class).getBody());
    Map<String, String> params = new HashMap<>();
    params.put("a", "5");
    assertEquals(5, (int) restTemplate.postForEntity(urlPrefix + "/intForm", params, int.class).getBody());
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
    headers.add("input", "11");
    @SuppressWarnings("rawtypes")
    HttpEntity entity = new HttpEntity<>(null, headers);
    ResponseEntity<Integer> response = restTemplate.exchange(urlPrefix + "/intHeaderWithDefault",
        HttpMethod.GET,
        entity,
        int.class);
    assertEquals(11, (int) response.getBody());
    headers.remove("input");
    @SuppressWarnings("rawtypes")
    HttpEntity<Object> entity1 = new HttpEntity<>(null, headers);
    ResponseEntity<Integer> response1 = restTemplate.exchange(urlPrefix + "/intHeaderWithDefault",
        HttpMethod.GET,
        entity1,
        int.class);
    assertEquals(13, (int) response1.getBody());
  }

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
//    HttpEntity entity1 = new HttpEntity<>(headers);
//    ResponseEntity<Integer> response1 = restTemplate.exchange(urlPrefix + "/intCookieWithDefault",
//        HttpMethod.GET,
//        entity1,
//        Integer.class);
//    assertEquals(10, (int) response1.getBody());
//  }

  @Test
  public void intFormWithDefault_rt() {
    HttpHeaders formHeaders = new HttpHeaders();
    formHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    Map<String, Integer> map = new HashMap<>();

    map.put("a", 10);
    HttpEntity<Map<String, Integer>> formEntiry = new HttpEntity<>(map, formHeaders);

    assertEquals(10,
        (int) restTemplate.postForEntity(urlPrefix + "/intFormWithDefault", formEntiry, int.class).getBody());

    map.remove("a");

    HttpEntity<Map<String, Integer>> formEntiry1 = new HttpEntity<>(map, formHeaders);

    assertEquals(13,
        (int) restTemplate.postForEntity(urlPrefix + "/intFormWithDefault", formEntiry1, int.class).getBody());
  }

  //伪契约不支持 highway
  @Test
  public void testRequest_rt() {
    //@context don not support highway
    Map<String, String> params = new HashMap<>();
    params.put("a", "5");
    params.put("b", "3");
    if (transport.equals(Const.RESTFUL)) {
      int result = restTemplate.getForObject(urlPrefix + "/request?a={a}&b={b}", Integer.class, 5, 4);
      assertEquals(1, result);
    }
  }

  @Test
  public void testDefault_intf() {
    int result = dataTypeIntf.defaultPath();
    assertEquals(result, 100);
  }

  @Test
  public void testDefault_rt() {
    Integer result = restTemplate.getForObject(urlPrefix, Integer.class);
    assertEquals((int) result, 100);
  }
}
