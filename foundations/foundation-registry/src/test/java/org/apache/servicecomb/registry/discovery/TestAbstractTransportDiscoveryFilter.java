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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        MicroserviceInstance instance) {
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
    parent.data(Collections.emptyMap());
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

  private MicroserviceInstance createInstance(String... schemas) {
    String id = UUID.randomUUID().toString();
    MicroserviceInstance instance = new MicroserviceInstance();
    instance.setInstanceId(id);
    for (int idx = 0; idx < schemas.length; idx++) {
      String schema = schemas[idx];
      instance.getEndpoints().add(String.format("%s://%s:%d", schema, id, 8080 + idx));
    }
    return instance;
  }

  private Map<String, MicroserviceInstance> createMicroserviceInstances(MicroserviceInstance... instances) {
    Map<String, MicroserviceInstance> map = new LinkedHashMap<>();
    for (MicroserviceInstance instance : instances) {
      map.put(instance.getInstanceId(), instance);
    }
    return map;
  }

  @Test
  public void createDiscoveryTree_oneTransport() {
    MicroserviceInstance instance1 = createInstance("a", "b");
    MicroserviceInstance instance2 = createInstance("b");
    Map<String, MicroserviceInstance> instances = createMicroserviceInstances(instance1, instance2);
    parent.data(instances);

    result = filter.createDiscoveryTreeNode("a", context, parent);

    Assertions.assertEquals("parent/a", result.name());
    MatcherAssert.assertThat(result.collectionData(), Matchers.contains(instance1.getEndpoints().get(0)));
  }

  @Test
  public void createDiscoveryTree_allTransport() {
    MicroserviceInstance instance1 = createInstance("a", "b");
    MicroserviceInstance instance2 = createInstance("b");
    Map<String, MicroserviceInstance> instances = createMicroserviceInstances(instance1, instance2);
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
    MicroserviceInstance instance1 = createInstance("a", "b");
    MicroserviceInstance instance2 = createInstance("");
    Map<String, MicroserviceInstance> instances = createMicroserviceInstances(instance1, instance2);
    parent.data(instances);

    result = filter.createDiscoveryTreeNode("", context, parent);

    Assertions.assertEquals("parent/", result.name());
    MatcherAssert.assertThat(result.collectionData(), Matchers.contains(instance1.getEndpoints().toArray()));
  }

  @Test
  public void createEndpointNull() {
    disableCreate = true;
    MicroserviceInstance instance1 = createInstance("a", "b");
    Map<String, MicroserviceInstance> instances = createMicroserviceInstances(instance1);
    parent.data(instances);

    result = filter.createDiscoveryTreeNode("", context, parent);

    Assertions.assertEquals("parent/", result.name());
    MatcherAssert.assertThat(result.collectionData(), Matchers.empty());
  }
}
