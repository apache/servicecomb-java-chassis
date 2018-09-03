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
