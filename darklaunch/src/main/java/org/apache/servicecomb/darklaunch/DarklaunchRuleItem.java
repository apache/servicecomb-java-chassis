package org.apache.servicecomb.darklaunch;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.darklaunch.oper.Condition;
import org.apache.servicecomb.loadbalance.ServiceCombServer;

public class DarklaunchRuleItem {
  private String groupName;

  private Condition groupCondition;

  private Condition policyCondition;

  private List<ServiceCombServer> servers = new ArrayList<ServiceCombServer>();

  public DarklaunchRuleItem(String groupName) {
    this.groupName = groupName;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public Condition getGroupCondition() {
    return groupCondition;
  }

  public void setGroupCondition(Condition groupCondition) {
    this.groupCondition = groupCondition;
  }

  public Condition getPolicyCondition() {
    return policyCondition;
  }

  public void setPolicyCondition(Condition policyCondition) {
    this.policyCondition = policyCondition;
  }

  public List<ServiceCombServer> getServers() {
    return this.servers;
  }

  public void addServer(ServiceCombServer server) {
    this.servers.add(server);
  }
}
