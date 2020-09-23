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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.QueryParam;

import org.apache.servicecomb.it.Consumers;
import org.apache.servicecomb.it.schema.objectparams.BeanParamRequest;
import org.apache.servicecomb.it.schema.objectparams.Color;
import org.apache.servicecomb.it.schema.objectparams.FlattenObjectRequest;
import org.apache.servicecomb.it.schema.objectparams.FlattenObjectResponse;
import org.apache.servicecomb.it.schema.objectparams.FluentSetterBeanParamRequest;
import org.apache.servicecomb.it.schema.objectparams.FluentSetterFlattenObjectRequest;
import org.apache.servicecomb.it.schema.objectparams.FluentSetterFlattenObjectResponse;
import org.apache.servicecomb.it.schema.objectparams.GenericObjectParam;
import org.apache.servicecomb.it.schema.objectparams.InnerRecursiveObjectParam;
import org.apache.servicecomb.it.schema.objectparams.MultiLayerObjectParam;
import org.apache.servicecomb.it.schema.objectparams.MultiLayerObjectParam2;
import org.apache.servicecomb.it.schema.objectparams.ObjectParamTypeSchema;
import org.apache.servicecomb.it.schema.objectparams.RecursiveObjectParam;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import io.swagger.annotations.ApiOperation;
import io.vertx.core.json.Json;

public class TestJAXRSObjectParamType {
  interface JAXRSObjectParamTypeSchema extends ObjectParamTypeSchema {
    BeanParamRequest testBeanParamRequest(String header, String path, int query,
        @QueryParam("query_array") String[] queryArray, @QueryParam("query_list") List<String> queryList);

    @ApiOperation(value = "", nickname = "testBeanParamRequest")
    BeanParamRequest testBeanParamRequestAggr(BeanParamRequest request);

    FluentSetterBeanParamRequest testFluentSetterBeanParamRequest(String header, String path, int query);
  }

  static Consumers<JAXRSObjectParamTypeSchema> consumers =
      new Consumers<>("JAXRSObjectParamTypeSchema", JAXRSObjectParamTypeSchema.class);

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
    response = consumers.getEdgeRestTemplate()
        .postForObject("/testFlattenObjectParam", request, FlattenObjectResponse.class);
    Assert.assertEquals(Json.encode(request), Json.encode(response));

