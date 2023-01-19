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

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.apache.servicecomb.governance.policy.BulkheadPolicy;
import org.apache.servicecomb.governance.properties.InstanceBulkheadProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.micrometer.tagged.BulkheadMetricNames;
import io.github.resilience4j.micrometer.tagged.TaggedBulkheadMetrics;

public class InstanceBulkheadHandler extends AbstractGovernanceHandler<Bulkhead, BulkheadPolicy> {
  private static final Logger LOGGER = LoggerFactory.getLogger(InstanceBulkheadHandler.class);

  private final InstanceBulkheadProperties bulkheadProperties;

  public InstanceBulkheadHandler(InstanceBulkheadProperties bulkheadProperties) {
    this.bulkheadProperties = bulkheadProperties;
  }

  @Override
  protected String createKey(GovernanceRequestExtractor requestExtractor, BulkheadPolicy policy) {
    return this.bulkheadProperties.getConfigKey()
        + "." + policy.getName()
        + "." + requestExtractor.serviceName()
        + "." + requestExtractor.instanceId();
  }

  @Override
  protected void onConfigurationChanged(String key) {
    if (key.startsWith(this.bulkheadProperties.getConfigKey())) {
      for (String processorKey : processors.keySet()) {
        if (processorKey.startsWith(key)) {
          Disposable<Bulkhead> processor = processors.remove(processorKey);
          if (processor != null) {
            LOGGER.info("remove instance bulkhead processor {}", key);
            processor.dispose();
          }
        }
      }
    }
  }

  @Override
  public BulkheadPolicy matchPolicy(GovernanceRequestExtractor requestExtractor) {
    if (StringUtils.isEmpty(requestExtractor.serviceName()) || StringUtils.isEmpty(
        requestExtractor.instanceId())) {
      LOGGER.info("Instance bulkhead is not properly configured, service id or instance id is empty.");
      return null;
    }
    return matchersManager.match(requestExtractor, bulkheadProperties.getParsedEntity());
  }

  @Override
  public Disposable<Bulkhead> createProcessor(String key, GovernanceRequestExtractor requestExtractor, BulkheadPolicy policy) {
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
                  .availableConcurrentCallsMetricName(
                      this.bulkheadProperties.getConfigKey() + ".available.concurrent.calls")
                  .maxAllowedConcurrentCallsMetricName(
                      this.bulkheadProperties.getConfigKey() + ".max.allowed.concurrent.calls").build(),
              registry)
          .bindTo(meterRegistry);
    }
    return new DisposableBulkhead(key, registry, registry.bulkhead(key, config));
  }
}
