/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicecomb.serviceregistry.api.registry;

import static io.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_APPLICATION_ID_KEY;
import static io.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_QUALIFIED_MICROSERVICE_DESCRIPTION_KEY;
import static io.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_QUALIFIED_MICROSERVICE_NAME_KEY;
import static io.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_QUALIFIED_MICROSERVICE_ROLE_KEY;
import static io.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_QUALIFIED_MICROSERVICE_VERSION_KEY;
import static io.servicecomb.foundation.common.base.ServiceCombConstants.DEFAULT_MICROSERVICE_NAME;
import static io.servicecomb.serviceregistry.definition.DefinitionConst.CONFIG_ALLOW_CROSS_APP_KEY;
import static io.servicecomb.serviceregistry.definition.DefinitionConst.DEFAULT_APPLICATION_ID;
import static io.servicecomb.serviceregistry.definition.DefinitionConst.DEFAULT_MICROSERVICE_VERSION;

import java.util.Map;

import org.apache.commons.configuration.Configuration;

import io.servicecomb.serviceregistry.config.ConfigurePropertyUtils;
import io.servicecomb.serviceregistry.config.MicroservicePropertiesLoader;
import io.servicecomb.serviceregistry.definition.MicroserviceDefinition;

public class MicroserviceFactory {
  public Microservice create(String appId, String microserviceName) {
    MicroserviceDefinition microserviceDefinition = MicroserviceDefinition.create(appId, microserviceName);
    return create(microserviceDefinition);
  }

  public Microservice create(MicroserviceDefinition microserviceDefinition) {
    Configuration configuration = microserviceDefinition.getConfiguration();
    Microservice microservice = createMicroserviceFromDefinition(configuration);
    microservice.setIntance(MicroserviceInstance.createFromDefinition(configuration));
    return microservice;
  }

  private Microservice createMicroserviceFromDefinition(Configuration configuration) {
    Microservice microservice = new Microservice();
    microservice.setServiceName(configuration.getString(CONFIG_QUALIFIED_MICROSERVICE_NAME_KEY,
        DEFAULT_MICROSERVICE_NAME));
    microservice.setAppId(configuration.getString(CONFIG_APPLICATION_ID_KEY, DEFAULT_APPLICATION_ID));
    microservice.setVersion(configuration.getString(CONFIG_QUALIFIED_MICROSERVICE_VERSION_KEY,
        DEFAULT_MICROSERVICE_VERSION));
    microservice.setDescription(configuration.getString(CONFIG_QUALIFIED_MICROSERVICE_DESCRIPTION_KEY, ""));
    microservice.setLevel(configuration.getString(CONFIG_QUALIFIED_MICROSERVICE_ROLE_KEY, "FRONT"));
    microservice.setPaths(ConfigurePropertyUtils.getMicroservicePaths(configuration));
    Map<String, String> propertiesMap = MicroservicePropertiesLoader.INSTANCE.loadProperties(configuration);
    microservice.setProperties(propertiesMap);

    // set alias name when allow cross app
    if (allowCrossApp(propertiesMap)) {
      microservice.setAlias(Microservice.generateAbsoluteMicroserviceName(microservice.getAppId(),
          microservice.getServiceName()));
    }

    return microservice;
  }

  private boolean allowCrossApp(Map<String, String> propertiesMap) {
    return Boolean.valueOf(propertiesMap.get(CONFIG_ALLOW_CROSS_APP_KEY));
  }
}
