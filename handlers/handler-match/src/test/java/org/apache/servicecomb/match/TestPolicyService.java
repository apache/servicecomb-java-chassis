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
package org.apache.servicecomb.match;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.match.policy.GovRule;
import org.apache.servicecomb.match.policy.RateLimitingPolicy;
import org.apache.servicecomb.match.policy.RetryPolicy;
import org.apache.servicecomb.match.propertirs.RateLimitProperties;
import org.apache.servicecomb.match.propertirs.RetryProperties;
import org.apache.servicecomb.match.service.PolicyService;
import org.apache.servicecomb.match.service.PolicyServiceImpl;
import org.junit.Assert;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

public class TestPolicyService {

  private PolicyService policyService = new PolicyServiceImpl();

  private Map<String, RetryPolicy> getMockRetry() {
    Map<String, RetryPolicy> map = new HashMap<>();
    RetryPolicy retryPolicy = new RetryPolicy();
    GovRule govRule = new GovRule();
    govRule.setMatch("demo-group.xxx");
    retryPolicy.setRules(govRule);
    retryPolicy.setMaxAttempts(3);
    map.put("demo-retry", retryPolicy);
    return map;
  }

  @Test
  public void testGetRetryPolicy(@Mocked RetryProperties retryProperties) {
    new Expectations() {
      {
        retryProperties.covert();
        result = getMockRetry();
      }
    };
    RetryPolicy policy = (RetryPolicy) policyService.getCustomPolicy("Retry", "demo-group.xxx");
    Assert.assertEquals(3L, (long) policy.getMaxAttempts());
  }


  private Map<String, RateLimitingPolicy> getRateLimiting() {
    Map<String, RateLimitingPolicy> map = new HashMap<>();
    RateLimitingPolicy retryPolicy = new RateLimitingPolicy();
    GovRule govRule = new GovRule();
    govRule.setMatch("demo-group.xxx");
    retryPolicy.setRules(govRule);
    retryPolicy.setRate(12);
    map.put("demo-ratelimit", retryPolicy);
    return map;
  }

  @Test
  public void testGetRateLimitingPolicy(@Mocked RateLimitProperties rateLimitProperties) {
    new Expectations() {
      {
        rateLimitProperties.covert();
        result = getRateLimiting();
      }
    };
    RateLimitingPolicy policy = (RateLimitingPolicy) policyService.getCustomPolicy("RateLimiting", "demo-group.xxx");
    Assert.assertEquals(12L, (long) policy.getRate());
  }
}
