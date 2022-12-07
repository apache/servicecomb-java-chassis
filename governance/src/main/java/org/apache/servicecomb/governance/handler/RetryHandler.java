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

import org.apache.servicecomb.governance.handler.ext.AbstractRetryExtension;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.policy.RetryPolicy;
import org.apache.servicecomb.governance.properties.RetryProperties;
import org.apache.servicecomb.governance.utils.GovernanceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.micrometer.tagged.RetryMetricNames;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;

public class RetryHandler extends AbstractGovernanceHandler<Retry, RetryPolicy> {

  private static final Logger LOGGER = LoggerFactory.getLogger(RetryHandler.class);

  private final RetryProperties retryProperties;

  private final AbstractRetryExtension retryExtension;

  public RetryHandler(RetryProperties retryProperties, AbstractRetryExtension retryExtension) {
    this.retryProperties = retryProperties;
    this.retryExtension = retryExtension;
  }

  @Override
  protected String createKey(GovernanceRequest governanceRequest, RetryPolicy policy) {
    return this.retryProperties.getConfigKey() + "." + policy.getName();
  }

  @Override
  public RetryPolicy matchPolicy(GovernanceRequest governanceRequest) {
    return matchersManager.match(governanceRequest, retryProperties.getParsedEntity());
  }

  @Override
  public Disposable<Retry> createProcessor(String key, GovernanceRequest governanceRequest, RetryPolicy policy) {
    return getRetry(key, policy);
  }

  private Disposable<Retry> getRetry(String key, RetryPolicy retryPolicy) {
    LOGGER.info("applying new policy {} for {}", key, retryPolicy.toString());

    RetryConfig config = RetryConfig.custom()
        .maxAttempts(retryPolicy.getMaxAttempts() + 1)
        .retryOnResult(response -> retryExtension.isFailedResult(retryPolicy.getRetryOnResponseStatus(), response))
        .retryOnException(retryExtension::isFailedResult)
        .intervalFunction(getIntervalFunction(retryPolicy))
        .failAfterMaxAttempts(retryPolicy.isFailAfterMaxAttempts())
        .build();

    RetryRegistry registry = RetryRegistry.of(config);
    if (meterRegistry != null) {
      TaggedRetryMetrics
          .ofRetryRegistry(RetryMetricNames.custom()
                  .callsMetricName(this.retryProperties.getConfigKey() + ".calls")
                  .build(),
              registry)
          .bindTo(meterRegistry);
    }
    return new DisposableRetry(key, registry, registry.retry(key));
  }

  private IntervalFunction getIntervalFunction(RetryPolicy retryPolicy) {
    if (GovernanceUtils.STRATEGY_RANDOM_BACKOFF.equals(retryPolicy.getRetryStrategy())) {
      return IntervalFunction.ofExponentialRandomBackoff(Duration.parse(retryPolicy.getInitialInterval()),
          retryPolicy.getMultiplier(), retryPolicy.getRandomizationFactor());
    }
    return IntervalFunction.of(Duration.parse(retryPolicy.getWaitDuration()));
  }
}
