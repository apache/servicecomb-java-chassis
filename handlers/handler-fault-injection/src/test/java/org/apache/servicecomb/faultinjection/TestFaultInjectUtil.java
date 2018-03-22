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

package org.apache.servicecomb.faultinjection;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.util.ReflectionUtils;

import com.netflix.config.DynamicProperty;

/**
 * Tests the fault inject util functionality.
 */
public class TestFaultInjectUtil {
  private static Method updatePropertyMethod =
      ReflectionUtils.findMethod(DynamicProperty.class, "updateProperty", String.class, Object.class);

  static {
    updatePropertyMethod.setAccessible(true);
  }

  DelayFault delayFault = Mockito.mock(DelayFault.class);

  AbortFault abortFault = Mockito.mock(AbortFault.class);

  public static void updateProperty(String key, Object value) {
    ReflectionUtils.invokeMethod(updatePropertyMethod, null, key, value);
  }

  @Test
  public void testFaultInjectUtil() throws Exception {
    AtomicLong count1 = FaultInjectionUtil.getOperMetTotalReq("test");
    Assert.assertEquals(1, count1.get());
    count1.incrementAndGet();
    AtomicLong count2 = FaultInjectionUtil.getOperMetTotalReq("test");
    Assert.assertEquals(2, count2.get());
    FaultInjectionUtil.setConfigCenterValue("sayHi", new AtomicInteger(123));
    int value = FaultInjectionUtil.getConfigCenterMap().get("sayHi").get();
    Assert.assertEquals(123, value);
  }
}
