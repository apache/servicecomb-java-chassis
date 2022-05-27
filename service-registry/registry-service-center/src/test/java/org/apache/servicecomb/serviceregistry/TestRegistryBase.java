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
package org.apache.servicecomb.serviceregistry;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.registry.consumer.AppManager;
import org.apache.servicecomb.registry.consumer.MicroserviceManager;
import org.apache.servicecomb.serviceregistry.registry.LocalServiceRegistryFactory;
import org.junit.AfterClass;
import org.junit.Before;

import com.google.common.eventbus.EventBus;

import mockit.Mock;
import mockit.MockUp;

public class TestRegistryBase {
  protected ServiceRegistry serviceRegistry;

  protected AppManager appManager;

  protected MicroserviceManager microserviceManager;

  protected EventBus eventBus;

  protected String appId = "default";

  protected String serviceId = "002";

  protected String serviceName = "ms2";

  protected String schemaId = "hello";

  protected String version = "1.0.0.0";

  protected String versionRule = "0.0.0.0+";

  public interface Hello {
    String hello(String value);
  }

  @Before
  public void setup() {
    ConfigUtil.installDynamicConfig();

    // avoid write too many logs
    Logger.getRootLogger().setLevel(Level.OFF);

    serviceRegistry = LocalServiceRegistryFactory.createLocal("registry.yaml");
    serviceRegistry.init();

    DiscoveryManager.renewInstance();
    appManager = DiscoveryManager.INSTANCE.getAppManager();
    microserviceManager = appManager.getOrCreateMicroserviceManager(appId);
    eventBus = serviceRegistry.getEventBus();

    RegistrationManager.INSTANCE.getSwaggerLoader().registerSwagger(appId, serviceName, schemaId, Hello.class);

    RegistryUtils.setServiceRegistry(serviceRegistry);

    Logger.getRootLogger().setLevel(Level.INFO);
  }

  @AfterClass
  public static void classTeardown() {
    RegistryUtils.setServiceRegistry(null);
    ArchaiusUtils.resetConfig();
  }

  protected void mockNotExist() {
    MicroserviceInstances microserviceInstances = new MicroserviceInstances();
    microserviceInstances.setMicroserviceNotExist(true);
    new MockUp<DiscoveryManager>() {
      @Mock
      MicroserviceInstances findServiceInstances(String appId, String serviceName,
          String versionRule, String revision) {
        return microserviceInstances;
      }
    };
  }

  protected void mockDisconnect() {
    new MockUp<DiscoveryManager>() {
      @Mock
      MicroserviceInstances findServiceInstances(String appId, String serviceName,
          String versionRule) {
        return null;
      }
    };
  }
}
