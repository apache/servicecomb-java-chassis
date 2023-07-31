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

package org.apache.servicecomb.registry.discovery;

import org.springframework.core.Ordered;

/**
 * Server list filters for DiscoveryTree.
 *
 * Implementation Notice: DiscoveryFilter is initialized using bean and instance shared for all
 * microservices. If implementation has states, can put the states to DiscoveryContext or parent DiscoveryTreeNode.
 */
public interface DiscoveryFilter extends Ordered {
  default boolean enabled() {
    return true;
  }

  /**
   * grouping filter, means grouping instances to some groups
   *  eg: operation/ZoneAware/transport
   */
  default boolean isGroupingFilter() {
    return false;
  }

  DiscoveryTreeNode discovery(DiscoveryContext context, DiscoveryTreeNode parent);
}
