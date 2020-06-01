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
import java.util.Collection;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.config.archaius.sources.MicroserviceConfigLoader;
import org.apache.servicecomb.registry.api.Registration;
import org.apache.servicecomb.registry.api.registry.BasePath;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceFactory;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.definition.MicroserviceDefinition;
import org.apache.servicecomb.zeroconfig.client.ClientUtil;
import org.apache.servicecomb.zeroconfig.client.ZeroConfigClient;
import org.apache.servicecomb.zeroconfig.server.ServerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZeroConfigRegistration implements Registration {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZeroConfigRegistration.class);

  public static ZeroConfigRegistration INSTANCE = new ZeroConfigRegistration();

  private static final String NAME = "zero-config registration";
  private static final String ENABLED = "servicecomb.zeroconfig.registry.registration.enabled";

  private ZeroConfigClient zeroConfigClient = ZeroConfigClient.INSTANCE;

  // registration objects
  private Microservice selfMicroservice;
  private MicroserviceInstance selfMicroserviceInstance;

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance().getBooleanProperty(ENABLED, true).get();
  }

  @Override
  public void init() {
    // init self Microservice & MicroserviceInstance objects
    MicroserviceConfigLoader loader = ConfigUtil.getMicroserviceConfigLoader();
    MicroserviceDefinition microserviceDefinition = new MicroserviceDefinition(
        loader.getConfigModels());
    MicroserviceFactory microserviceFactory = new MicroserviceFactory();
    this.selfMicroservice = microserviceFactory.create(microserviceDefinition);
    this.selfMicroserviceInstance = selfMicroservice.getInstance();

    ServerUtil.init();
    ClientUtil.init();
  }

  @Override
  public void run() {
    // register service instance
    boolean registerResult = zeroConfigClient.register();

    if (!registerResult) {
      LOGGER.error("Failed to Register Service Instance in Zero-Config mode");
    }
  }

  @Override
  public void destroy() {
    // unregister service instance
    boolean unregisterResult = zeroConfigClient.unregister();

    if (!unregisterResult) {
      LOGGER.error("Failed to Unregister Service Instance in Zero-Config mode");
    }
  }

  @Override
  public int getOrder() {
    return 101;
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public MicroserviceInstance getMicroserviceInstance() {
    return this.selfMicroserviceInstance;
  }

  @Override
  public Microservice getMicroservice() {
    return this.selfMicroservice;
  }

  @Override
  public String getAppId() {
    return this.selfMicroservice.getAppId();
  }

  @Override
  public boolean updateMicroserviceInstanceStatus(MicroserviceInstanceStatus status) {
    this.selfMicroserviceInstance.setStatus(status);
    return true;
  }

  @Override
  public void addSchema(String schemaId, String content) {
    this.selfMicroservice.addSchema(schemaId, content);
  }

  @Override
  public void addEndpoint(String endpoint) {
    this.selfMicroserviceInstance.getEndpoints().add(endpoint);
  }

  @Override
  public void addBasePath(Collection<BasePath> basePaths) {
    this.selfMicroservice.getPaths().addAll(basePaths);
  }

  // setter/getter

  public void setSelfMicroservice(
      Microservice selfMicroservice) {
    this.selfMicroservice = selfMicroservice;
  }

  public Microservice getSelfMicroservice() {
    return this.selfMicroservice;
  }

  public void setSelfMicroserviceInstance(
      MicroserviceInstance selfMicroserviceInstance) {
    this.selfMicroserviceInstance = selfMicroserviceInstance;
  }

  public MicroserviceInstance getSelfMicroserviceInstance() {
    return this.selfMicroserviceInstance;
  }

}
