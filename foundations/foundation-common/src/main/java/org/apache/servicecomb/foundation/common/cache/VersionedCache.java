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

package org.apache.servicecomb.foundation.common.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unchecked")
public class VersionedCache {
  protected static final AtomicInteger VERSION = new AtomicInteger();

  interface IsEmpty {
    boolean isEmpty();
  }

  protected int cacheVersion;

  // an optional name
  protected String name;

  protected Object data;

  protected IsEmpty isEmpty = this::isCommonEmpty;

  private boolean isCommonEmpty() {
    return data == null;
  }

  private boolean isMapEmpty() {
    return ((Map<?, ?>) data).isEmpty();
  }

  private boolean isCollectionEmpty() {
    return ((Collection<?>) data).isEmpty();
  }

  private boolean isArrayEmpty() {
    return ((Object[]) data).length == 0;
  }

  public int cacheVersion() {
    return cacheVersion;
  }

  public <T extends VersionedCache> T autoCacheVersion() {
    this.cacheVersion = VERSION.incrementAndGet();
    return (T) this;
  }

  public <T extends VersionedCache> T cacheVersion(int cacheVersion) {
    this.cacheVersion = cacheVersion;
    return (T) this;
  }

  public String name() {
    return name;
  }

  public <T extends VersionedCache> T name(String name) {
    this.name = name;
    return (T) this;
  }

  public <T extends VersionedCache> T subName(VersionedCache parent, String subName) {
    Objects.requireNonNull(parent.name);
    Objects.requireNonNull(subName);

    this.name = parent.name + "/" + subName;
    return (T) this;
  }


  public <T> T data() {
    return (T) data;
  }

  // caller maker sure data is a map
  public <K, V> Map<K, V> mapData() {
    return (Map<K, V>) data;
  }

  // caller maker sure data is a collection
  public <T> Collection<T> collectionData() {
    return (Collection<T>) data;
  }

  //caller maker sure data is a array
  public <T> T[] arrayData() {
    return (T[]) data;
  }

  public <T extends VersionedCache> T data(Object data) {
    this.data = data;

    if (data == null) {
      isEmpty = this::isCommonEmpty;
    } else if (Map.class.isInstance(data)) {
      isEmpty = this::isMapEmpty;
    } else if (Collection.class.isInstance(data)) {
      isEmpty = this::isCollectionEmpty;
    } else if (data.getClass().isArray()) {
      isEmpty = this::isArrayEmpty;
    } else {
      isEmpty = this::isCommonEmpty;
    }
    return (T) this;
  }

  // only newData is new than this, means expired.
  public boolean isExpired(VersionedCache newCache) {
    // cacheVersion maybe overflow, so must use decrease to determine
    return newCache.cacheVersion - cacheVersion > 0;
  }

  public boolean isEmpty() {
    return isEmpty.isEmpty();
  }
}
