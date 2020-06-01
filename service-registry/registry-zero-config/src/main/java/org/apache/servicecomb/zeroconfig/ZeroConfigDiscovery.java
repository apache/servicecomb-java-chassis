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
package org.apache.servicecomb.zeroconfig;

import com.netflix.config.DynamicPropertyFactory;
import com.sun.xml.internal.bind.v2.TODO;
import java.util.Collection;
import java.util.List;
import org.apache.servicecomb.registry.api.Discovery;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.zeroconfig.client.ZeroConfigClient;

import static org.apache.servicecomb.zeroconfig.ZeroConfigRegistryConstants.ENABLED;

public class ZeroConfigDiscovery implements Discovery {

  private static final String NAME = "zero-config discovery";

  private ZeroConfigClient zeroConfigClient = ZeroConfigClient.INSTANCE;
  private String revision;

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance().getBooleanProperty(ENABLED, true).get();
  }

  @Override
  public void init() {
    // done in registration
  }

  @Override
  public void run() {
    // done in registration
  }

  @Override
  public void destroy() {
    // done in registration
  }

  @Override
  public int getOrder() {
    return 101;
  }

  @Override
  public Microservice getMicroservice(String microserviceId) {
    return zeroConfigClient.getMicroservice(microserviceId);
  }

  @Override
  public List<Microservice> getAllMicroservices() {
    return zeroConfigClient.getAllMicroservices();
  }

  //TODO
  @Override
  public String getSchema(String microserviceId, Collection<MicroserviceInstance> instances,
      String schemaId) {
    return zeroConfigClient.getSchema(microserviceId, schemaId);
  }

  @Override
  public MicroserviceInstance getMicroserviceInstance(String serviceId, String instanceId) {
    return zeroConfigClient.findMicroserviceInstance(serviceId, instanceId);
  }

  @Override
  public MicroserviceInstances findServiceInstances(String appId, String serviceName,
      String versionRule) {
    return zeroConfigClient.findServiceInstances(appId, serviceName, versionRule);
  }

  @Override
  public String getRevision() {
    return this.revision;
  }

  @Override
  public void setRevision(String revision) {
    this.revision = revision;
  }

  @Override
  public String name() {
    return NAME;
  }

}
