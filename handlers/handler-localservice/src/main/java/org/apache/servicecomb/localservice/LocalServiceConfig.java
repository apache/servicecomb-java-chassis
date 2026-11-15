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

package org.apache.servicecomb.localservice;

import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;

/**
 * Configuration for local service priority scheduling.
 */
public final class LocalServiceConfig {
  public static final String KEY_ENABLED = "servicecomb.localservice.enabled";

  public static final String KEY_FILTER_ENABLED = "servicecomb.loadbalance.filter.localService.enabled";

  private LocalServiceConfig() {
    // Utility class
  }

  /**
   * Check if local service listener is enabled.
   *
   * @return true if enabled, false otherwise (default: false)
   */
  public static boolean isEnabled() {
    return LegacyPropertyFactory.getBooleanProperty(KEY_ENABLED, false);
  }

  /**
   * Check if the local service priority filter is enabled.
   *
   * @return true if enabled, false otherwise (default: true)
   */
  public static boolean isFilterEnabled() {
    return LegacyPropertyFactory.getBooleanProperty(KEY_FILTER_ENABLED, true);
  }
}
