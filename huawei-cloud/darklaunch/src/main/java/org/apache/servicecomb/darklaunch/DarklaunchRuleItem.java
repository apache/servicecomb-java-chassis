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

import org.apache.servicecomb.darklaunch.oper.Condition;
import org.apache.servicecomb.loadbalance.ServiceCombServer;

public class DarklaunchRuleItem {
  private String groupName;

  private Condition groupCondition;

  private Condition policyCondition;

  private final List<ServiceCombServer> servers = new ArrayList<>();

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
