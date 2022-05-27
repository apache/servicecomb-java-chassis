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

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.servicecomb.config.ConfigUtil;

import java.util.Arrays;

public class CustomDeploymentProvider implements DeploymentProvider {
  private static AbstractConfiguration configuration = ConfigUtil.createLocalConfig();

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public SystemBootstrapInfo getSystemBootStrapInfo(String systemKey) {
    if (!systemKey.equals("TestCenter")) {
      return null;
    }
    SystemBootstrapInfo cc = new SystemBootstrapInfo();
    cc.setAccessURL(Arrays.asList("http://localhost/custom"));
    return cc;
  }

  @VisibleForTesting
  public static void setConfiguration(AbstractConfiguration configuration) {
    CustomDeploymentProvider.configuration = configuration;
  }
}
