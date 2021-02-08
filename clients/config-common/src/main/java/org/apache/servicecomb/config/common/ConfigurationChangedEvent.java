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

package org.apache.servicecomb.config.common;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationChangedEvent {
  private final Map<String, Object> added;

  private final Map<String, Object> deleted;

  private final Map<String, Object> updated;

  private ConfigurationChangedEvent(Map<String, Object> added, Map<String, Object> updated,
      Map<String, Object> deleted) {
    this.added = added;
    this.deleted = deleted;
    this.updated = updated;
  }

  public static ConfigurationChangedEvent createIncremental(Map<String, Object> latest, Map<String, Object> last) {
    Map<String, Object> itemsCreated = new HashMap<>();
    Map<String, Object> itemsDeleted = new HashMap<>();
    Map<String, Object> itemsModified = new HashMap<>();

    for (String itemKey : latest.keySet()) {
      if (!last.containsKey(itemKey)) {
        itemsCreated.put(itemKey, latest.get(itemKey));
      } else if (!last.get(itemKey).equals(latest.get(itemKey))) {
        itemsModified.put(itemKey, latest.get(itemKey));
      }
    }
    for (String itemKey : last.keySet()) {
      if (!latest.containsKey(itemKey)) {
        itemsDeleted.put(itemKey, "");
      }
    }
    return ConfigurationChangedEvent.createIncremental(itemsCreated, itemsModified, itemsDeleted);
  }

  public static ConfigurationChangedEvent createIncremental(Map<String, Object> added, Map<String, Object> updated,
      Map<String, Object> deleted) {
    return new ConfigurationChangedEvent(added, updated, deleted);
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

  public final Map<String, Object> getComplete() {
    Map<String, Object> result = new HashMap<>(added.size() + updated.size());
    result.putAll(added);
    result.putAll(updated);
    return result;
  }
}
