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

package org.apache.servicecomb.core.governance;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.servicecomb.config.event.RefreshGovernanceConfigurationEvent;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.governance.event.GovernanceConfigurationChangedEvent;
import org.apache.servicecomb.governance.event.GovernanceEventManager;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.Subscribe;

@Component
public class ServiceCombConfigurationEventAdapter {
  public ServiceCombConfigurationEventAdapter() {
    EventManager.register(this);
  }

  @Subscribe
  public void onConfigurationChangedEvent(RefreshGovernanceConfigurationEvent event) {
    Set<String> changedKeys = new HashSet<>();
    addMap(changedKeys, event.getEvent().getAdded());
    addMap(changedKeys, event.getEvent().getDeleted());
    addMap(changedKeys, event.getEvent().getChanged());
    addMap(changedKeys, event.getEvent().getComplete());
    GovernanceConfigurationChangedEvent newEvent = new GovernanceConfigurationChangedEvent(changedKeys);
    GovernanceEventManager.post(newEvent);
  }

  private void addMap(Set<String> keys, Map<String, Object> changed) {
    if (changed != null) {
      keys.addAll(changed.keySet());
    }
  }
}
