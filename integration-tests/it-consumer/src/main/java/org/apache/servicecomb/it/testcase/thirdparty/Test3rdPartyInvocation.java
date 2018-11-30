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
import javax.xml.ws.Holder;

import org.apache.servicecomb.it.extend.engine.GateRestTemplate;
import org.apache.servicecomb.provider.pojo.Invoker;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.provider.springmvc.reference.async.CseAsyncRestTemplate;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RestTemplate;

public class Test3rdPartyInvocation {

  private static final String THIRD_PARTY_MICROSERVICE_NAME = "3rdPartyDataTypeJaxrs";

  private static final String ASYNC_THIRD_PARTY_MICROSERVICE_NAME = THIRD_PARTY_MICROSERVICE_NAME + "Async";

  // to get endpoint from urlPrefix
  static GateRestTemplate rt = GateRestTemplate.createEdgeRestTemplate("dataTypeJaxrs");

  private static DataTypeJaxrsSchemaIntf dataTypeJaxrsSchema;

  private static DataTypeJaxrsSchemaAsyncIntf dataTypeJaxrsSchemaAsync;

  @BeforeClass
  public static void beforeClass() {
    String urlPrefix = rt.getUrlPrefix();
    int beginIndex = urlPrefix.indexOf("//");
    int endIndex = urlPrefix.indexOf("/", beginIndex + 3);
    String endpoint = "rest:" + urlPrefix.substring(beginIndex, endIndex);
    RegistryUtils.getServiceRegistry()
        .registerMicroserviceMappingByEndpoints(
            THIRD_PARTY_MICROSERVICE_NAME, "1.2.1",
            Collections.singletonList(endpoint),
            DataTypeJaxrsSchemaIntf.class);

    MicroserviceInstance instance = new MicroserviceInstance();
    instance.setEndpoints(Collections.singletonList(endpoint));
    RegistryUtils.getServiceRegistry()
        .registerMicroserviceMapping(
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
    Assert.assertEquals(1, dataTypeJaxrsSchema.intPath(1));
    Assert.assertEquals(2, dataTypeJaxrsSchema.intQuery(2));
    Assert.assertEquals(3, dataTypeJaxrsSchema.intHeader(3));
    Assert.assertEquals(4, dataTypeJaxrsSchema.intCookie(4));
    Assert.assertEquals(5, dataTypeJaxrsSchema.intForm(5));
    Assert.assertEquals(6, dataTypeJaxrsSchema.intBody(6));
    Assert.assertEquals(7, dataTypeJaxrsSchema.intAdd(3, 4));
    Assert.assertEquals("abc", dataTypeJaxrsSchema.stringPath("abc"));
    Assert.assertEquals("def", dataTypeJaxrsSchema.stringQuery("def"));
    Assert.assertEquals("ghi", dataTypeJaxrsSchema.stringHeader("ghi"));
    Assert.assertEquals("jkl", dataTypeJaxrsSchema.stringCookie("jkl"));
    Assert.assertEquals("mn", dataTypeJaxrsSchema.stringForm("mn"));
    Assert.assertEquals("opq", dataTypeJaxrsSchema.stringBody("opq"));
    Assert.assertEquals("uvwxyz", dataTypeJaxrsSchema.stringConcat("uvw", "xyz"));
  }

  @Test
  public void testAsyncInvoke_RPC() throws ExecutionException, InterruptedException {
    Holder<Boolean> addChecked = new Holder<>(false);
    dataTypeJaxrsSchemaAsync.intAdd(5, 6).whenComplete((result, t) -> {
      Assert.assertEquals(11, result.intValue());
      Assert.assertNull(t);
      addChecked.value = true;
    }).get();
    Assert.assertTrue(addChecked.value);

    Holder<Boolean> postStringChecked = new Holder<>(false);
    dataTypeJaxrsSchemaAsync.stringBody("abc").whenComplete((result, t) -> {
      Assert.assertEquals("abc", result);
      Assert.assertNull(t);
      postStringChecked.value = true;
    }).get();
    Assert.assertTrue(postStringChecked.value);

    Holder<Boolean> concatChecked = new Holder<>(false);
    dataTypeJaxrsSchemaAsync.stringConcat("uvw", "xyz").whenComplete((result, t) -> {
      Assert.assertEquals("uvwxyz", result);
      Assert.assertNull(t);
      concatChecked.value = true;
    }).get();
    Assert.assertTrue(concatChecked.value);
  }

  @Test
  public void testSyncInvoke_RestTemplate() {
    RestTemplate restTemplate = RestTemplateBuilder.create();
    ResponseEntity<Integer> responseEntity = restTemplate
        .getForEntity(
            "cse://" + THIRD_PARTY_MICROSERVICE_NAME + "/rest/it-producer/v1/dataTypeJaxrs/intAdd?num1=11&num2=22",
            int.class);
    Assert.assertEquals(200, responseEntity.getStatusCodeValue());
    Assert.assertEquals(33, responseEntity.getBody().intValue());

    ResponseEntity<String> stringBodyResponse = restTemplate
        .exchange("cse://" + THIRD_PARTY_MICROSERVICE_NAME + "/rest/it-producer/v1/dataTypeJaxrs/stringBody",
            HttpMethod.POST,
            new HttpEntity<>("abc"), String.class);
    Assert.assertEquals(200, stringBodyResponse.getStatusCodeValue());
    Assert.assertEquals("abc", stringBodyResponse.getBody());
  }

  @Test
  public void testAsyncInvoke_RestTemplate() throws ExecutionException, InterruptedException {
    CseAsyncRestTemplate cseAsyncRestTemplate = new CseAsyncRestTemplate();
    ListenableFuture<ResponseEntity<Integer>> responseFuture = cseAsyncRestTemplate
        .getForEntity(
            "cse://" + ASYNC_THIRD_PARTY_MICROSERVICE_NAME
                + "/rest/it-producer/v1/dataTypeJaxrs/intAdd?num1=11&num2=22",
            Integer.class);
    ResponseEntity<Integer> responseEntity = responseFuture.get();
    Assert.assertEquals(200, responseEntity.getStatusCodeValue());
    Assert.assertEquals(33, responseEntity.getBody().intValue());

    ListenableFuture<ResponseEntity<String>> stringBodyFuture = cseAsyncRestTemplate
        .exchange("cse://" + ASYNC_THIRD_PARTY_MICROSERVICE_NAME + "/rest/it-producer/v1/dataTypeJaxrs/stringBody",
            HttpMethod.POST,
            new HttpEntity<>("abc"), String.class);
    ResponseEntity<String> stringBodyResponse = stringBodyFuture.get();
    Assert.assertEquals(200, stringBodyResponse.getStatusCodeValue());
    Assert.assertEquals("abc", stringBodyResponse.getBody());
  }

  @Path("/rest/it-producer/v1/dataTypeJaxrs")
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
  }

  @Path("/rest/it-producer/v1/dataTypeJaxrs")
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
