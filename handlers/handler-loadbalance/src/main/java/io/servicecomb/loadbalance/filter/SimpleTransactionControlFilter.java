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

package io.servicecomb.loadbalance.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.netflix.loadbalancer.Server;

import io.servicecomb.loadbalance.Configuration;
import io.servicecomb.loadbalance.CseServer;

/**
 * 简单的分流filter
 * 策略：选择properties包含filter的所有options的所有实例，即filter的options为所选实例的properties的一个子集
 */
public class SimpleTransactionControlFilter extends TransactionControlFilter {

  @Override
  public List<Server> getFilteredListOfServers(List<Server> servers) {
    List<Server> filteredServers = new ArrayList<>();
    Map<String, String> filterOptions =
        Configuration.INSTANCE.getFlowsplitFilterOptions(getInvocation().getMicroserviceName());
    for (Server server : servers) {
      if (allowVisit((CseServer) server, filterOptions)) {
        filteredServers.add(server);
      }
    }
    return filteredServers;
  }

  protected boolean allowVisit(CseServer server, Map<String, String> filterOptions) {
    Map<String, String> propertiesMap = server.getInstance().getProperties();
    for (Entry<String, String> entry : filterOptions.entrySet()) {
      if (!entry.getValue().equals(propertiesMap.get(entry.getKey()))) {
        return false;
      }
    }
    return true;
  }
}
