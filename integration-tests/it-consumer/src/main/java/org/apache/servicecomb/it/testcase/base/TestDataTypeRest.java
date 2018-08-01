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

import org.apache.servicecomb.it.extend.engine.ITClientHttpRequestFactory;
import org.apache.servicecomb.it.extend.engine.ITInvoker;
import org.apache.servicecomb.it.testcase.support.DataTypeRestIntf;
import org.apache.servicecomb.it.testcase.support.ProducerDevMode;
import org.apache.servicecomb.provider.springmvc.reference.CseRestTemplate;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

public class TestDataTypeRest {
  private static DataTypeRestIntf dataTypeIntf;

  private static ITClientHttpRequestFactory clientHttpRequestFactory = new ITClientHttpRequestFactory();

  private static RestTemplate restTemplate = new CseRestTemplate();

  private static String urlPrefix;

  private static String transport;

  static {
    restTemplate.setRequestFactory(clientHttpRequestFactory);
  }

  public static void init(String transport, ProducerDevMode producerDevMode) {
    TestDataTypeRest.transport = transport;
    dataTypeIntf = ITInvoker
        .createProxy("it-producer", "dataType" + producerDevMode.name(), transport, DataTypeRestIntf.class);

    clientHttpRequestFactory.setTransport(transport);

    urlPrefix = "cse://it-producer/v1/dataType" + producerDevMode.name();
  }

  @Test
  public void checkTransport_intf() {
    Assert.assertEquals(transport, dataTypeIntf.checkTransport());
  }

  @Test
  public void checkTransport_rt() {
    Assert.assertEquals(transport, restTemplate.getForObject(urlPrefix + "/checkTransport", String.class));
  }

  @Test
  public void intPath_intf() {
    int expect = 10;
    Assert.assertEquals(expect, dataTypeIntf.intPath(expect));
  }

  @Test
  public void intPath_rt() {
    int expect = 10;
    Assert.assertEquals(expect, (int) restTemplate.getForObject(urlPrefix + "/intPath/" + expect, int.class));
  }

  @Test
  public void intQuery() {
    Assert.assertEquals(10, dataTypeIntf.intQuery(10));
  }

  @Test
  public void intHeader() {
    Assert.assertEquals(10, dataTypeIntf.intHeader(10));
  }

  @Test
  public void intCookie() {
    Assert.assertEquals(10, dataTypeIntf.intCookie(10));
  }

  @Test
  public void intBody() {
    Assert.assertEquals(10, dataTypeIntf.intBody(10));
  }
}
