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

import org.apache.servicecomb.foundation.common.utils.SPIEnabled;
import org.apache.servicecomb.foundation.common.utils.SPIOrder;
import org.apache.servicecomb.registry.api.registry.BasePath;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstanceStatus;

/**
 * This is the core service registration interface. <br/>
 */
public interface Registration extends SPIEnabled, SPIOrder, LifeCycle {
  String name();

  /**
   * get MicroserviceInstance </br>
   *
   * Life Cycle：This method is called anytime after <code>run</code>.
   */
  MicroserviceInstance getMicroserviceInstance();

  /**
   * get Microservice </br>
   *
   * Life Cycle：This method is called anytime after <code>run</code>.
   */
  Microservice getMicroservice();

  /**
   * get application id </br>
   *
   * Life Cycle：This method is called anytime after <code>run</code>.
   */
  String getAppId();

  /**
   * update MicroserviceInstance status </br>
   *
   * Life Cycle：This method is called anytime after <code>run</code>.
   */
  boolean updateMicroserviceInstanceStatus(MicroserviceInstanceStatus status);

  /**
   * adding schemas to Microservice </br>
   *
   * Life Cycle：This method is called after <code>init</code> and before <code>run</code>.
   */
  void addSchema(String schemaId, String content);

  /**
   * adding endpoints to MicroserviceInstance </br>
   *
   * Life Cycle：This method is called after <code>init</code> and before <code>run</code>.
   */
  void addEndpoint(String endpoint);

  /**
   * adding BasePath to MicroserviceInstance </br>
   *
   * Life Cycle：This method is called after <code>init</code> and before <code>run</code>.
   */
  void addBasePath(Collection<BasePath> basePaths);
}
