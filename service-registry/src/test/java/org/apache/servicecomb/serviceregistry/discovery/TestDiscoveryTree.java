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

package org.apache.servicecomb.serviceregistry.discovery;

import java.util.Arrays;
import java.util.List;

import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.cache.InstanceCacheManager;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

public class TestDiscoveryTree {
  DiscoveryTree discoveryTree = new DiscoveryTree();

  List<DiscoveryFilter> filters = Deencapsulation.getField(discoveryTree, "filters");

  DiscoveryContext context = new DiscoveryContext();

  DiscoveryTreeNode parent = new DiscoveryTreeNode().name("parent");

  DiscoveryTreeNode result;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

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

    Assert.assertThat(filters, Matchers.contains(f1, f2));
  }

  @Test
  public void sort(@Mocked DiscoveryFilter f1, @Mocked DiscoveryFilter f2) {
    new Expectations() {
      {
        f1.getOrder();
        result = -1;
        f2.getOrder();
        result = 0;
      }
    };

    discoveryTree.addFilter(f2);
    discoveryTree.addFilter(f1);
    discoveryTree.sort();

    Assert.assertThat(filters, Matchers.contains(f1, f2));
  }

  @Test
  public void isExpired_existingNull() {
    Assert.assertTrue(discoveryTree.isExpired(null, null));
  }

  @Test
  public void isExpired_yes() {
    parent.cacheVersion(1);
    Assert.assertTrue(discoveryTree.isExpired(new DiscoveryTreeNode().cacheVersion(0), parent));
  }

  @Test
  public void isExpired_no() {
    parent.cacheVersion(0);
    Assert.assertFalse(discoveryTree.isExpired(new DiscoveryTreeNode().cacheVersion(0), parent));
  }

  class DiscoveryFilterForTest implements DiscoveryFilter {
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

    Assert.assertEquals("1.0.0-2.0.0/g1/g2", result.name());
  }

  @Test
  public void easyDiscovery(@Mocked ServiceRegistry serviceRegistry,
      @Mocked InstanceCacheManager instanceCacheManager) {
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getServiceRegistry();
        result = serviceRegistry;
        serviceRegistry.getInstanceCacheManager();
        result = instanceCacheManager;
        instanceCacheManager.getOrCreateVersionedCache(anyString, anyString, anyString);
        result = parent;
      }
    };

    result = discoveryTree.discovery(context, null, null, null);
    Assert.assertEquals(parent.name(), result.name());
    Assert.assertEquals(parent.cacheVersion(), result.cacheVersion());
  }

  @Test
  public void discovery_filterReturnNull(@Mocked ServiceRegistry serviceRegistry,
      @Mocked InstanceCacheManager instanceCacheManager) {
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getServiceRegistry();
        result = serviceRegistry;
        serviceRegistry.getInstanceCacheManager();
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

    expectedException.expect(ServiceCombException.class);
    expectedException.expectMessage(Matchers.is(filter.getClass().getName() + " discovery return null."));

    result = discoveryTree.discovery(context, null, null, null);
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

    Assert.assertEquals("second", result.data());
  }

  @Test
  public void avoidConcurrentProblem() {
    Deencapsulation.setField(discoveryTree, "root", parent.cacheVersion(1));
    Assert.assertTrue(parent.children().isEmpty());

    discoveryTree.discovery(context, new VersionedCache().cacheVersion(0).name("input"));
    Assert.assertTrue(parent.children().isEmpty());
  }
}
