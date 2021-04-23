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

package org.apache.servicecomb.serviceregistry.collect;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.deployment.DeploymentProvider;
import org.apache.servicecomb.deployment.SystemBootstrapInfo;

import java.util.Arrays;

public class ServiceCenterDefaultDeploymentProvider implements DeploymentProvider {
  public static final String SYSTEM_KEY_SERVICE_CENTER = "ServiceCenter";

  private static AbstractConfiguration configuration = ConfigUtil.createLocalConfig();

  @Override
  public SystemBootstrapInfo getSystemBootStrapInfo(String systemKey) {
    if (systemKey.contentEquals(SYSTEM_KEY_SERVICE_CENTER)) {
      SystemBootstrapInfo sc = new SystemBootstrapInfo();
      String[] urls = configuration.getStringArray("servicecomb.service.registry.address");
      if (urls == null || urls.length == 0) {
        urls = new String[]{"http://127.0.0.1:30100"};
      }
      sc.setAccessURL(Arrays.asList(urls));
      return sc;
    }
    return null;
  }

  @VisibleForTesting
  public static void setConfiguration(AbstractConfiguration configuration) {
    ServiceCenterDefaultDeploymentProvider.configuration = configuration;
  }
}
