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

import java.util.Collection;
import java.util.List;

import org.apache.servicecomb.foundation.common.utils.SPIEnabled;
import org.apache.servicecomb.foundation.common.utils.SPIOrder;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;

/**
 * This is the core service discovery interface. <br/>
 */
public interface Discovery extends SPIEnabled, SPIOrder, LifeCycle {
  /**
   * get Microservice </br>
   *
   * Life Cycle：This method is called anytime after <code>run</code>.
   */
  Microservice getMicroservice(String microserviceId);

  /**
   * get all Microservices </br>
   *
   * Life Cycle：This method is called anytime after <code>run</code>.
   */
  List<Microservice> getAllMicroservices();

  /**
   * get schema content </br>
   *
   * Life Cycle：This method is called anytime after <code>run</code>.
   */
  String getSchema(String microserviceId, Collection<MicroserviceInstance> instances, String schemaId);

  /**
   * get MicroserviceInstance </br>
   *
   * Life Cycle：This method is called anytime after <code>run</code>.
   */
  MicroserviceInstance getMicroserviceInstance(String serviceId, String instanceId);

  /**
   * Find all instances. Implementations can use <code>gerRevision</code> to retrieve the
   * latest instances changed. </br>
   *
   * Life Cycle：This method is called anytime after <code>run</code>.
   *
   * @param appId application id
   * @param serviceName microservice name
   * @param versionRule literal version rule. e.g. 1.0.0, 1.0.0+, [1.0.0, 2.0.0)
   * @return all instances match the criteria.
   */
  default MicroserviceInstances findServiceInstances(String appId, String serviceName, String versionRule) {
    return findServiceInstances(appId, serviceName, versionRule, null);
  }

  default MicroserviceInstances findServiceInstances(String appId, String serviceName, String versionRule,
      String revision) {
    return null;
  }

  String name();
}
