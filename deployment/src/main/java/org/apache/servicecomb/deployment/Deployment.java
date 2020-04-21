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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;

import com.google.common.annotations.VisibleForTesting;

public class Deployment {
  /*
   * class loader scope property is used when users run java-chassis in an class loader separated environment.
   *
   * For examples, deploy two war's to web container, or deploy two bundles in an OSGI container.
   *
   * Now java chassis not testing this feature carefully, but we will support users doing so.
   *
   * users who using this feature can feed back your problems in issues.
   *
   * NOTICE： this context is better put into module `core`, however now `core` depends on `service-registry`.
   *
   * Maybe it's better to refactor project that `service-registry` depends on `core`.
   */
  private static final Map<String, String> CLASS_LOADER_SCOPE_CONTEXT = new HashMap<>();

  private static List<DeploymentProvider> providerList = SPIServiceUtils.getSortedService(DeploymentProvider.class);

  public static SystemBootstrapInfo getSystemBootStrapInfo(String systemKey) {
    for (DeploymentProvider provider : providerList) {
      if (provider.getSystemBootStrapInfo(systemKey) != null) {
        return provider.getSystemBootStrapInfo(systemKey);
      }
    }
    return null;
  }

  public static void setClassLoaderScopeProperty(String key, String value) {
    CLASS_LOADER_SCOPE_CONTEXT.put(key, value);
  }

  public static String getClassLoaderScopeProperty(String key) {
    return CLASS_LOADER_SCOPE_CONTEXT.get(key);
  }

  @VisibleForTesting
  public static void clearClassLoaderScopeProperty() {
    CLASS_LOADER_SCOPE_CONTEXT.clear();
  }
}
