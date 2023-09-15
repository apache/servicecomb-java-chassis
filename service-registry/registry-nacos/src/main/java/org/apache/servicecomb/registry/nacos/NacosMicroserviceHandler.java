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

import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.MicroserviceProperties;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.springframework.core.env.Environment;

import com.alibaba.nacos.api.naming.pojo.Instance;

public class NacosMicroserviceHandler {
  private static final String VERSION_MAPPING = "VERSION_MAPPING";

  private static final String CAS_APPLICATION_ID = "CAS_APPLICATION_ID";

  private static final String CAS_COMPONENT_NAME = "CAS_COMPONENT_NAME";

  private static final String CAS_INSTANCE_VERSION = "CAS_INSTANCE_VERSION";

  private static final String CAS_INSTANCE_ID = "CAS_INSTANCE_ID";

  private static final String CAS_ENVIRONMENT_ID = "CAS_ENVIRONMENT_ID";

  private static final String INSTANCE_PROPS = "SERVICECOMB_INSTANCE_PROPS";

  public static Instance createMicroserviceInstance(NacosDiscoveryProperties properties, Environment environment,
      MicroserviceProperties microserviceProperties) {
    Instance instance = new Instance();
    instance.setIp(StringUtils.isEmpty(properties.getIp()) ? NetUtils.getHostName() : properties.getIp());
    instance.setPort(getEnvPort(environment));
    instance.setInstanceId(buildInstanceId(instance));
    instance.setWeight(properties.getWeight());
    instance.setEnabled(properties.isInstanceEnabled());
    Map<String, String> metadata = properties.getMetadata();
    metadata.put("version", microserviceProperties.getVersion());
    metadata.put("alias", microserviceProperties.getAlias());
    metadata.put("description", microserviceProperties.getDescription());
    metadata.put("secure", String.valueOf(properties.isSecure()));
    EnvironmentConfiguration envConfig = new EnvironmentConfiguration();
    if (!StringUtils.isEmpty(envConfig.getString(VERSION_MAPPING)) &&
        !StringUtils.isEmpty(envConfig.getString(envConfig.getString(VERSION_MAPPING)))) {
      metadata.put("version", envConfig.getString(VERSION_MAPPING));
    }
    metadata.putAll(genCasProperties());
    instance.setMetadata(metadata);
    instance.setClusterName(properties.getClusterName());
    instance.setEphemeral(properties.isEphemeral());
    instance.setServiceName(microserviceProperties.getName());
    return instance;
  }

  private static String buildInstanceId(Instance instance) {
    String instanceId = instance.getIp() + "-" + instance.getPort();
    return instanceId.replaceAll("[^0-9a-zA-Z]", "-");
  }

  private static int getEnvPort(Environment environment) {
    return Integer.parseInt(environment.getProperty("server.port"));
  }

  private static Map<String, String> genCasProperties() {
    Map<String, String> properties = new HashMap<>();
    EnvironmentConfiguration envConfig = new EnvironmentConfiguration();
    if (!StringUtils.isEmpty(envConfig.getString(CAS_APPLICATION_ID))) {
      properties.put(CAS_APPLICATION_ID, envConfig.getString(CAS_APPLICATION_ID));
    }
    if (!StringUtils.isEmpty(envConfig.getString(CAS_COMPONENT_NAME))) {
      properties.put(CAS_COMPONENT_NAME, envConfig.getString(CAS_COMPONENT_NAME));
    }
    if (!StringUtils.isEmpty(envConfig.getString(CAS_INSTANCE_VERSION))) {
      properties.put(CAS_INSTANCE_VERSION, envConfig.getString(CAS_INSTANCE_VERSION));
    }
    if (!StringUtils.isEmpty(envConfig.getString(CAS_INSTANCE_ID))) {
      properties.put(CAS_INSTANCE_ID, envConfig.getString(CAS_INSTANCE_ID));
    }
    if (!StringUtils.isEmpty(envConfig.getString(CAS_ENVIRONMENT_ID))) {
      properties.put(CAS_ENVIRONMENT_ID, envConfig.getString(CAS_ENVIRONMENT_ID));
    }

    String[] instancePropArray = envConfig.getStringArray(INSTANCE_PROPS);
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
