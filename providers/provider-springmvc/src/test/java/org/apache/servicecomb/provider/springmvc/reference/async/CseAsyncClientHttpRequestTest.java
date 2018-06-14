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

package org.apache.servicecomb.provider.springmvc.reference.async;

import java.net.URI;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import javax.xml.ws.Holder;

import org.apache.servicecomb.common.rest.RestEngineSchemaListener;
import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.SCBStatus;
import org.apache.servicecomb.core.unittest.UnitTestMeta;
import org.apache.servicecomb.provider.springmvc.reference.CseClientHttpResponse;
import org.apache.servicecomb.swagger.generator.springmvc.SpringmvcSwaggerGeneratorContext;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class CseAsyncClientHttpRequestTest {
  static UnitTestMeta meta = new UnitTestMeta();

  @BeforeClass
  public static void classSetup() {
    CseContext.getInstance()
        .getSchemaListenerManager()
        .setSchemaListenerList(Collections.singletonList(new RestEngineSchemaListener()));
    meta.registerSchema(new SpringmvcSwaggerGeneratorContext(), CseAsyncClientHttpRequestTestSchema.class);
    SCBEngine.getInstance().setConsumerProviderManager(meta.getConsumerProviderManager());
    SCBEngine.getInstance().setStatus(SCBStatus.UP);
  }

  @RequestMapping(path = "CseAsyncClientHttpRequestTestSchema")
  static class CseAsyncClientHttpRequestTestSchema {
    @RequestMapping(path = "/testbytes", method = RequestMethod.POST)
    public byte[] testbytes(@RequestBody byte[] input) {
      input[0] = (byte) (input[0] + 1);
      return input;
    }
  }

  @Test
  public void testNormal() {
    Holder<Invocation> holder = new Holder<>();
    CseAsyncClientHttpRequest client =
        new CseAsyncClientHttpRequest(URI.create(
            "cse://app:test/" + CseAsyncClientHttpRequestTest.CseAsyncClientHttpRequestTestSchema.class.getSimpleName()
                + "/testbytes"),
            HttpMethod.POST) {
          @Override
          protected CompletableFuture<ClientHttpResponse> doAsyncInvoke(Invocation invocation) {
            CompletableFuture<ClientHttpResponse> completableFuture = new CompletableFuture<>();
            holder.value = invocation;
            completableFuture.complete(new CseClientHttpResponse(Response.ok("result")));
            return completableFuture;
          }
        };
    byte[] body = "abc".getBytes();
    client.setRequestBody(body);
    client.executeAsync();
    Assert.assertArrayEquals(body, holder.value.getSwaggerArgument(0));
  }

  @Test
  public void testFail() {
    Throwable error = new Error("failed");
    Response response = Response.createConsumerFail(error);

    CseAsyncClientHttpRequest client =
        new CseAsyncClientHttpRequest(URI.create(
            "cse://app:test/" + CseAsyncClientHttpRequestTest.CseAsyncClientHttpRequestTestSchema.class.getSimpleName()
                + "/testbytes"),
            HttpMethod.POST) {
          @Override
          protected CompletableFuture<ClientHttpResponse> doAsyncInvoke(Invocation invocation) {
            CompletableFuture<ClientHttpResponse> completableFuture = new CompletableFuture<>();
            completableFuture.complete(new CseClientHttpResponse(response));
            return completableFuture;
          }
        };
    ListenableFuture<ClientHttpResponse> future = client.executeAsync();
    future.addCallback(
        new ListenableFutureCallback<ClientHttpResponse>() {
          @Override
          public void onFailure(Throwable ex) {
            Assert.assertSame(error, ex);
          }

          @Override
          public void onSuccess(ClientHttpResponse result) {
          }
        }
    );
  }
}