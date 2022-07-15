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

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.it.extend.engine.GateRestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class TestRequestBodySpringMvcSchema {
  private static GateRestTemplate edgeClient = GateRestTemplate.createEdgeRestTemplate("requestBodySpringMvcSchema");

  @Test
  public void basicRequestResponse() {
    basicRequestResponseImpl();
    basicRequestResponseImpl();
    basicRequestResponseImpl();
  }

  @Test
  public void testDefaultForPrimitive() {
    testDefaultForPrimitiveImpl();
    testDefaultForPrimitiveImpl();
    testDefaultForPrimitiveImpl();
  }

  private void testDefaultForPrimitiveImpl() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    Map<String, Object> request = new HashMap<>();
    request.put("integerType", 100);
    request.put("message", "hi");
    request.put("catalog", 100);
    request.put("extendedMessage", "hi");

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
    @SuppressWarnings("unchecked")
    Map<String, Object> result = edgeClient.postForObject("/base", entity, Map.class);
    Assertions.assertEquals(result.size(), 6);
    Assertions.assertEquals(result.get("type"), 0);
    Assertions.assertEquals(result.get("integerType"), request.get("integerType"));
    Assertions.assertEquals(result.get("message"), request.get("message"));
    Assertions.assertEquals(result.get("catalog"), request.get("catalog"));
    Assertions.assertNull(result.get("integerCatalog"));
    Assertions.assertEquals(result.get("extendedMessage"), request.get("extendedMessage"));
  }

  private void basicRequestResponseImpl() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    Map<String, Object> request = new HashMap<>();
    request.put("type", 100);
    request.put("integerType", 100);
    request.put("message", "hi");
    request.put("catalog", 100);
    request.put("integerCatalog", 100);
    request.put("extendedMessage", "hi");

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
    @SuppressWarnings("unchecked")
    Map<String, Object> result =
        edgeClient.postForObject("/base", entity, Map.class);
    Assertions.assertEquals(result.size(), request.size());
    Assertions.assertEquals(result.get("type"), request.get("type"));
    Assertions.assertEquals(result.get("integerType"), request.get("integerType"));
    Assertions.assertEquals(result.get("message"), request.get("message"));
    Assertions.assertEquals(result.get("catalog"), request.get("catalog"));
    Assertions.assertEquals(result.get("integerCatalog"), request.get("integerCatalog"));
    Assertions.assertEquals(result.get("extendedMessage"), request.get("extendedMessage"));
  }
}
