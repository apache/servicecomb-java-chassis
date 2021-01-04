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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicecomb.governance.event.ConfigurationChangedEvent;
import org.apache.servicecomb.governance.event.EventManager;

import com.google.common.eventbus.Subscribe;

public abstract class AbstractGovernanceHandler<PROCESSOR, POLICY> {
  private Map<String, PROCESSOR> map = new ConcurrentHashMap<>();

  protected AbstractGovernanceHandler() {
    EventManager.register(this);
  }

  public <R> PROCESSOR getActuator(POLICY policy) {
    String key = createKey(policy);
    PROCESSOR processor = map.get(key);
    if (processor == null) {
      processor = createProcessor(policy);
      map.put(key, processor);
    }
    return processor;
  }

  abstract protected String createKey(POLICY policy);

  abstract protected PROCESSOR createProcessor(POLICY policy);

  @Subscribe
  public void onDynamicConfigurationListener(ConfigurationChangedEvent event) {
    event.getChangedConfigurations().forEach(v -> map.remove(v));
  }
}
