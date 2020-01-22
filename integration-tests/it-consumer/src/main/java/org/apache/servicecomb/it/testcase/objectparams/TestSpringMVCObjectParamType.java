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

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.it.Consumers;
import org.apache.servicecomb.it.schema.objectparams.Color;
import org.apache.servicecomb.it.schema.objectparams.FlattenObjectRequest;
import org.apache.servicecomb.it.schema.objectparams.FlattenObjectResponse;
import org.apache.servicecomb.it.schema.objectparams.GenericObjectParam;
import org.apache.servicecomb.it.schema.objectparams.InnerRecursiveObjectParam;
import org.apache.servicecomb.it.schema.objectparams.MultiLayerObjectParam;
import org.apache.servicecomb.it.schema.objectparams.MultiLayerObjectParam2;
import org.apache.servicecomb.it.schema.objectparams.ObjectParamTypeSchema;
import org.apache.servicecomb.it.schema.objectparams.RecursiveObjectParam;
import org.apache.servicecomb.it.schema.objectparams.TestNullFieldAndDefaultValueParam;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import io.vertx.core.json.Json;

public class TestSpringMVCObjectParamType {
  interface SpringMVCObjectParamTypeSchema extends ObjectParamTypeSchema {
    TestNullFieldAndDefaultValueParam testNullFieldAndDefaultValue(Object request);

    FlattenObjectRequest testQueryObjectParam(byte anByte, short anShort, int anInt, long anLong, float anFloat,
        double anDouble, boolean anBoolean, char anChar, Byte anWrappedByte, Short anWrappedShort,
        Integer anWrappedInteger,
        Long anWrappedLong, Float anWrappedFloat, Double anWrappedDouble, Boolean anWrappedBoolean,
        Character anWrappedCharacter, String string, Color color);
  }

  static Consumers<SpringMVCObjectParamTypeSchema> consumers =
      new Consumers<>("SpringMVCObjectParamTypeSchema", SpringMVCObjectParamTypeSchema.class);

  @Test
  public void testFlattenObjectParam_rpc() {
    FlattenObjectRequest request = createFlattenObjectRequest();
    FlattenObjectResponse response = consumers.getIntf().testFlattenObjectParam(request);
    Assert.assertEquals(Json.encode(request), Json.encode(response));

    request = new FlattenObjectRequest();
    response = consumers.getIntf().testFlattenObjectParam(request);
    Assert.assertEquals(Json.encode(request), Json.encode(response));
  }

  @Test
  public void testFlattenObjectParam_rt() {
    FlattenObjectRequest request = createFlattenObjectRequest();
    FlattenObjectResponse response = consumers.getSCBRestTemplate()
        .postForObject("/testFlattenObjectParam", request, FlattenObjectResponse.class);
    Assert.assertEquals(Json.encode(request), Json.encode(response));

    request = new FlattenObjectRequest();
    response = consumers.getSCBRestTemplate()
        .postForObject("/testFlattenObjectParam", request, FlattenObjectResponse.class);
    Assert.assertEquals(Json.encode(request), Json.encode(response));
  }

  @Test
  public void testFlattenObjectParam_edge() {
    FlattenObjectRequest request = createFlattenObjectRequest();
    FlattenObjectResponse response = consumers.getEdgeRestTemplate()
        .postForObject("/testFlattenObjectParam", request, FlattenObjectResponse.class);
    Assert.assertEquals(Json.encode(request), Json.encode(response));

    request = new FlattenObjectRequest();
    ResponseEntity<FlattenObjectResponse> responseEntity = consumers.getEdgeRestTemplate()
        .postForEntity("/testFlattenObjectParam", request, FlattenObjectResponse.class);
    Assert.assertEquals(Json.encode(request), Json.encode(responseEntity.getBody()));
    Assert.assertEquals(FlattenObjectResponse.class, responseEntity.getBody().getClass());
    Assert.assertEquals(200, responseEntity.getStatusCodeValue());
  }

  @Test
  public void testMultiLayerObjectParam_rpc() {
    MultiLayerObjectParam request = new MultiLayerObjectParam("sss-1", new Date(),
        new MultiLayerObjectParam2("sss-2", 12.12, createFlattenObjectRequest()));
    MultiLayerObjectParam response = consumers.getIntf().testMultiLayerObjectParam(request);
    Assert.assertEquals(request, response);
//  Highway will not give null return value
    response = consumers.getIntf().testMultiLayerObjectParam(null);
    Assert.assertTrue(response == null || response.getString() == null);
  }

