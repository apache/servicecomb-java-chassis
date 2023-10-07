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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

public class TestDiscoveryTree {
  DiscoveryContext context = new DiscoveryContext();

  DiscoveryTreeNode parent = new DiscoveryTreeNode().name("parent");

  DiscoveryTreeNode result;

  @BeforeEach
  public void before() {
    ConfigUtil.installDynamicConfig();
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void isMatch_existingNull() {
    DiscoveryTree discoveryTree = new DiscoveryTree(
        new DiscoveryManager(Collections.emptyList(), List.of(new TelnetInstancePing())));
    Assertions.assertFalse(discoveryTree.isMatch(null, null));
  }

  @Test
  public void isMatch_yes() {
    DiscoveryTree discoveryTree = new DiscoveryTree(new DiscoveryManager(Collections.emptyList(),
        List.of(new TelnetInstancePing())));
    parent.cacheVersion(1);
    Assertions.assertTrue(discoveryTree.isMatch(new DiscoveryTreeNode().cacheVersion(1), parent));
  }

  @Test
  public void isMatch_no() {
    DiscoveryTree discoveryTree = new DiscoveryTree(
        new DiscoveryManager(Collections.emptyList(), List.of(new TelnetInstancePing())));
    parent.cacheVersion(0);
    Assertions.assertFalse(discoveryTree.isExpired(new DiscoveryTreeNode().cacheVersion(1), parent));
  }

  @Test
  public void isExpired_existingNull() {
    DiscoveryTree discoveryTree = new DiscoveryTree(
        new DiscoveryManager(Collections.emptyList(), List.of(new TelnetInstancePing())));
    Assertions.assertTrue(discoveryTree.isExpired(null, null));
  }

  @Test
  public void isExpired_yes() {
    DiscoveryTree discoveryTree = new DiscoveryTree(
        new DiscoveryManager(Collections.emptyList(), List.of(new TelnetInstancePing())));
    parent.cacheVersion(1);
    Assertions.assertTrue(discoveryTree.isExpired(new DiscoveryTreeNode().cacheVersion(0), parent));
  }

  @Test
  public void isExpired_no() {
    DiscoveryTree discoveryTree = new DiscoveryTree(
        new DiscoveryManager(Collections.emptyList(), List.of(new TelnetInstancePing())));
    parent.cacheVersion(0);
    Assertions.assertFalse(discoveryTree.isExpired(new DiscoveryTreeNode().cacheVersion(0), parent));
  }

  static class DiscoveryFilterForTest implements DiscoveryFilter {
    protected String groupName;

    public DiscoveryFilterForTest(String groupName) {
      this.groupName = groupName;
    }

    @Override
    public int getOrder() {
      return 0;
    }

    @Override
    public boolean isGroupingFilter() {
      return groupName != null;
    }

    @Override
    public DiscoveryTreeNode discovery(DiscoveryContext context, DiscoveryTreeNode parent) {
      DiscoveryTreeNode child = new DiscoveryTreeNode();
      if (groupName != null) {
        child.subName(parent, groupName);
      }
      return child;
    }
  }

  @Test
  public void filterNormal() {
    DiscoveryTree discoveryTree = new DiscoveryTree(
        new DiscoveryManager(Collections.emptyList(), List.of(new TelnetInstancePing())));
    parent.name("1.0.0-2.0.0");

    discoveryTree.setDiscoveryFilters(Arrays.asList(new DiscoveryFilterForTest("g1"),
        new DiscoveryFilterForTest(null), new DiscoveryFilterForTest("g2"),
        new DiscoveryFilterForTest(null)));
    result = discoveryTree.discovery("app", "service", context, parent);

    Assertions.assertEquals("1.0.0-2.0.0/g1/g2", result.name());
  }

  @Test
  public void easyDiscovery() {
    DiscoveryManager discoveryManager = Mockito.mock(DiscoveryManager.class);
    DiscoveryTree discoveryTree = new DiscoveryTree(discoveryManager);
    Mockito.when(discoveryManager.getOrCreateVersionedCache("app", "svc")).thenReturn(parent);

    result = discoveryTree.discovery(context, "app", "svc");
    Assertions.assertEquals(parent.name(), result.name());
    Assertions.assertEquals(parent.cacheVersion(), result.cacheVersion());
  }

  @Test
  public void discovery_filterReturnNull() {
    DiscoveryManager discoveryManager = Mockito.mock(DiscoveryManager.class);
    Mockito.when(discoveryManager.getOrCreateVersionedCache("app", "service")).thenReturn(parent);
    DiscoveryTree discoveryTree = new DiscoveryTree(discoveryManager);
    DiscoveryFilter filter = new DiscoveryFilter() {
      @Override
      public int getOrder() {
        return 0;
      }

      @Override
      public DiscoveryTreeNode discovery(DiscoveryContext context, DiscoveryTreeNode parent) {
        return null;
      }
    };
    discoveryTree.setDiscoveryFilters(Arrays.asList(filter));

    ServiceCombException exception = Assertions.assertThrows(ServiceCombException.class,
        () -> result = discoveryTree.discovery(context, "app", "service"));
    Assertions.assertEquals(filter.getClass().getName() + " discovery return null.", exception.getMessage());
  }

  @Test
  public void filterRerun() {
    parent.name("1.0.0-2.0.0");

    DiscoveryFilterForTest f1 = new DiscoveryFilterForTest("g1") {
      @Override
      public DiscoveryTreeNode discovery(DiscoveryContext context, DiscoveryTreeNode parent) {
        if (context.getContextParameter("step") == null) {
          context.pushRerunFilter();
          context.putContextParameter("step", 1);
          return new DiscoveryTreeNode().name(groupName).data("first");
        }

        return new DiscoveryTreeNode().name(groupName).data("second");
      }
    };
    DiscoveryFilterForTest f2 = new DiscoveryFilterForTest(null) {
      @Override
      public DiscoveryTreeNode discovery(DiscoveryContext context, DiscoveryTreeNode parent) {
        if ("first".equals(parent.data())) {
          return new DiscoveryTreeNode();
        }

        return new DiscoveryTreeNode().data(parent.data());
      }
    };
    DiscoveryTree discoveryTree = new DiscoveryTree(
        new DiscoveryManager(Collections.emptyList(), List.of(new TelnetInstancePing())));
    discoveryTree.setDiscoveryFilters(Arrays.asList(f1, f2));
    result = discoveryTree.discovery("app", "service", context, parent);

    Assertions.assertEquals("second", result.data());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void test_multi_service_discovery_correct() {
    DiscoveryManager discoveryManager = Mockito.mock(DiscoveryManager.class);
    DiscoveryTree discoveryTree = new DiscoveryTree(discoveryManager);
    DiscoveryContext discoveryContext = new DiscoveryContext();

    List<String> service1 = Arrays.asList("s11", "s12");
    Mockito.when(discoveryManager.getOrCreateVersionedCache("app", "service1"))
        .thenReturn(new VersionedCache().name("0+").data(service1));
    DiscoveryTreeNode result = discoveryTree.discovery(discoveryContext, "app", "service1");
    Assertions.assertArrayEquals(service1.toArray(new String[0]),
        ((List<String>) result.data()).toArray(new String[0]));

    List<String> service2 = Arrays.asList("s21", "s22");
    Mockito.when(discoveryManager.getOrCreateVersionedCache("app", "service2"))
        .thenReturn(new VersionedCache().name("0+").data(service2));
    result = discoveryTree.discovery(discoveryContext, "app", "service2");
    Assertions.assertArrayEquals(service2.toArray(new String[0]),
        ((List<String>) result.data()).toArray(new String[0]));

    result = discoveryTree.discovery(discoveryContext, "app", "service1");
    Assertions.assertArrayEquals(service1.toArray(new String[0]),
        ((List<String>) result.data()).toArray(new String[0]));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void test_one_service_concurrent_correct() throws Exception {
    DiscoveryManager discoveryManager = Mockito.mock(DiscoveryManager.class);
    DiscoveryTree discoveryTree = new DiscoveryTree(discoveryManager);
    DiscoveryContext discoveryContext = new DiscoveryContext();
    InstanceStatusDiscoveryFilter filter = new InstanceStatusDiscoveryFilter();
    Environment environment = Mockito.mock(Environment.class);
    Mockito.when(environment
            .getProperty("servicecomb.loadbalance.filter.status.enabled", Boolean.class, true))
        .thenReturn(true);
    filter.setEnvironment(environment);
    discoveryTree.setDiscoveryFilters(List.of(filter));

    StatefulDiscoveryInstance instance1 = Mockito.mock(StatefulDiscoveryInstance.class);
    StatefulDiscoveryInstance instance2 = Mockito.mock(StatefulDiscoveryInstance.class);

    VersionedCache expects0 = new VersionedCache().autoCacheVersion().name("0+")
        .data(Arrays.asList(instance1, instance2));
    VersionedCache[] expects999 = new VersionedCache[999];
    for (int i = 0; i < 999; i++) {
      expects999[i] = new VersionedCache().name("0+")
          .data(Arrays.asList(instance1, instance2)).cacheVersion(i + 1);
    }
    Mockito.when(discoveryManager.getOrCreateVersionedCache("app", "service1"))
        .thenReturn(expects0, expects999);

    CountDownLatch countDownLatch = new CountDownLatch(1000);
    AtomicInteger success = new AtomicInteger(0);
    for (int i = 0; i < 10; i++) {
      new Thread(() -> {
        for (int j = 0; j < 100; j++) {
          DiscoveryTreeNode result = discoveryTree.discovery(discoveryContext, "app", "service1");
          if (((List<StatefulDiscoveryInstance>) result.data()).size() == 2) {
            success.getAndIncrement();
          }
          countDownLatch.countDown();
        }
      }).start();
    }

    countDownLatch.await(3000, TimeUnit.MILLISECONDS);
    Assertions.assertEquals(1000, success.get());
  }
}
