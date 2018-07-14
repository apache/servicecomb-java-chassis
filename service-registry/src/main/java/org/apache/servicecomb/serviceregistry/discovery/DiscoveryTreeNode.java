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

package org.apache.servicecomb.serviceregistry.discovery;

import java.util.Map;

import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;

public class DiscoveryTreeNode extends VersionedCache {
  private volatile boolean childrenInited;

  private int level;

  protected Map<String, Object> attributes = new ConcurrentHashMapEx<>();

  protected Map<String, DiscoveryTreeNode> children = new ConcurrentHashMapEx<>();

  public boolean childrenInited() {
    return childrenInited;
  }

  public DiscoveryTreeNode childrenInited(boolean childrenInited) {
    this.childrenInited = childrenInited;
    return this;
  }

  public int level() {
    return level;
  }

  public DiscoveryTreeNode level(int level) {
    this.level = level;
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T> T attribute(String key) {
    return (T) attributes.get(key);
  }

  public DiscoveryTreeNode attribute(String key, Object value) {
    attributes.put(key, value);
    return this;
  }

  public Map<String, DiscoveryTreeNode> children() {
    return children;
  }

  public DiscoveryTreeNode children(Map<String, DiscoveryTreeNode> children) {
    this.children = children;
    return this;
  }

  public DiscoveryTreeNode child(String childName) {
    return children.get(childName);
  }

  public DiscoveryTreeNode child(String childName, DiscoveryTreeNode child) {
    children.put(childName, child);
    return this;
  }

  public DiscoveryTreeNode fromCache(VersionedCache other) {
    this.cacheVersion = other.cacheVersion();
    this.name = other.name();
    this.data(other.data());
    return this;
  }
}
