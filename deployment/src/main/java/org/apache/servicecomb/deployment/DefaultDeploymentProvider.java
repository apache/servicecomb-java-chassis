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
import org.apache.servicecomb.config.ConfigUtil;

import com.google.common.annotations.VisibleForTesting;

public class DefaultDeploymentProvider implements DeploymentProvider {
  private static AbstractConfiguration configuration = ConfigUtil.createLocalConfig();

  @Override
  public SystemBootstrapInfo getSystemBootStrapInfo(String systemKey) {
    switch (systemKey) {
      case SYSTEM_KEY_SERVICE_CENTER:
        SystemBootstrapInfo sc = new SystemBootstrapInfo();
        String[] urls = configuration.getStringArray("servicecomb.service.registry.address");
        if (urls == null || urls.length == 0) {
          urls = new String[] {"https://127.0.0.1:30100"};
        }
        sc.setAccessURL(Arrays.asList(urls));
        return sc;
      case SYSTEM_KEY_CONFIG_CENTER:
        String[] ccAddresses = configuration.getStringArray("servicecomb.config.client.serverUri");
        if (ccAddresses == null || ccAddresses.length == 0) {
          return null;
        }
        SystemBootstrapInfo cc = new SystemBootstrapInfo();
        cc.setAccessURL(Arrays.asList(ccAddresses));
        return cc;
      default:
        return null;
    }
  }

  @VisibleForTesting
  public static void setConfiguration(AbstractConfiguration configuration) {
    DefaultDeploymentProvider.configuration = configuration;
  }
}
