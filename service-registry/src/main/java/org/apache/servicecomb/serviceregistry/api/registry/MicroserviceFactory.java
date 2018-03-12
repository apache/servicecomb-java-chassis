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
package org.apache.servicecomb.serviceregistry.api.registry;

import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_APPLICATION_ID_KEY;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_QUALIFIED_MICROSERVICE_DESCRIPTION_KEY;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_QUALIFIED_MICROSERVICE_NAME_KEY;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_QUALIFIED_MICROSERVICE_ROLE_KEY;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_QUALIFIED_MICROSERVICE_VERSION_KEY;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.DEFAULT_MICROSERVICE_NAME;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.SERVICECOMB_ENV;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.DEFAULT_SERVICECOMB_ENV;
import static org.apache.servicecomb.serviceregistry.definition.DefinitionConst.CONFIG_ALLOW_CROSS_APP_KEY;
import static org.apache.servicecomb.serviceregistry.definition.DefinitionConst.DEFAULT_APPLICATION_ID;
import static org.apache.servicecomb.serviceregistry.definition.DefinitionConst.DEFAULT_MICROSERVICE_VERSION;

import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.serviceregistry.config.ConfigurePropertyUtils;
import org.apache.servicecomb.serviceregistry.config.MicroservicePropertiesLoader;
import org.apache.servicecomb.serviceregistry.definition.MicroserviceDefinition;

public class MicroserviceFactory {
  public Microservice create(String appId, String microserviceName) {
    MicroserviceDefinition microserviceDefinition = MicroserviceDefinition.create(appId, microserviceName);
    return create(microserviceDefinition);
  }

  public Microservice create(MicroserviceDefinition microserviceDefinition) {
    Configuration configuration = microserviceDefinition.getConfiguration();
    Microservice microservice = createMicroserviceFromDefinition(configuration);
    microservice.setInstance(MicroserviceInstance.createFromDefinition(configuration));
    return microservice;
  }

  private Microservice createMicroserviceFromDefinition(Configuration configuration) {
    Microservice microservice = new Microservice();
    microservice.setServiceName(configuration.getString(CONFIG_QUALIFIED_MICROSERVICE_NAME_KEY,
        DEFAULT_MICROSERVICE_NAME));
    microservice.setAppId(configuration.getString(CONFIG_APPLICATION_ID_KEY, DEFAULT_APPLICATION_ID));
    microservice.setVersion(configuration.getString(CONFIG_QUALIFIED_MICROSERVICE_VERSION_KEY,
        DEFAULT_MICROSERVICE_VERSION));
    setDescription(configuration, microservice);
    microservice.setLevel(configuration.getString(CONFIG_QUALIFIED_MICROSERVICE_ROLE_KEY, "FRONT"));
    microservice.setPaths(ConfigurePropertyUtils.getMicroservicePaths(configuration));
    Map<String, String> propertiesMap = MicroservicePropertiesLoader.INSTANCE.loadProperties(configuration);
    microservice.setProperties(propertiesMap);
    microservice.setEnvironment(configuration.getString(SERVICECOMB_ENV, DEFAULT_SERVICECOMB_ENV));

    // set alias name when allow cross app
    if (allowCrossApp(propertiesMap)) {
      microservice.setAlias(Microservice.generateAbsoluteMicroserviceName(microservice.getAppId(),
          microservice.getServiceName()));
    }

    return microservice;
  }

  /**
   * {@code service_description.description} is split by {@code ,},
   * need to combine the description array to raw description.
   */
  private void setDescription(Configuration configuration, Microservice microservice) {
    String[] descriptionArray = configuration.getStringArray(CONFIG_QUALIFIED_MICROSERVICE_DESCRIPTION_KEY);
    if (null == descriptionArray || descriptionArray.length < 1) {
      return;
    }

    StringBuilder rawDescriptionBuilder = new StringBuilder();
    for (String desc : descriptionArray) {
      rawDescriptionBuilder.append(desc).append(",");
    }

    microservice.setDescription(rawDescriptionBuilder.substring(0, rawDescriptionBuilder.length() - 1));
  }

  private boolean allowCrossApp(Map<String, String> propertiesMap) {
    return Boolean.valueOf(propertiesMap.get(CONFIG_ALLOW_CROSS_APP_KEY));
  }
}
