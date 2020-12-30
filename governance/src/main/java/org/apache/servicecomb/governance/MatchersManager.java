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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.governance.marker.GovHttpRequest;
import org.apache.servicecomb.governance.policy.AbstractPolicy;
import org.apache.servicecomb.governance.service.MatchersService;
import org.apache.servicecomb.governance.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MatchersManager {

  @Autowired
  private MatchersService matchersService;

  @Autowired
  private PolicyService policyService;

  @Autowired
  private InvocationContext invocationContext;

  public MatchersManager() {
  }

  public <T extends AbstractPolicy> T match(GovHttpRequest request, Map<String, T> policies) {
    List<T> matchedPolicy = new ArrayList<>();
    List<String> matchedKeys = new ArrayList<>();

    Map<String, Boolean> calculatedMatches = invocationContext.getCalculatedMatches();
    calculatedMatches.forEach((k, v) -> {
      if (v) {
        matchedKeys.add(k);
      }
    });

    for (Entry<String, T> entry : policies.entrySet()) {
      T policy = entry.getValue();

      if (policy.match(matchedKeys)) {
        matchedPolicy.add(policy);
        continue;
      }

      List<String> parsedMatches = policy.getParsedMatch();
      if (parsedMatches != null) {
        parsedMatches.stream().forEach(key -> {
          if (!calculatedMatches.containsKey(key)) {
            boolean keyMatch = matchersService.checkMatch(request, key);
            invocationContext.addMatch(key, keyMatch);
            if (keyMatch) {
              matchedPolicy.add(policy);
            }
          }
        });
      }
    }

    if (matchedPolicy.size() > 0) {
      matchedPolicy.sort(AbstractPolicy::compare);
      return matchedPolicy.get(0);
    }
    return null;
  }
}
