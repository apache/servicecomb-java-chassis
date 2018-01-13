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
package org.apache.servicecomb.foundation.common.concurrent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ConcurrentHashMapEx<K, V> extends ConcurrentHashMap<K, V> {
  private static final long serialVersionUID = -7753722464102569902L;

  public ConcurrentHashMapEx() {
    super();
  }

  public ConcurrentHashMapEx(int initialCapacity) {
    super(initialCapacity);
  }

  public ConcurrentHashMapEx(Map<? extends K, ? extends V> m) {
    super(m);
  }

  public ConcurrentHashMapEx(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
  }

  public ConcurrentHashMapEx(int initialCapacity,
      float loadFactor, int concurrencyLevel) {
    super(initialCapacity, loadFactor, concurrencyLevel);
  }

  // ConcurrentHashMap.computeIfAbsent always do "synchronized" operation
  // so we wrap it to improve performance
  @Override
  public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
    V value = get(key);
    if (value != null) {
      return value;
    }

    return super.computeIfAbsent(key, mappingFunction);
  }
}
