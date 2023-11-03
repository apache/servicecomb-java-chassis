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
package org.apache.servicecomb.router.match;

import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.apache.servicecomb.governance.marker.RequestProcessor;
import org.apache.servicecomb.router.cache.RouterRuleCache;
import org.apache.servicecomb.router.model.PolicyRuleItem;

public class RouterRuleMatcher {
  private final RouterRuleCache routerRuleCache;

  private final RequestProcessor requestProcessor;

  public RouterRuleMatcher(RouterRuleCache routerRuleCache, RequestProcessor requestProcessor) {
    this.routerRuleCache = routerRuleCache;
    this.requestProcessor = requestProcessor;
  }

  public PolicyRuleItem match(String serviceName, GovernanceRequestExtractor request) {
    for (PolicyRuleItem rule : routerRuleCache.getServiceInfoCacheMap().get(serviceName)
        .getAllrule()) {
      if (rule.getMatch() == null || requestProcessor.match(request, rule.getMatch())) {
        return rule;
      }
    }
    return null;
  }
}
