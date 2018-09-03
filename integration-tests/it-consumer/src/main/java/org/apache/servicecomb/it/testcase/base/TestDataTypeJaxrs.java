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
}
