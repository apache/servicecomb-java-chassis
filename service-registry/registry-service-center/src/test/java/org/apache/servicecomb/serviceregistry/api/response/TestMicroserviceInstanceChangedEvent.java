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

package org.apache.servicecomb.serviceregistry.api.response;

import org.apache.servicecomb.registry.api.MicroserviceKey;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.WatchAction;
import org.apache.servicecomb.registry.api.event.MicroserviceInstanceChangedEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

public class TestMicroserviceInstanceChangedEvent {

  MicroserviceInstanceChangedEvent oMicroserviceInstanceChangedEvent = null;

  MicroserviceKey oMockMicroserviceKey = null;

  MicroserviceInstance oMockMicroserviceInstance = null;

  @Before
  public void setUp() throws Exception {
    oMicroserviceInstanceChangedEvent = new MicroserviceInstanceChangedEvent();
    oMockMicroserviceKey = Mockito.mock(MicroserviceKey.class);
    oMockMicroserviceInstance = Mockito.mock(MicroserviceInstance.class);
  }

  @After
  public void tearDown() throws Exception {
    oMicroserviceInstanceChangedEvent = null;
    oMockMicroserviceKey = null;
    oMockMicroserviceInstance = null;
  }

  @Test
  public void testDefaultValues() {
    Assertions.assertNull(oMicroserviceInstanceChangedEvent.getAction());
    Assertions.assertNull(oMicroserviceInstanceChangedEvent.getInstance());
    Assertions.assertNull(oMicroserviceInstanceChangedEvent.getKey());
  }

  @Test
  public void testInitializedValues() {
    initFields(); //Initialize the Object
    Assertions.assertEquals(WatchAction.CREATE, oMicroserviceInstanceChangedEvent.getAction());
    Assertions.assertEquals("CREATE", oMicroserviceInstanceChangedEvent.getAction().getName());
    Assertions.assertEquals(oMockMicroserviceInstance, oMicroserviceInstanceChangedEvent.getInstance());
    Assertions.assertEquals(oMockMicroserviceKey, oMicroserviceInstanceChangedEvent.getKey());

    //Test Different Actions
    oMicroserviceInstanceChangedEvent.setAction(WatchAction.UPDATE);
    Assertions.assertEquals("UPDATE", oMicroserviceInstanceChangedEvent.getAction().getName());

    oMicroserviceInstanceChangedEvent.setAction(WatchAction.DELETE);
    Assertions.assertEquals("DELETE", oMicroserviceInstanceChangedEvent.getAction().getName());
  }

  private void initFields() {
    oMicroserviceInstanceChangedEvent.setInstance(oMockMicroserviceInstance);
    oMicroserviceInstanceChangedEvent.setKey(oMockMicroserviceKey);
    oMicroserviceInstanceChangedEvent.setAction(WatchAction.CREATE);
  }
}
