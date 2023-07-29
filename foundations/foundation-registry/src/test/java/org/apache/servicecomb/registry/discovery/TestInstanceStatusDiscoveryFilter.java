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
package org.apache.servicecomb.registry.discovery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance.HistoryStatus;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance.IsolationStatus;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance.PingStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestInstanceStatusDiscoveryFilter {
  @Test
  public void test_all_group_correct_init() {
    InstanceStatusDiscoveryFilter filter = new InstanceStatusDiscoveryFilter();
    DiscoveryTreeNode parent = new DiscoveryTreeNode();
    List<StatefulDiscoveryInstance> instances = new ArrayList<>();
    StatefulDiscoveryInstance instance1 = Mockito.mock(StatefulDiscoveryInstance.class);
    Mockito.when(instance1.getHistoryStatus()).thenReturn(HistoryStatus.CURRENT);
    Mockito.when(instance1.getMicroserviceInstanceStatus()).thenReturn(MicroserviceInstanceStatus.UP);
    Mockito.when(instance1.getPingStatus()).thenReturn(PingStatus.OK);
    Mockito.when(instance1.getIsolationStatus()).thenReturn(IsolationStatus.NORMAL);
    StatefulDiscoveryInstance instance2 = Mockito.mock(StatefulDiscoveryInstance.class);
    Mockito.when(instance2.getHistoryStatus()).thenReturn(HistoryStatus.CURRENT);
    Mockito.when(instance2.getMicroserviceInstanceStatus()).thenReturn(MicroserviceInstanceStatus.UP);
    Mockito.when(instance2.getPingStatus()).thenReturn(PingStatus.UNKNOWN);
    Mockito.when(instance2.getIsolationStatus()).thenReturn(IsolationStatus.NORMAL);
    StatefulDiscoveryInstance instance3 = Mockito.mock(StatefulDiscoveryInstance.class);
    Mockito.when(instance3.getHistoryStatus()).thenReturn(HistoryStatus.HISTORY);
    Mockito.when(instance3.getMicroserviceInstanceStatus()).thenReturn(MicroserviceInstanceStatus.UP);
    Mockito.when(instance3.getPingStatus()).thenReturn(PingStatus.OK);
    Mockito.when(instance3.getIsolationStatus()).thenReturn(IsolationStatus.NORMAL);
    StatefulDiscoveryInstance instance4 = Mockito.mock(StatefulDiscoveryInstance.class);
    Mockito.when(instance4.getHistoryStatus()).thenReturn(HistoryStatus.CURRENT);
    Mockito.when(instance4.getMicroserviceInstanceStatus()).thenReturn(MicroserviceInstanceStatus.UP);
    Mockito.when(instance4.getPingStatus()).thenReturn(PingStatus.FAIL);
    Mockito.when(instance4.getIsolationStatus()).thenReturn(IsolationStatus.NORMAL);
    instances.addAll(Arrays.asList(instance1, instance2, instance3, instance4));

    parent.name("parent");
    parent.data(instances);
    filter.init(new DiscoveryContext(), parent);

    List<StatefulDiscoveryInstance> level0 = parent
        .child(InstanceStatusDiscoveryFilter.GROUP_PREFIX + 0)
        .data();
    Assertions.assertEquals(1, level0.size());
    List<StatefulDiscoveryInstance> level1 = parent
        .child(InstanceStatusDiscoveryFilter.GROUP_PREFIX + 1)
        .data();
    Assertions.assertEquals(1, level1.size());
    Assertions.assertEquals(1, level1.size());
    List<StatefulDiscoveryInstance> level2 = parent
        .child(InstanceStatusDiscoveryFilter.GROUP_PREFIX + 2)
        .data();
    Assertions.assertEquals(1, level2.size());
    List<StatefulDiscoveryInstance> level3 = parent
        .child(InstanceStatusDiscoveryFilter.GROUP_PREFIX + 3)
        .data();
    Assertions.assertEquals(1, level3.size());

    Assertions.assertEquals(null, parent
        .child(InstanceStatusDiscoveryFilter.GROUP_PREFIX + 4));
  }
}
