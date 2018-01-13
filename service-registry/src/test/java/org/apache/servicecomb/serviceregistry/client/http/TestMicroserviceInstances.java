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

import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.response.FindInstancesResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
    Assert.assertNull(microserviceInstances.getInstancesResponse());
    Assert.assertTrue(microserviceInstances.isNeedRefresh());
    Assert.assertNull(microserviceInstances.getRevision());
    Assert.assertNull(findInstancesResponse.getInstances());
  }

  @Test
  public void testInitializedValues() {
    initFields(); //Initialize the Object
    Assert.assertEquals(1, microserviceInstances.getInstancesResponse().getInstances().size());
    Assert.assertFalse(microserviceInstances.isNeedRefresh());
    Assert.assertEquals("1", microserviceInstances.getRevision());
  }

  private void initFields() {
    findInstancesResponse.setInstances(instances);
    microserviceInstances.setInstancesResponse(findInstancesResponse);
    microserviceInstances.setNeedRefresh(false);
    microserviceInstances.setRevision("1");
  }

}
