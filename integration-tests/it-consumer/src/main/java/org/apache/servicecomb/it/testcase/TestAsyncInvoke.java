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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.it.Consumers;
import org.apache.servicecomb.it.junit.ITJUnitUtils;
import org.apache.servicecomb.it.schema.DefaultJsonValueResponse;
import org.apache.servicecomb.provider.springmvc.reference.async.CseAsyncRestTemplate;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;

public class TestAsyncInvoke {

  private CseAsyncRestTemplate cseAsyncRestTemplate = new CseAsyncRestTemplate();

  interface DataTypeAsyncIntf {
    CompletableFuture<ResponseEntity<String>> responseEntityString();

    CompletableFuture<ResponseEntity<DefaultJsonValueResponse>> responseEntityDataObject();
  }

  private static Consumers<DataTypeAsyncIntf> dataTypeAsyncJaxrs =
      new Consumers<>("dataTypeAsyncJaxrs", DataTypeAsyncIntf.class);

  @Test
  public void responseEntity_string_intf() {
    BiConsumer<ResponseEntity<String>, Throwable> checkLogic = (responseEntity, ex) -> {
      Assertions.assertEquals(203, responseEntity.getStatusCodeValue());
      MatcherAssert.assertThat(responseEntity.getHeaders().get("testH"), Matchers.containsInAnyOrder("testV1", "testV2"));
      Assertions.assertEquals("TestOK", responseEntity.getBody());
      Assertions.assertNull(ex);
    };

    // Async RPC
    CompletableFuture<ResponseEntity<String>> responseEntityCompletableFuture =
        dataTypeAsyncJaxrs.getIntf().responseEntityString();
    check(responseEntityCompletableFuture, checkLogic);

    // RestTemplate
    ResponseEntity<String> result = dataTypeAsyncJaxrs.getSCBRestTemplate()
        .getForEntity("/responseEntityString", String.class);
    checkLogic.accept(result, null);

    // AsyncRestTemplate
    ListenableFuture<ResponseEntity<String>> responseEntityListenableFuture = cseAsyncRestTemplate
        .getForEntity("cse://" + ITJUnitUtils.getProducerName() + "/v1/dataTypeAsyncJaxrs/responseEntityString",
            String.class);
    checkAsyncRt(responseEntityListenableFuture, checkLogic);

    ResponseEntity<String> edgeResponseEntity = dataTypeAsyncJaxrs.getEdgeRestTemplate()
        .getForEntity("/responseEntityString", String.class);
    checkLogic.accept(edgeResponseEntity, null);
  }

  @Test
  public void responseEntity_dataObject_intf() {
    BiConsumer<ResponseEntity<DefaultJsonValueResponse>, Throwable> checkLogic = (responseEntity, ex) -> {
      Assertions.assertEquals(203, responseEntity.getStatusCodeValue());
      MatcherAssert.assertThat(responseEntity.getHeaders().get("testH"), Matchers.containsInAnyOrder("testV1", "testV2"));
      Assertions.assertEquals(DefaultJsonValueResponse.class, responseEntity.getBody().getClass());
      Assertions.assertEquals("TestOK", responseEntity.getBody().getMessage());
      Assertions.assertEquals(2, responseEntity.getBody().getType());
      Assertions.assertNull(ex);
    };

    // Async RPC
    CompletableFuture<ResponseEntity<DefaultJsonValueResponse>> responseEntityCompletableFuture =
        dataTypeAsyncJaxrs.getIntf().responseEntityDataObject();
    check(responseEntityCompletableFuture, checkLogic);

    // RestTemplate
    ResponseEntity<DefaultJsonValueResponse> result =
        dataTypeAsyncJaxrs.getSCBRestTemplate()
            .getForEntity("/responseEntityDataObject", DefaultJsonValueResponse.class);
    checkLogic.accept(result, null);

    // AsyncRestTemplate
    ListenableFuture<ResponseEntity<DefaultJsonValueResponse>> responseEntityListenableFuture = cseAsyncRestTemplate
        .getForEntity("cse://" + ITJUnitUtils.getProducerName() + "/v1/dataTypeAsyncJaxrs/responseEntityDataObject",
            DefaultJsonValueResponse.class);
    checkAsyncRt(responseEntityListenableFuture, checkLogic);

    ResponseEntity<DefaultJsonValueResponse> edgeResponseEntity = dataTypeAsyncJaxrs.getEdgeRestTemplate()
        .getForEntity("/responseEntityDataObject", DefaultJsonValueResponse.class);
    checkLogic.accept(edgeResponseEntity, null);
  }

  /**
   * Wait for response and check
   */
  private <T> void check(CompletableFuture<ResponseEntity<T>> responseEntityCompletableFuture,
      BiConsumer<ResponseEntity<T>, Throwable> checkLogic) {
    CountDownLatch countDownLatch = new CountDownLatch(1);
    Holder<Boolean> responseChecked = new Holder<>();

    responseEntityCompletableFuture.whenComplete((responseEntity, ex) -> {
      checkLogic.accept(responseEntity, ex);
      responseChecked.value = true;
      countDownLatch.countDown();
    });

    try {
      countDownLatch.await(3000, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Assertions.fail("error occurs while waiting for response, " + e.getMessage());
    }

    Assertions.assertTrue(responseChecked.value, "response check unfinished!");
  }

  /**
   * Transfer {@link ListenableFuture} to {@link CompletableFuture}, wait for response and check
   */
  private <T> void checkAsyncRt(ListenableFuture<ResponseEntity<T>> responseEntityListenableFuture,
      BiConsumer<ResponseEntity<T>, Throwable> checkLogic) {
    CompletableFuture<ResponseEntity<T>> entityCompletableFuture = new CompletableFuture<>();
    responseEntityListenableFuture.addCallback(
        entityCompletableFuture::complete,
        entityCompletableFuture::completeExceptionally
    );
    check(entityCompletableFuture, checkLogic);
  }
}