    request = new FlattenObjectRequest();
    ResponseEntity<FlattenObjectResponse> responseEntity = consumers.getEdgeRestTemplate()
        .postForEntity("/testFlattenObjectParam", request, FlattenObjectResponse.class);
    Assert.assertEquals(Json.encode(request), Json.encode(responseEntity.getBody()));
    Assert.assertEquals(FlattenObjectResponse.class, responseEntity.getBody().getClass());
    Assert.assertEquals(200, responseEntity.getStatusCodeValue());
    responseEntity = consumers.getEdgeRestTemplate()
        .postForEntity("/testFlattenObjectParam", request, FlattenObjectResponse.class);
    Assert.assertEquals(Json.encode(request), Json.encode(responseEntity.getBody()));
    Assert.assertEquals(FlattenObjectResponse.class, responseEntity.getBody().getClass());
    Assert.assertEquals(200, responseEntity.getStatusCodeValue());
  }

  @Test
  public void testFluentSetterFlattenObjectParam_rpc() {
    FluentSetterFlattenObjectRequest fluentRequest = createFluentSetterFlattenObjectRequest();
    FluentSetterFlattenObjectResponse fluentResponse = consumers.getIntf()
        .testFluentSetterFlattenObjectParam(fluentRequest);
    Assert.assertEquals(Json.encode(fluentRequest), Json.encode(fluentResponse));

    fluentRequest = new FluentSetterFlattenObjectRequest();
    fluentResponse = consumers.getIntf().testFluentSetterFlattenObjectParam(fluentRequest);
    Assert.assertEquals(Json.encode(fluentRequest), Json.encode(fluentResponse));
  }

  @Test
  public void testFluentSetterFlattenObjectParam_rt() {
    FluentSetterFlattenObjectRequest fluentRequest = createFluentSetterFlattenObjectRequest();
    FluentSetterFlattenObjectResponse fluentResponse = consumers.getSCBRestTemplate()
        .postForObject("/testFluentSetterFlattenObjectParam", fluentRequest, FluentSetterFlattenObjectResponse.class);
    Assert.assertEquals(Json.encode(fluentRequest), Json.encode(fluentResponse));

    fluentRequest = new FluentSetterFlattenObjectRequest();
    fluentResponse = consumers.getSCBRestTemplate()
        .postForObject("/testFluentSetterFlattenObjectParam", fluentRequest, FluentSetterFlattenObjectResponse.class);
    Assert.assertEquals(Json.encode(fluentRequest), Json.encode(fluentResponse));
  }

  @Test
  public void testFluentSetterFlattenObjectParam_edge() {
    FluentSetterFlattenObjectRequest fluentRequest = createFluentSetterFlattenObjectRequest();
    FluentSetterFlattenObjectResponse response = consumers.getEdgeRestTemplate()
        .postForObject("/testFluentSetterFlattenObjectParam", fluentRequest, FluentSetterFlattenObjectResponse.class);
    Assert.assertEquals(Json.encode(fluentRequest), Json.encode(response));
    response = consumers.getEdgeRestTemplate()
        .postForObject("/testFluentSetterFlattenObjectParam", fluentRequest, FluentSetterFlattenObjectResponse.class);
    Assert.assertEquals(Json.encode(fluentRequest), Json.encode(response));

    fluentRequest = new FluentSetterFlattenObjectRequest();
    ResponseEntity<FluentSetterFlattenObjectResponse> responseEntity = consumers.getEdgeRestTemplate()
        .postForEntity("/testFluentSetterFlattenObjectParam", fluentRequest, FluentSetterFlattenObjectResponse.class);
    Assert.assertEquals(Json.encode(fluentRequest), Json.encode(responseEntity.getBody()));
    Assert.assertEquals(FluentSetterFlattenObjectResponse.class, responseEntity.getBody().getClass());
    Assert.assertEquals(200, responseEntity.getStatusCodeValue());
    responseEntity = consumers.getEdgeRestTemplate()
        .postForEntity("/testFluentSetterFlattenObjectParam", fluentRequest, FluentSetterFlattenObjectResponse.class);
    Assert.assertEquals(Json.encode(fluentRequest), Json.encode(responseEntity.getBody()));
    Assert.assertEquals(FluentSetterFlattenObjectResponse.class, responseEntity.getBody().getClass());
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
            new HttpEntity<>(request), MultiLayerObjectParam.class);
    Assert.assertEquals(request, responseEntity.getBody());
    Assert.assertEquals(200, responseEntity.getStatusCodeValue());

    responseEntity = consumers.getEdgeRestTemplate()
        .exchange("/testMultiLayerObjectParam", HttpMethod.PUT,
            new HttpEntity<>(null), MultiLayerObjectParam.class);
    // highway will not return null object
    Assert.assertTrue(responseEntity.getBody() == null || responseEntity.getBody().getString() == null);
    Assert.assertEquals(200, responseEntity.getStatusCodeValue());
    responseEntity = consumers.getEdgeRestTemplate()
        .exchange("/testMultiLayerObjectParam", HttpMethod.PUT,
            new HttpEntity<>(null), MultiLayerObjectParam.class);
    // highway will not return null object
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
    responseEntity = consumers.getEdgeRestTemplate()
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
  @SuppressWarnings("unchecked")
  public void testListObjectParam_edge() {
    List<GenericObjectParam<List<RecursiveObjectParam>>> request = Arrays.asList(
        new GenericObjectParam<>("s1", 1,
            Arrays.asList(
                createRecursiveObjectParam(),
                createRecursiveObjectParam()
            )),
        new GenericObjectParam<>("s2", 2, null)
    );
    List<GenericObjectParam<List<RecursiveObjectParam>>> responseRest = consumers.getEdgeRestTemplate()
        .postForObject("/testListObjectParam", request, List.class);
    Assert.assertEquals(Json.encode(request), Json.encode(responseRest));
    List<GenericObjectParam<List<RecursiveObjectParam>>> responseHighway = consumers.getEdgeRestTemplate()
        .postForObject("/testListObjectParam", request, List.class);
    Assert.assertEquals(Json.encode(request), Json.encode(responseHighway));
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
  public void testBeanParamRequest() {
    String[] queryArray = {"a", "b"};
    List<String> queryList = Arrays.asList("c", "d");
    BeanParamRequest expected = new BeanParamRequest("ss2", 123, "ss1", queryArray, queryList);
    String expectedJson = Json.encodePrettily(expected);

    BeanParamRequest response = consumers.getIntf().testBeanParamRequest("ss1", "ss2", 123, queryArray, queryList);
    assertThat(Json.encodePrettily(response)).isEqualTo(expectedJson);

    response = consumers.getIntf().testBeanParamRequestAggr(expected);
    assertThat(Json.encodePrettily(response)).isEqualTo(expectedJson);

    HttpHeaders headers = new HttpHeaders();
    headers.add("header", "ss1");
    String url = "/beanParamRequest/ss2?query=123&query_array=a&query_array=b&query_list=c&query_list=d";
    ResponseEntity<BeanParamRequest> responseEntity = consumers.getSCBRestTemplate()
        .exchange(url, HttpMethod.GET, new HttpEntity<>(headers), BeanParamRequest.class);
    assertThat(Json.encodePrettily(responseEntity.getBody())).isEqualTo(expectedJson);
    Assert.assertEquals(200, responseEntity.getStatusCodeValue());

    responseEntity = consumers.getEdgeRestTemplate()
        .exchange(url, HttpMethod.GET, new HttpEntity<>(headers), BeanParamRequest.class);
    assertThat(Json.encodePrettily(responseEntity.getBody())).isEqualTo(expectedJson);
    Assert.assertEquals(200, responseEntity.getStatusCodeValue());
  }

  @Test
  public void testFluentSetterBeanParamRequest() {
    FluentSetterBeanParamRequest response = consumers.getIntf().testFluentSetterBeanParamRequest("ss1", "ss2", 123);
    FluentSetterBeanParamRequest expected = new FluentSetterBeanParamRequest("ss2", 123, "ss1");
    Assert.assertEquals(expected, response);

    HttpHeaders headers = new HttpHeaders();
    headers.add("header", "ss1");
    ResponseEntity<FluentSetterBeanParamRequest> responseEntity = consumers.getSCBRestTemplate()
        .exchange("/fluentSetterBeanParamRequest/ss2?query=123", HttpMethod.GET, new HttpEntity<>(headers),
            FluentSetterBeanParamRequest.class);
    Assert.assertEquals(expected, responseEntity.getBody());
    Assert.assertEquals(200, responseEntity.getStatusCodeValue());

    responseEntity = consumers.getEdgeRestTemplate()
        .exchange("/fluentSetterBeanParamRequest/ss2?query=123", HttpMethod.GET, new HttpEntity<>(headers),
            FluentSetterBeanParamRequest.class);
    Assert.assertEquals(expected, responseEntity.getBody());
    Assert.assertEquals(200, responseEntity.getStatusCodeValue());
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

  private FluentSetterFlattenObjectRequest createFluentSetterFlattenObjectRequest() {
    FluentSetterFlattenObjectRequest request = new FluentSetterFlattenObjectRequest();
    return request.setAnByte((byte) 8)
        .setAnShort((short) 7)
        .setAnInt(6)
        .setAnLong(5)
        .setAnFloat(4.4f)
        .setAnDouble(3.3)
        .setAnBoolean(true)
        .setAnChar('c')
        .setAnWrappedByte((byte) 16)
        .setAnWrappedShort((short) 15)
        .setAnWrappedInteger(14)
        .setAnWrappedLong(13L)
        .setAnWrappedFloat(12.2f)
        .setAnWrappedDouble(11.1)
        .setAnWrappedBoolean(true)
        .setAnWrappedCharacter('d')
        .setString("abc")
        .setColor(Color.BLUE);
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
