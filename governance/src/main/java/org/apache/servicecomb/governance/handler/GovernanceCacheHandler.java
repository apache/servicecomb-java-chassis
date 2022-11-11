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

import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.policy.GovernanceCachePolicy;
import org.apache.servicecomb.governance.properties.GovernanceCacheProperties;
import org.apache.servicecomb.governance.service.GovernanceCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class GovernanceCacheHandler<K, V>
    extends AbstractGovernanceHandler<GovernanceCache<K, V>, GovernanceCachePolicy> {
  private static final Logger LOGGER = LoggerFactory.getLogger(GovernanceCacheHandler.class);

  private final GovernanceCacheProperties cacheProperties;

  public GovernanceCacheHandler(GovernanceCacheProperties cacheProperties) {
    this.cacheProperties = cacheProperties;
  }

  @Override
  public GovernanceCachePolicy matchPolicy(GovernanceRequest governanceRequest) {
    return matchersManager.match(governanceRequest, cacheProperties.getParsedEntity());
  }

  @Override
  protected String createKey(GovernanceRequest governanceRequest, GovernanceCachePolicy policy) {
    return cacheProperties.getConfigKey() + "." + policy.getName();
  }

  @Override
  protected Disposable<GovernanceCache<K, V>> createProcessor(String key, GovernanceRequest governanceRequest,
      GovernanceCachePolicy policy) {
    return getGovernanceCache(key, policy);
  }

  protected Disposable<GovernanceCache<K, V>> getGovernanceCache(String key, GovernanceCachePolicy policy) {
    LOGGER.info("applying new policy {} for {}", key, policy.toString());
    Cache<K,
        V> cache = CacheBuilder.newBuilder()
        .expireAfterWrite(Duration.parse(policy.getTtl()))
        .maximumSize(policy.getMaximumSize())
        .concurrencyLevel(policy.getConcurrencyLevel())
        .build();
    GovernanceCache<K, V> governanceCache = GovernanceCache.of(cache);
    return new DisposableHolder<>(key, governanceCache);
  }
}
