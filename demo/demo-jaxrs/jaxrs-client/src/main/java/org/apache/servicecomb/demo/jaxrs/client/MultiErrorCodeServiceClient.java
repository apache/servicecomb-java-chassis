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
package org.apache.servicecomb.demo.jaxrs.client;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.demo.DemoConst;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.multiErrorCode.MultiRequest;
import org.apache.servicecomb.demo.multiErrorCode.MultiResponse200;
import org.apache.servicecomb.demo.multiErrorCode.MultiResponse400;
import org.apache.servicecomb.demo.multiErrorCode.MultiResponse500;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class MultiErrorCodeServiceClient {
  private static final String SERVER = "cse://jaxrs";

  private static String serverDirectURL;

  private static RestTemplate template = RestTemplateBuilder.create();

  public static void runTest() {
    for (String transport : DemoConst.transports) {
      CseContext.getInstance().getConsumerProviderManager().setTransport("jaxrs", transport);

      testErrorCode();
      testErrorCodeWithHeader();
      testErrorCodeWithHeaderJAXRS();
      testErrorCodeWithHeaderJAXRSUsingRowType();
      testNoClientErrorCode();
    }

    prepareServerDirectURL();
    testErrorCodeWrongType();
  }

  private static void prepareServerDirectURL() {
    Microservice microservice = RegistryUtils.getMicroservice();
    MicroserviceInstance microserviceInstance = (MicroserviceInstance) RegistryUtils.getServiceRegistry()
        .getAppManager()
        .getOrCreateMicroserviceVersionRule(microservice.getAppId(), "jaxrs", DefinitionConst.VERSION_RULE_ALL)
        .getVersionedCache()
        .mapData()
        .values()
        .stream()
        .findFirst()
        .get();
    URIEndpointObject edgeAddress = new URIEndpointObject(microserviceInstance.getEndpoints().get(0));
    serverDirectURL = String.format("http://%s:%d/", edgeAddress.getHostOrIp(), edgeAddress.getPort());
  }

  private static void testErrorCodeWrongType() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String body = "{\"message\":\"hello\",\"code\":\"wrongType\"";
    HttpEntity<String> entity = new HttpEntity<>(body, headers);
    ResponseEntity<MultiResponse200> result;
    try {
      template
          .postForEntity(serverDirectURL + "/MultiErrorCodeService/errorCode", entity, MultiResponse200.class);
    } catch (HttpClientErrorException e) {
      TestMgr.check(e.getStatusCode(), 400);
      TestMgr.check(e.getMessage(), "400 Bad Request");
    }

    entity = new HttpEntity<>(null, headers);
    result = template
        .postForEntity(serverDirectURL + "/MultiErrorCodeService/errorCode", entity, MultiResponse200.class);
    TestMgr.check(result.getStatusCodeValue(), 590);

    body = "{\"message\":\"hello\",\"code\":\"200\"}";
    entity = new HttpEntity<>(body, headers);
    result = template
        .postForEntity(serverDirectURL + "/MultiErrorCodeService/errorCode", entity, MultiResponse200.class);
    TestMgr.check(result.getStatusCode(), 200);
    TestMgr.check(result.getBody().getMessage(), "success result");
  }

  private static void testErrorCode() {
    MultiRequest request = new MultiRequest();

    request.setCode(200);
    ResponseEntity<MultiResponse200> result = template
        .postForEntity(SERVER + "/MultiErrorCodeService/errorCode", request, MultiResponse200.class);
    TestMgr.check(result.getStatusCode(), 200);
    TestMgr.check(result.getBody().getMessage(), "success result");

    request.setCode(400);
    MultiResponse400 t400 = null;
    try {
      template.postForEntity(SERVER + "/MultiErrorCodeService/errorCode", request, MultiResponse400.class);
    } catch (InvocationException e) {
      t400 = (MultiResponse400) e.getErrorData();
    }
    TestMgr.check(t400.getCode(), 400);
    TestMgr.check(t400.getMessage(), "bad request");

    request.setCode(500);
    MultiResponse500 t500 = null;
    try {
      template.postForEntity(SERVER + "/MultiErrorCodeService/errorCode", request, MultiResponse400.class);
    } catch (InvocationException e) {
      t500 = (MultiResponse500) e.getErrorData();
    }
    TestMgr.check(t500.getCode(), 500);
    TestMgr.check(t500.getMessage(), "internal error");
  }

  private static void testErrorCodeWithHeader() {
    MultiRequest request = new MultiRequest();

    request.setCode(200);
    ResponseEntity<MultiResponse200> result = template
        .postForEntity(SERVER + "/MultiErrorCodeService/errorCodeWithHeader", request, MultiResponse200.class);
    TestMgr.check(result.getStatusCode(), 200);
    TestMgr.check(result.getBody().getMessage(), "success result");
    TestMgr.check(result.getBody().getCode(), 200);
    TestMgr.check(result.getHeaders().getFirst("x-code"), 200);

    request.setCode(400);
    MultiResponse400 t400 = null;
    try {
      template.postForEntity(SERVER + "/MultiErrorCodeService/errorCodeWithHeader", request, MultiResponse400.class);
    } catch (InvocationException e) {
      t400 = (MultiResponse400) e.getErrorData();
      TestMgr.check(e.getStatus().getStatusCode(), Status.BAD_REQUEST.getStatusCode());
    }
    TestMgr.check(t400.getCode(), 400);
    TestMgr.check(t400.getMessage(), "bad request");

    request.setCode(500);
    MultiResponse500 t500 = null;
    try {
      template.postForEntity(SERVER + "/MultiErrorCodeService/errorCodeWithHeader", request, MultiResponse400.class);
    } catch (InvocationException e) {
      t500 = (MultiResponse500) e.getErrorData();
      TestMgr.check(e.getStatus().getStatusCode(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
    TestMgr.check(t500.getCode(), 500);
    TestMgr.check(t500.getMessage(), "internal error");
  }

  private static void testErrorCodeWithHeaderJAXRS() {
    MultiRequest request = new MultiRequest();

    request.setCode(200);
    request.setMessage("success result");
    ResponseEntity<MultiResponse200> result = template
        .postForEntity(SERVER + "/MultiErrorCodeService/errorCodeWithHeaderJAXRS", request, MultiResponse200.class);
    TestMgr.check(result.getStatusCode(), 200);
    TestMgr.check(result.getBody().getMessage(), "success result");
    TestMgr.check(result.getBody().getCode(), 200);
    TestMgr.check(result.getHeaders().getFirst("x-code"), 200);

    request.setCode(400);
    request.setMessage("bad request");
    MultiResponse400 t400 = null;
    try {
      template
          .postForEntity(SERVER + "/MultiErrorCodeService/errorCodeWithHeaderJAXRS", request, MultiResponse400.class);
    } catch (InvocationException e) {
      t400 = (MultiResponse400) e.getErrorData();
      TestMgr.check(e.getStatus().getStatusCode(), Status.BAD_REQUEST.getStatusCode());
    }
    TestMgr.check(t400.getCode(), 400);
    TestMgr.check(t400.getMessage(), "bad request");

    request.setCode(500);
    request.setMessage("internal error");
    MultiResponse500 t500 = null;
    try {
      template
          .postForEntity(SERVER + "/MultiErrorCodeService/errorCodeWithHeaderJAXRS", request, MultiResponse400.class);
    } catch (InvocationException e) {
      t500 = (MultiResponse500) e.getErrorData();
      TestMgr.check(e.getStatus().getStatusCode(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
    TestMgr.check(t500.getCode(), 500);
    TestMgr.check(t500.getMessage(), "internal error");
  }

  private static void testErrorCodeWithHeaderJAXRSUsingRowType() {
    JsonObject requestJson = new JsonObject();
    requestJson.put("code", 200);
    requestJson.put("message", "test message");

    ResponseEntity<MultiResponse200> result = template
        .postForEntity(SERVER + "/MultiErrorCodeService/errorCodeWithHeaderJAXRS", requestJson, MultiResponse200.class);
    TestMgr.check(result.getStatusCode(), 200);
    TestMgr.check(result.getBody().getMessage(), "test message");
    TestMgr.check(result.getBody().getCode(), 200);
    TestMgr.check(result.getHeaders().getFirst("x-code"), 200);

    MultiRequest request = new MultiRequest();
    request.setCode(200);
    request.setMessage("test message");
    String stringRequest = Json.encode(request);
    // wrap request to JsonObject
    result = template
        .postForEntity(SERVER + "/MultiErrorCodeService/errorCodeWithHeaderJAXRS", new JsonObject(stringRequest),
            MultiResponse200.class);
    TestMgr.check(result.getStatusCode(), 200);
    TestMgr.check(result.getBody().getMessage(), "test message");
    TestMgr.check(result.getBody().getCode(), 200);
    TestMgr.check(result.getHeaders().getFirst("x-code"), 200);

    // using string
    result = template
        .postForEntity(SERVER + "/MultiErrorCodeService/errorCodeWithHeaderJAXRS", stringRequest,
            MultiResponse200.class);
    TestMgr.check(result.getStatusCode(), 200);
    TestMgr.check(result.getBody().getMessage(), "test message");
    TestMgr.check(result.getBody().getCode(), 200);
    TestMgr.check(result.getHeaders().getFirst("x-code"), 200);
  }

  private static void testNoClientErrorCode() {
    JsonObject requestJson = new JsonObject();
    requestJson.put("code", 200);
    requestJson.put("message", "test message");

    @SuppressWarnings("rawtypes")
    ResponseEntity<List> listResult = template
        .postForEntity(SERVER + "/MultiErrorCodeService/noClientErrorCode", requestJson, List.class);
    TestMgr.check(listResult.getStatusCode(), 200);
    Map<?, ?> mapResult =
        RestObjectMapperFactory.getRestObjectMapper().convertValue(listResult.getBody().get(0), Map.class);
    TestMgr.check(mapResult.get("message"), "test message");
    TestMgr.check(mapResult.get("code"), 200);
    TestMgr.check(mapResult.get("t200"), 200);

    try {
      requestJson.put("code", 400);
      template
          .postForEntity(SERVER + "/MultiErrorCodeService/noClientErrorCode", requestJson, Object.class);
    } catch (InvocationException e) {
      TestMgr.check(e.getStatusCode(), 400);
      mapResult = RestObjectMapperFactory.getRestObjectMapper().convertValue(e.getErrorData(), Map.class);
      TestMgr.check(mapResult.get("message"), "test message");
      TestMgr.check(mapResult.get("code"), 400);
      TestMgr.check(mapResult.get("t400"), 400);
    }
  }
}
