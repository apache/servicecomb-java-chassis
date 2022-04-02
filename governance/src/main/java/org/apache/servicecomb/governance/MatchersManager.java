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

import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.policy.AbstractPolicy;
import org.apache.servicecomb.governance.service.MatchersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MatchersManager {
  private MatchersService matchersService;

  private InvocationContext invocationContext;

  @Autowired
  public MatchersManager(MatchersService matchersService, InvocationContext invocationContext) {
    this.matchersService = matchersService;
    this.invocationContext = invocationContext;
  }

  public MatchersManager() {
  }

  public <T extends AbstractPolicy> T match(GovernanceRequest request, Map<String, T> policies) {
    Map<String, Boolean> calculatedMatches = invocationContext.getCalculatedMatches();

    for (Entry<String, T> entry : policies.entrySet()) {
      T policy = entry.getValue();

      if (calculatedMatches.containsKey(entry.getKey())) {
        return policy;
      }

      boolean keyMatch = matchersService.checkMatch(request, entry.getKey());
      invocationContext.addMatch(entry.getKey(), keyMatch);
      if (keyMatch) {
        return policy;
      }
    }
    return null;
  }
}
