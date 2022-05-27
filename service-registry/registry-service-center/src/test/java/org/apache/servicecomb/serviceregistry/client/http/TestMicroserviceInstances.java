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

package org.apache.servicecomb.serviceregistry.client.http;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.registry.api.registry.FindInstancesResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

public class TestMicroserviceInstances {

  MicroserviceInstances microserviceInstances = null;

  FindInstancesResponse findInstancesResponse = null;

  List<MicroserviceInstance> instances = null;

  @Before
  public void setUp() throws Exception {
    microserviceInstances = new MicroserviceInstances();
    findInstancesResponse = new FindInstancesResponse();
    instances = new ArrayList<>();
    instances.add(Mockito.mock(MicroserviceInstance.class));
  }

  @After
  public void tearDown() throws Exception {
    instances = null;
    microserviceInstances = null;
    findInstancesResponse = null;
  }

  @Test
  public void testDefaultValues() {
    Assertions.assertNull(microserviceInstances.getInstancesResponse());
    Assertions.assertTrue(microserviceInstances.isNeedRefresh());
    Assertions.assertNull(microserviceInstances.getRevision());
    Assertions.assertNull(findInstancesResponse.getInstances());
  }

  @Test
  public void testInitializedValues() {
    initFields(); //Initialize the Object
    Assertions.assertEquals(1, microserviceInstances.getInstancesResponse().getInstances().size());
    Assertions.assertFalse(microserviceInstances.isNeedRefresh());
    Assertions.assertEquals("1", microserviceInstances.getRevision());
  }

  private void initFields() {
    findInstancesResponse.setInstances(instances);
    microserviceInstances.setInstancesResponse(findInstancesResponse);
    microserviceInstances.setNeedRefresh(false);
    microserviceInstances.setRevision("1");
  }
}
