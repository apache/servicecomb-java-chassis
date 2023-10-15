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
import java.util.Map;

import org.apache.servicecomb.foundation.common.event.EventManager;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;

/**
 * This DynamicPropertiesSource is created for easier system tests.
 */
public class InMemoryDynamicPropertiesSource implements DynamicPropertiesSource<Map<String, Object>> {
  public static final String SOURCE_NAME = "in-memory";

  private static final Map<String, Object> DYNAMIC = new HashMap<>();

  @Override
  public EnumerablePropertySource<Map<String, Object>> create(Environment environment) {
    return new MapPropertySource(SOURCE_NAME, DYNAMIC);
  }

  @Override
  public int getOrder() {
    return -100;
  }

  public static void update(String key, Object value) {
    DYNAMIC.put(key, value);

    HashMap<String, Object> updated = new HashMap<>();
    updated.put(key, value);
    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));
  }

  public static void reset() {
    DYNAMIC.clear();
  }
}
