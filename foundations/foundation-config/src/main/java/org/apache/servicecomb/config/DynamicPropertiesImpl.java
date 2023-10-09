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
  private final Map<String, Set<Consumer<String>>> stringCallbacks = new HashMap<>();

  private final Map<String, Set<IntConsumer>> intCallbacks = new HashMap<>();

  private final Map<String, Set<LongConsumer>> longCallbacks = new HashMap<>();

  private final Map<String, Set<DoubleConsumer>> floatCallbacks = new HashMap<>();

  private final Map<String, Set<DoubleConsumer>> doubleCallbacks = new HashMap<>();

  private final Map<String, Set<Consumer<Boolean>>> booleanCallbacks = new HashMap<>();

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
      updateDefault(entry);
    }
  }

  private void updateDefault(Entry<String, Object> entry) {
    if (stringCallbacks.containsKey(entry.getKey())) {
      for (Consumer<String> callbacks : stringCallbacks.get(entry.getKey())) {
        callbacks.accept(null);
      }
    }
    if (intCallbacks.containsKey(entry.getKey())) {
      for (IntConsumer callbacks : intCallbacks.get(entry.getKey())) {
        callbacks.accept(0);
      }
    }
    if (longCallbacks.containsKey(entry.getKey())) {
      for (LongConsumer callbacks : longCallbacks.get(entry.getKey())) {
        callbacks.accept(0L);
      }
    }
    if (floatCallbacks.containsKey(entry.getKey())) {
      for (DoubleConsumer callbacks : floatCallbacks.get(entry.getKey())) {
        callbacks.accept(0F);
      }
    }
    if (doubleCallbacks.containsKey(entry.getKey())) {
      for (DoubleConsumer callbacks : doubleCallbacks.get(entry.getKey())) {
        callbacks.accept(0D);
      }
    }
    if (booleanCallbacks.containsKey(entry.getKey())) {
      for (Consumer<Boolean> callbacks : booleanCallbacks.get(entry.getKey())) {
        callbacks.accept(false);
      }
    }
  }

  private void updateValue(Entry<String, Object> entry) {
    if (stringCallbacks.containsKey(entry.getKey())) {
      for (Consumer<String> callbacks : stringCallbacks.get(entry.getKey())) {
        callbacks.accept((String) entry.getValue());
      }
    }
    if (intCallbacks.containsKey(entry.getKey())) {
      for (IntConsumer callbacks : intCallbacks.get(entry.getKey())) {
        callbacks.accept((int) entry.getValue());
      }
    }
    if (longCallbacks.containsKey(entry.getKey())) {
      for (LongConsumer callbacks : longCallbacks.get(entry.getKey())) {
        callbacks.accept((long) entry.getValue());
      }
    }
    if (floatCallbacks.containsKey(entry.getKey())) {
      for (DoubleConsumer callbacks : floatCallbacks.get(entry.getKey())) {
        callbacks.accept((float) entry.getValue());
      }
    }
    if (doubleCallbacks.containsKey(entry.getKey())) {
      for (DoubleConsumer callbacks : doubleCallbacks.get(entry.getKey())) {
        callbacks.accept((double) entry.getValue());
      }
    }
    if (booleanCallbacks.containsKey(entry.getKey())) {
      for (Consumer<Boolean> callbacks : booleanCallbacks.get(entry.getKey())) {
        callbacks.accept((Boolean) entry.getValue());
      }
    }
  }

  @Override
  public String getStringProperty(String propertyName, Consumer<String> consumer, String defaultValue) {
    stringCallbacks.computeIfAbsent(propertyName, key -> new HashSet<>()).add(consumer);
    return environment.getProperty(propertyName, defaultValue);
  }

  @Override
  public String getStringProperty(String propertyName, String defaultValue) {
    return environment.getProperty(propertyName, defaultValue);
  }

  @Override
  public int getIntProperty(String propertyName, IntConsumer consumer, int defaultValue) {
    intCallbacks.computeIfAbsent(propertyName, key -> new HashSet<>()).add(consumer);
    return environment.getProperty(propertyName, int.class, defaultValue);
  }

  @Override
  public int getIntProperty(String propertyName, int defaultValue) {
    return environment.getProperty(propertyName, int.class, defaultValue);
  }

  @Override
  public long getLongProperty(String propertyName, LongConsumer consumer, long defaultValue) {
    longCallbacks.computeIfAbsent(propertyName, key -> new HashSet<>()).add(consumer);
    return environment.getProperty(propertyName, long.class, defaultValue);
  }

  @Override
  public long getLongProperty(String propertyName, long defaultValue) {
    return environment.getProperty(propertyName, long.class, defaultValue);
  }

  @Override
  public float getFloatProperty(String propertyName, DoubleConsumer consumer, float defaultValue) {
    floatCallbacks.computeIfAbsent(propertyName, key -> new HashSet<>()).add(consumer);
    return environment.getProperty(propertyName, float.class, defaultValue);
  }

  @Override
  public float getFloatProperty(String propertyName, float defaultValue) {
    return environment.getProperty(propertyName, float.class, defaultValue);
  }

  @Override
  public double getDoubleProperty(String propertyName, DoubleConsumer consumer, double defaultValue) {
    doubleCallbacks.computeIfAbsent(propertyName, key -> new HashSet<>()).add(consumer);
    return environment.getProperty(propertyName, double.class, defaultValue);
  }

  @Override
  public double getDoubleProperty(String propertyName, double defaultValue) {
    return environment.getProperty(propertyName, double.class, defaultValue);
  }

  @Override
  public boolean getBooleanProperty(String propertyName, Consumer<Boolean> consumer, boolean defaultValue) {
    booleanCallbacks.computeIfAbsent(propertyName, key -> new HashSet<>()).add(consumer);
    return environment.getProperty(propertyName, boolean.class, defaultValue);
  }

  @Override
  public boolean getBooleanProperty(String propertyName, boolean defaultValue) {
    return environment.getProperty(propertyName, boolean.class, defaultValue);
  }
}
