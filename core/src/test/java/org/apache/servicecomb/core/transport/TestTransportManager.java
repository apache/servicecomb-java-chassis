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
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.junit.Test;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import org.junit.jupiter.api.Assertions;

public class TestTransportManager {
  @Test
  public void testTransportManagerInitFail(@Mocked SCBEngine scbEngine, @Injectable Transport transport)
      throws Exception {
    new Expectations() {
      {
        transport.getName();
        result = "test";
        transport.init();
        result = false;
        transport.canInit();
        result = true;
      }
    };
    List<Transport> transports = Arrays.asList(transport);

    TransportManager manager = new TransportManager();
    manager.addTransportsBeforeInit(transports);

    manager.init(scbEngine);
    Assertions.assertEquals(manager.findTransport("test"), transport);
  }

  @Test
  public void testTransportManagerInitSucc(@Mocked SCBEngine scbEngine, @Injectable Transport transport,
      @Injectable Endpoint endpoint, @Injectable MicroserviceInstance instance) throws Exception {
    new Expectations() {
      {
        transport.getName();
        result = "test";
        transport.canInit();
        result = true;
        transport.init();
        result = true;
        transport.getPublishEndpoint();
        result = endpoint;
      }
    };
    List<Transport> transports = Arrays.asList(transport);

    TransportManager manager = new TransportManager();
    manager.addTransportsBeforeInit(transports);

    manager.init(scbEngine);
    Assertions.assertEquals(manager.findTransport("test"), transport);
  }

  @Test
  public void testGroupByName(@Mocked Transport t1, @Mocked Transport t2_1, @Mocked Transport t2_2) {
    new Expectations() {
      {
        t1.getName();
        result = "t1";

        t2_1.getName();
        result = "t2";
        t2_2.getName();
        result = "t2";
      }
    };

    TransportManager manager = new TransportManager();
    manager.addTransportsBeforeInit(Arrays.asList(t1, t2_1, t2_2));

    Map<String, List<Transport>> groups = manager.groupByName();
    Assertions.assertEquals(2, groups.size());
    Assertions.assertEquals(1, groups.get("t1").size());
    Assertions.assertEquals(t1, groups.get("t1").get(0));
    Assertions.assertEquals(2, groups.get("t2").size());
    Assertions.assertEquals(t2_1, groups.get("t2").get(0));
    Assertions.assertEquals(t2_2, groups.get("t2").get(1));
  }

  @Test
  public void testCheckTransportGroupInvalid(@Mocked Transport t1, @Mocked Transport t2) {
    new Expectations() {
      {
        t1.getOrder();
        result = 1;

        t2.getOrder();
        result = 1;
      }
    };

    TransportManager manager = new TransportManager();
    List<Transport> group = Arrays.asList(t1, t2);

    try {
      manager.checkTransportGroup(group);
      Assertions.fail("must throw exception");
    } catch (ServiceCombException e) {
      Assertions.assertEquals(
          "org.apache.servicecomb.core.$Impl_Transport and org.apache.servicecomb.core.$Impl_Transport have the same order 1",
          e.getMessage());
    }
  }

  @Test
  public void testCheckTransportGroupValid(@Mocked Transport t1, @Mocked Transport t2) {
    new Expectations() {
      {
        t1.getOrder();
        result = 1;

        t2.getOrder();
        result = 2;
      }
    };

    TransportManager manager = new TransportManager();
    List<Transport> group = Arrays.asList(t1, t2);

    try {
      manager.checkTransportGroup(group);
    } catch (ServiceCombException e) {
      Assertions.fail("must not throw exception: " + e.getMessage());
    }
  }

  @Test
  public void testChooseOneTransportFirst(@Mocked Transport t1, @Mocked Transport t2) {
    new Expectations() {
      {
        t1.getOrder();
        result = 1;
        t1.canInit();
        result = true;

        t2.getOrder();
        result = 2;
      }
    };

    TransportManager manager = new TransportManager();
    List<Transport> group = Arrays.asList(t1, t2);

    Assertions.assertEquals(t1, manager.chooseOneTransport(group));
  }

  @Test
  public void testChooseOneTransportSecond(@Mocked Transport t1, @Mocked Transport t2) {
    new Expectations() {
      {
        t1.getOrder();
        result = Integer.MAX_VALUE;
        t1.canInit();
        result = true;

        t2.getOrder();
        result = -1000;
        t2.canInit();
        result = false;
      }
    };

    TransportManager manager = new TransportManager();
    List<Transport> group = Arrays.asList(t1, t2);

    Assertions.assertEquals(t1, manager.chooseOneTransport(group));
  }

  @Test
  public void testChooseOneTransportNone(@Mocked Transport t1, @Mocked Transport t2) {
    new Expectations() {
      {
        t1.getName();
        result = "t";
        t1.getOrder();
        result = 1;
        t1.canInit();
        result = false;

        t2.getOrder();
        result = 2;
        t2.canInit();
        result = false;
      }
    };

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
