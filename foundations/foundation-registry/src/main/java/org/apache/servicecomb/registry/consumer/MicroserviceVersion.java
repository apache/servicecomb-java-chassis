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

package org.apache.servicecomb.registry.consumer;

import java.util.Collection;
import java.util.List;

import org.apache.servicecomb.foundation.common.VendorExtensions;
import org.apache.servicecomb.registry.api.event.CreateMicroserviceVersionEvent;
import org.apache.servicecomb.registry.api.event.DestroyMicroserviceVersionEvent;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.foundation.common.Version;

public class MicroserviceVersion {
  protected AppManager appManager;

  protected MicroserviceVersions microserviceVersions;

  // because of cross app invoke
  // microserviceName not always equals microservice.serviceName
  protected String microserviceName;

  protected Version version;

  protected Microservice microservice;

  protected Collection<MicroserviceInstance> instances;

  private final VendorExtensions vendorExtensions = new VendorExtensions();

  public MicroserviceVersion(MicroserviceVersions microserviceVersions, String microserviceId,
      String microserviceName,
      Collection<MicroserviceInstance> instances) {
    Microservice microservice = DiscoveryManager.INSTANCE.getMicroservice(microserviceId);
    if (microservice == null) {
      throw new IllegalStateException(
          String.format("failed to query by microserviceId '%s' from ServiceCenter.", microserviceId));
    }

    init(microserviceVersions, microservice, microserviceName, instances);
    appManager.getEventBus().post(new CreateMicroserviceVersionEvent(this));
  }

  public MicroserviceVersion(MicroserviceVersions microserviceVersions,
      Microservice microservice, String microserviceName,
      Collection<MicroserviceInstance> instances) {
    init(microserviceVersions, microservice, microserviceName, instances);
    appManager.getEventBus().post(new CreateMicroserviceVersionEvent(this));
  }

  protected void init(MicroserviceVersions microserviceVersions, Microservice microservice,
      String microserviceName,
      Collection<MicroserviceInstance> instances) {
    this.appManager = microserviceVersions.getAppManager();
    this.microserviceVersions = microserviceVersions;
    this.microservice = microservice;
    this.microserviceName = microserviceName;
    this.instances = instances;
    this.version = new Version(microservice.getVersion());
  }

  public MicroserviceVersions getMicroserviceVersions() {
    return microserviceVersions;
  }

  public Collection<MicroserviceInstance> getInstances() {
    return instances;
  }

  public void setInstances(List<MicroserviceInstance> instances) {
    this.instances = instances;
  }

  public String getMicroserviceName() {
    return microserviceName;
  }

  public String getMicroserviceId() {
    return microservice.getServiceId();
  }

  public Microservice getMicroservice() {
    return microservice;
  }

  public Version getVersion() {
    return version;
  }

  public VendorExtensions getVendorExtensions() {
    return vendorExtensions;
  }

  public void destroy() {
    appManager.getEventBus().post(new DestroyMicroserviceVersionEvent(this));
  }
}
