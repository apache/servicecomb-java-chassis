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

import static org.junit.Assert.assertNotNull;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;

/**
 *
 */
public class TestHystrixPropertiesStrategyExt {

  @Test
  public void testGetCommandPropertiesCacheKey() {

    assertNotNull(HystrixPropertiesStrategyExt.getInstance());

    HystrixPropertiesStrategyExt hps = HystrixPropertiesStrategyExt.getInstance();
    HystrixCommandKey commandKey = Mockito.mock(HystrixCommandKey.class);

    Invocation invocation = Mockito.mock(Invocation.class);
    Mockito.when(invocation.getOperationMeta()).thenReturn(Mockito.mock(OperationMeta.class));
    Mockito.when(invocation.getOperationMeta().getMicroserviceName()).thenReturn("testqualify");

    HystrixCommandProperties.Setter setter = HystrixCommandProperties.Setter()
        .withRequestCacheEnabled(true)
        .withRequestLogEnabled(false)
        .withFallbackIsolationSemaphoreMaxConcurrentRequests(
            Configuration.INSTANCE.getFallbackMaxConcurrentRequests("groupname",
                "testing",
                invocation.getOperationMeta().getMicroserviceQualifiedName()));

    String str1 = hps.getCommandPropertiesCacheKey(commandKey, setter);
    Assert.assertNull(str1);
  }

  @Test
  public void testgetCommandProperties() {
    HystrixCommandKey commandKey = Mockito.mock(HystrixCommandKey.class);
    Mockito.when(commandKey.name())
        .thenReturn("provider.HystrixPropertiesStrategyExtTest.testgetCommandProperties");
    HystrixCommandProperties commandPro = HystrixPropertiesStrategyExt.getInstance()
        .getCommandProperties(commandKey, HystrixCommandProperties.Setter());
    Assert.assertTrue(commandPro.circuitBreakerEnabled().get());
    Assert.assertEquals(Integer.valueOf(50), commandPro.circuitBreakerErrorThresholdPercentage().get());
    Assert.assertFalse(commandPro.circuitBreakerForceClosed().get());
    Assert.assertFalse(commandPro.circuitBreakerForceOpen().get());
    Assert.assertEquals(Integer.valueOf(20), commandPro.circuitBreakerRequestVolumeThreshold().get());
    Assert.assertEquals(Integer.valueOf(15000), commandPro.circuitBreakerSleepWindowInMilliseconds().get());
    Assert.assertEquals(Integer.valueOf(1000), commandPro.executionIsolationSemaphoreMaxConcurrentRequests().get());
    Assert.assertTrue(commandPro.executionIsolationThreadInterruptOnTimeout().get());
    Assert.assertEquals(null, commandPro.executionIsolationThreadPoolKeyOverride().get());
    Assert.assertEquals(Integer.valueOf(30000), commandPro.executionTimeoutInMilliseconds().get());
    Assert.assertFalse(commandPro.executionTimeoutEnabled().get());
    Assert.assertEquals(Integer.valueOf(10), commandPro.fallbackIsolationSemaphoreMaxConcurrentRequests().get());
    Assert.assertTrue(commandPro.fallbackEnabled().get());
    Assert.assertEquals(Integer.valueOf(100), commandPro.metricsRollingPercentileBucketSize().get());
    Assert.assertFalse(commandPro.metricsRollingPercentileEnabled().get());
  }
}
