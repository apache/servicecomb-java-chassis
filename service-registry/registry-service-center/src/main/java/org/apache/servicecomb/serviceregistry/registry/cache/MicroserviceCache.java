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

package org.apache.servicecomb.serviceregistry.registry.cache;

import java.util.List;

import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;

public interface MicroserviceCache {
  MicroserviceCacheKey getKey();

  List<MicroserviceInstance> getInstances();

  String getRevisionId();

  MicroserviceCacheStatus getStatus();

  void refresh();

  void forceRefresh();

  enum MicroserviceCacheStatus {
    /**
     * init status, not pull instances from sc yet
     */
    INIT,
    /**
     * unknown error
     */
    UNKNOWN_ERROR,
    /**
     * error occurs while getting access to service center
     */
    CLIENT_ERROR,
    /**
     * success to query the service center, but no target microservice found
     */
    SERVICE_NOT_FOUND,
    /**
     * success to query the service center, but the target microservice instance list is not changed
     */
    NO_CHANGE,
    /**
     * success to query the service center, and the target microservice instance list is changed.
     * the cached instance list gets refreshed successfully.
     */
    REFRESHED,
    /**
     * unknown error occurs while setting the pulled instances into this cache
     */
    SETTING_CACHE_ERROR
  }
}
