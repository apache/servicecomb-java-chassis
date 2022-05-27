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
import java.util.List;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.cache.InstanceCacheManager;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

public class TestDiscoveryTree {
  @Before
  public void before() {
    ConfigUtil.installDynamicConfig();
  }
  @After
  public void tearDown() {
    ArchaiusUtils.resetConfig();
  }

  DiscoveryTree discoveryTree = new DiscoveryTree();

  List<DiscoveryFilter> filters = Deencapsulation.getField(discoveryTree, "filters");

  DiscoveryContext context = new DiscoveryContext();

  DiscoveryTreeNode parent = new DiscoveryTreeNode().name("parent");

  DiscoveryTreeNode result;

  @Test
  public void loadFromSPI(@Mocked DiscoveryFilter f1, @Mocked DiscoveryFilter f2) {
    Class<? extends DiscoveryFilter> cls = DiscoveryFilter.class;
    new Expectations(SPIServiceUtils.class) {
      {
        SPIServiceUtils.getSortedService(cls);
        result = Arrays.asList(f1, f2);
      }
    };

    discoveryTree.loadFromSPI(cls);

    MatcherAssert.assertThat(filters, Matchers.contains(f1, f2));
  }

  @Test
  public void sort(@Mocked DiscoveryFilter f1, @Mocked DiscoveryFilter f2, @Mocked DiscoveryFilter f3) {
    new Expectations() {
      {
        f1.getOrder();
        result = -1;
        f1.enabled();
        result = true;
        f2.getOrder();
        result = 0;
        f2.enabled();
        result = true;
        f3.getOrder();
        result = 0;
        f3.enabled();
        result = false;
      }
    };
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
  public void easyDiscovery(@Mocked InstanceCacheManager instanceCacheManager) {
    new Expectations(DiscoveryManager.class) {
      {
        DiscoveryManager.INSTANCE.getInstanceCacheManager();
        result = instanceCacheManager;
        instanceCacheManager.getOrCreateVersionedCache(anyString, anyString, anyString);
        result = parent;
      }
    };

    result = discoveryTree.discovery(context, null, null, null);
    Assertions.assertEquals(parent.name(), result.name());
    Assertions.assertEquals(parent.cacheVersion(), result.cacheVersion());
  }

  @Test
  public void discovery_filterReturnNull(@Mocked InstanceCacheManager instanceCacheManager) {
    new Expectations(DiscoveryManager.class) {
      {
        DiscoveryManager.INSTANCE.getInstanceCacheManager();
        result = instanceCacheManager;
        instanceCacheManager.getOrCreateVersionedCache(anyString, anyString, anyString);
        result = parent;
      }
    };

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
            () -> {
              result = discoveryTree.discovery(context, null, null, null);
            });
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
    Deencapsulation.setField(discoveryTree, "root", parent.cacheVersion(1));
    Assertions.assertTrue(parent.children().isEmpty());

    discoveryTree.discovery(context, new VersionedCache().cacheVersion(0).name("input"));
    Assertions.assertTrue(parent.children().isEmpty());
  }

  @Test
  public void getOrCreateRoot_match() {
    Deencapsulation.setField(discoveryTree, "root", parent);

    DiscoveryTreeNode root = discoveryTree.getOrCreateRoot(parent);

    Assertions.assertSame(parent, root);
  }

  @Test
  public void getOrCreateRoot_expired() {
    Deencapsulation.setField(discoveryTree, "root", parent);

    VersionedCache inputCache = new VersionedCache().cacheVersion(parent.cacheVersion() + 1);
    DiscoveryTreeNode root = discoveryTree.getOrCreateRoot(inputCache);

    Assertions.assertEquals(inputCache.cacheVersion(), root.cacheVersion());
    Assertions.assertSame(Deencapsulation.getField(discoveryTree, "root"), root);
  }

  @Test
  public void getOrCreateRoot_tempRoot() {
    Deencapsulation.setField(discoveryTree, "root", parent);

    VersionedCache inputCache = new VersionedCache().cacheVersion(parent.cacheVersion() - 1);
    DiscoveryTreeNode root = discoveryTree.getOrCreateRoot(inputCache);

    Assertions.assertEquals(inputCache.cacheVersion(), root.cacheVersion());
    Assertions.assertNotSame(Deencapsulation.getField(discoveryTree, "root"), root);
  }
}
