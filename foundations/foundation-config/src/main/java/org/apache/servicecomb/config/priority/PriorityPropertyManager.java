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
package org.apache.servicecomb.config.priority;

import static java.util.Collections.synchronizedMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.apache.servicecomb.config.ConfigurationChangedEvent;
import org.apache.servicecomb.foundation.common.event.EventManager;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.Subscribe;

public class PriorityPropertyManager {
  private final ConfigObjectFactory configObjectFactory;

  // key is config object instance
  // value is properties of the config object instance
  private final Map<Object, List<ConfigObjectProperty>> configObjectMap = synchronizedMap(new WeakHashMap<>());

  public PriorityPropertyManager(ConfigObjectFactory configObjectFactory) {
    this.configObjectFactory = configObjectFactory;
    EventManager.register(this);
  }

  public PriorityPropertyFactory getPropertyFactory() {
    return configObjectFactory.getPropertyFactory();
  }

  public void close() {

  }

  @Subscribe
  public void onConfigurationChangedEvent(ConfigurationChangedEvent event) {
    // just loop all properties, it's very fast, no need to do any optimize
    for (Entry<Object, List<ConfigObjectProperty>> entry : configObjectMap.entrySet()) {
      Object instance = entry.getKey();
      entry.getValue().forEach(
          configObjectProperty -> {
            for (String updatedKey : event.getChanged()) {
              configObjectProperty.updateValueWhenChanged(instance, updatedKey);
            }
          });
    }
  }

  @VisibleForTesting
  public Map<Object, List<ConfigObjectProperty>> getConfigObjectMap() {
    return configObjectMap;
  }

  public <T> T createConfigObject(Class<T> cls, Object... kvs) {
    Map<String, Object> parameters = new HashMap<>();
    for (int idx = 0; idx < kvs.length; idx += 2) {
      parameters.put(kvs[idx].toString(), kvs[idx + 1]);
    }

    return createConfigObject(cls, parameters);
  }

  public <T> T createConfigObject(Class<T> cls, Map<String, Object> parameters) {
    ConfigObject<T> configObject = configObjectFactory.create(cls, parameters);
    return saveConfigObject(configObject);
  }

  public <T> T createConfigObject(T instance, Map<String, Object> parameters) {
    ConfigObject<T> configObject = configObjectFactory.create(instance, parameters);
    return saveConfigObject(configObject);
  }

  private <T> T saveConfigObject(ConfigObject<T> configObject) {
    configObjectMap.put(configObject.getInstance(), configObject.getProperties());
    return configObject.getInstance();
  }
}
