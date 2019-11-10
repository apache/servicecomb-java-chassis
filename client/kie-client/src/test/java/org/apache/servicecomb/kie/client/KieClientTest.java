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

package org.apache.servicecomb.kie.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.kie.client.http.HttpResponse;
import org.apache.servicecomb.kie.client.model.KVBody;
import org.apache.servicecomb.kie.client.model.KVDoc;
import org.apache.servicecomb.kie.client.model.KVResponse;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by   on 2019/10/24.
 */
public class KieClientTest {

  @Test
  public void putKeyValue() throws IOException {
    KieRawClient kieRawClient = Mockito.mock(KieRawClient.class);

    KVBody kvBody = new KVBody();
    kvBody.setValue("test");
    kvBody.setValueType("string");
    Map<String, String> labels = new HashMap<>();
    labels.put("app1", "111");
    kvBody.setLabels(labels);

    HttpResponse httpResponse = new HttpResponse();
    httpResponse.setStatusCode(200);
    httpResponse.setMessage("OK");
    String responseContext = "{\n" +
        " \"_id\": \"5db0213bb927bafaf707e06a\",\n" +
        " \"label_id\": \"5db0039bb927bafaf707e037\",\n" +
        " \"key\": \"testKey\",\n" +
        " \"value\": \"testValue\",\n" +
        " \"value_type\": \"string\",\n" +
        " \"labels\": {\n" +
        "  \"app1\": \"111\"\n" +
        " },\n" +
        " \"domain\": \"default\",\n" +
        " \"revision\": 10\n" +
        "}";
    httpResponse.setContent(responseContext);
    ObjectMapper mapper = new ObjectMapper();

    Mockito.when(kieRawClient.putHttpRequest("/kie/kv/test1", null, mapper.writeValueAsString(kvBody)))
        .thenReturn(httpResponse);

    KieClient kieClient = new KieClient(kieRawClient);
    String kvResponses = kieClient.putKeyValue("test1", kvBody);

    Assert.assertNotNull(kvResponses);
//    Assert.assertEquals("testKey", mapper.writeValueAsString(kvResponses).getString("key"));
//    Assert.assertEquals("testValue", JSONObject.parseObject(kvResponses).getString("value"));
  }

  @Test
  public void getKeyValue() throws IOException {
    KieRawClient kieRawClient = Mockito.mock(KieRawClient.class);

    HttpResponse httpResponse = new HttpResponse();
    httpResponse.setStatusCode(200);
    httpResponse.setMessage("OK");
    String responseContext = "[\n" +
        "    {\n" +
        "        \"label\": {\n" +
        "            \"label_id\": \"5db0039bb927bafaf707e037\",\n" +
        "            \"labels\": {\"app1\": \"111\"\n" +
        "            }\n" +
        "        },\n" +
        "        \"data\": [\n" +
        "            {\n" +
        "                \"_id\": \"5db0127db927bafaf707e068\",\n" +
        "                \"key\": \"111\",\n" +
        "                \"value\": \"test\",\n" +
        "                \"value_type\": \"string\"\n" +
        "            }\n" +
        "        ]\n" +
        "    }\n" +
        "]";
    httpResponse.setContent(responseContext);

    Mockito.when(kieRawClient.getHttpRequest("/kie/kv/test1", null, null)).thenReturn(httpResponse);
    Mockito.when(kieRawClient.getHttpRequest("/kie/kv?q=app1:111", null, null)).thenReturn(httpResponse);

    KieClient kieClient = new KieClient(kieRawClient);
    List<KVResponse> kvResponses = kieClient.getValueOfKey("test1");

    Map<String, String> map = new HashMap<>();
    map.put("app1", "111");
    List<KVResponse> searchKVResponses = kieClient.searchKeyValueByLabels(map);

    Assert.assertNotNull(kvResponses);
    Assert.assertEquals("111", kvResponses.get(0).getData().get(0).getKey());
    Assert.assertEquals("test", kvResponses.get(0).getData().get(0).getValue());
    Assert.assertEquals("{app1=111}", kvResponses.get(0).getLabel().getLabels().toString());

    Assert.assertNotNull(searchKVResponses);
    Assert.assertEquals("111", searchKVResponses.get(0).getData().get(0).getKey());
    Assert.assertEquals("test", searchKVResponses.get(0).getData().get(0).getValue());
    Assert.assertEquals("{app1=111}", searchKVResponses.get(0).getLabel().getLabels().toString());
  }

  @Test
  public void deleteKeyValue() throws IOException {
    KieRawClient kieRawClient = Mockito.mock(KieRawClient.class);

    KVDoc kvDoc = new KVDoc();
    kvDoc.setId("111");
    kvDoc.setKey("test");
    kvDoc.setValue("testValue");

    HttpResponse httpResponse = new HttpResponse();
    httpResponse.setStatusCode(204);
    httpResponse.setMessage("OK");

    Mockito.when(kieRawClient.deleteHttpRequest("/kie/kv/?kvID=" + kvDoc.getId(), null, null))
        .thenReturn(httpResponse);

    KieClient kieClient = new KieClient(kieRawClient);
    kieClient.deleteKeyValue(kvDoc);
  }
}
