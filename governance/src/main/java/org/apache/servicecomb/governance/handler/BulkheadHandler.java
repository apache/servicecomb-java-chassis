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
import org.apache.servicecomb.governance.policy.BulkheadPolicy;
import org.apache.servicecomb.governance.properties.BulkheadProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.micrometer.tagged.BulkheadMetricNames;
import io.github.resilience4j.micrometer.tagged.TaggedBulkheadMetrics;

public class BulkheadHandler extends AbstractGovernanceHandler<Bulkhead, BulkheadPolicy> {
  private static final Logger LOGGER = LoggerFactory.getLogger(BulkheadHandler.class);

  private final BulkheadProperties bulkheadProperties;

  public BulkheadHandler(BulkheadProperties bulkheadProperties) {
    this.bulkheadProperties = bulkheadProperties;
  }

  @Override
  protected String createKey(GovernanceRequest governanceRequest, BulkheadPolicy policy) {
    return bulkheadProperties.getConfigKey() + "." + policy.getName();
  }

  @Override
  public BulkheadPolicy matchPolicy(GovernanceRequest governanceRequest) {
    return matchersManager.match(governanceRequest, bulkheadProperties.getParsedEntity());
  }

  @Override
  public Disposable<Bulkhead> createProcessor(String key, GovernanceRequest governanceRequest, BulkheadPolicy policy) {
    return getBulkhead(key, policy);
  }

  private Disposable<Bulkhead> getBulkhead(String key, BulkheadPolicy policy) {
    LOGGER.info("applying new policy {} for {}", key, policy.toString());

    BulkheadConfig config = BulkheadConfig.custom()
        .maxConcurrentCalls(policy.getMaxConcurrentCalls())
        .maxWaitDuration(Duration.parse(policy.getMaxWaitDuration()))
        .build();

    BulkheadRegistry registry = BulkheadRegistry.of(config);
    if (meterRegistry != null) {
      TaggedBulkheadMetrics
          .ofBulkheadRegistry(BulkheadMetricNames.custom()
              .availableConcurrentCallsMetricName(bulkheadProperties.getConfigKey() + ".available.concurrent.calls")
              .maxAllowedConcurrentCallsMetricName(
                  bulkheadProperties.getConfigKey() + ".max.allowed.concurrent.calls").build(), registry)
          .bindTo(meterRegistry);
    }
    return new DisposableBulkhead(key, registry, registry.bulkhead(key));
  }
}
