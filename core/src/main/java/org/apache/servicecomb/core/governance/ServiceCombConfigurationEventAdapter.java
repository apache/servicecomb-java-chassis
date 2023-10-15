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


import org.apache.servicecomb.config.ConfigurationChangedEvent;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.governance.event.GovernanceConfigurationChangedEvent;
import org.apache.servicecomb.governance.event.GovernanceEventManager;

import com.google.common.eventbus.Subscribe;

public class ServiceCombConfigurationEventAdapter {
  public ServiceCombConfigurationEventAdapter() {
    EventManager.register(this);
  }

  @Subscribe
  public void onConfigurationChangedEvent(ConfigurationChangedEvent event) {
    GovernanceConfigurationChangedEvent newEvent = new GovernanceConfigurationChangedEvent(event.getChanged());
    GovernanceEventManager.post(newEvent);
  }
}
