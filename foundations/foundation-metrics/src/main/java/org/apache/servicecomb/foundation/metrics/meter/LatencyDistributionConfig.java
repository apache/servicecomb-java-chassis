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
package org.apache.servicecomb.foundation.metrics.meter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LatencyDistributionConfig {
  private static final Logger LOGGER = LoggerFactory.getLogger(LatencyDistributionConfig.class);

  private List<LatencyScopeConfig> scopeConfigs = new ArrayList<>();

  /**
   *
   * @param config scope definition, time unit is milliseconds, eg:0,1,10
   */
  public LatencyDistributionConfig(String config) {
    if (StringUtils.isEmpty(config)) {
      return;
    }
    config = config.trim() + "," + Long.MAX_VALUE;
    String[] array = config.split("\\s*,+\\s*");
    try {
      for (int idx = 0; idx < array.length - 1; idx++) {
        long msMin = Long.parseLong(array[idx]);
        long msMax = Long.parseLong(array[idx + 1]);
        if (msMin >= msMax) {
          String msg = String.format("invalid latency scope, min=%s, max=%s.", array[idx], array[idx + 1]);
          throw new IllegalStateException(msg);
        }

        LatencyScopeConfig latencyScopeConfig = new LatencyScopeConfig(msMin, msMax);
        scopeConfigs.add(latencyScopeConfig);
      }
    } catch (Throwable e) {
      LOGGER.error("Failed to parse latencyDistributionConfig, value={}", config, e);
      throw e;
    }
  }

  public List<LatencyScopeConfig> getScopeConfigs() {
    return scopeConfigs;
  }
}
