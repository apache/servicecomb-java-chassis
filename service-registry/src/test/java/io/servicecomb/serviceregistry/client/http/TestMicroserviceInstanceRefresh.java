/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.serviceregistry.client.http;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;

public class TestMicroserviceInstanceRefresh {

  MicroserviceInstanceRefresh microserviceInstanceRefresh = null;
  List<MicroserviceInstance> instances = null;

  @Before
  public void setUp() throws Exception {
    microserviceInstanceRefresh = new MicroserviceInstanceRefresh();
    instances = new ArrayList<>();
    instances.add(Mockito.mock(MicroserviceInstance.class));
  }

  @After
  public void tearDown() throws Exception {
    microserviceInstanceRefresh = null;
    instances = null;
  }

  @Test
  public void testDefaultValues() {
    Assert.assertNull(microserviceInstanceRefresh.getRevision());
    Assert.assertTrue(microserviceInstanceRefresh.isNeedRefresh());
    Assert.assertNull(microserviceInstanceRefresh.getInstances());
  }

  @Test
  public void testInitializedValues() {
    initFields(); //Initialize the Object
    Assert.assertEquals("1", microserviceInstanceRefresh.getRevision());
    Assert.assertFalse(microserviceInstanceRefresh.isNeedRefresh());
    Assert.assertEquals(1, microserviceInstanceRefresh.getInstances().size());
  }

  private void initFields() {
    microserviceInstanceRefresh.setNeedRefresh(false);
    microserviceInstanceRefresh.setRevision("1");
    microserviceInstanceRefresh.setInstances(instances);
  }
}