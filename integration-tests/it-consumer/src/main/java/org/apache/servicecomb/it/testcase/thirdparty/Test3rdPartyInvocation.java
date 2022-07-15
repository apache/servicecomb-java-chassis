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

package org.apache.servicecomb.it.testcase.thirdparty;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.it.Consumers;
import org.apache.servicecomb.it.extend.engine.ITSCBRestTemplate;
import org.apache.servicecomb.provider.pojo.Invoker;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.provider.springmvc.reference.async.CseAsyncRestTemplate;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RestTemplate;

public class Test3rdPartyInvocation {

  private static final String THIRD_PARTY_MICROSERVICE_NAME = "3rdPartyDataTypeJaxrs";

  private static final String ASYNC_THIRD_PARTY_MICROSERVICE_NAME = THIRD_PARTY_MICROSERVICE_NAME + "Async";

  // to get endpoint from urlPrefix
  private static Consumers<DataTypeJaxrsSchemaIntf> consumersJaxrs =
      new Consumers<>("dataTypePojo", DataTypeJaxrsSchemaIntf.class);

  private static DataTypeJaxrsSchemaIntf dataTypeJaxrsSchema;

  private static DataTypeJaxrsSchemaAsyncIntf dataTypeJaxrsSchemaAsync;

  @BeforeAll
  public static void beforeClass() {
    String endpoint =
        ((ITSCBRestTemplate) consumersJaxrs.getSCBRestTemplate()).getAddress(Const.RESTFUL);
    RegistrationManager.INSTANCE.registerMicroserviceMappingByEndpoints(
        THIRD_PARTY_MICROSERVICE_NAME, "1.2.1",
        Collections.singletonList(endpoint),
        DataTypeJaxrsSchemaIntf.class);

    MicroserviceInstance instance = new MicroserviceInstance();
    instance.setEndpoints(Collections.singletonList(endpoint));
    RegistrationManager.INSTANCE.registerMicroserviceMapping(
        ASYNC_THIRD_PARTY_MICROSERVICE_NAME, "1.1.1",
        Collections.singletonList(instance),
        DataTypeJaxrsSchemaAsyncIntf.class
    );

    dataTypeJaxrsSchema = Invoker.createProxy(
        THIRD_PARTY_MICROSERVICE_NAME, THIRD_PARTY_MICROSERVICE_NAME, DataTypeJaxrsSchemaIntf.class);
    dataTypeJaxrsSchemaAsync = Invoker.createProxy(
        ASYNC_THIRD_PARTY_MICROSERVICE_NAME, ASYNC_THIRD_PARTY_MICROSERVICE_NAME, DataTypeJaxrsSchemaAsyncIntf.class);
  }

  @Test
  public void testSyncInvoke_RPC() {
    Assertions.assertEquals(1, dataTypeJaxrsSchema.intPath(1));
    Assertions.assertEquals(2, dataTypeJaxrsSchema.intQuery(2));
    Assertions.assertEquals(3, dataTypeJaxrsSchema.intHeader(3));
    Assertions.assertEquals(4, dataTypeJaxrsSchema.intCookie(4));
    Assertions.assertEquals(5, dataTypeJaxrsSchema.intForm(5));
    Assertions.assertEquals(6, dataTypeJaxrsSchema.intBody(6));
    Assertions.assertEquals(7, dataTypeJaxrsSchema.intAdd(3, 4));
    Assertions.assertEquals("abc", dataTypeJaxrsSchema.stringPath("abc"));
    Assertions.assertEquals("def", dataTypeJaxrsSchema.stringQuery("def"));
    Assertions.assertEquals("ghi", dataTypeJaxrsSchema.stringHeader("ghi"));
    Assertions.assertEquals("jkl", dataTypeJaxrsSchema.stringCookie("jkl"));
    Assertions.assertEquals("mn", dataTypeJaxrsSchema.stringForm("mn"));
    Assertions.assertEquals("opq", dataTypeJaxrsSchema.stringBody("opq"));
    Assertions.assertEquals("uvwxyz", dataTypeJaxrsSchema.stringConcat("uvw", "xyz"));
  }

  @Test
  public void testExposeServiceCombHeaders() {
    String testParam = "test";
    String testParam2 = "test2";
    List<String> response = dataTypeJaxrsSchema.getRequestHeaders(testParam, testParam2);
    // user defined header, even though start with x-cse, will not be removed
    MatcherAssert.assertThat(response, Matchers.contains("host", "x_cse_test", "x_cse_test2"));

    ArchaiusUtils.setProperty("servicecomb.request.clientRequestHeaderFilterEnabled", "false");
    response = dataTypeJaxrsSchema.getRequestHeaders(testParam, testParam2);
    MatcherAssert.assertThat(response,
        Matchers.contains("host", "x-cse-context", "x-cse-target-microservice", "x_cse_test", "x_cse_test2"));

    ArchaiusUtils.setProperty("servicecomb.request.clientRequestHeaderFilterEnabled", "true");
    ArchaiusUtils.setProperty("servicecomb.request.clientRequestHeaderFilterEnabled.3rdPartyDataTypeJaxrs", "false");
    response = dataTypeJaxrsSchema.getRequestHeaders(testParam, testParam2);
    MatcherAssert.assertThat(response,
        Matchers.contains("host", "x-cse-context", "x-cse-target-microservice", "x_cse_test", "x_cse_test2"));

    ArchaiusUtils.setProperty("servicecomb.request.clientRequestHeaderFilterEnabled.3rdPartyDataTypeJaxrs", "true");
  }

