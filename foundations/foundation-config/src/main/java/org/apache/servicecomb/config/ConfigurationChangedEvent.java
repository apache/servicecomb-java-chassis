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

package org.apache.servicecomb.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ConfigurationChangedEvent {
  private final Map<String, Object> added;

  private final Map<String, Object> deleted;

  private final Map<String, Object> updated;

  private Set<String> changed;

  private ConfigurationChangedEvent(Map<String, Object> added, Map<String, Object> updated,
      Map<String, Object> deleted) {
    this.added = added;
    this.deleted = deleted;
    this.updated = updated;
    this.changed = new HashSet<>();
    this.changed.addAll(added.keySet());
    this.changed.addAll(updated.keySet());
    this.changed.addAll(deleted.keySet());
  }

  public static ConfigurationChangedEvent createIncremental(Map<String, Object> latest, Map<String, Object> last) {
    Map<String, Object> itemsCreated = new HashMap<>();
    Map<String, Object> itemsDeleted = new HashMap<>();
    Map<String, Object> itemsModified = new HashMap<>();

    for (Map.Entry<String, Object> entry : latest.entrySet()) {
      String itemKey = entry.getKey();
      if (!last.containsKey(itemKey)) {
        itemsCreated.put(itemKey, entry.getValue());
      } else if (!Objects.equals(last.get(itemKey), latest.get(itemKey))) {
        itemsModified.put(itemKey, entry.getValue());
      }
    }
    for (String itemKey : last.keySet()) {
      if (!latest.containsKey(itemKey)) {
        itemsDeleted.put(itemKey, null);
      }
    }
    ConfigurationChangedEvent event = ConfigurationChangedEvent
        .createIncremental(itemsCreated, itemsModified, itemsDeleted);
    return event;
  }

  public static ConfigurationChangedEvent createIncremental(Map<String, Object> added, Map<String, Object> updated,
      Map<String, Object> deleted) {
    return new ConfigurationChangedEvent(added, updated, deleted);
  }

  public static ConfigurationChangedEvent createIncremental(Map<String, Object> updated) {
    return new ConfigurationChangedEvent(new HashMap<>(), updated, new HashMap<>());
  }

  public final Map<String, Object> getAdded() {
    return added;
  }


  public final Map<String, Object> getUpdated() {
    return updated;
  }


  public final Map<String, Object> getDeleted() {
    return deleted;
  }

  public final Set<String> getChanged() {
    return changed;
  }
}
