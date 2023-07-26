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
package org.apache.servicecomb.registry.api.registry;

import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_DEFAULT_REGISTER_BY;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_FRAMEWORK_DEFAULT_NAME;

import java.util.Map;

import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.foundation.common.Version;
import org.apache.servicecomb.registry.config.MicroservicePropertiesLoader;
import org.springframework.core.env.Environment;

public class MicroserviceFactory {
  public Microservice create(Environment environment) {
    Microservice microservice = createMicroserviceFromConfiguration(environment);
    microservice.setInstance(MicroserviceInstance.createFromDefinition(environment));
    return microservice;
  }

  private Microservice createMicroserviceFromConfiguration(Environment environment) {
    Microservice microservice = new Microservice();

    microservice.setEnvironment(environment.getProperty(BootStrapProperties.CONFIG_SERVICE_ENVIRONMENT,
        BootStrapProperties.DEFAULT_MICROSERVICE_ENVIRONMENT));
    microservice.setAppId(environment.getProperty(BootStrapProperties.CONFIG_SERVICE_APPLICATION,
        BootStrapProperties.DEFAULT_APPLICATION));
    microservice.setServiceName(environment.getProperty(BootStrapProperties.CONFIG_SERVICE_NAME,
        BootStrapProperties.DEFAULT_MICROSERVICE_NAME));
    microservice.setVersion(environment.getProperty(BootStrapProperties.CONFIG_SERVICE_VERSION,
        BootStrapProperties.DEFAULT_MICROSERVICE_VERSION));
    // just check version format
    new Version(microservice.getVersion());

    microservice.setDescription(environment.getProperty(BootStrapProperties.CONFIG_SERVICE_DESCRIPTION));
    Map<String, String> propertiesMap = MicroservicePropertiesLoader.INSTANCE.loadProperties(environment);
    microservice.setProperties(propertiesMap);

    // set alias name when allow cross app
    if (microservice.allowCrossApp()) {
      microservice.setAlias(Microservice.generateAbsoluteMicroserviceName(microservice.getAppId(),
          microservice.getServiceName()));
    } else {
      microservice.setAlias(environment.getProperty(BootStrapProperties.CONFIG_SERVICE_ALIAS));
    }

    microservice.setFramework(createFramework());
    microservice.setRegisterBy(CONFIG_DEFAULT_REGISTER_BY);

    return microservice;
  }

  private Framework createFramework() {
    Framework framework = new Framework();
    framework.setName(CONFIG_FRAMEWORK_DEFAULT_NAME);
    framework.setVersion(FrameworkVersions.allVersions());
    return framework;
  }
}
