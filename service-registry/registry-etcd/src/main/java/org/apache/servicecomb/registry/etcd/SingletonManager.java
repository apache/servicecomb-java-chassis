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
package org.apache.servicecomb.registry.etcd;

import java.util.function.Function;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;

public class SingletonManager {

  private static SingletonManager instance;

  private ConcurrentHashMapEx<String, Object> singletons = new ConcurrentHashMapEx<>();

  private SingletonManager() {
  }

  public static synchronized SingletonManager getInstance() {
    if (instance == null) {
      instance = new SingletonManager();
    }
    return instance;
  }

  public <T> T computeIfAbsent(String key, Function<? super String, ? extends T> mappingFunction) {
    return (T) singletons.computeIfAbsent(key, mappingFunction);
  }

  public <T> T get(String key) {
    return (T) singletons.get(key);
  }

  public void remove(String key) {
    singletons.remove(key);
  }

  public void destroy() {
    singletons.clear();
    instance = null;
  }
}
