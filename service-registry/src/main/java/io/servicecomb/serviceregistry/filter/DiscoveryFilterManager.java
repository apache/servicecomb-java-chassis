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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.foundation.common.cache.VersionedCache;
import io.servicecomb.foundation.common.utils.SPIServiceUtils;

// every microservice have one InstancesFilterManager instance
public class DiscoveryFilterManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryFilterManager.class);

  private List<DiscoveryFilter> filters = new ArrayList<>();

  public void loadFromSPI(Class<? extends DiscoveryFilter> cls) {
    filters.addAll(SPIServiceUtils.getSortedService(cls));
  }

  public void addFilter(DiscoveryFilter filter) {
    filters.add(filter);
  }

  // group name are qualifiedName
  // all leaf group will create a loadbalancer instance, groupName is loadBalancer key
  public void sort() {
    filters.sort((f1, f2) -> {
      return Integer.compare(f1.getOrder(), f2.getOrder());
    });

    for (DiscoveryFilter filter : filters) {
      LOGGER.info("DiscoveryFilter {}.", filter.getClass().getName());
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends VersionedCache> T filter(DiscoveryFilterContext context, VersionedCache newCache) {
    VersionedCache result = newCache;
    for (int idx = 0; idx < filters.size();) {
      DiscoveryFilter filter = filters.get(idx);
      context.setCurrentFilter(idx);

      VersionedCache input = result;
      result = filter.filter(context, result);
      if (!filter.isGroupingFilter()) {
        result.name(input.name());
      }

      if (result.isEmpty()) {
        // maybe need to rerun some filter
        int rerunIdx = context.popRerunFilter();
        if (rerunIdx != -1) {
          idx = rerunIdx;
          continue;
        }

        // no rerun support, go on even result is empty
        // because maybe some filter use other mechanism to create a instance(eg:domain name)
      }
      idx++;
    }
    return (T) result;
  }
}
