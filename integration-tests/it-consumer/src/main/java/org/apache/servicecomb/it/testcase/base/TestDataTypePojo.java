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
import org.apache.servicecomb.it.testcase.support.DataTypePojoIntf;
import org.apache.servicecomb.it.testcase.support.ProducerDevMode;
import org.apache.servicecomb.provider.springmvc.reference.CseRestTemplate;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

public class TestDataTypePojo {
  private static DataTypePojoIntf dataTypePojoIntf;

  private static ITClientHttpRequestFactory clientHttpRequestFactory = new ITClientHttpRequestFactory();

  private static RestTemplate restTemplate = new CseRestTemplate();

  private static String urlPrefix;

  private static String transport;

  static {
    restTemplate.setRequestFactory(clientHttpRequestFactory);
  }

  public static void init(String transport, ProducerDevMode producerDevMode) {
    TestDataTypePojo.transport = transport;
    dataTypePojoIntf = ITInvoker.createProxy("it-producer", "dataTypePojo", transport, DataTypePojoIntf.class);

    clientHttpRequestFactory.setTransport(transport);

    urlPrefix = "cse://it-producer/v1/dataType" + producerDevMode.name();
  }

  @Test
  public void checkTransport_intf() {
    assertEquals(transport, dataTypePojoIntf.checkTransport());
  }

  @Test
  public void checkTransport_rt() {
    assertEquals(transport, restTemplate.postForObject(urlPrefix + "/checkTransport", "", String.class));
  }

  @Test
  public void intBody_intf() {
    assertEquals(10, dataTypePojoIntf.intBody(10));
  }

  @Test
  public void intBody_rt() {
    Map<String, Integer> map = new HashMap<>();
    Map<String, String> map1 = new HashMap<>();
    map.put("input", 10);
    map1.put("input", "10");

    assertEquals(10, (int) restTemplate.postForObject(urlPrefix + "/intBody", map, int.class));
    assertEquals(10, (int) restTemplate.postForObject(urlPrefix + "/intBody", map1
        , int.class));
  }

  @Test
  public void intReduce_intf() {
    assertEquals(8, dataTypePojoIntf.reduce(10, 2));
  }

  @Test
  public void intReduce_rt() {
    Map<String, Integer> map = new HashMap<>();
    map.put("a", 10);
    map.put("b", 2);
    Map<String, String> map1 = new HashMap<>();
    map1.put("a", "10");
    map1.put("b", "2");
    assertEquals(8, (int) restTemplate.postForObject(urlPrefix + "/reduce", map, int.class));
    assertEquals(8, (int) restTemplate.postForObject(urlPrefix + "/reduce", map1, int.class));
  }
}
