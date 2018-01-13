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

package org.apache.servicecomb.loadbalance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.serviceregistry.cache.CacheEndpoint;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 *
 */
public class TestCseServer {

  private Transport transport = Mockito.mock(Transport.class);

  private CseServer cs = new CseServer(transport, new CacheEndpoint("abcd", null));

  @Test
  public void testCseServerObj() {
    assertNotNull(cs);
  }

  @Test
  public void testGetEndpoint() {
    cs.getEndpoint();
    assertNotNull(cs.getEndpoint());
  }

  @Test
  public void testEqualsMethod() {
    Assert.assertFalse(cs.equals((Object) "abcd"));

    CseServer other = new CseServer(transport, new CacheEndpoint("1234", null));
    Assert.assertFalse(cs.equals(other));

    other = new CseServer(transport, new CacheEndpoint("abcd", null));
    Assert.assertTrue(cs.equals(other));
  }

  @Test
  public void testToStringMethod() {
    cs.toString();
    assertNotNull(cs.toString());
  }

  @Test
  public void testGetHost() {
    cs.getHost();
    assertNotNull(cs.getHost());
  }

  @Test
  public void testHashCodeMethod() {
    cs.hashCode();
    assertNotNull(cs.hashCode());
  }

  @Test
  public void testIncrementContinuousFailureCount() {
    int countBefore = cs.getCountinuousFailureCount();
    cs.incrementContinuousFailureCount();
    int countAfter = cs.getCountinuousFailureCount();
    assertEquals(countBefore + 1, countAfter);
  }

  @Test
  public void testClearContinuousFailure() {
    cs.incrementContinuousFailureCount();
    cs.clearContinuousFailure();
    assertEquals(0, cs.getCountinuousFailureCount());
  }
}
