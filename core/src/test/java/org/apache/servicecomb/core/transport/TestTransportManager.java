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

package org.apache.servicecomb.core.transport;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestTransportManager {
  @Test
  public void testTransportManagerInitFail()
      throws Exception {
    SCBEngine scbEngine = Mockito.mock(SCBEngine.class);
    Transport transport = Mockito.mock(Transport.class);
    Mockito.when(transport.getName()).thenReturn("test");
    Mockito.when(transport.init()).thenReturn(false);
    Mockito.when(transport.canInit()).thenReturn(true);
    List<Transport> transports = Arrays.asList(transport);
    TransportManager manager = new TransportManager();
    manager.addTransportsBeforeInit(transports);

    manager.init(scbEngine);
    Assertions.assertEquals(manager.findTransport("test"), transport);
  }

  @Test
  public void testTransportManagerInitSuccess() throws Exception {
    SCBEngine scbEngine = Mockito.mock(SCBEngine.class);
    Transport transport = Mockito.mock(Transport.class);
    Endpoint endpoint = Mockito.mock(Endpoint.class);
    Mockito.when(transport.getName()).thenReturn("test");
    Mockito.when(transport.init()).thenReturn(true);
    Mockito.when(transport.canInit()).thenReturn(true);
    Mockito.when(transport.getPublishEndpoint()).thenReturn(endpoint);
    List<Transport> transports = Arrays.asList(transport);

    TransportManager manager = new TransportManager();
    manager.addTransportsBeforeInit(transports);

    manager.init(scbEngine);
    Assertions.assertEquals(manager.findTransport("test"), transport);
  }

  @Test
  public void testGroupByName() {
    Transport t1 = Mockito.mock(Transport.class);
    Mockito.when(t1.getName()).thenReturn("t1");
    Transport t21 = Mockito.mock(Transport.class);
    Mockito.when(t21.getName()).thenReturn("t2");
    Transport t22 = Mockito.mock(Transport.class);
    Mockito.when(t22.getName()).thenReturn("t2");
    TransportManager manager = new TransportManager();
    manager.addTransportsBeforeInit(Arrays.asList(t1, t21, t22));

    Map<String, List<Transport>> groups = manager.groupByName();
    Assertions.assertEquals(2, groups.size());
    Assertions.assertEquals(1, groups.get("t1").size());
    Assertions.assertEquals(t1, groups.get("t1").get(0));
    Assertions.assertEquals(2, groups.get("t2").size());
    Assertions.assertEquals(t21, groups.get("t2").get(0));
    Assertions.assertEquals(t22, groups.get("t2").get(1));
  }

  @Test
  public void testCheckTransportGroupInvalid() {
    Transport t1 = Mockito.mock(Transport.class);
    Mockito.when(t1.getOrder()).thenReturn(1);
    Transport t2 = Mockito.mock(Transport.class);
    Mockito.when(t2.getOrder()).thenReturn(1);

    TransportManager manager = new TransportManager();
    List<Transport> group = Arrays.asList(t1, t2);

    try {
      manager.checkTransportGroup(group);
      Assertions.fail("must throw exception");
    } catch (ServiceCombException e) {
      Assertions.assertTrue(e.getMessage().contains("have the same order"));
    }
  }

  @Test
  public void testCheckTransportGroupValid() {
    Transport t1 = Mockito.mock(Transport.class);
    Mockito.when(t1.getOrder()).thenReturn(1);
    Transport t2 = Mockito.mock(Transport.class);
    Mockito.when(t2.getOrder()).thenReturn(2);

    TransportManager manager = new TransportManager();
    List<Transport> group = Arrays.asList(t1, t2);

    try {
      manager.checkTransportGroup(group);
    } catch (ServiceCombException e) {
      Assertions.fail("must not throw exception: " + e.getMessage());
    }
  }

  @Test
  public void testChooseOneTransportFirst() {
    Transport t1 = Mockito.mock(Transport.class);
    Mockito.when(t1.getOrder()).thenReturn(1);
    Mockito.when(t1.canInit()).thenReturn(true);
    Transport t2 = Mockito.mock(Transport.class);
    Mockito.when(t2.getOrder()).thenReturn(2);

    TransportManager manager = new TransportManager();
    List<Transport> group = Arrays.asList(t1, t2);

    Assertions.assertEquals(t1, manager.chooseOneTransport(group));
  }

  @Test
  public void testChooseOneTransportSecond() {
    Transport t1 = Mockito.mock(Transport.class);
    Mockito.when(t1.getOrder()).thenReturn(Integer.MAX_VALUE);
    Mockito.when(t1.canInit()).thenReturn(true);
    Transport t2 = Mockito.mock(Transport.class);
    Mockito.when(t2.getOrder()).thenReturn(-1000);
    Mockito.when(t2.canInit()).thenReturn(false);
    TransportManager manager = new TransportManager();
    List<Transport> group = Arrays.asList(t1, t2);

    Assertions.assertEquals(t1, manager.chooseOneTransport(group));
  }

  @Test
  public void testChooseOneTransportNone() {
    Transport t1 = Mockito.mock(Transport.class);
    Mockito.when(t1.getName()).thenReturn("t");
    Mockito.when(t1.getOrder()).thenReturn(1);
    Mockito.when(t1.canInit()).thenReturn(false);
    Transport t2 = Mockito.mock(Transport.class);
    Mockito.when(t2.getOrder()).thenReturn(2);
    Mockito.when(t2.canInit()).thenReturn(false);

    TransportManager manager = new TransportManager();
    List<Transport> group = Arrays.asList(t1, t2);

    try {
      manager.chooseOneTransport(group);
      Assertions.fail("must throw exception");
    } catch (ServiceCombException e) {
      Assertions.assertEquals("all transport named t refused to init.", e.getMessage());
    }
  }
}
