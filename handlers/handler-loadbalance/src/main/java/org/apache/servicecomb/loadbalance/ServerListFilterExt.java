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

package org.apache.servicecomb.loadbalance;

import java.util.List;

import org.apache.servicecomb.core.Invocation;

/**
 *  Base interface for server list filters.
 *
 *  Robin ServerListFilter can not support invocation based filter strategies, so we create a new one to
 *  support this.
 */
public interface ServerListFilterExt {
  int ORDER_NORMAL = 0;

  int ORDER_ISOLATION = -100;

  int ORDER_ZONE_AWARE = 200;

  String EMPTY_INSTANCE_PROTECTION = "servicecomb.loadbalance.filter.isolation.emptyInstanceProtectionEnabled";

  String ISOLATION_FILTER_ENABLED = "servicecomb.loadbalance.filter.isolation.enabled";

  String ZONE_AWARE_FILTER_ENABLED = "servicecomb.loadbalance.filter.zoneaware.enabled";

  default int getOrder() {
    return ORDER_NORMAL;
  }

  default boolean enabled() {
    return true;
  }

  default void setLoadBalancer(LoadBalancer loadBalancer) {
  }

  List<ServiceCombServer> getFilteredListOfServers(List<ServiceCombServer> servers, Invocation invocation);
}
