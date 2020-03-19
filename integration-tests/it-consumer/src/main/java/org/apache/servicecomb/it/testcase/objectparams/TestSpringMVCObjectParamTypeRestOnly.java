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

package org.apache.servicecomb.it.testcase.objectparams;

import java.util.LinkedHashMap;

import org.apache.servicecomb.it.Consumers;
import org.apache.servicecomb.it.schema.objectparams.ObjectParamTypeSchema;
import org.apache.servicecomb.it.schema.objectparams.TestNullFieldAndDefaultValueParam;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import io.vertx.core.json.Json;

public class TestSpringMVCObjectParamTypeRestOnly {
  interface SpringMVCObjectRestSchemaRestOnly extends ObjectParamTypeSchema {
    TestNullFieldAndDefaultValueParam testNullFieldAndDefaultValue(Object request);
  }

  static Consumers<SpringMVCObjectRestSchemaRestOnly> consumers =
      new Consumers<>("springMVCObjectRestSchemaRestOnly", SpringMVCObjectRestSchemaRestOnly.class);

  @Test
  public void testNullFieldAndDefaultValue_rpc() {
    LinkedHashMap<Object, Object> request = new LinkedHashMap<>();
    request.put("s1", "sss1");
    request.put("i1", 111);
    TestNullFieldAndDefaultValueParam response =
        consumers.getIntf().testNullFieldAndDefaultValue(request);
    TestNullFieldAndDefaultValueParam expectedResponse =
        new TestNullFieldAndDefaultValueParam("sss1", 111, null, 0, "defaultS3", 2333);
    expectedResponse.setRawRequest(Json.encode(expectedResponse));
    Assert.assertEquals(expectedResponse, response);

    request.put("s2", "sss2");
    request.put("i2", 1234);
    request.put("s3", "sss3");
    request.put("i3", 3333);
    response = consumers.getIntf().testNullFieldAndDefaultValue(request);
    expectedResponse = new TestNullFieldAndDefaultValueParam("sss1", 111, "sss2", 1234, "sss3", 3333);
    expectedResponse.setRawRequest(Json.encode(expectedResponse));
    Assert.assertEquals(expectedResponse, response);
  }

  @Test
  public void testNullFieldAndDefaultValue_rt() {
    LinkedHashMap<Object, Object> request = new LinkedHashMap<>();
    request.put("s1", "sss1");
    request.put("i1", 111);
    TestNullFieldAndDefaultValueParam response = consumers.getSCBRestTemplate()
        .postForObject("/testNullFieldAndDefaultValue", request, TestNullFieldAndDefaultValueParam.class);
    TestNullFieldAndDefaultValueParam expectedResponse =
        new TestNullFieldAndDefaultValueParam("sss1", 111, null, 0, "defaultS3", 2333);
    expectedResponse.setRawRequest(Json.encode(expectedResponse));
    Assert.assertEquals(expectedResponse, response);

    request.put("s2", "sss2");
    request.put("i2", 1234);
    request.put("s3", "sss3");
    request.put("i3", 3333);
    ResponseEntity<TestNullFieldAndDefaultValueParam> responseEntity = consumers.getSCBRestTemplate()
        .postForEntity("/testNullFieldAndDefaultValue", request, TestNullFieldAndDefaultValueParam.class);
    expectedResponse = new TestNullFieldAndDefaultValueParam("sss1", 111, "sss2", 1234, "sss3", 3333);
    expectedResponse.setRawRequest(Json.encode(expectedResponse));
    Assert.assertEquals(expectedResponse, responseEntity.getBody());
    Assert.assertEquals(200, responseEntity.getStatusCodeValue());
  }

  @Test
  public void testNullFieldAndDefaultValue_edge() {
    LinkedHashMap<Object, Object> request = new LinkedHashMap<>();
    request.put("s1", "sss1");
    request.put("i1", 111);
    TestNullFieldAndDefaultValueParam expectedResponse =
        new TestNullFieldAndDefaultValueParam("sss1", 111, null, 0, "defaultS3", 2333);
    TestNullFieldAndDefaultValueParam response = consumers.getEdgeRestTemplate()
        .postForObject("/testNullFieldAndDefaultValue", request, TestNullFieldAndDefaultValueParam.class);
    expectedResponse.setRawRequest(Json.encode(expectedResponse));
    Assert.assertEquals(expectedResponse, response);

    request.put("s2", "sss2");
    request.put("i2", 1234);
    request.put("s3", "sss3");
    request.put("i3", 3333);
    ResponseEntity<TestNullFieldAndDefaultValueParam> responseEntity = consumers.getEdgeRestTemplate()
        .postForEntity("/testNullFieldAndDefaultValue", request, TestNullFieldAndDefaultValueParam.class);
    expectedResponse = new TestNullFieldAndDefaultValueParam("sss1", 111, "sss2", 1234, "sss3", 3333);
    expectedResponse.setRawRequest(Json.encode(expectedResponse));
    Assert.assertEquals(expectedResponse, responseEntity.getBody());
    Assert.assertEquals(200, responseEntity.getStatusCodeValue());
  }
}
