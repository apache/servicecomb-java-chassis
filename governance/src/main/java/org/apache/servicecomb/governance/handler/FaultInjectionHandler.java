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

package org.apache.servicecomb.governance.handler;

import java.time.Duration;

import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.policy.FaultInjectionPolicy;
import org.apache.servicecomb.governance.properties.FaultInjectionProperties;
import org.apache.servicecomb.injection.FaultInjectionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FaultInjectionHandler extends AbstractGovernanceHandler<FaultInjectionPolicy, FaultInjectionPolicy> {
  private static final Logger LOGGER = LoggerFactory.getLogger(FaultInjectionHandler.class);

  @Autowired
  private FaultInjectionProperties faultInjectionProperties;

  @Override
  protected String createKey(FaultInjectionPolicy policy) {
    return "servicecomb.faultInjection." + policy.getName();
  }

  @Override
  public FaultInjectionPolicy matchPolicy(GovernanceRequest governanceRequest) {
    return matchersManager.match(governanceRequest, faultInjectionProperties.getParsedEntity());
  }

  @Override
  protected FaultInjectionPolicy createProcessor(FaultInjectionPolicy policy) {
    return policy;
  }


  private FaultInjectionConfig getFaultInjection(FaultInjectionPolicy policy) {
    LOGGER.info("applying new policy: {}", policy.toString());

    FaultInjectionConfig config;
    config = FaultInjectionConfig.custom()
        .setType(policy.getType())
        .setDelayTime(Duration.parse(policy.getDelayTime()))
        .setPercentage(policy.getPercentage())
        .setResponseStatus(policy.getErrorCode())
        .build();
    return config;
  }
}
