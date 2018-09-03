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

    int add(int a, int b);
  }

  public interface DataTypeRestIntf {
    int intPath(int input);

    int intQuery(int input);

    int intHeader(int input);

    int intCookie(int input);

    int intBody(int input);

    int intForm(int input);

    int add(int a, int b);
  }

  private static Consumers<DataTypePojoIntf> consumersPojo = new Consumers<>("dataTypePojo", DataTypePojoIntf.class);

  private static Consumers<DataTypeRestIntf> consumersJaxrs = new Consumers<>("dataTypeJaxrs", DataTypeRestIntf.class);

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
  public void int_pojo_rt() {
    Map<String, Integer> map = new HashMap<>();
    map.put("input", 10);

    assertEquals(10, (int) consumersPojo.getSCBRestTemplate().postForObject("/intBody", map, int.class));
  }

  @Test
  public void add_pojo_intf() {
    assertEquals(12, consumersPojo.getIntf().add(10, 2));
  }

  @Test
  public void add_pojo_rt() {
    Map<String, Integer> map = new HashMap<>();
    map.put("a", 10);
    map.put("b", 2);
    assertEquals(12, (int) consumersPojo.getSCBRestTemplate().postForObject("/add", map, int.class));
  }

  @Test
  public void intPath_jaxrs_intf() {
    assertEquals(10, consumersJaxrs.getIntf().intPath(10));
  }

  @Test
  public void intPath_jaxrs_rt() {
    assertEquals(10, (int) consumersJaxrs.getSCBRestTemplate().getForObject("/intPath/10", int.class));
  }

  @Test
  public void intQuery_jaxrs_intf() {
    assertEquals(10, consumersJaxrs.getIntf().intQuery(10));
  }

  @Test
  public void intQuery_jaxrs_rt() {
    assertEquals(10, (int) consumersJaxrs.getSCBRestTemplate().getForObject("/intQuery?input=10", int.class));
  }

  @Test
  public void intHeader_jaxrs_intf() {
    assertEquals(10, consumersJaxrs.getIntf().intHeader(10));
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
  public void intCookie_jaxrs_intf() {
    assertEquals(10, consumersJaxrs.getIntf().intCookie(10));
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
  public void intForm_jaxrs_intf() {
    assertEquals(10, consumersJaxrs.getIntf().intForm(10));
  }

  @Test
  public void intForm_jaxrs_rt() {
    Map<String, Integer> map = new HashMap<>();
    map.put("input", 10);
    HttpEntity<Map<String, Integer>> formEntiry = new HttpEntity<>(map);

    assertEquals(10,
        (int) consumersJaxrs.getSCBRestTemplate().postForEntity("/intForm", formEntiry, int.class).getBody());
  }

  @Test
  public void intBody_jaxrs_intf() {
    assertEquals(10, consumersJaxrs.getIntf().intBody(10));
  }

  @Test
  public void intBody_jaxrs_rt() {
    assertEquals(10, (int) consumersJaxrs.getSCBRestTemplate().postForObject("/intBody", 10, int.class));
  }

  @Test
  public void add_jaxrs_intf() {
    assertEquals(12, consumersJaxrs.getIntf().add(10, 2));
  }

  @Test
  public void add_jaxrs_rt() {
    assertEquals(12, (int) consumersJaxrs.getSCBRestTemplate().getForObject("/add?a=10&b=2", int.class));
  }

  @Test
  public void intPath_springmvc_intf() {
    assertEquals(10, consumersSpringmvc.getIntf().intPath(10));
  }

  @Test
  public void intPath_springmvc_rt() {
    assertEquals(10, (int) consumersSpringmvc.getSCBRestTemplate().getForObject("/intPath/10", int.class));
  }

  @Test
  public void intQuery_springmvc_intf() {
    assertEquals(10, consumersSpringmvc.getIntf().intQuery(10));
  }

  @Test
  public void intQuery_springmvc_rt() {
    assertEquals(10, (int) consumersSpringmvc.getSCBRestTemplate().getForObject("/intQuery?input=10", int.class));
  }

  @Test
  public void intHeader_springmvc_intf() {
    assertEquals(10, consumersSpringmvc.getIntf().intHeader(10));
  }

  @Test
  public void intHeader_springmvc_rt() {
    intHeader_rt(consumersSpringmvc);
  }

  @Test
  public void intCookie_springmvc_intf() {
    assertEquals(10, consumersSpringmvc.getIntf().intCookie(10));
  }

  @Test
  public void intCookie_springmvc_rt() {
    intCookie_rt(consumersSpringmvc);
  }

  @Test
  public void intForm_springmvc_intf() {
    assertEquals(10, consumersSpringmvc.getIntf().intForm(10));
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
  public void intBody_springmvc_intf() {
    assertEquals(10, consumersSpringmvc.getIntf().intBody(10));
  }

  @Test
  public void intBody_springmvc_rt() {
    assertEquals(10, (int) consumersSpringmvc.getSCBRestTemplate().postForObject("/intBody", 10, int.class));
  }

  @Test
  public void add_springmvc_intf() {
    assertEquals(12, consumersSpringmvc.getIntf().add(10, 2));
  }

  @Test
  public void add_springmvc_rt() {
    assertEquals(12, (int) consumersSpringmvc.getSCBRestTemplate().getForObject("/add?a=10&b=2", int.class));
  }
}
