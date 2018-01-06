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

package io.servicecomb.bizkeeper;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixObservableCommand;
import com.netflix.hystrix.strategy.HystrixPlugins;

import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.swagger.invocation.AsyncResponse;

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
    System.setProperty("cse.fallback.Group_Name.testHandleForceThrowException.force", "true");
    System.setProperty("cse.fallbackpolicy.Group_Name.testHandleForceThrowException.policy", "throwexception");
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
    System.setProperty("cse.fallback.Group_Name.testHandleForceReturnnull.force", "true");
    System.setProperty("cse.fallbackpolicy.Group_Name.testHandleForceReturnnull.policy", "returnnull");
    bizkeeperHandler.handle(invocation, f -> {
      Assert.assertTrue(f.isSuccessed());
      Assert.assertEquals(null, f.getResult());
    });
  }
}