  @Test
  public void testMultiLayerObjectParam_rt() {
    MultiLayerObjectParam request = new MultiLayerObjectParam("sss-1", new Date(),
        new MultiLayerObjectParam2("sss-2", 12.12, createFlattenObjectRequest()));
    ResponseEntity<MultiLayerObjectParam> responseEntity = consumers.getSCBRestTemplate()
        .exchange("/testMultiLayerObjectParam", HttpMethod.PUT,
            new HttpEntity<>(request), MultiLayerObjectParam.class);
    Assert.assertEquals(request, responseEntity.getBody());
    Assert.assertEquals(200, responseEntity.getStatusCodeValue());

    responseEntity = consumers.getSCBRestTemplate()
        .exchange("/testMultiLayerObjectParam", HttpMethod.PUT,
            new HttpEntity<>(null), MultiLayerObjectParam.class);
    //  Highway will not give null return value
    Assert.assertTrue(responseEntity.getBody() == null || responseEntity.getBody().getString() == null);
    Assert.assertEquals(200, responseEntity.getStatusCodeValue());
  }

  @Test
  public void testMultiLayerObjectParam_edge() {
    MultiLayerObjectParam request = new MultiLayerObjectParam("sss-1", new Date(),
        new MultiLayerObjectParam2("sss-2", 12.12, createFlattenObjectRequest()));
    ResponseEntity<MultiLayerObjectParam> responseEntity = consumers.getEdgeRestTemplate()
        .exchange("/testMultiLayerObjectParam", HttpMethod.PUT,
            new HttpEntity<>(request), MultiLayerObjectParam.class);
    Assert.assertEquals(request, responseEntity.getBody());
    Assert.assertEquals(200, responseEntity.getStatusCodeValue());

    responseEntity = consumers.getEdgeRestTemplate()
        .exchange("/testMultiLayerObjectParam", HttpMethod.PUT,
            new HttpEntity<>(null), MultiLayerObjectParam.class);
    // Highway will not return null
    Assert.assertTrue(responseEntity.getBody() == null || responseEntity.getBody().getString() == null);
    Assert.assertEquals(200, responseEntity.getStatusCodeValue());
  }

  @Test
  public void testRecursiveObjectParam_rpc() {
    RecursiveObjectParam request = createRecursiveObjectParam();
    RecursiveObjectParam response = consumers.getIntf().testRecursiveObjectParam(request);
    Assert.assertEquals(request, response);
  }

  @Test
  public void testRecursiveObjectParam_rt() {
    RecursiveObjectParam request = createRecursiveObjectParam();
    ResponseEntity<RecursiveObjectParam> responseEntity = consumers.getSCBRestTemplate()
        .postForEntity("/testRecursiveObjectParam", request, RecursiveObjectParam.class);
    Assert.assertEquals(request, responseEntity.getBody());
    Assert.assertEquals(200, responseEntity.getStatusCodeValue());
  }

  @Test
  public void testRecursiveObjectParam_edge() {
    RecursiveObjectParam request = createRecursiveObjectParam();
    ResponseEntity<RecursiveObjectParam> responseEntity = consumers.getEdgeRestTemplate()
        .postForEntity("/testRecursiveObjectParam", request, RecursiveObjectParam.class);
    Assert.assertEquals(request, responseEntity.getBody());
    Assert.assertEquals(200, responseEntity.getStatusCodeValue());
  }

  @Test
  public void testListObjectParam_rpc() {
    List<GenericObjectParam<List<RecursiveObjectParam>>> request = Arrays.asList(
        new GenericObjectParam<>("s1", 1,
            Arrays.asList(
                createRecursiveObjectParam(),
                createRecursiveObjectParam()
            )),
        new GenericObjectParam<>("s2", 2, null)
    );
    List<GenericObjectParam<List<RecursiveObjectParam>>> response = consumers.getIntf()
        .testListObjectParam(request);
    Assert.assertEquals(request, response);
  }

  @Test
  public void testListObjectParam_rt() {
    List<GenericObjectParam<List<RecursiveObjectParam>>> request = Arrays.asList(
        new GenericObjectParam<>("s1", 1,
            Arrays.asList(
                createRecursiveObjectParam(),
                createRecursiveObjectParam()
            )),
        new GenericObjectParam<>("s2", 2, null)
    );
    @SuppressWarnings("unchecked")
    List<GenericObjectParam<List<RecursiveObjectParam>>> response = consumers.getSCBRestTemplate()
        .postForObject("/testListObjectParam", request, List.class);
    Assert.assertEquals(request, response);
  }

