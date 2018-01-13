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

package org.apache.servicecomb.foundation.common;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractObjectManager<KEY_OWNER, KEY, VALUE> {
  protected Map<KEY, VALUE> objMap = new ConcurrentHashMap<>();

  protected final Object lockObj = new Object();

  public VALUE getOrCreate(KEY_OWNER keyOwner) {
    KEY key = getKey(keyOwner);
    VALUE value = objMap.get(key);
    if (value == null) {
      synchronized (lockObj) {
        value = objMap.get(key);
        if (value == null) {
          value = create(keyOwner);
          if (value == null) {
            // 创建失败，下次重新创建
            return null;
          }
          objMap.put(key, value);
        }
      }
    }

    return value;
  }

  public VALUE findByKey(KEY key) {
    return objMap.get(key);
  }

  public VALUE findByContainer(KEY_OWNER keyOwner) {
    KEY key = getKey(keyOwner);
    return objMap.get(key);
  }

  public Set<KEY> keys() {
    return objMap.keySet();
  }

  public Collection<VALUE> values() {
    return objMap.values();
  }

  protected abstract KEY getKey(KEY_OWNER keyOwner);

  /**
   * 只会在锁的保护下执行
   */
  protected abstract VALUE create(KEY_OWNER keyOwner);
}
