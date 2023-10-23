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

import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.multiErrorCode.MultiRequest;
import org.apache.servicecomb.demo.multiErrorCode.MultiResponse200;
import org.apache.servicecomb.demo.multiErrorCode.MultiResponse400;
import org.apache.servicecomb.demo.multiErrorCode.MultiResponse500;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.core.Response.Status;

@Component
public class MultiErrorCodeServiceClient implements CategorizedTestCase {
  private static final String SERVER = "cse://jaxrs";

  private static RestOperations template = RestTemplateBuilder.create();

  @Override
  public void testAllTransport() throws Exception {
    testErrorCode();
    testErrorCodeWithHeader();
    testErrorCodeWithHeaderJAXRS();
    testErrorCodeWithHeaderJAXRSUsingRowType();
    testNoClientErrorCode();
  }

  @Override
  public void testRestTransport() throws Exception {
    testErrorCodeWrongType();
  }

  @Override
  public void testHighwayTransport() throws Exception {

  }


  private static void testErrorCodeWrongType() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String body = "{\"message\":\"hello\",\"code\":\"wrongType\"";
    HttpEntity<String> entity = new HttpEntity<>(body, headers);
    ResponseEntity<MultiResponse200> result;
    try {
      template
          .postForEntity(SERVER + "/MultiErrorCodeService/errorCode", entity, MultiResponse200.class);
      TestMgr.fail("expect failed.");
    } catch (InvocationException e) {
      TestMgr.check(e.getStatusCode(), 400);
    }

    entity = new HttpEntity<>(null, headers);
    try {
      template
          .postForEntity(SERVER + "/MultiErrorCodeService/errorCode", entity, MultiResponse200.class);
      TestMgr.check(590, 200);
    } catch (InvocationException e) {
      TestMgr.check(e.getStatusCode(), 400);
    }

    // wrong type
    body = "{\"message\":\"hello\",\"code\":\"200\"}";
    entity = new HttpEntity<>(body, headers);
    try {
      template
          .postForEntity(SERVER + "/MultiErrorCodeService/errorCode", entity, MultiResponse200.class);
      TestMgr.check(590, 200);
    } catch (InvocationException e) {
      TestMgr.check(e.getStatusCode(), 400);
    }
  }

  private static void testErrorCode() {
    MultiRequest request = new MultiRequest();

    request.setCode(200);
    ResponseEntity<MultiResponse200> result = template
        .postForEntity(SERVER + "/MultiErrorCodeService/errorCode", request, MultiResponse200.class);
    TestMgr.check(result.getStatusCode().value(), 200);
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
    TestMgr.check(result.getStatusCode().value(), 200);
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
    TestMgr.check(result.getStatusCode().value(), 200);
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
    TestMgr.check(result.getStatusCode().value(), 200);
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
    TestMgr.check(result.getStatusCode().value(), 200);
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
    TestMgr.check(listResult.getStatusCode().value(), 200);
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