  @Test
  public void testListObjectParam_edge() {
    List<GenericObjectParam<List<RecursiveObjectParam>>> request = Arrays.asList(
        new GenericObjectParam<>("s1", 1,
            Arrays.asList(
                createRecursiveObjectParam(),
                createRecursiveObjectParam()
            )),
        new GenericObjectParam<>("s2", 2, null)
    );
    @SuppressWarnings("unchecked")
    List<GenericObjectParam<List<RecursiveObjectParam>>> response = consumers.getEdgeRestTemplate()
        .postForObject("/testListObjectParam", request, List.class);
    Assert.assertEquals(Json.encode(request), Json.encode(response));
  }

  @Test
  public void testMapObjectParam() {
    Map<String, GenericObjectParam<Map<String, GenericObjectParam<RecursiveObjectParam>>>> request = new LinkedHashMap<>();
    LinkedHashMap<String, GenericObjectParam<RecursiveObjectParam>> innerMap = new LinkedHashMap<>();
    innerMap.put("k1-1", new GenericObjectParam<>("k1-1", 11, createRecursiveObjectParam()));
    innerMap.put("k1-2", new GenericObjectParam<>("k1-2", 12, createRecursiveObjectParam()));
    request.put("k1", new GenericObjectParam<>("k1", 1, innerMap));
    innerMap = new LinkedHashMap<>();
    innerMap.put("k2-1", new GenericObjectParam<>("k2-1", 21, createRecursiveObjectParam()));
    request.put("k2", new GenericObjectParam<>("k2", 2, innerMap));
    Map<String, GenericObjectParam<Map<String, GenericObjectParam<RecursiveObjectParam>>>> response =
        consumers.getIntf().testMapObjectParam(request);
    Assert.assertEquals(Json.encode(request), Json.encode(response));

    @SuppressWarnings("unchecked")
    Map<String, GenericObjectParam<Map<String, GenericObjectParam<RecursiveObjectParam>>>> responseRT
        = consumers.getSCBRestTemplate().postForObject("/testMapObjectParam", request, Map.class);
    Assert.assertEquals(Json.encode(request), Json.encode(responseRT));

    @SuppressWarnings("unchecked")
    Map<String, GenericObjectParam<Map<String, GenericObjectParam<RecursiveObjectParam>>>> responseEdge
        = consumers.getEdgeRestTemplate().postForObject("/testMapObjectParam", request, Map.class);
    Assert.assertEquals(Json.encode(request), Json.encode(responseEdge));
  }

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
    TestNullFieldAndDefaultValueParam response = consumers.getEdgeRestTemplate()
        .postForObject("/testNullFieldAndDefaultValue", request, TestNullFieldAndDefaultValueParam.class);
    TestNullFieldAndDefaultValueParam expectedResponse =
        new TestNullFieldAndDefaultValueParam("sss1", 111, null, 0, "defaultS3", 2333);
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

