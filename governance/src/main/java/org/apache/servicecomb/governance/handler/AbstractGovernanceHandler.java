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

import org.apache.servicecomb.governance.MatchersManager;
import org.apache.servicecomb.governance.event.GovernanceConfigurationChangedEvent;
import org.apache.servicecomb.governance.event.GovernanceEventManager;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.policy.AbstractPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.eventbus.Subscribe;

import io.micrometer.core.instrument.MeterRegistry;

public abstract class AbstractGovernanceHandler<PROCESSOR, POLICY extends AbstractPolicy> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGovernanceHandler.class);

  protected final DisposableMap<PROCESSOR> processors;

  private final Object lock = new Object();

  protected MatchersManager matchersManager;

  protected MeterRegistry meterRegistry;

  protected AbstractGovernanceHandler() {
    GovernanceEventManager.register(this);
    processors = new DisposableMap<>(this::onConfigurationChanged);
  }

  @Autowired
  public void setMatchersManager(MatchersManager matchersManager) {
    this.matchersManager = matchersManager;
  }

  @Autowired(required = false)
  public void setMeterRegistry(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  public PROCESSOR getActuator(GovernanceRequest governanceRequest) {
    POLICY policy = matchPolicy(governanceRequest);
    if (policy == null) {
      return null;
    }

    String key = createKey(governanceRequest, policy);
    if (key == null) {
      return null;
    }

    Disposable<PROCESSOR> processor = processors.get(key);
    if (processor == null) {
      synchronized (lock) {
        processor = processors.get(key);
        if (processor == null) {
          processor = createProcessor(key, governanceRequest, policy);
          processors.put(key, processor);
        }
      }
    }

    return processor.get();
  }

  protected abstract String createKey(GovernanceRequest governanceRequest, POLICY policy);

  protected abstract POLICY matchPolicy(GovernanceRequest governanceRequest);

  protected abstract Disposable<PROCESSOR> createProcessor(String key, GovernanceRequest governanceRequest,
      POLICY policy);

  protected void onConfigurationChanged(String key) {
    Disposable<PROCESSOR> processor = processors.remove(key);
    if (processor != null) {
      LOGGER.info("remove governance processor {}", key);
      processor.dispose();
    }
  }

  @Subscribe
  public void onDynamicConfigurationListener(GovernanceConfigurationChangedEvent event) {
    event.getChangedConfigurations().forEach(this::onConfigurationChanged);
  }
}
