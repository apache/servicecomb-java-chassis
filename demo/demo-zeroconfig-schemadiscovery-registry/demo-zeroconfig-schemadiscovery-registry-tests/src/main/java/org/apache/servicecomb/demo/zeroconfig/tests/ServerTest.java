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

package org.apache.servicecomb.demo.zeroconfig.tests;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import io.vertx.core.json.JsonObject;

@Component
public class ServerTest implements CategorizedTestCase {

  RestTemplate template = RestTemplateBuilder.create();

  @Override
  public void testRestTransport() throws Exception {
    testServerGetName();
    testGetAllMicroservice();
    testJsonObject();
    testString();
    testDateForEdge();
    testContextMapper();
  }

  private void testContextMapper() throws URISyntaxException {
    MultiValueMap<String, String> headers = new HttpHeaders();
    headers.add("clientHeader", "v1");
    headers.add("gatewayHeader", "v2");
    RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET,
        new URI("cse://demo-zeroconfig-schemadiscovery-registry-edge/register/url/prefix/contextMapper?clientQuery=v3&"
            + "gatewayQuery=v4"));
    ResponseEntity<String> response = template.exchange(requestEntity, String.class);
    TestMgr.check(response.getBody(), "success");
  }

  @SuppressWarnings("unchecked")
  private void testDateForEdge() {
    for (int i = 0; i < 3; i++) {
      ClientModel clientModelReq = new ClientModel();
      Date date = new Date(1663590135202L);
      clientModelReq.setUpdateDate(date);
      Map<String, Object> response = template
          .postForObject(
              "cse://demo-zeroconfig-schemadiscovery-registry-edge"
                  + "/register/url/prefix/postModel", clientModelReq,
              Map.class);
      Object result = response.get("updateDate");
      // TODO: highway and rest Date field will give different result
      // we can not change this now, because it is incompatible
      if (result instanceof Long) {
        TestMgr.check(response.get("updateDate"), 1663590135202L);
      } else {
        TestMgr.check(response.get("updateDate"), "2022-09-19T12:22:15.202+00:00");
      }
    }
  }

  private void testServerGetName() throws Exception {
    // invoke demo-zeroconfig-schemadiscovery-registry-client
    TestMgr.check("world", template
        .getForObject(
            "cse://demo-zeroconfig-schemadiscovery-registry-client/register/url/prefix/getName?name=world",
            String.class));
    // invoke demo-zeroconfig-schemadiscovery-registry-edge
    // create many threads to test event-loop not blocking
    int thread = 32;
    CountDownLatch latch = new CountDownLatch(thread);
    for (int i = 0; i < thread; i++) {
      new Thread(() -> {
        for (int j = 0; j < 20; j++) {
          try {
            TestMgr.check("world", template
                .getForObject(
                    "cse://demo-zeroconfig-schemadiscovery-registry-edge/register/url/prefix/getName?name=world",
                    String.class));
          } catch (Exception e) {
            TestMgr.failed("test failed", e);
          }
        }
        latch.countDown();
      }).start();
    }

    latch.await();
  }

  @SuppressWarnings("rawTypes")
  private void testGetAllMicroservice() {
    // invoke demo-zeroconfig-schemadiscovery-registry-client
    TestMgr.check("2", template
        .getForObject(
            "cse://demo-zeroconfig-schemadiscovery-registry-client"
                + "/register/url/prefix/getRegisteredMicroservice",
            List.class).size());
    // invoke demo-zeroconfig-schemadiscovery-registry-edge
    TestMgr.check("2", template
        .getForObject(
            "cse://demo-zeroconfig-schemadiscovery-registry-edge"
                + "/register/url/prefix/getRegisteredMicroservice",
            List.class).size());
  }

  private void testJsonObject() {
    JsonObject in = new JsonObject();
    JsonObject inner = new JsonObject();
    //调用者需要按照swagger传参
    inner.put("hello", "world");
    in.put("map", inner);

    JsonObject result = template
        .postForObject(
            "cse://demo-zeroconfig-schemadiscovery-registry-client"
                + "/register/url/prefix/jsonObject", in, JsonObject.class);
    TestMgr.check(inner.toString(), result.toString());
    TestMgr.check(result.getString("hello"), "world");
  }

  private void testString() {
    String in = "{\"hello\":\"world\"}";
    String result = template
        .postForObject(
            "cse://demo-zeroconfig-schemadiscovery-registry-client"
                + "/register/url/prefix/getString", in, String.class);
    TestMgr.check(in, result);
  }
}
