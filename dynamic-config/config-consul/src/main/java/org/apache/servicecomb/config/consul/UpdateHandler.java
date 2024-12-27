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

package org.apache.servicecomb.config.consul;

import org.apache.servicecomb.config.ConfigurationChangedEvent;
import org.apache.servicecomb.foundation.common.event.EventManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UpdateHandler {
  private final Map<String, Object> valueCache = new ConcurrentHashMap<>();

  public UpdateHandler() {
  }

  public void handle(Map<String, Object> current, Map<String, Object> last) {
    ConfigurationChangedEvent event = ConfigurationChangedEvent.createIncremental(current, last);
    valueCache.putAll(event.getAdded());
    valueCache.putAll(event.getUpdated());
    event.getDeleted().forEach((k, v) -> valueCache.remove(k));
    EventManager.post(event);
  }
}
