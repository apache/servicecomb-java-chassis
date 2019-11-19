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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.it.extend.engine.GateRestTemplate;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class TestDefaultJsonValueJaxrsSchema {
  static GateRestTemplate client = GateRestTemplate.createEdgeRestTemplate("defaultJsonValueJaxrs");

  @Test
  public void invokeFromEdgeWithQuery() {
    String result = client.getForObject("/queryInput?size=3", String.class);
    Assert.assertEquals("expected:3:3", result);

    result = client.getForObject("/queryInput", String.class);
    Assert.assertEquals("expected:0:0", result);

    result = client.getForObject("/queryInput?size=", String.class);
    Assert.assertEquals("expected:0:", result);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void invokeFromEdgeWithRawJson() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    Map<String, Object> body = new HashMap<>();
    body.put("type", 100);
    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
    Map<String, Object> result = client.postForObject("/jsonInput", entity, Map.class);
    Assert.assertEquals(100, result.get("type"));
    Assert.assertEquals("expected:null:null", result.get("message"));

    body = new HashMap<>();
    body.put("type", 100);
    body.put("defaultValue", null);
    entity = new HttpEntity<>(body, headers);
    result = client.postForObject("/jsonInput", entity, Map.class);
    Assert.assertEquals(100, result.get("type"));
    Assert.assertEquals("expected:null:null", result.get("message"));

    body = new HashMap<>();
    body.put("type", 200);
    body.put("defaultValue", -1);
    entity = new HttpEntity<>(body, headers);
    result = client.postForObject("/jsonInput", entity, Map.class);
    Assert.assertEquals(200, result.get("type"));
    Assert.assertEquals("expected:-1:null", result.get("message"));

    body = new HashMap<>();
    body.put("type", 200);
    body.put("defaultValue", -1);
    body.put("items", null);
    entity = new HttpEntity<>(body, headers);
    result = client.postForObject("/jsonInput", entity, Map.class);
    Assert.assertEquals(200, result.get("type"));
    Assert.assertEquals("expected:-1:null", result.get("message"));

    body = new HashMap<>();
    body.put("type", 200);
    body.put("defaultValue", -1);
    body.put("items", new ArrayList<String>());
    entity = new HttpEntity<>(body, headers);
    result = client.postForObject("/jsonInput", entity, Map.class);
    Assert.assertEquals(200, result.get("type"));
    Assert.assertEquals("expected:-1:0", result.get("message"));
  }
}
