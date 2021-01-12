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
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.servicecomb.governance.handler.ext.RetryExtension;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.policy.RateLimitingPolicy;
import org.apache.servicecomb.governance.policy.RetryPolicy;
import org.apache.servicecomb.governance.properties.RetryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;

@Component
public class RetryHandler extends AbstractGovernanceHandler<Retry, RetryPolicy> {

  private static final Logger LOGGER = LoggerFactory.getLogger(RetryHandler.class);

  @Autowired
  private RetryProperties retryProperties;

  @Autowired
  private RetryExtension retryExtension;

  @Override
  protected String createKey(RetryPolicy policy) {
    return "servicecomb.retry." + policy.getName();
  }

  @Override
  public RetryPolicy matchPolicy(GovernanceRequest governanceRequest) {
    return matchersManager.match(governanceRequest, retryProperties.getParsedEntity());
  }

  @Override
  protected Retry createProcessor(RetryPolicy policy) {
    return getRetry(policy);
  }

  private Retry getRetry(RetryPolicy retryPolicy) {
    LOGGER.info("applying new policy: {}", retryPolicy.toString());

    List<Integer> statusList = Arrays.stream(retryPolicy.getRetryOnResponseStatus().split(","))
        .map(Integer::parseInt).collect(Collectors.toList());

    RetryConfig config = RetryConfig.custom()
        .maxAttempts(retryPolicy.getMaxAttempts())
        .retryOnResult(getPredicate(statusList))
        .retryExceptions(retryExtension.retryExceptions())
        .waitDuration(Duration.ofMillis(retryPolicy.getWaitDuration()))
        .build();

    RetryRegistry registry = RetryRegistry.of(config);
    return registry.retry(retryPolicy.getName());
  }

  private Predicate<Object> getPredicate(List<Integer> statusList) {
    return response -> retryExtension.isRetry(statusList, response);
  }
}
