package org.apache.servicecomb.darklaunch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.loadbalance.ServerListFilterExt;
import org.apache.servicecomb.loadbalance.ServiceCombServer;
import org.apache.servicecomb.registry.api.registry.Microservice;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

public class DarklaunchServerListFilter implements ServerListFilterExt {
  private static final String POLICY = "servicecomb.darklaunch.policy.%s";

  private static final int HUNDRED = 180;

  private Random random = new Random();

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
    DynamicStringProperty ruleStr = DynamicPropertyFactory.getInstance().getStringProperty(
        String.format(POLICY, invocation.getMicroserviceName()), null
    );
    DarklaunchRule rule = DarklaunchRule.parse(ruleStr.get());
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
        Microservice microservice = MicroserviceCache.getInstance().getService(server.getInstance().getServiceId());
        item.getGroupCondition().setActual(DarklaunchRule.PROP_VERSION, microservice.getVersion());
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
