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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.cache.InstanceCacheManager;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;


public class TestDiscoveryTree {
  @BeforeEach
  public void before() {
    ConfigUtil.installDynamicConfig();
  }

  @AfterEach
  public void tearDown() {
    ArchaiusUtils.resetConfig();
  }

  DiscoveryTree discoveryTree = new DiscoveryTree();

  List<DiscoveryFilter> filters = discoveryTree.getFilters();

  DiscoveryContext context = new DiscoveryContext();

  DiscoveryTreeNode parent = new DiscoveryTreeNode().name("parent");

  DiscoveryTreeNode result;

  @Test
  public void loadFromSPI() {
    DiscoveryFilter f1 = Mockito.mock(DiscoveryFilter.class);
    DiscoveryFilter f2 = Mockito.mock(DiscoveryFilter.class);

    Class<? extends DiscoveryFilter> cls = DiscoveryFilter.class;
    try (MockedStatic<SPIServiceUtils> spiServiceUtilsMockedStatic = Mockito.mockStatic(SPIServiceUtils.class)) {
      spiServiceUtilsMockedStatic.when(() -> SPIServiceUtils.getSortedService(cls)).thenReturn(Arrays.asList(f1, f2));

      discoveryTree.loadFromSPI(cls);

      MatcherAssert.assertThat(filters, Matchers.contains(f1, f2));
    }
  }


  @Test
  public void sort() {
    DiscoveryFilter f1 = Mockito.mock(DiscoveryFilter.class);
    DiscoveryFilter f2 = Mockito.mock(DiscoveryFilter.class);
    DiscoveryFilter f3 = Mockito.mock(DiscoveryFilter.class);
    Mockito.when(f1.getOrder()).thenReturn(-1);
    Mockito.when(f1.enabled()).thenReturn(true);
    Mockito.when(f2.getOrder()).thenReturn(0);
    Mockito.when(f2.enabled()).thenReturn(true);
    Mockito.when(f3.getOrder()).thenReturn(0);
    Mockito.when(f3.enabled()).thenReturn(false);

    discoveryTree.addFilter(f3);
    discoveryTree.addFilter(f2);
    discoveryTree.addFilter(f1);
    discoveryTree.sort();

    MatcherAssert.assertThat(filters, Matchers.contains(f1, f2));
  }


  @Test
  public void isMatch_existingNull() {
    Assertions.assertFalse(discoveryTree.isMatch(null, null));
  }

  @Test
  public void isMatch_yes() {
    parent.cacheVersion(1);
    Assertions.assertTrue(discoveryTree.isMatch(new DiscoveryTreeNode().cacheVersion(1), parent));
  }

  @Test
  public void isMatch_no() {
    parent.cacheVersion(0);
    Assertions.assertFalse(discoveryTree.isExpired(new DiscoveryTreeNode().cacheVersion(1), parent));
  }

  @Test
  public void isExpired_existingNull() {
    Assertions.assertTrue(discoveryTree.isExpired(null, null));
  }

  @Test
  public void isExpired_yes() {
    parent.cacheVersion(1);
    Assertions.assertTrue(discoveryTree.isExpired(new DiscoveryTreeNode().cacheVersion(0), parent));
  }

  @Test
  public void isExpired_no() {
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
    parent.name("1.0.0-2.0.0");

    discoveryTree.addFilter(new DiscoveryFilterForTest("g1"));
    discoveryTree.addFilter(new DiscoveryFilterForTest(null));
    discoveryTree.addFilter(new DiscoveryFilterForTest("g2"));
    discoveryTree.addFilter(new DiscoveryFilterForTest(null));

    result = discoveryTree.discovery(context, parent);

    Assertions.assertEquals("1.0.0-2.0.0/g1/g2", result.name());
  }

  @Test
  public void easyDiscovery() {
    InstanceCacheManager instanceCacheManager = Mockito.mock(InstanceCacheManager.class);
    DiscoveryManager.INSTANCE = Mockito.spy(DiscoveryManager.INSTANCE);
    Mockito.when(DiscoveryManager.INSTANCE.getInstanceCacheManager()).thenReturn(instanceCacheManager);
    Mockito.when(instanceCacheManager.getOrCreateVersionedCache(null, null, null)).thenReturn(parent);

    result = discoveryTree.discovery(context, null, null, null);
    Assertions.assertEquals(parent.name(), result.name());
    Assertions.assertEquals(parent.cacheVersion(), result.cacheVersion());
  }

  @Test
  public void discovery_filterReturnNull() {
    InstanceCacheManager instanceCacheManager = Mockito.mock(InstanceCacheManager.class);
    DiscoveryManager.INSTANCE = Mockito.spy(DiscoveryManager.INSTANCE);
    Mockito.when(DiscoveryManager.INSTANCE.getInstanceCacheManager()).thenReturn(instanceCacheManager);
    Mockito.when(instanceCacheManager.getOrCreateVersionedCache(null, null, null)).thenReturn(parent);

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
    discoveryTree.addFilter(filter);

    ServiceCombException exception = Assertions.assertThrows(ServiceCombException.class,
        () -> result = discoveryTree.discovery(context, null, null, null));
    Assertions.assertEquals(filter.getClass().getName() + " discovery return null.", exception.getMessage());
  }