  @Test
  public void testQueryObjectParam() {
    FlattenObjectRequest expected = createFlattenObjectRequest();
    FlattenObjectRequest response = consumers.getIntf().testQueryObjectParam(
        expected.getAnByte(), expected.getAnShort(), expected.getAnInt(), expected.getAnLong(), expected.getAnFloat(),
        expected.getAnDouble(), expected.isAnBoolean(), expected.getAnChar(),
        expected.getAnWrappedByte(), expected.getAnWrappedShort(), expected.getAnWrappedInteger(),
        expected.getAnWrappedLong(), expected.getAnWrappedFloat(), expected.getAnWrappedDouble(),
        expected.getAnWrappedBoolean(), expected.getAnWrappedCharacter(),
        expected.getString(), expected.getColor()
    );
    Assert.assertEquals(expected, response);

    StringBuilder requestUriBuilder = new StringBuilder();
    requestUriBuilder.append("/testQueryObjectParam?")
        .append("anByte=" + expected.getAnByte()).append("&")
        .append("anShort=" + expected.getAnShort()).append("&")
        .append("anInt=" + expected.getAnInt()).append("&")
        .append("anLong=" + expected.getAnLong()).append("&")
        .append("anFloat=" + expected.getAnFloat()).append("&")
        .append("anDouble=" + expected.getAnDouble()).append("&")
        .append("anBoolean=" + expected.isAnBoolean()).append("&")
        .append("anChar=" + expected.getAnChar()).append("&")
        .append("anWrappedByte=" + expected.getAnWrappedByte()).append("&")
        .append("anWrappedShort=" + expected.getAnWrappedShort()).append("&")
        .append("anWrappedInteger=" + expected.getAnWrappedInteger()).append("&")
        .append("anWrappedLong=" + expected.getAnWrappedLong()).append("&")
        .append("anWrappedFloat=" + expected.getAnWrappedFloat()).append("&")
        .append("anWrappedDouble=" + expected.getAnWrappedDouble()).append("&")
        .append("anWrappedBoolean=" + expected.getAnWrappedBoolean()).append("&")
        .append("anWrappedCharacter=" + expected.getAnWrappedCharacter()).append("&")
        .append("string=" + expected.getString()).append("&")
        .append("color=" + expected.getColor());
    ResponseEntity<FlattenObjectRequest> responseEntity = consumers.getSCBRestTemplate()
        .getForEntity(requestUriBuilder.toString(), FlattenObjectRequest.class);
    Assert.assertEquals(expected, responseEntity.getBody());
    Assert.assertEquals(200, responseEntity.getStatusCodeValue());

    responseEntity = consumers.getEdgeRestTemplate()
        .getForEntity(requestUriBuilder.toString(), FlattenObjectRequest.class);
    Assert.assertEquals(expected, responseEntity.getBody());
    Assert.assertEquals(200, responseEntity.getStatusCodeValue());

    expected.setAnWrappedBoolean(null);
    expected.setString(null);
    expected.setAnInt(0);
    expected.setAnWrappedInteger(null);
    response = consumers.getIntf().testQueryObjectParam(
        expected.getAnByte(), expected.getAnShort(), expected.getAnInt(), expected.getAnLong(), expected.getAnFloat(),
        expected.getAnDouble(), expected.isAnBoolean(), expected.getAnChar(),
        expected.getAnWrappedByte(), expected.getAnWrappedShort(), expected.getAnWrappedInteger(),
        expected.getAnWrappedLong(), expected.getAnWrappedFloat(), expected.getAnWrappedDouble(),
        expected.getAnWrappedBoolean(), expected.getAnWrappedCharacter(),
        expected.getString(), expected.getColor()
    );
    Assert.assertEquals(expected, response);
  }

  private FlattenObjectRequest createFlattenObjectRequest() {
    FlattenObjectRequest request = new FlattenObjectRequest();
    request.setAnByte((byte) 8);
    request.setAnShort((short) 7);
    request.setAnInt(6);
    request.setAnLong(5);
    request.setAnFloat(4.4f);
    request.setAnDouble(3.3);
    request.setAnBoolean(true);
    request.setAnChar('c');
    request.setAnWrappedByte((byte) 16);
    request.setAnWrappedShort((short) 15);
    request.setAnWrappedInteger(14);
    request.setAnWrappedLong(13L);
    request.setAnWrappedFloat(12.2f);
    request.setAnWrappedDouble(11.1);
    request.setAnWrappedBoolean(true);
    request.setAnWrappedCharacter('d');
    request.setString("abc");
    request.setColor(Color.BLUE);
    return request;
  }

  private RecursiveObjectParam createRecursiveObjectParam() {
    return new RecursiveObjectParam(new InnerRecursiveObjectParam(1, "sss1",
        new RecursiveObjectParam(new InnerRecursiveObjectParam(2, "sss2", new RecursiveObjectParam()),
            new RecursiveObjectParam(new InnerRecursiveObjectParam(3, "sss3", new RecursiveObjectParam()),
                null,
                4L,
                "sss4",
                Color.GREEN),
            5L,
            "sss5",
            Color.RED
        )),
        new RecursiveObjectParam(new InnerRecursiveObjectParam(6, "sss6",
            new RecursiveObjectParam(new InnerRecursiveObjectParam(7, "sss7",
                new RecursiveObjectParam(new InnerRecursiveObjectParam(),
                    null, 8, "sss8", Color.BLUE)),
                new RecursiveObjectParam(),
                9L,
                "sss9",
                Color.RED)),
            new RecursiveObjectParam(),
            10,
            "sss10",
            Color.GREEN
        ),
        11,
        "sss11",
        Color.BLUE
    );
  }
}
