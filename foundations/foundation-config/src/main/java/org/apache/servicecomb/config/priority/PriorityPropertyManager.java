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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.servicecomb.config.inject.ConfigObjectFactory;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;

import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;

public class PriorityPropertyManager {
  private ConfigurationListener configurationListener = this::configurationListener;

  private Map<PriorityProperty<?>, PriorityProperty<?>> priorityPropertyMap = new ConcurrentHashMapEx<>();

  private Map<Object, List<PriorityProperty<?>>> configObjectMap = new ConcurrentHashMapEx<>();

  // will be reset to null after register or unregister
  // and build when configuration changed
  private Map<String, List<PriorityProperty<?>>> keyCache;

  public PriorityPropertyManager() {
    // make sure create a DynamicPropertyFactory instance
    // otherwise will cause wrong order of configurationListeners
    DynamicPropertyFactory.getInstance();

    ConfigurationManager.getConfigInstance().addConfigurationListener(configurationListener);
  }

  public void close() {
    ConfigurationManager.getConfigInstance().removeConfigurationListener(configurationListener);
  }

  public synchronized void configurationListener(ConfigurationEvent event) {
    if (event.isBeforeUpdate()) {
      return;
    }

    if (keyCache == null) {
      keyCache = new ConcurrentHashMapEx<>();
      updateCache(priorityPropertyMap.values());
      configObjectMap.values().stream().forEach(this::updateCache);
    }

    if (event.getPropertyName() != null) {
      keyCache.getOrDefault(event.getPropertyName(), Collections.emptyList()).stream()
          .forEach(p -> p.updateFinalValue(false));
      return;
    }

    // event like add configuration source, need to make a global refresh
    keyCache.values().stream().flatMap(Collection::stream).forEach(p -> p.updateFinalValue(false));
  }

  private void updateCache(Collection<PriorityProperty<?>> properties) {
    for (PriorityProperty<?> priorityProperty : properties) {
      for (String key : priorityProperty.getPriorityKeys()) {
        keyCache.computeIfAbsent(key, k -> new ArrayList<>()).add(priorityProperty);
      }
      priorityProperty.updateFinalValue(false);
    }
  }

  public Map<PriorityProperty<?>, PriorityProperty<?>> getPriorityPropertyMap() {
    return priorityPropertyMap;
  }

  public Map<Object, List<PriorityProperty<?>>> getConfigObjectMap() {
    return configObjectMap;
  }

  private synchronized void registerPriorityProperty(PriorityProperty<?> property) {
    priorityPropertyMap.put(property, property);
    keyCache = null;
  }

  private synchronized void registerConfigObject(Object configObject, List<PriorityProperty<?>> properties) {
    configObjectMap.put(configObject, properties);
    keyCache = null;
  }

  public synchronized void unregisterPriorityProperty(PriorityProperty<?> property) {
    priorityPropertyMap.remove(property);
    keyCache = null;
  }

  public synchronized void unregisterConfigObject(Object configObject) {
    if (configObject == null) {
      return;
    }

    configObjectMap.remove(configObject);
    keyCache = null;
  }

  public <T> T createConfigObject(Class<T> cls, Object... kvs) {
    Map<String, Object> parameters = new HashMap<>();
    for (int idx = 0; idx < kvs.length; idx += 2) {
      parameters.put(kvs[idx].toString(), kvs[idx + 1]);
    }

    return createConfigObject(cls, parameters);
  }

  public <T> T createConfigObject(Class<T> cls, Map<String, Object> parameters) {
    ConfigObjectFactory factory = new ConfigObjectFactory();
    T configObject = factory.create(this, cls, parameters);
    registerConfigObject(configObject, factory.getPriorityProperties());
    return configObject;
  }

  public <T> PriorityProperty<T> newPriorityProperty(Type cls, T invalidValue, T defaultValue,
      String... priorityKeys) {
    return new PriorityProperty<>(cls, invalidValue, defaultValue, priorityKeys);
  }

  public <T> PriorityProperty<T> createPriorityProperty(Type cls, T invalidValue, T defaultValue,
      String... priorityKeys) {
    PriorityProperty<T> priorityProperty = new PriorityProperty<>(cls, invalidValue, defaultValue, priorityKeys);
    registerPriorityProperty(priorityProperty);
    return priorityProperty;
  }
}
