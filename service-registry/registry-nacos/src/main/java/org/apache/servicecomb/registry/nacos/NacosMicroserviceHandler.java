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

package org.apache.servicecomb.registry.nacos;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.config.DataCenterProperties;
import org.springframework.core.env.Environment;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.utils.NamingUtils;

public class NacosMicroserviceHandler {
  private static final String VERSION_MAPPING = "VERSION_MAPPING";

  private static final String CAS_APPLICATION_ID = "CAS_APPLICATION_ID";

  private static final String CAS_COMPONENT_NAME = "CAS_COMPONENT_NAME";

  private static final String CAS_INSTANCE_VERSION = "CAS_INSTANCE_VERSION";

  private static final String CAS_INSTANCE_ID = "CAS_INSTANCE_ID";

  private static final String CAS_ENVIRONMENT_ID = "CAS_ENVIRONMENT_ID";

  private static final String INSTANCE_PROPS = "SERVICECOMB_INSTANCE_PROPS";

  public static Instance createMicroserviceInstance(
      DataCenterProperties dataCenterProperties, NacosDiscoveryProperties properties, Environment environment) {
    Instance instance = new Instance();
    instance.setServiceName(NamingUtils.getGroupedName(
        BootStrapProperties.readServiceName(environment), BootStrapProperties.readApplication(environment)));
    instance.setWeight(properties.getWeight());
    instance.setEnabled(properties.isInstanceEnabled());
    instance.setClusterName(properties.getClusterName());
    instance.setEphemeral(properties.isEphemeral());

    Map<String, String> metadata = properties.getMetadata();
    metadata.put(NacosConst.PROPERTY_VERSION, BootStrapProperties.readServiceVersion(environment));
    metadata.put(NacosConst.PROPERTY_ALIAS, BootStrapProperties.readServiceAlias(environment));
    metadata.put(NacosConst.PROPERTY_DESCRIPTION, BootStrapProperties.readServiceDescription(environment));
    metadata.put(NacosConst.PROPERTY_DATACENTER, dataCenterProperties.getName());
    metadata.put(NacosConst.PROPERTY_REGION, dataCenterProperties.getRegion());
    metadata.put(NacosConst.PROPERTY_ZONE, dataCenterProperties.getAvailableZone());
    metadata.put(NacosConst.NACOS_STATUS, properties.getInitialStatus());
    if (!StringUtils.isEmpty(environment.getProperty(VERSION_MAPPING)) &&
        !StringUtils.isEmpty(environment.getProperty(environment.getProperty(VERSION_MAPPING)))) {
      metadata.put("version", environment.getProperty(environment.getProperty(VERSION_MAPPING)));
    }
    metadata.putAll(genCasProperties(environment));
    instance.setMetadata(metadata);
    return instance;
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
