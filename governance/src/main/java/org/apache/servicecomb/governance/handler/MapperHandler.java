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
import org.apache.servicecomb.governance.policy.MapperPolicy;
import org.apache.servicecomb.governance.processor.mapping.Mapper;
import org.apache.servicecomb.governance.properties.MapperProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapperHandler extends AbstractGovernanceHandler<Mapper, MapperPolicy> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitingHandler.class);

  private final MapperProperties mapperProperties;

  public MapperHandler(MapperProperties mapperProperties) {
    this.mapperProperties = mapperProperties;
  }

  @Override
  protected String createKey(GovernanceRequest governanceRequest, MapperPolicy policy) {
    return mapperProperties.getConfigKey() + "." + policy.getName();
  }

  @Override
  public MapperPolicy matchPolicy(GovernanceRequest governanceRequest) {
    return matchersManager.match(governanceRequest, mapperProperties.getParsedEntity());
  }

  @Override
  public Disposable<Mapper> createProcessor(String key, GovernanceRequest governanceRequest,
      MapperPolicy policy) {
    return getMapper(key, policy);
  }

  private Disposable<Mapper> getMapper(String key, MapperPolicy policy) {
    LOGGER.info("applying new policy {} for {}", key, policy.toString());

    return new DisposableHolder<>(key, Mapper.create(policy.getTarget()));
  }
}
