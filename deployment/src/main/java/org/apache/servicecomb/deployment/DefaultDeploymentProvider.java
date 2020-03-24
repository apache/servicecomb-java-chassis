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

package org.apache.servicecomb.deployment;

import java.util.Arrays;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.ConfigUtil;

import com.google.common.annotations.VisibleForTesting;

public class DefaultDeploymentProvider implements DeploymentProvider {
  private static AbstractConfiguration configuration = ConfigUtil.createLocalConfig();

  @Override
  public SystemBootstrapInfo getSystemBootStrapInfo(String systemKey) {
    switch (systemKey) {
      case SYSTEM_KEY_SERVICE_CENTER_REGISTRY:
        SystemBootstrapInfo tryGetScRegistry = doGetSystemBootstrapInfo(
            "servicecomb.service.registry.registrator.address", null);
        return tryGetScRegistry != null ? tryGetScRegistry
            : doGetSystemBootstrapInfo("servicecomb.service.registry.address", "https://127.0.0.1:30100");
      case SYSTEM_KEY_SERVICE_CENTER_DISCOVERY:
        SystemBootstrapInfo tryGetScDiscovery = doGetSystemBootstrapInfo(
            "servicecomb.service.registry.serviceDiscovery.address", null);
        return tryGetScDiscovery != null ? tryGetScDiscovery
            : doGetSystemBootstrapInfo("servicecomb.service.registry.address", "https://127.0.0.1:30100");
      case SYSTEM_KEY_SERVICE_CENTER:
        return doGetSystemBootstrapInfo("servicecomb.service.registry.address", "https://127.0.0.1:30100");
      case SYSTEM_KEY_CONFIG_CENTER:
        return doGetSystemBootstrapInfo("servicecomb.config.client.serverUri", null);
      default:
        return null;
    }
  }

  private SystemBootstrapInfo doGetSystemBootstrapInfo(String config, String defaultUrl) {
    String[] urls = configuration.getStringArray(config);
    if (urls == null || urls.length == 0) {
      if (StringUtils.isEmpty(defaultUrl)) {
        return null;
      }
      urls = new String[] {defaultUrl};
    }
    SystemBootstrapInfo bootstrapInfo = new SystemBootstrapInfo();
    bootstrapInfo.setAccessURL(Arrays.asList(urls));
    return bootstrapInfo;
  }

  @VisibleForTesting
  public static void setConfiguration(AbstractConfiguration configuration) {
    DefaultDeploymentProvider.configuration = configuration;
  }
}
