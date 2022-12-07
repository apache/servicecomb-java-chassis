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

package org.apache.servicecomb.governance;

import org.apache.servicecomb.governance.handler.LoadBalanceHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.processor.loadbanlance.LoadBalance;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {GovernanceConfiguration.class, MockConfiguration.class})
public class LoadBalancerTest {
  private LoadBalanceHandler loadBalanceHandler;

  @Autowired
  public void setLoadBalanceHandler(LoadBalanceHandler loadBalanceHandler) {
    this.loadBalanceHandler = loadBalanceHandler;
  }

  public LoadBalancerTest() {

  }

  @Test
  public void test_loadbalance_random() {
    GovernanceRequest request = new GovernanceRequest();
    request.setUri("/loadrandom");
    request.setServiceName("loadrandom");
    LoadBalance loadBalance = loadBalanceHandler.getActuator(request);
    Assert.assertEquals("Random", loadBalance.getRule());
  }

  @Test
  public void test_loadbalance_roundRobin() {
    GovernanceRequest request = new GovernanceRequest();
    request.setUri("/loadroundRobin");
    request.setServiceName("loadroundRobin");
    LoadBalance loadBalance = loadBalanceHandler.getActuator(request);
    Assert.assertEquals("RoundRobin", loadBalance.getRule());
  }
}
