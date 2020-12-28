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
import java.util.function.Function;

import com.google.common.eventbus.Subscribe;
import org.apache.servicecomb.governance.event.ConfigurationChangedEvent;
import org.apache.servicecomb.governance.event.EventManager;

public abstract class AbstractGovHandler<PROCESSOR> implements GovHandler {
  private Map<String, PROCESSOR> map = new ConcurrentHashMap<>();

  protected AbstractGovHandler() {
    EventManager.register(this);
  }

  protected <R> PROCESSOR getActuator(String key, R policy, Function<R, PROCESSOR> func) {
    PROCESSOR processor = map.get(key);
    if (processor == null) {
      processor = func.apply(policy);
      map.put(key, processor);
    }
    return processor;
  }

  @Subscribe
  public void onDynamicConfigurationListener(ConfigurationChangedEvent event) {
    event.getChangedConfigurations().forEach(v -> map.remove(v));
  }
}
