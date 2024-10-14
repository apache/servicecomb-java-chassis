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

package org.apache.servicecomb.samples;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Component
public class HelloWorldIT implements CategorizedTestCase {
  RestOperations template = new RestTemplate();

  @Override
  public void testRestTransport() throws Exception {
    testHelloWorld();
    testGetConfig();
  }

  private void testGetConfig() {
    String result = template
        .getForObject(Config.GATEWAY_URL + "/getConfig?key=key1", String.class);
    TestMgr.check("1", result);
    result = template
        .getForObject(Config.GATEWAY_URL + "/getConfig?key=key2", String.class);
    TestMgr.check("3", result);
    result = template
        .getForObject(Config.GATEWAY_URL + "/getConfig?key=key3", String.class);
    TestMgr.check("5", result);
  }

  private void testHelloWorld() {
    String result = template
        .getForObject(Config.GATEWAY_URL + "/sayHello?name=World", String.class);
    TestMgr.check("Hello World", result);

    // test trace id added
    MultiValueMap<String, String> headers = new HttpHeaders();
    headers.add("X-B3-TraceId", "81de2eb7691c2bbb");
    HttpEntity<Object> entity = new HttpEntity(headers);
    ResponseEntity<String> response =
        template.exchange(Config.GATEWAY_URL + "/sayHello?name=World", HttpMethod.GET, entity, String.class);
    TestMgr.check(1, response.getHeaders().get("X-B3-TraceId").size());
    TestMgr.check("81de2eb7691c2bbb", response.getHeaders().getFirst("X-B3-TraceId"));
    TestMgr.check("Hello World", response.getBody());
  }
}
