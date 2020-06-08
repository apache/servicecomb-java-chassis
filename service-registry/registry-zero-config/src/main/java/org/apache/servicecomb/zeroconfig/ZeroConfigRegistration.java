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

import static org.apache.servicecomb.zeroconfig.ZeroConfigRegistryConstants.ENABLED;
import static org.apache.servicecomb.zeroconfig.ZeroConfigRegistryConstants.ORDER;

import java.util.Collection;

import org.apache.servicecomb.registry.api.Registration;
import org.apache.servicecomb.registry.api.registry.BasePath;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.zeroconfig.client.ClientUtil;
import org.apache.servicecomb.zeroconfig.client.ZeroConfigClient;
import org.apache.servicecomb.zeroconfig.server.ServerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicPropertyFactory;

public class ZeroConfigRegistration implements Registration {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZeroConfigRegistration.class);

  public static ZeroConfigRegistration INSTANCE = new ZeroConfigRegistration();

  private static final String NAME = "zero-config registration";

  private ZeroConfigClient zeroConfigClient = ZeroConfigClient.INSTANCE;

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance().getBooleanProperty(ENABLED, true).get();
  }

  @Override
  public void init() {
    zeroConfigClient.init();
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
    return ORDER;
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public MicroserviceInstance getMicroserviceInstance() {
    return zeroConfigClient.getSelfMicroserviceInstance();
  }

  @Override
  public Microservice getMicroservice() {
    return zeroConfigClient.getSelfMicroservice();
  }

  @Override
  public String getAppId() {
    return zeroConfigClient.getSelfMicroservice().getAppId();
  }

  @Override
  public boolean updateMicroserviceInstanceStatus(MicroserviceInstanceStatus status) {
    zeroConfigClient.getSelfMicroserviceInstance().setStatus(status);
    return true;
  }

  @Override
  public void addSchema(String schemaId, String content) {
    zeroConfigClient.getSelfMicroservice().addSchema(schemaId, content);
  }

  @Override
  public void addEndpoint(String endpoint) {
    zeroConfigClient.getSelfMicroserviceInstance().getEndpoints().add(endpoint);
  }

  @Override
  public void addBasePath(Collection<BasePath> basePaths) {
    zeroConfigClient.getSelfMicroservice().getPaths().addAll(basePaths);
  }
}
