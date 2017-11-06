/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.serviceregistry.filter;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.foundation.common.cache.VersionedCache;
import io.servicecomb.foundation.common.utils.SPIServiceUtils;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

public class TestDiscoveryFilterManager {
  DiscoveryFilterManager manager = new DiscoveryFilterManager();

  List<DiscoveryFilter> filters = Deencapsulation.getField(manager, "filters");

  @Test
  public void loadFromSPI(@Mocked DiscoveryFilter f1, @Mocked DiscoveryFilter f2) {
    Class<? extends DiscoveryFilter> cls = DiscoveryFilter.class;
    new Expectations(SPIServiceUtils.class) {
      {
        SPIServiceUtils.getSortedService(cls);
        result = Arrays.asList(f1, f2);
      }
    };

    manager.loadFromSPI(cls);

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

    manager.addFilter(f2);
    manager.addFilter(f1);
    manager.sort();

    Assert.assertThat(filters, Matchers.contains(f1, f2));
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
    public VersionedCache filter(DiscoveryFilterContext context, VersionedCache inputCache) {
      VersionedCache result = new VersionedCache().cacheVersion(inputCache);
      if (groupName != null) {
        result.subName(inputCache, groupName);
      }
      return result;
    }
  }

  @Test
  public void filterNormal() {
    DiscoveryFilterContext context = new DiscoveryFilterContext();
    VersionedCache inputCache = new VersionedCache().name("1.0.0-2.0.0");

    manager.addFilter(new DiscoveryFilterForTest("g1"));
    manager.addFilter(new DiscoveryFilterForTest(null));
    manager.addFilter(new DiscoveryFilterForTest("g2"));
    manager.addFilter(new DiscoveryFilterForTest(null));

    VersionedCache result = manager.filter(context, inputCache);

    Assert.assertEquals(inputCache.cacheVersion(), result.cacheVersion());
    Assert.assertEquals("1.0.0-2.0.0/g1/g2", result.name());
  }

  @Test
  public void filterRerun() {
    DiscoveryFilterContext context = new DiscoveryFilterContext();
    VersionedCache inputCache = new VersionedCache().name("1.0.0-2.0.0");

    manager.addFilter(new DiscoveryFilterForTest("g1") {
      @Override
      public VersionedCache filter(DiscoveryFilterContext context, VersionedCache inputCache) {
        if (context.getContextParameter("step") == null) {
          context.pushRerunFilter();
          context.putContextParameter("step", 1);
          return new VersionedCache().cacheVersion(inputCache).name(groupName).data("first");
        }

        return new VersionedCache().cacheVersion(inputCache).name(groupName).data("second");
      }
    });
    manager.addFilter(new DiscoveryFilterForTest(null) {
      @Override
      public VersionedCache filter(DiscoveryFilterContext context, VersionedCache inputCache) {
        if ("first".equals(inputCache.data())) {
          return new VersionedCache().cacheVersion(inputCache);
        }

        return inputCache;
      }
    });

    VersionedCache result = manager.filter(context, inputCache);

    Assert.assertEquals(inputCache.cacheVersion(), result.cacheVersion());
    Assert.assertEquals("second", result.data());
  }
}
