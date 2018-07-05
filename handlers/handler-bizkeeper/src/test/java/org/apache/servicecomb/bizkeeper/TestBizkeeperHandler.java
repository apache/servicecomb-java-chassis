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

package org.apache.servicecomb.bizkeeper;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixObservableCommand;
import com.netflix.hystrix.strategy.HystrixPlugins;

public class TestBizkeeperHandler extends BizkeeperHandler {

  BizkeeperHandler bizkeeperHandler = null;

  private static final String GROUP_NAME = "Group_Name";

  Invocation invocation = null;

  AsyncResponse asyncResp = null;

  public TestBizkeeperHandler() {
    super(GROUP_NAME);
  }

  @Before
  public void setUp() throws Exception {
    bizkeeperHandler = new TestBizkeeperHandler();
    invocation = Mockito.mock(Invocation.class);
    asyncResp = Mockito.mock(AsyncResponse.class);

    FallbackPolicyManager.addPolicy(new ReturnNullFallbackPolicy());
    FallbackPolicyManager.addPolicy(new ThrowExceptionFallbackPolicy());
    FallbackPolicyManager.addPolicy(new FromCacheFallbackPolicy());
  }

  @After
  public void tearDown() throws Exception {
    bizkeeperHandler = null;
    invocation = null;
    asyncResp = null;
  }

  @Test
  public void testHandleWithException() {
    boolean validAssert;
    HystrixPlugins.reset();
    try {
      validAssert = true;
      bizkeeperHandler.handle(invocation, asyncResp);
    } catch (Exception e) {
      validAssert = false;
    }
    Assert.assertFalse(validAssert);
  }

  @Test
  public void testHandle() {
    boolean validAssert;

    try {
      Assert.assertNotNull(bizkeeperHandler);
      Mockito.when(invocation.getMicroserviceName()).thenReturn("test1");
      Mockito.when(invocation.getOperationMeta()).thenReturn(Mockito.mock(OperationMeta.class));
      Mockito.when(invocation.getOperationMeta().getMicroserviceQualifiedName()).thenReturn("test1");
      validAssert = true;

      bizkeeperHandler.handle(invocation, asyncResp);
    } catch (Exception exce) {
      validAssert = false;
    }
    Assert.assertTrue(validAssert);
  }

  @Test
  public void testSetCommonProperties() {
    boolean validAssert;
    try {
      validAssert = true;
      HystrixPlugins.reset();
      HystrixCommandProperties.Setter setter = Mockito.mock(HystrixCommandProperties.Setter.class);
      bizkeeperHandler.setCommonProperties(invocation, setter);
    } catch (Exception e) {
      validAssert = false;
    }
    Assert.assertTrue(validAssert);
  }

  @Override
  protected BizkeeperCommand createBizkeeperCommand(Invocation invocation) {
    HystrixCommandProperties.Setter setter = HystrixCommandProperties.Setter()
        .withRequestCacheEnabled(true)
        .withRequestLogEnabled(false);

    BizkeeperCommand bizkeeperCommand = new ConsumerBizkeeperCommand("groupname", invocation,
        HystrixObservableCommand.Setter
            .withGroupKey(CommandKey.toHystrixCommandGroupKey("groupname", invocation))
            .andCommandKey(CommandKey.toHystrixCommandKey("groupname", invocation))
            .andCommandPropertiesDefaults(setter));
    return bizkeeperCommand;
  }

  @Test
  public void testHandleForceThrowException() throws Exception {
    Mockito.when(invocation.getMicroserviceName()).thenReturn("testHandleForceThrowException");
    Mockito.when(invocation.getOperationMeta()).thenReturn(Mockito.mock(OperationMeta.class));
    Mockito.when(invocation.getOperationMeta().getMicroserviceQualifiedName())
        .thenReturn("testHandleForceThrowException");
    System.setProperty("servicecomb.fallback.Group_Name.testHandleForceThrowException.force", "true");
    System.setProperty("servicecomb.fallbackpolicy.Group_Name.testHandleForceThrowException.policy", "throwexception");
    bizkeeperHandler.handle(invocation, f -> {
      Assert.assertTrue(f.isFailed());
    });
  }

  @Test
  public void testHandleForceReturnnull() throws Exception {
    Mockito.when(invocation.getMicroserviceName()).thenReturn("testHandleForceReturnnull");
    Mockito.when(invocation.getOperationMeta()).thenReturn(Mockito.mock(OperationMeta.class));
    Mockito.when(invocation.getOperationMeta().getMicroserviceQualifiedName())
        .thenReturn("testHandleForceReturnnull");
    System.setProperty("servicecomb.fallback.Group_Name.testHandleForceReturnnull.force", "true");
    System.setProperty("servicecomb.fallbackpolicy.Group_Name.testHandleForceReturnnull.policy", "returnnull");
    bizkeeperHandler.handle(invocation, f -> {
      Assert.assertTrue(f.isSuccessed());
      Assert.assertNull(f.getResult());
    });
  }

  @Test
  public void testHandleInError() throws Exception {
    Mockito.when(invocation.getMicroserviceName()).thenReturn("testHandleInError");
    Mockito.when(invocation.getOperationMeta()).thenReturn(Mockito.mock(OperationMeta.class));
    Mockito.when(invocation.getOperationMeta().getMicroserviceQualifiedName())
        .thenReturn("testHandleInError");
    FallbackPolicy policy = Mockito.mock(FallbackPolicy.class);
    Mockito.when(policy.name()).thenReturn("throwException");
    Mockito.when(policy.getFallbackResponse(Mockito.any(Invocation.class))).thenThrow(new RuntimeException());
    FallbackPolicyManager.addPolicy(policy);
    System.setProperty("servicecomb.fallbackpolicy.groupname.testHandleInError.policy", "throwException");
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) {
        AsyncResponse asyncRsp = invocation.getArgumentAt(0, AsyncResponse.class);
        asyncRsp.fail(InvocationType.CONSUMER, new Exception("testHandleInError"));
        return null;
      }
    }).when(invocation).next(Mockito.any(AsyncResponse.class));
    bizkeeperHandler.handle(invocation, f -> {
      Assert.assertTrue(f.isFailed());
    });
  }

  @Test
  public void testHandlNextException() throws Exception {
    Mockito.when(invocation.getMicroserviceName()).thenReturn("testHandlNextException");
    Mockito.when(invocation.getOperationMeta()).thenReturn(Mockito.mock(OperationMeta.class));
    Mockito.when(invocation.getOperationMeta().getMicroserviceQualifiedName())
        .thenReturn("testHandlNextException");
    Mockito.doThrow(new Exception("testHandlNextException")).when(invocation).next(Mockito.any(AsyncResponse.class));
    bizkeeperHandler.handle(invocation, f -> {
      Assert.assertTrue(f.isFailed());
    });
  }

  @Test
  public void testHandleSuccess() throws Exception {
    Mockito.when(invocation.getMicroserviceName()).thenReturn("testHandleSuccess");
    Mockito.when(invocation.getOperationMeta()).thenReturn(Mockito.mock(OperationMeta.class));
    Mockito.when(invocation.getOperationMeta().getMicroserviceQualifiedName())
        .thenReturn("testHandleSuccess");
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) {
        AsyncResponse asyncRsp = invocation.getArgumentAt(0, AsyncResponse.class);
        asyncRsp.success("");
        return null;
      }
    }).when(invocation).next(Mockito.any(AsyncResponse.class));
    bizkeeperHandler.handle(invocation, f -> {
      Assert.assertTrue(f.isSuccessed());
    });
  }
}