  @Test
  public void filterRerun() {
    parent.name("1.0.0-2.0.0");

    discoveryTree.addFilter(new DiscoveryFilterForTest("g1") {
      @Override
      public DiscoveryTreeNode discovery(DiscoveryContext context, DiscoveryTreeNode parent) {
        if (context.getContextParameter("step") == null) {
          context.pushRerunFilter();
          context.putContextParameter("step", 1);
          return new DiscoveryTreeNode().name(groupName).data("first");
        }

        return new DiscoveryTreeNode().name(groupName).data("second");
      }
    });
    discoveryTree.addFilter(new DiscoveryFilterForTest(null) {
      @Override
      public DiscoveryTreeNode discovery(DiscoveryContext context, DiscoveryTreeNode parent) {
        if ("first".equals(parent.data())) {
          return new DiscoveryTreeNode();
        }

        return new DiscoveryTreeNode().data(parent.data());
      }
    });

    result = discoveryTree.discovery(context, parent);

    Assertions.assertEquals("second", result.data());
  }

  @Test
  public void avoidConcurrentProblem() {
    discoveryTree.setRoot(parent.cacheVersion(1));
    Assertions.assertTrue(parent.children().isEmpty());

    discoveryTree.discovery(context, new VersionedCache().cacheVersion(0).name("input"));
    Assertions.assertTrue(parent.children().isEmpty());
  }

  @Test
  public void getOrCreateRoot_match() {
    discoveryTree.setRoot(parent);

    DiscoveryTreeNode root = discoveryTree.getOrCreateRoot(parent);

    Assertions.assertSame(parent, root);
  }

  @Test
  public void getOrCreateRoot_expired() {
    discoveryTree.setRoot(parent);

    VersionedCache inputCache = new VersionedCache().cacheVersion(parent.cacheVersion() + 1);
    DiscoveryTreeNode root = discoveryTree.getOrCreateRoot(inputCache);

    Assertions.assertEquals(inputCache.cacheVersion(), root.cacheVersion());
    Assertions.assertSame(discoveryTree.getRoot(), root);
  }

  @Test
  public void getOrCreateRoot_tempRoot() {
    discoveryTree.setRoot(parent);

    VersionedCache inputCache = new VersionedCache().cacheVersion(parent.cacheVersion() - 1);
    DiscoveryTreeNode root = discoveryTree.getOrCreateRoot(inputCache);

    Assertions.assertEquals(inputCache.cacheVersion(), root.cacheVersion());
    Assertions.assertNotSame(discoveryTree.getRoot(), root);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void test_one_service_concurrent_correct() throws Exception {
    DiscoveryTree discoveryTree = new DiscoveryTree();
    DiscoveryContext discoveryContext = new DiscoveryContext();
    discoveryTree.addFilter(new InstanceStatusDiscoveryFilter());

    Map<String, MicroserviceInstance> service1 = new HashMap<>();
    MicroserviceInstance instance1 = Mockito.mock(MicroserviceInstance.class);
    MicroserviceInstance instance2 = Mockito.mock(MicroserviceInstance.class);
    Mockito.when(instance1.getInstanceId()).thenReturn("instance1");
    Mockito.when(instance1.getStatus()).thenReturn(MicroserviceInstanceStatus.UP);
    Mockito.when(instance2.getInstanceId()).thenReturn("instance2");
    Mockito.when(instance2.getStatus()).thenReturn(MicroserviceInstanceStatus.UP);
    service1.put(instance1.getInstanceId(), instance1);
    service1.put(instance2.getInstanceId(), instance2);

    InstanceCacheManager instanceCacheManager = Mockito.mock(InstanceCacheManager.class);
    DiscoveryManager.INSTANCE = Mockito.spy(DiscoveryManager.INSTANCE);
    Mockito.when(DiscoveryManager.INSTANCE.getInstanceCacheManager()).thenReturn(instanceCacheManager);

    VersionedCache expects0 = new VersionedCache().autoCacheVersion().name("0+").data(service1);
    VersionedCache[] expects999 = new VersionedCache[999];
    for (int i = 0; i < 999; i++) {
      expects999[i] = new VersionedCache().name("0+").data(service1).cacheVersion(i + 1);
    }
    Mockito.when(instanceCacheManager.getOrCreateVersionedCache("app", "service1",
        "0+")).thenReturn(expects0, expects999);

    CountDownLatch countDownLatch = new CountDownLatch(1000);
    AtomicInteger success = new AtomicInteger(0);
    for (int i = 0; i < 10; i++) {
      new Thread(() -> {
        for (int j = 0; j < 100; j++) {
          DiscoveryTreeNode result = discoveryTree.discovery(discoveryContext, "app", "service1", "0+");
          if (((Map<String, MicroserviceInstance>) result.data()).size() == 2) {
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
