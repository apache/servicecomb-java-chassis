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

package org.apache.servicecomb.loadbalance.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.loadbalance.Configuration;
import org.apache.servicecomb.loadbalance.CseServer;

import com.netflix.loadbalancer.Server;

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
