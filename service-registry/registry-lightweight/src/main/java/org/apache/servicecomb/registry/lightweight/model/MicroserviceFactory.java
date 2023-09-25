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
package org.apache.servicecomb.registry.lightweight.model;

import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.APP_MAPPING;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_DEFAULT_REGISTER_BY;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.SERVICE_MAPPING;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.VERSION_MAPPING;

import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.foundation.common.Version;
import org.springframework.core.env.Environment;

public class MicroserviceFactory {
  public Microservice create(Environment environment) {
    Microservice microservice = createMicroserviceFromConfiguration(environment);
    microservice.setInstance(MicroserviceInstance.createFromDefinition(environment));
    return microservice;
  }

  private Microservice createMicroserviceFromConfiguration(Environment environment) {
    Microservice microservice = new Microservice();

    EnvironmentConfiguration envConfig = new EnvironmentConfiguration();
    if (!StringUtils.isEmpty(envConfig.getString(APP_MAPPING)) &&
        !StringUtils.isEmpty(envConfig.getString(envConfig.getString(APP_MAPPING)))) {
      microservice.setAppId(envConfig.getString(envConfig.getString(APP_MAPPING)));
    } else {
      microservice.setAppId(BootStrapProperties.readApplication(environment));
    }
    if (!StringUtils.isEmpty(envConfig.getString(SERVICE_MAPPING)) &&
        !StringUtils.isEmpty(envConfig.getString(envConfig.getString(SERVICE_MAPPING)))) {
      microservice.setServiceName(envConfig.getString(envConfig.getString(SERVICE_MAPPING)));
    } else {
      microservice.setServiceName(BootStrapProperties.readServiceName(environment));
    }
    String version;
    if (!StringUtils.isEmpty(envConfig.getString(VERSION_MAPPING)) &&
        !StringUtils.isEmpty(envConfig.getString(envConfig.getString(VERSION_MAPPING)))) {
      version = envConfig.getString(envConfig.getString(VERSION_MAPPING));
    } else {
      version = BootStrapProperties.readServiceVersion(environment);
    }
    // just check version format
    new Version(version);
    microservice.setVersion(version);

    microservice.setDescription(BootStrapProperties.readServiceDescription(environment));
    microservice.setLevel(BootStrapProperties.readServiceRole(environment));
    Map<String, String> propertiesMap = MicroservicePropertiesLoader.INSTANCE.loadProperties(environment);
    microservice.setProperties(propertiesMap);
    microservice.setEnvironment(BootStrapProperties.readServiceEnvironment(environment));

    // set alias name when allow cross app
    if (microservice.allowCrossApp()) {
      microservice.setAlias(Microservice.generateAbsoluteMicroserviceName(microservice.getAppId(),
          microservice.getServiceName()));
    }

    microservice.setRegisterBy(CONFIG_DEFAULT_REGISTER_BY);

    return microservice;
  }
}
