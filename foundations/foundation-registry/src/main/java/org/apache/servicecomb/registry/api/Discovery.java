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

package org.apache.servicecomb.registry.api;

import java.util.List;

import org.apache.servicecomb.foundation.common.utils.SPIEnabled;
import org.apache.servicecomb.foundation.common.utils.SPIOrder;

/**
 * This is the core service discovery interface. <br/>
 */
public interface Discovery<D extends DiscoveryInstance> extends SPIEnabled, SPIOrder, LifeCycle {
  interface InstanceChangedListener<D extends DiscoveryInstance> {
    void onInstanceChanged(String discoveryName, String application, String serviceName, List<D> updatedInstances);
  }

  String name();

  /**
   * If this implementation enabled for this microservice.
   */
  default boolean enabled(String application, String serviceName) {
    return true;
  }

  /**
   * Find all instances.
   *
   * Life Cycle：This method is called anytime after <code>run</code>.
   *
   * @param application application
   * @param serviceName microservice name
   * @return all instances match the criteria.
   */
  List<D> findServiceInstances(String application, String serviceName);

  /**
   * Discovery can call InstanceChangedListener when instance get changed.
   */
  void setInstanceChangedListener(InstanceChangedListener<D> instanceChangedListener);
}
