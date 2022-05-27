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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;

public class TestHystrixPropertiesStrategyExt {

  @Test
  public void testGetCommandPropertiesCacheKey() {

    Assertions.assertNotNull(HystrixPropertiesStrategyExt.getInstance());

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
    Assertions.assertNull(str1);
  }

  @Test
  public void testgetCommandProperties() {
    HystrixCommandKey commandKey = Mockito.mock(HystrixCommandKey.class);
    Mockito.when(commandKey.name())
        .thenReturn("provider.HystrixPropertiesStrategyExtTest.testgetCommandProperties");
    HystrixCommandProperties commandPro = HystrixPropertiesStrategyExt.getInstance()
        .getCommandProperties(commandKey, HystrixCommandProperties.Setter());
    Assertions.assertTrue(commandPro.circuitBreakerEnabled().get());
    Assertions.assertEquals(Integer.valueOf(50), commandPro.circuitBreakerErrorThresholdPercentage().get());
    Assertions.assertFalse(commandPro.circuitBreakerForceClosed().get());
    Assertions.assertFalse(commandPro.circuitBreakerForceOpen().get());
    Assertions.assertEquals(Integer.valueOf(20), commandPro.circuitBreakerRequestVolumeThreshold().get());
    Assertions.assertEquals(Integer.valueOf(15000), commandPro.circuitBreakerSleepWindowInMilliseconds().get());
    Assertions.assertEquals(Integer.valueOf(1000), commandPro.executionIsolationSemaphoreMaxConcurrentRequests().get());
    Assertions.assertTrue(commandPro.executionIsolationThreadInterruptOnTimeout().get());
    Assertions.assertNull(commandPro.executionIsolationThreadPoolKeyOverride().get());
    Assertions.assertEquals(Integer.valueOf(30000), commandPro.executionTimeoutInMilliseconds().get());
    Assertions.assertFalse(commandPro.executionTimeoutEnabled().get());
    Assertions.assertEquals(Integer.valueOf(10), commandPro.fallbackIsolationSemaphoreMaxConcurrentRequests().get());
    Assertions.assertTrue(commandPro.fallbackEnabled().get());
    Assertions.assertEquals(Integer.valueOf(100), commandPro.metricsRollingPercentileBucketSize().get());
    Assertions.assertFalse(commandPro.metricsRollingPercentileEnabled().get());
  }
}
