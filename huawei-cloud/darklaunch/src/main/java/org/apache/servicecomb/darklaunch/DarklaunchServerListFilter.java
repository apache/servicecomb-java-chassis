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

package org.apache.servicecomb.darklaunch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.loadbalance.ServerListFilterExt;
import org.apache.servicecomb.loadbalance.ServiceCombServer;

public class DarklaunchServerListFilter implements ServerListFilterExt {

  private static final String POLICY_SERVICE_COMB = "servicecomb.darklaunch.policy.%s";

  private static final int HUNDRED = 100;

  private final Random random = new Random();

  public DarklaunchServerListFilter() {
  }

  @Override
  public int getOrder() {
    return HUNDRED;
  }

  @Override
  public boolean enabled() {
    return true;
  }

  @Override
  public List<ServiceCombServer> getFilteredListOfServers(List<ServiceCombServer> serverList, Invocation invocation) {
    DarklaunchRule rule = DarklaunchRule.parse(LegacyPropertyFactory
        .getStringProperty(String.format(POLICY_SERVICE_COMB, invocation.getMicroserviceName())));
    if (rule == null) {
      return serverList;
    }
    List<ServiceCombServer> defaultGroup = new ArrayList<>();
    divideServerGroup(serverList, rule, defaultGroup);

    if (rule.getPolicyType() == PolicyType.RULE) {
      for (DarklaunchRuleItem item : rule.getRuleItems()) {
        List<ServiceCombServer> ruleServers = getRuleServers(invocation, item, defaultGroup);
        if (ruleServers != null) {
          return ruleServers;
        }
      }
    } else {
      int rate = random.nextInt(HUNDRED);
      for (DarklaunchRuleItem item : rule.getRuleItems()) {
        item.getPolicyCondition().setActual(DarklaunchRule.PROP_PERCENT, rate);
        if (item.getPolicyCondition().match()) {
          if (item.getServers().isEmpty()) {
            return defaultGroup;
          }
          return item.getServers();
        }
        rate = rate - Integer.parseInt(item.getPolicyCondition().expected());
      }
    }
    return defaultGroup;
  }

  private List<ServiceCombServer> getRuleServers(Invocation invocation, DarklaunchRuleItem item,
      List<ServiceCombServer> defaultGroup) {
    invocation.getSwaggerArguments().forEach((k, v) -> item.getPolicyCondition().setActual(k, v));
    for (String key : invocation.getContext().keySet()) {
      item.getPolicyCondition().setActual(key, invocation.getContext(key));
    }
    if (item.getPolicyCondition().match()) {
      if (item.getServers().isEmpty()) {
        return defaultGroup;
      }
      return item.getServers();
    }
    return null;
  }

  private void divideServerGroup(List<ServiceCombServer> serverList, DarklaunchRule rule,
      List<ServiceCombServer> defaultGroup) {
    for (ServiceCombServer server : serverList) {
      boolean hasGroup = false;
      for (DarklaunchRuleItem item : rule.getRuleItems()) {
        item.getGroupCondition()
            .setActual(DarklaunchRule.PROP_VERSION, server.getInstance().getVersion());
        if (item.getGroupCondition().match()) {
          item.addServer(server);
          hasGroup = true;
        }
      }
      if (!hasGroup) {
        defaultGroup.add(server);
      }
    }
  }
}
