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
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.PreDestroy;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;

import com.netflix.config.ConfigurationManager;

public class PriorityPropertyFactory {
  private final AbstractConfiguration configuration;

  private final ConfigurationListener configurationListener = this::configurationListener;

  // same to com.netflix.config.DynamicProperty.ALL_PROPS
  // the set is finite
  // will not cause OOM exception
  private final Map<PriorityPropertyType<?>, PriorityProperty<?>> properties = new ConcurrentHashMapEx<>();

  public PriorityPropertyFactory() {
    this.configuration = ConfigurationManager.getConfigInstance();
    this.configuration.addConfigurationListener(configurationListener);
  }

  @PreDestroy
  public void onDestroy() {
    this.configuration.removeConfigurationListener(configurationListener);
  }

  public Stream<PriorityProperty<?>> getProperties() {
    return properties.values().stream();
  }

  private void configurationListener(ConfigurationEvent event) {
    if (event.isBeforeUpdate()) {
      return;
    }

    // just update all properties, it's very fast, no need to do any optimize
    getProperties().forEach(PriorityProperty::updateValue);
  }

  @SuppressWarnings("unchecked")
  public <T> PriorityProperty<T> getOrCreate(Type type, T invalidValue, T defaultValue, String... priorityKeys) {
    PriorityPropertyType<T> propertyType = new PriorityPropertyType<>(type, invalidValue, defaultValue, priorityKeys);
    return (PriorityProperty<T>) properties.computeIfAbsent(propertyType, PriorityProperty::new);
  }
}