  @Test
  public void testAsyncInvoke_RPC() throws ExecutionException, InterruptedException {
    Holder<Boolean> addChecked = new Holder<>(false);
    dataTypeJaxrsSchemaAsync.intAdd(5, 6).whenComplete((result, t) -> {
      Assertions.assertEquals(11, result.intValue());
      Assertions.assertNull(t);
      addChecked.value = true;
    }).get();
    Assertions.assertTrue(addChecked.value);

    Holder<Boolean> postStringChecked = new Holder<>(false);
    dataTypeJaxrsSchemaAsync.stringBody("abc").whenComplete((result, t) -> {
      Assertions.assertEquals("abc", result);
      Assertions.assertNull(t);
      postStringChecked.value = true;
    }).get();
    Assertions.assertTrue(postStringChecked.value);

    Holder<Boolean> concatChecked = new Holder<>(false);
    dataTypeJaxrsSchemaAsync.stringConcat("uvw", "xyz").whenComplete((result, t) -> {
      Assertions.assertEquals("uvwxyz", result);
      Assertions.assertNull(t);
      concatChecked.value = true;
    }).get();
    Assertions.assertTrue(concatChecked.value);
  }

  @Test
  public void testSyncInvoke_RestTemplate() {
    RestTemplate restTemplate = RestTemplateBuilder.create();
    ResponseEntity<Integer> responseEntity = restTemplate
        .getForEntity(
            "cse://" + THIRD_PARTY_MICROSERVICE_NAME + "/v1/dataTypeJaxrs/intAdd?num1=11&num2=22",
            int.class);
    Assertions.assertEquals(200, responseEntity.getStatusCodeValue());
    Assertions.assertEquals(33, responseEntity.getBody().intValue());

    ResponseEntity<String> stringBodyResponse = restTemplate
        .exchange("cse://" + THIRD_PARTY_MICROSERVICE_NAME + "/v1/dataTypeJaxrs/stringBody",
            HttpMethod.POST,
            new HttpEntity<>("abc"), String.class);
    Assertions.assertEquals(200, stringBodyResponse.getStatusCodeValue());
    Assertions.assertEquals("abc", stringBodyResponse.getBody());
  }

  @Test
  public void testAsyncInvoke_RestTemplate() throws ExecutionException, InterruptedException {
    CseAsyncRestTemplate cseAsyncRestTemplate = new CseAsyncRestTemplate();
    ListenableFuture<ResponseEntity<Integer>> responseFuture = cseAsyncRestTemplate
        .getForEntity(
            "cse://" + ASYNC_THIRD_PARTY_MICROSERVICE_NAME
                + "/v1/dataTypeJaxrs/intAdd?num1=11&num2=22",
            Integer.class);
    ResponseEntity<Integer> responseEntity = responseFuture.get();
    Assertions.assertEquals(200, responseEntity.getStatusCodeValue());
    Assertions.assertEquals(33, responseEntity.getBody().intValue());

    ListenableFuture<ResponseEntity<String>> stringBodyFuture = cseAsyncRestTemplate
        .exchange("cse://" + ASYNC_THIRD_PARTY_MICROSERVICE_NAME + "/v1/dataTypeJaxrs/stringBody",
            HttpMethod.POST,
            new HttpEntity<>("abc"), String.class);
    ResponseEntity<String> stringBodyResponse = stringBodyFuture.get();
    Assertions.assertEquals(200, stringBodyResponse.getStatusCodeValue());
    Assertions.assertEquals("abc", stringBodyResponse.getBody());
  }

  @Path("/v1/dataTypeJaxrs")
  interface DataTypeJaxrsSchemaIntf {
    @Path("intPath/{input}")
    @GET
    int intPath(@PathParam("input") int input);

    @Path("intQuery")
    @GET
    int intQuery(@QueryParam("input") int input);

    @Path("intHeader")
    @GET
    int intHeader(@HeaderParam("input") int input);

    @Path("intCookie")
    @GET
    int intCookie(@CookieParam("input") int input);

    @Path("intForm")
    @POST
    int intForm(@FormParam("input") int input);

    @Path("intBody")
    @POST
    int intBody(int input);

    @Path("intAdd")
    @GET
    int intAdd(@QueryParam("num1") int num1, @QueryParam("num2") int num2);

    //strinnum1
    @Path("stringPath/{input}")
    @GET
    String stringPath(@PathParam("input") String input);

    @Path("stringQuery")
    @GET
    String stringQuery(@QueryParam("input") String input);

    @Path("stringHeader")
    @GET
    String stringHeader(@HeaderParam("input") String input);

    @Path("stringCookie")
    @GET
    String stringCookie(@CookieParam("input") String input);

    @Path("stringForm")
    @POST
    String stringForm(@FormParam("input") String input);

    @Path("stringBody")
    @POST
    String stringBody(String input);

    @Path("stringConcat")
    @GET
    String stringConcat(@QueryParam("str1") String str1, @QueryParam("str2") String str2);

    @Path("requestHeaders")
    @GET
    List<String> getRequestHeaders(@HeaderParam(value = "x_cse_test") String testServiceCombHeader,
        @HeaderParam(value = "x_cse_test2") String testServiceCombHeader2);
  }

  @Path("/v1/dataTypeJaxrs")
  interface DataTypeJaxrsSchemaAsyncIntf {
    @Path("intAdd")
    @GET
    CompletableFuture<Integer> intAdd(@QueryParam("num1") int num1, @QueryParam("num2") int num2);

    @Path("stringBody")
    @POST
    CompletableFuture<String> stringBody(String input);

    @Path("stringConcat")
    @GET
    CompletableFuture<String> stringConcat(@QueryParam("str1") String str1, @QueryParam("str2") String str2);
  }
}
