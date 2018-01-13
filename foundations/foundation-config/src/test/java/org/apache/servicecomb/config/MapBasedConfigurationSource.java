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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.config.spi.ConfigCenterConfigurationSource;

import com.netflix.config.WatchedUpdateListener;
import com.netflix.config.WatchedUpdateResult;

public class MapBasedConfigurationSource implements ConfigCenterConfigurationSource {

  private static final Map<String, Object> properties = new ConcurrentHashMap<>();

  private static final Set<WatchedUpdateListener> listeners = new HashSet<>();

  static {
    properties.put("servicecomb.abc.key", "xyz");
  }

  @Override
  public void init(Configuration localConfiguration) {

  }

  @Override
  public void addUpdateListener(WatchedUpdateListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeUpdateListener(WatchedUpdateListener listener) {
    listeners.remove(listener);
  }

  @Override
  public Map<String, Object> getCurrentData() throws Exception {
    return properties;
  }

  void addProperty(String property, Object value) {
    properties.put(property, value);

    Map<String, Object> adds = new HashMap<>();
    adds.put(property, value);

    listeners.forEach(
        listener -> listener.updateConfiguration(WatchedUpdateResult.createIncremental(adds, null, null)));
  }

  void setProperty(String property, Object value) {
    properties.replace(property, value);

    Map<String, Object> changeds = new HashMap<>();
    changeds.put(property, value);

    listeners.forEach(listener -> listener
        .updateConfiguration(WatchedUpdateResult.createIncremental(null, changeds, null)));
  }

  void deleteProperty(String property) {
    properties.remove(property);

    Map<String, Object> deletes = new HashMap<>();
    deletes.put(property, null);

    listeners.forEach(
        listener -> listener.updateConfiguration(WatchedUpdateResult.createIncremental(null, null, deletes)));
  }

  public Map<String, Object> getProperties() {
    return properties;
  }
}
