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

import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.policy.AbstractPolicy;
import org.apache.servicecomb.governance.service.MatchersService;

public class MatchersManager {
  private MatchersService matchersService;

  public MatchersManager(MatchersService matchersService) {
    this.matchersService = matchersService;
  }

  public <T extends AbstractPolicy> T match(GovernanceRequest request, Map<String, T> policies) {
    List<T> sortPolicies = new ArrayList<>(policies.size());
    sortPolicies.addAll(policies.values());
    sortPolicies.sort(T::compareTo);

    for (T policy : sortPolicies) {
      if (matchersService.checkMatch(request, policy.getName())) {
        return policy;
      }
    }
    return null;
  }
}
