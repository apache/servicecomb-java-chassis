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

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.springframework.core.env.Environment;

import com.google.common.annotations.VisibleForTesting;

public class PriorityPropertyFactory {
  private final Map<PriorityPropertyType<?>, PriorityProperty<?>> properties = new ConcurrentHashMapEx<>();

  private final Environment environment;

  public PriorityPropertyFactory(Environment environment) {
    this.environment = environment;
  }

  @VisibleForTesting
  public Stream<PriorityProperty<?>> getProperties() {
    return properties.values().stream();
  }

  @SuppressWarnings("unchecked")
  public <T> PriorityProperty<T> getOrCreate(Type type, T invalidValue, T defaultValue, String... priorityKeys) {
    PriorityPropertyType<T> propertyType = new PriorityPropertyType<>(type, invalidValue, defaultValue, priorityKeys);
    return (PriorityProperty<T>) properties.computeIfAbsent(propertyType,
        key -> new PriorityProperty<>(environment, key));
  }
}
