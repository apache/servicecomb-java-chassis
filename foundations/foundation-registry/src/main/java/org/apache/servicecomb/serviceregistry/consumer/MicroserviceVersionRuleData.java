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
package org.apache.servicecomb.serviceregistry.consumer;

import java.util.Map;

import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.cache.InstanceCache;

public class MicroserviceVersionRuleData {
  // if instances is not empty, latestVersion is relate to instances
  // if instances is empty, latestVersion is relate to versions
  MicroserviceVersion latestVersion;

  // key is microserviceId
  Map<String, MicroserviceVersion> versions;

  // key is instanceId
  Map<String, MicroserviceInstance> instances;

  InstanceCache instanceCache;

  VersionedCache versionedCache;

  @SuppressWarnings("unchecked")
  public <T extends MicroserviceVersion> T getLatestMicroserviceVersion() {
    return (T) latestVersion;
  }

  public Map<String, MicroserviceVersion> getVersions() {
    return versions;
  }

  public Map<String, MicroserviceInstance> getInstances() {
    return instances;
  }

  public InstanceCache getInstanceCache() {
    return instanceCache;
  }

  public VersionedCache getVersionedCache() {
    return versionedCache;
  }
}
