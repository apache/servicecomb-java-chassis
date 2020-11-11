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
package org.apache.servicecomb.match.service;

import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.match.policy.AbstractPolicy;
import org.apache.servicecomb.match.policy.Policy;
import org.apache.servicecomb.match.propertirs.RateLimitProperties;
import org.apache.servicecomb.match.propertirs.RetryProperties;

public class PolicyServiceImpl implements PolicyService {

  private static final String MATCH_NONE = "none";

  private final RateLimitProperties rateLimitProperties = new RateLimitProperties();

  private final RetryProperties retryProperties = new RetryProperties();

  @Override
  public Policy getCustomPolicy(String kind, String mark) {
    if (StringUtils.isEmpty(mark)) {
      return null;
    }
    switch (kind) {
      case "RateLimiting":
        return match(rateLimitProperties.covert(), mark);
      case "Retry":
        return match(retryProperties.covert(), mark);
      default:
        return null;
    }
  }

  private <T extends AbstractPolicy> Policy match(Map<String, T> policies, String mark) {
    List<String> marks = Arrays.asList(mark.split(","));
    return match(policies, marks);
  }

  private <T extends AbstractPolicy> Policy match(Map<String, T> policies, List<String> marks) {
    AbstractPolicy policyResult;
    AbstractPolicy defaultPolicy = null;
    for (Entry<String, T> entry : policies.entrySet()) {
      if (entry.getValue().getRules().getMatch().equals(MATCH_NONE)) {
        defaultPolicy = entry.getValue();
        defaultPolicy.setName(entry.getKey());
      }
      if (entry.getValue().match(marks)) {
        policyResult = entry.getValue();
        policyResult.setName(entry.getKey());
        return policyResult;
      }
    }
    return defaultPolicy;
  }
}
