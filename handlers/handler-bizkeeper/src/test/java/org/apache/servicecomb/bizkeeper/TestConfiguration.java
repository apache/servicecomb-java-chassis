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

public class TestConfiguration {
  @Test
  public void testConfiguration() {

    Assertions.assertNotNull(Configuration.INSTANCE);
    Assertions.assertEquals("returnnull", Configuration.FALLBACKPOLICY_POLICY_RETURN);
    Assertions.assertEquals("throwexception", Configuration.FALLBACKPOLICY_POLICY_THROW);

    Configuration c = Configuration.INSTANCE;
    Invocation invocation = Mockito.mock(Invocation.class);
    String test2 = invocation.getMicroserviceName();

    Mockito.when(invocation.getOperationMeta()).thenReturn(Mockito.mock(OperationMeta.class));
    Mockito.when(invocation.getOperationMeta().getMicroserviceName()).thenReturn("testqualify");

    int res = c.getIsolationTimeoutInMilliseconds("groupname", test2, "testqualify");
    Assertions.assertEquals(30000, res);
    boolean b1 = c.getIsolationTimeoutEnabled("groupname", test2, "testqualify");
    Assertions.assertFalse(b1);
    int res1 = c.getIsolationMaxConcurrentRequests("groupname", test2, "testqualify");
    Assertions.assertEquals(1000, res1);
    boolean b2 = c.isCircuitBreakerEnabled("groupname", test2, "testqualify");
    Assertions.assertTrue(b2);
    String str = c.getFallbackPolicyPolicy("groupname", test2, "testqualify");
    // no need to give default value now
    Assertions.assertNull(str);

    Assertions.assertFalse(c.isCircuitBreakerForceOpen("groupname", test2, "testqualify"));
    Assertions.assertFalse(c.isCircuitBreakerForceClosed("groupname", test2, "testqualify"));
    Assertions.assertEquals(15000, c.getCircuitBreakerSleepWindowInMilliseconds("groupname", test2, "testqualify"));
    Assertions.assertEquals(20, c.getCircuitBreakerRequestVolumeThreshold("groupname", test2, "testqualify"));
    Assertions.assertEquals(50, c.getCircuitBreakerErrorThresholdPercentage("groupname", test2, "testqualify"));
  }
}
