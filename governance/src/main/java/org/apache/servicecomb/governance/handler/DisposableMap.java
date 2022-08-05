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

package org.apache.servicecomb.governance.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * check if some items are expired when put new item to map and remove expired items.
 */
public class DisposableMap<V> extends ConcurrentHashMap<String, Disposable<V>> {
  private static final long serialVersionUID = 7249069246763182397L;

  public interface RemoveListener {
    void onRemoveEntry(String k);
  }

  private static final int EXPIRE_TIME = 10 * 60 * 1000;

  private final RemoveListener listener;

  public DisposableMap(RemoveListener listener) {
    this.listener = listener;
  }

  @Override
  public Disposable<V> put(String key, Disposable<V> value) {
    Disposable<V> result = super.put(key, value);

    checkExpired();

    return result;
  }

  private void checkExpired() {
    List<String> expired = new ArrayList<>();
    this.values().forEach(v -> {
      if (System.currentTimeMillis() - v.lastAccessed() >= EXPIRE_TIME) {
        expired.add(v.getKey());
      }
    });
    expired.forEach(listener::onRemoveEntry);
  }

  @Override
  public Disposable<V> get(Object key) {
    return super.get(key);
  }
}
