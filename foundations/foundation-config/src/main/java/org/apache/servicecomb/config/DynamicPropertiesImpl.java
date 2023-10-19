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
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

import org.apache.servicecomb.foundation.common.event.EventManager;
import org.springframework.core.env.Environment;

import com.google.common.eventbus.Subscribe;

public class DynamicPropertiesImpl implements DynamicProperties {
  private static class Holder<C, D> {
    C callback;

    D defaultValue;

    Holder(C callback, D defaultValue) {
      this.callback = callback;
      this.defaultValue = defaultValue;
    }
  }

  private final Map<String, Set<Holder<Consumer<String>, String>>> stringCallbacks = new HashMap<>();

  private final Map<String, Set<Holder<IntConsumer, Integer>>> intCallbacks = new HashMap<>();

  private final Map<String, Set<Holder<LongConsumer, Long>>> longCallbacks = new HashMap<>();

  private final Map<String, Set<Holder<DoubleConsumer, Float>>> floatCallbacks = new HashMap<>();

  private final Map<String, Set<Holder<DoubleConsumer, Double>>> doubleCallbacks = new HashMap<>();

  private final Map<String, Set<Holder<Consumer<Boolean>, Boolean>>> booleanCallbacks = new HashMap<>();

  private final Environment environment;

  public DynamicPropertiesImpl(Environment environment) {
    this.environment = environment;
    EventManager.register(this);
  }

  @Subscribe
  public void onConfigurationChangedEvent(ConfigurationChangedEvent event) {
    for (Entry<String, Object> entry : event.getAdded().entrySet()) {
      updateValue(entry);
    }

    for (Entry<String, Object> entry : event.getUpdated().entrySet()) {
      updateValue(entry);
    }

    for (Entry<String, Object> entry : event.getDeleted().entrySet()) {
      updateValue(entry);
    }
  }

  private void updateValue(Entry<String, Object> entry) {
    if (stringCallbacks.containsKey(entry.getKey())) {
      for (Holder<Consumer<String>, String> callbacks : stringCallbacks.get(entry.getKey())) {
        callbacks.callback.accept(environment.getProperty(entry.getKey(), callbacks.defaultValue));
      }
    }
    if (intCallbacks.containsKey(entry.getKey())) {
      for (Holder<IntConsumer, Integer> callbacks : intCallbacks.get(entry.getKey())) {
        callbacks.callback.accept(environment.getProperty(entry.getKey(), Integer.class, callbacks.defaultValue));
      }
    }
    if (longCallbacks.containsKey(entry.getKey())) {
      for (Holder<LongConsumer, Long> callbacks : longCallbacks.get(entry.getKey())) {
        callbacks.callback.accept(environment.getProperty(entry.getKey(), Long.class, callbacks.defaultValue));
      }
    }
    if (floatCallbacks.containsKey(entry.getKey())) {
      for (Holder<DoubleConsumer, Float> callbacks : floatCallbacks.get(entry.getKey())) {
        callbacks.callback.accept(environment.getProperty(entry.getKey(), Float.class, callbacks.defaultValue));
      }
    }
    if (doubleCallbacks.containsKey(entry.getKey())) {
      for (Holder<DoubleConsumer, Double> callbacks : doubleCallbacks.get(entry.getKey())) {
        callbacks.callback.accept(environment.getProperty(entry.getKey(), Double.class, callbacks.defaultValue));
      }
    }
    if (booleanCallbacks.containsKey(entry.getKey())) {
      for (Holder<Consumer<Boolean>, Boolean> callbacks : booleanCallbacks.get(entry.getKey())) {
        callbacks.callback.accept(environment.getProperty(entry.getKey(), Boolean.class, callbacks.defaultValue));
      }
    }
  }

  @Override
  public String getStringProperty(String propertyName, Consumer<String> consumer, String defaultValue) {
    stringCallbacks.computeIfAbsent(propertyName, key -> new HashSet<>()).add(new Holder<>(consumer, defaultValue));
    return environment.getProperty(propertyName, defaultValue);
  }

  @Override
  public String getStringProperty(String propertyName, String defaultValue) {
    return environment.getProperty(propertyName, defaultValue);
  }

  @Override
  public int getIntProperty(String propertyName, IntConsumer consumer, int defaultValue) {
    intCallbacks.computeIfAbsent(propertyName, key -> new HashSet<>()).add(new Holder<>(consumer, defaultValue));
    return environment.getProperty(propertyName, int.class, defaultValue);
  }

  @Override
  public int getIntProperty(String propertyName, int defaultValue) {
    return environment.getProperty(propertyName, int.class, defaultValue);
  }

  @Override
  public long getLongProperty(String propertyName, LongConsumer consumer, long defaultValue) {
    longCallbacks.computeIfAbsent(propertyName, key -> new HashSet<>()).add(new Holder<>(consumer, defaultValue));
    return environment.getProperty(propertyName, long.class, defaultValue);
  }

  @Override
  public long getLongProperty(String propertyName, long defaultValue) {
    return environment.getProperty(propertyName, long.class, defaultValue);
  }

  @Override
  public float getFloatProperty(String propertyName, DoubleConsumer consumer, float defaultValue) {
    floatCallbacks.computeIfAbsent(propertyName, key -> new HashSet<>()).add(new Holder<>(consumer, defaultValue));
    return environment.getProperty(propertyName, float.class, defaultValue);
  }

  @Override
  public float getFloatProperty(String propertyName, float defaultValue) {
    return environment.getProperty(propertyName, float.class, defaultValue);
  }

  @Override
  public double getDoubleProperty(String propertyName, DoubleConsumer consumer, double defaultValue) {
    doubleCallbacks.computeIfAbsent(propertyName, key -> new HashSet<>()).add(new Holder<>(consumer, defaultValue));
    return environment.getProperty(propertyName, double.class, defaultValue);
  }

  @Override
  public double getDoubleProperty(String propertyName, double defaultValue) {
    return environment.getProperty(propertyName, double.class, defaultValue);
  }

  @Override
  public boolean getBooleanProperty(String propertyName, Consumer<Boolean> consumer, boolean defaultValue) {
    booleanCallbacks.computeIfAbsent(propertyName, key -> new HashSet<>()).add(new Holder<>(consumer, defaultValue));
    return environment.getProperty(propertyName, boolean.class, defaultValue);
  }

  @Override
  public boolean getBooleanProperty(String propertyName, boolean defaultValue) {
    return environment.getProperty(propertyName, boolean.class, defaultValue);
  }
}
