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
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import javax.xml.ws.Holder;

import org.apache.servicecomb.common.rest.RestEngineSchemaListener;
import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfigUtils;
import org.apache.servicecomb.core.unittest.UnitTestMeta;
import org.apache.servicecomb.provider.springmvc.reference.CseClientHttpResponse;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.registry.ServiceRegistryFactory;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class CseAsyncClientHttpRequestTest {

  @Before
  public void setup() {
    ReferenceConfigUtils.setReady(true);
  }

  @After
  public void teardown() {
    ReferenceConfigUtils.setReady(false);
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
  public void testNotReady() {
    String exceptionMessage = "System is not ready for remote calls. "
        + "When beans are making remote calls in initialization, it's better to "
        + "implement " + BootListener.class.getName() + " and do it after EventType.AFTER_REGISTRY.";
    ReferenceConfigUtils.setReady(false);
    CseAsyncClientHttpRequest clientHttpRequest = new CseAsyncClientHttpRequest(URI.create("cse://app:test/"),
        HttpMethod.POST);
    try {
      clientHttpRequest.executeAsync();
    } catch (IllegalStateException e) {
      Assert.assertEquals(exceptionMessage, e.getMessage());
    }
  }

  @Test
  public void testNormal() {
    ServiceRegistry serviceRegistry = ServiceRegistryFactory.createLocal();
    serviceRegistry.init();
    RegistryUtils.setServiceRegistry(serviceRegistry);
    UnitTestMeta meta = new UnitTestMeta();

    CseContext.getInstance()
        .getSchemaListenerManager()
        .setSchemaListenerList(Arrays.asList(new RestEngineSchemaListener()));

    SchemaMeta schemaMeta = meta.getOrCreateSchemaMeta(CseAsyncClientHttpRequestTest.CseAsyncClientHttpRequestTestSchema.class);
    CseContext.getInstance().getSchemaListenerManager().notifySchemaListener(schemaMeta);

    Holder<Invocation> holder = new Holder<>();
    CseAsyncClientHttpRequest client =
        new CseAsyncClientHttpRequest(URI.create(
            "cse://app:test/" + CseAsyncClientHttpRequestTest.CseAsyncClientHttpRequestTestSchema.class.getSimpleName() + "/testbytes"),
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
    ServiceRegistry serviceRegistry = ServiceRegistryFactory.createLocal();
    serviceRegistry.init();
    RegistryUtils.setServiceRegistry(serviceRegistry);
    UnitTestMeta meta = new UnitTestMeta();
    CseContext.getInstance()
        .getSchemaListenerManager()
        .setSchemaListenerList(Arrays.asList(new RestEngineSchemaListener()));
    SchemaMeta schemaMeta = meta.getOrCreateSchemaMeta(CseAsyncClientHttpRequestTest.CseAsyncClientHttpRequestTestSchema.class);
    CseContext.getInstance().getSchemaListenerManager().notifySchemaListener(schemaMeta);

    Throwable error = new Error("failed");
    Response response = Response.createConsumerFail(error);

    CseAsyncClientHttpRequest client =
        new CseAsyncClientHttpRequest(URI.create(
            "cse://app:test/" + CseAsyncClientHttpRequestTest.CseAsyncClientHttpRequestTestSchema.class.getSimpleName() + "/testbytes"),
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