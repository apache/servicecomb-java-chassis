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

import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.APP_MAPPING;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_DEFAULT_REGISTER_BY;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_FRAMEWORK_DEFAULT_NAME;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.SERVICE_MAPPING;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.VERSION_MAPPING;

import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.registry.config.ConfigurePropertyUtils;
import org.apache.servicecomb.registry.config.MicroservicePropertiesLoader;
import org.apache.servicecomb.registry.version.Version;

public class MicroserviceFactory {
  public Microservice create() {
    return create(ConfigUtil.createLocalConfig());
  }

  public Microservice create(Configuration configuration) {
    Microservice microservice = createMicroserviceFromConfiguration(configuration);
    microservice.setInstance(MicroserviceInstance.createFromDefinition(configuration));
    return microservice;
  }

  private Microservice createMicroserviceFromConfiguration(Configuration configuration) {
    Microservice microservice = new Microservice();

    EnvironmentConfiguration envConfig = new EnvironmentConfiguration();
    if (!StringUtils.isEmpty(envConfig.getString(APP_MAPPING)) &&
        !StringUtils.isEmpty(envConfig.getString(envConfig.getString(APP_MAPPING)))) {
      microservice.setAppId(envConfig.getString(envConfig.getString(APP_MAPPING)));
    } else {
      microservice.setAppId(BootStrapProperties.readApplication(configuration));
    }
    if (!StringUtils.isEmpty(envConfig.getString(SERVICE_MAPPING)) &&
        !StringUtils.isEmpty(envConfig.getString(envConfig.getString(SERVICE_MAPPING)))) {
      microservice.setServiceName(envConfig.getString(envConfig.getString(SERVICE_MAPPING)));
    } else {
      microservice.setServiceName(BootStrapProperties.readServiceName(configuration));
    }
    String version;
    if (!StringUtils.isEmpty(envConfig.getString(VERSION_MAPPING)) &&
        !StringUtils.isEmpty(envConfig.getString(envConfig.getString(VERSION_MAPPING)))) {
      version = envConfig.getString(envConfig.getString(VERSION_MAPPING));
    } else {
      version = BootStrapProperties.readServiceVersion(configuration);
    }
    // just check version format
    new Version(version);
    microservice.setVersion(version);

    microservice.setDescription(BootStrapProperties.readServiceDescription(configuration));
    microservice.setLevel(BootStrapProperties.readServiceRole(configuration));
    microservice.setPaths(ConfigurePropertyUtils.getMicroservicePaths(configuration));
    Map<String, String> propertiesMap = MicroservicePropertiesLoader.INSTANCE.loadProperties(configuration);
    microservice.setProperties(propertiesMap);
    microservice.setEnvironment(BootStrapProperties.readServiceEnvironment(configuration));

    // set alias name when allow cross app
    if (microservice.allowCrossApp()) {
      microservice.setAlias(Microservice.generateAbsoluteMicroserviceName(microservice.getAppId(),
          microservice.getServiceName()));
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
