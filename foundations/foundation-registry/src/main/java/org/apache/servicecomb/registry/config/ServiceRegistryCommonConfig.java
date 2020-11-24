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

package org.apache.servicecomb.registry.config;

import com.netflix.config.DynamicPropertyFactory;

public class ServiceRegistryCommonConfig {
  private static final String REGISTRY_EMPTY_PROTECTION = "servicecomb.service.registry.instance.empty.protection";

  private static final String REGISTRY_FILTER_UP_INSTANCES = "servicecomb.service.registry.instance.useUpInstancesOnly";

  public static boolean isEmptyInstanceProtectionEnabled() {
    return
        DynamicPropertyFactory.getInstance()
            .getBooleanProperty(REGISTRY_EMPTY_PROTECTION,
                true).get();
  }

  public static boolean useUpInstancesOnly() {
    return
        DynamicPropertyFactory.getInstance()
            .getBooleanProperty(REGISTRY_FILTER_UP_INSTANCES,
                false).get();
  }
}
