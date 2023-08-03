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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestAbstractTransportDiscoveryFilter {
  class AbstractEndpointDiscoveryFilterForTest extends AbstractEndpointDiscoveryFilter {
    @Override
    public int getOrder() {
      return 0;
    }

    @Override
    protected String findTransportName(DiscoveryContext context, DiscoveryTreeNode parent) {
      return transportName;
    }

    @Override
    protected Object createEndpoint(DiscoveryContext context, String transportName, String endpoint,
        StatefulDiscoveryInstance instance) {
      if (disableCreate) {
        return null;
      }

      return endpoint;
    }
  }

  boolean disableCreate;

  String transportName;

  AbstractEndpointDiscoveryFilterForTest filter = new AbstractEndpointDiscoveryFilterForTest();

  DiscoveryContext context = new DiscoveryContext();

  DiscoveryTreeNode parent = new DiscoveryTreeNode().name("parent");

  DiscoveryTreeNode result;

  @Test
  public void isGroupingFilter() {
    Assertions.assertTrue(filter.isGroupingFilter());
  }

  @Test
  public void isTransportNameMatch_expectAll() {
    Assertions.assertTrue(filter.isTransportNameMatch("any", ""));
  }

  @Test
  public void isTransportNameMatch_equals() {
    Assertions.assertTrue(filter.isTransportNameMatch("rest", "rest"));
  }

  @Test
  public void isTransportNameMatch_notEquals() {
    Assertions.assertFalse(filter.isTransportNameMatch("rest", "highway"));
  }

  @Test
  public void discoveryNotExist() {
    transportName = "notExist";
    parent.data(Collections.emptyList());
    result = filter.discovery(context, parent);

    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  public void discoveryExist() {
    transportName = "rest";
    DiscoveryTreeNode child = new DiscoveryTreeNode();
    parent.child(transportName, child);
    result = filter.discovery(context, parent);

    Assertions.assertSame(child, result);
  }

  private StatefulDiscoveryInstance createInstance(String... schemas) {
    String id = UUID.randomUUID().toString();
    DiscoveryInstance discoveryInstance = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance instance = new StatefulDiscoveryInstance(discoveryInstance);
    List<String> endpoints = new ArrayList<>();
    for (int idx = 0; idx < schemas.length; idx++) {
      String schema = schemas[idx];
      endpoints.add(String.format("%s://%s:%d", schema, id, 8080 + idx));
    }
    Mockito.when(discoveryInstance.getInstanceId()).thenReturn(id);
    Mockito.when(discoveryInstance.getEndpoints()).thenReturn(endpoints);
    return instance;
  }

  private List<StatefulDiscoveryInstance> createMicroserviceInstances(StatefulDiscoveryInstance... instances) {
    List<StatefulDiscoveryInstance> result = new ArrayList<>();
    for (StatefulDiscoveryInstance instance : instances) {
      result.add(instance);
    }
    return result;
  }

  @Test
  public void createDiscoveryTree_oneTransport() {
    StatefulDiscoveryInstance instance1 = createInstance("a", "b");
    StatefulDiscoveryInstance instance2 = createInstance("b");
    List<StatefulDiscoveryInstance> instances = createMicroserviceInstances(instance1, instance2);
    parent.data(instances);

    result = filter.createDiscoveryTreeNode("a", context, parent);

    Assertions.assertEquals("parent/a", result.name());
    MatcherAssert.assertThat(result.collectionData(),
        Matchers.contains(instance1.getEndpoints().get(0)));
  }

  @Test
  public void createDiscoveryTree_allTransport() {
    StatefulDiscoveryInstance instance1 = createInstance("a", "b");
    StatefulDiscoveryInstance instance2 = createInstance("b");
    List<StatefulDiscoveryInstance> instances = createMicroserviceInstances(instance1, instance2);
    parent.data(instances);

    result = filter.createDiscoveryTreeNode("", context, parent);

    Assertions.assertEquals("parent/", result.name());

    List<String> expect = new ArrayList<>();
    expect.addAll(instance1.getEndpoints());
    expect.addAll(instance2.getEndpoints());
    MatcherAssert.assertThat(result.collectionData(), Matchers.contains(expect.toArray()));
  }

  @Test
  public void createDiscoveryTree_ignoreInvalid() {
    StatefulDiscoveryInstance instance1 = createInstance("a", "b");
    StatefulDiscoveryInstance instance2 = createInstance("");
    List<StatefulDiscoveryInstance> instances = createMicroserviceInstances(instance1, instance2);
    parent.data(instances);

    result = filter.createDiscoveryTreeNode("", context, parent);

    Assertions.assertEquals("parent/", result.name());
    MatcherAssert.assertThat(result.collectionData(),
        Matchers.contains(instance1.getEndpoints().toArray()));
  }

  @Test
  public void createEndpointNull() {
    disableCreate = true;
    StatefulDiscoveryInstance instance1 = createInstance("a", "b");
    List<StatefulDiscoveryInstance> instances = createMicroserviceInstances(instance1);
    parent.data(instances);

    result = filter.createDiscoveryTreeNode("", context, parent);

    Assertions.assertEquals("parent/", result.name());
    MatcherAssert.assertThat(result.collectionData(), Matchers.empty());
  }
}
