/*

 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.registry.sc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.config.DataCenterProperties;
import org.apache.servicecomb.config.MicroserviceProperties;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.apache.servicecomb.service.center.client.model.DataCenterInfo;
import org.apache.servicecomb.service.center.client.model.Framework;
import org.apache.servicecomb.service.center.client.model.HealthCheck;
import org.apache.servicecomb.service.center.client.model.HealthCheckMode;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;
import org.apache.servicecomb.service.center.client.model.MicroserviceStatus;
import org.springframework.core.env.Environment;

public class MicroserviceHandler {
  private static final String SERVICE_MAPPING = "SERVICE_MAPPING";

  private static final String VERSION_MAPPING = "VERSION_MAPPING";

  private static final String APP_MAPPING = "APP_MAPPING";

  private static final String CAS_APPLICATION_ID = "CAS_APPLICATION_ID";

  private static final String CAS_COMPONENT_NAME = "CAS_COMPONENT_NAME";

  private static final String CAS_INSTANCE_VERSION = "CAS_INSTANCE_VERSION";

  private static final String CAS_INSTANCE_ID = "CAS_INSTANCE_ID";

  private static final String CAS_ENVIRONMENT_ID = "CAS_ENVIRONMENT_ID";

  private static final String SERVICE_PROPS = "SERVICECOMB_SERVICE_PROPS";

  private static final String INSTANCE_PROPS = "SERVICECOMB_INSTANCE_PROPS";

  public static Microservice createMicroservice(
      Environment environment,
      SCConfigurationProperties bootstrapProperties,
      MicroserviceProperties microserviceProperties) {
    Microservice microservice = new Microservice();
    microservice.setProperties(microserviceProperties.getProperties());
    if (!StringUtils.isEmpty(environment.getProperty(APP_MAPPING)) &&
        !StringUtils.isEmpty(environment.getProperty(environment.getProperty(APP_MAPPING)))) {
      microservice.setAppId(environment.getProperty(environment.getProperty(APP_MAPPING)));
    } else {
      microservice.setAppId(microserviceProperties.getApplication());
    }
    if (!StringUtils.isEmpty(environment.getProperty(SERVICE_MAPPING)) &&
        !StringUtils.isEmpty(environment.getProperty(environment.getProperty(SERVICE_MAPPING)))) {
      microservice.setServiceName(environment.getProperty(environment.getProperty(SERVICE_MAPPING)));
    } else {
      microservice.setServiceName(microserviceProperties.getName());
    }
    if (!StringUtils.isEmpty(environment.getProperty(VERSION_MAPPING)) &&
        !StringUtils.isEmpty(environment.getProperty(environment.getProperty(VERSION_MAPPING)))) {
      microservice.setVersion(environment.getProperty(environment.getProperty(VERSION_MAPPING)));
    } else {
      microservice.setVersion(microserviceProperties.getVersion());
    }
    microservice.setEnvironment(microserviceProperties.getEnvironment());

    Framework framework = createFramework();
    microservice.setFramework(framework);
    String[] servicePropArray = ConfigUtil.parseArrayValue(environment.getProperty(SERVICE_PROPS))
        .toArray(new String[0]);
    if (servicePropArray.length != 0) {
      microservice.getProperties().putAll(parseProps(servicePropArray));
    }

    microservice.setStatus(MicroserviceStatus.UP);
    return microservice;
  }

  private static Framework createFramework() {
    Framework framework = new Framework();
    framework.setName("springCloud");
    framework.setVersion(MicroserviceHandler.class.getPackage().getImplementationVersion());
    return framework;
  }

  public static MicroserviceInstance createMicroserviceInstance(
      Environment environment,
      SCConfigurationProperties scConfigurationProperties,
      MicroserviceProperties microserviceProperties,
      DataCenterProperties dataCenterProperties) {
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    String hostName = StringUtils.isEmpty(scConfigurationProperties.getHostname()) ? NetUtils.getHostName()
        : scConfigurationProperties.getHostname();
    if (hostName.length() > 64) {
      hostName = hostName.substring(0, 64);
    }
    microserviceInstance.setHostName(hostName);

    if (StringUtils.isNotEmpty(dataCenterProperties.getName())) {
      DataCenterInfo dataCenterInfo = new DataCenterInfo();
      dataCenterInfo.setName(dataCenterProperties.getName());
      dataCenterInfo.setRegion(dataCenterProperties.getRegion());
      dataCenterInfo.setAvailableZone(dataCenterProperties.getAvailableZone());
      microserviceInstance.setDataCenterInfo(dataCenterInfo);
    }

    HealthCheck healthCheck = new HealthCheck();
    healthCheck.setMode(HealthCheckMode.push);
    healthCheck.setInterval(scConfigurationProperties.getHealthCheckIntervalInSeconds());
    healthCheck.setTimes(scConfigurationProperties.getHealthCheckTimes());
    microserviceInstance.setHealthCheck(healthCheck);
    String currTime = String.valueOf(System.currentTimeMillis());
    microserviceInstance.setTimestamp(currTime);
    microserviceInstance.setModTimestamp(currTime);

    // what's MicroserviceInstance doing? same sa Microservice?
    if (!StringUtils.isEmpty(environment.getProperty(VERSION_MAPPING)) &&
        !StringUtils.isEmpty(environment.getProperty(environment.getProperty(VERSION_MAPPING)))) {
      microserviceInstance.setVersion(environment.getProperty(environment.getProperty(VERSION_MAPPING)));
    } else {
      microserviceInstance.setVersion(microserviceProperties.getVersion());
    }

    Map<String, String> properties = new HashMap<>();
    properties.putAll(microserviceProperties.getProperties());
    properties.putAll(genCasProperties(environment));
    microserviceInstance.setProperties(properties);
    microserviceInstance.setStatus(MicroserviceInstanceStatus.valueOf(scConfigurationProperties.getInitialStatus()));
    return microserviceInstance;
  }

  private static Map<String, String> genCasProperties(Environment environment) {
    Map<String, String> properties = new HashMap<>();
    if (!StringUtils.isEmpty(environment.getProperty(CAS_APPLICATION_ID))) {
      properties.put(CAS_APPLICATION_ID, environment.getProperty(CAS_APPLICATION_ID));
    }
    if (!StringUtils.isEmpty(environment.getProperty(CAS_COMPONENT_NAME))) {
      properties.put(CAS_COMPONENT_NAME, environment.getProperty(CAS_COMPONENT_NAME));
    }
    if (!StringUtils.isEmpty(environment.getProperty(CAS_INSTANCE_VERSION))) {
      properties.put(CAS_INSTANCE_VERSION, environment.getProperty(CAS_INSTANCE_VERSION));
    }
    if (!StringUtils.isEmpty(environment.getProperty(CAS_INSTANCE_ID))) {
      properties.put(CAS_INSTANCE_ID, environment.getProperty(CAS_INSTANCE_ID));
    }
    if (!StringUtils.isEmpty(environment.getProperty(CAS_ENVIRONMENT_ID))) {
      properties.put(CAS_ENVIRONMENT_ID, environment.getProperty(CAS_ENVIRONMENT_ID));
    }

    String[] instancePropArray = ConfigUtil.parseArrayValue(environment.getProperty(INSTANCE_PROPS))
        .toArray(new String[0]);
    if (instancePropArray.length != 0) {
      properties.putAll(parseProps(instancePropArray));
    }

    return properties;
  }

  private static Map<String, String> parseProps(String... value) {
    return Arrays.stream(value).map(v -> v.split(":"))
        .filter(v -> v.length == 2)
        .collect(Collectors.toMap(v -> v[0], v -> v[1]));
  }
}
