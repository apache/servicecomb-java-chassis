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

package org.apache.servicecomb.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.provider.consumer.ConsumerProviderManager;
import org.apache.servicecomb.core.provider.consumer.SyncResponseExecutor;
import org.apache.servicecomb.foundation.common.RegisterManager;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class TestConsumer {

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  public void testConsumerProviderManager() {
    ConsumerProviderManager oConsumerProviderManager = new ConsumerProviderManager();
    RegisterManager oRegisterManager = new RegisterManager("cse consumer provider manager");
    oRegisterManager.register("servicecomb.references.cse consumer provider manager",
        "cse consumer provider manager");
    boolean validAssert = true;
    try {
      oConsumerProviderManager.getReferenceConfig("consumer provider manager");
    } catch (Throwable ee) {
      Assert.assertNotEquals(null, ee);
      validAssert = false;
    }
    Assert.assertFalse(validAssert);
  }

  @Test
  public void testReferenceConfig() throws InterruptedException {
    Map<String, String> oMap = new ConcurrentHashMap<>();
    oMap.put("test1", "value1");
    RegisterManager<String, String> oManager = new RegisterManager<>("test");
    oManager.register("test1", "value1");

    SyncResponseExecutor oExecutor = new SyncResponseExecutor();
    oExecutor.execute(new Runnable() {

      @Override
      public void run() {
        oExecutor.setResponse(Response.succResp("success"));
      }
    });
    Assert.assertEquals(true, oExecutor.waitResponse().isSuccessed());
  }

  @Test
  public void testInvocation() {
    OperationMeta oOperationMeta = Mockito.mock(OperationMeta.class);
    SchemaMeta oSchemaMeta = Mockito.mock(SchemaMeta.class);
    AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);
    List<Handler> oHandlerList = new ArrayList<>();

    Mockito.when(oSchemaMeta.getProviderHandlerChain()).thenReturn(oHandlerList);
    Mockito.when(oSchemaMeta.getMicroserviceName()).thenReturn("TMK");
    Mockito.when(oOperationMeta.getSchemaMeta()).thenReturn(oSchemaMeta);
    Endpoint oEndpoint = Mockito.mock(Endpoint.class);
    Transport oTransport = Mockito.mock(Transport.class);
    Mockito.when(oEndpoint.getTransport()).thenReturn(oTransport);
    Mockito.when(oOperationMeta.getOperationId()).thenReturn("TMK");

    Invocation oInvocation = new Invocation(oEndpoint, oOperationMeta, null);
    Assert.assertNotNull(oInvocation.getTransport());
    Assert.assertNotNull(oInvocation.getInvocationType());
    oInvocation.setResponseExecutor(Mockito.mock(Executor.class));
    Assert.assertNotNull(oInvocation.getResponseExecutor());
    Assert.assertNotNull(oInvocation.getSchemaMeta());
    Assert.assertNotNull(oInvocation.getOperationMeta());
    Assert.assertNull(oInvocation.getArgs());
    Assert.assertNotNull(oInvocation.getEndpoint());
    oInvocation.setEndpoint(null);
    Map<String, String> map = oInvocation.getContext();
    Assert.assertNotNull(map);
    String str = oInvocation.getSchemaId();
    Assert.assertEquals(null, str);
    String str1 = oInvocation.getMicroserviceName();
    Assert.assertEquals("TMK", str1);
    Map<String, Object> mapp = oInvocation.getHandlerContext();
    Assert.assertNotNull(mapp);
    Assert.assertEquals(true, oInvocation.getHandlerIndex() >= 0);
    oInvocation.setHandlerIndex(8);
    Assert.assertEquals("TMK", oInvocation.getOperationName());
    Assert.assertEquals("TMK", oInvocation.getMicroserviceName());

    boolean validAssert;

    try {

      validAssert = true;

      oInvocation.next(asyncResp);
    } catch (Exception e) {
      validAssert = false;
    }
    Assert.assertFalse(validAssert);
  }
}
