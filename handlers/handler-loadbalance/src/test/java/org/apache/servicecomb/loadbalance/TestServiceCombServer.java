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

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

/**
 *
 *
 */
public class TestServiceCombServer {

  private final Transport transport = Mockito.mock(Transport.class);

  private ServiceCombServer cs;

  @Before
  public void setUp() {
    DiscoveryInstance discoveryInstance = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance instance = new StatefulDiscoveryInstance(discoveryInstance);
    Mockito.when(discoveryInstance.getInstanceId()).thenReturn("123456");
    cs = new ServiceCombServer(null, transport, new Endpoint(transport, "abcd", instance));
  }

  @Test
  public void testCseServerObj() {
    Assertions.assertNotNull(cs);
  }

  @Test
  public void testGetEndpoint() {
    cs.getEndpoint();
    Assertions.assertNotNull(cs.getEndpoint());
  }

  @Test
  public void testEqualsMethod() {
    Assertions.assertNotEquals(cs, (Object) "abcd");

    DiscoveryInstance discoveryInstance = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance instance1 = new StatefulDiscoveryInstance(discoveryInstance);
    Mockito.when(discoveryInstance.getInstanceId()).thenReturn("1234");
    ServiceCombServer other = new ServiceCombServer(null, transport, new Endpoint(transport, "1234", instance1));
    Assertions.assertNotEquals(cs, other);

    DiscoveryInstance discoveryInstance2 = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance instance2 = new StatefulDiscoveryInstance(discoveryInstance2);
    Mockito.when(discoveryInstance2.getInstanceId()).thenReturn("123456");
    other = new ServiceCombServer(null, transport, new Endpoint(transport, "abcd", instance2));
    Assertions.assertEquals(cs, other);
  }

  @Test
  public void testToStringMethod() {
    cs.toString();
    Assertions.assertNotNull(cs.toString());
  }

  @Test
  public void testGetHost() {
    cs.getHost();
    Assertions.assertNotNull(cs.getHost());
  }

  @Test
  public void testHashCodeMethod() {
    cs.hashCode();
    Assertions.assertNotNull(cs.hashCode());
  }
}
