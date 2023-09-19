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
package org.apache.servicecomb.foundation.metrics;

import org.springframework.core.env.Environment;

public class MetricsBootstrapConfig {
  public static final String METRICS_WINDOW_TIME = "servicecomb.metrics.window_time";

  public static final String CONFIG_LATENCY_DISTRIBUTION = "servicecomb.metrics.invocation.latencyDistribution";

  public static final String CONFIG_LATENCY_DISTRIBUTION_MIN_SCOPE_LEN =
      "servicecomb.metrics.publisher.defaultLog.invocation.latencyDistribution.minScopeLength";

  public static final int DEFAULT_METRICS_WINDOW_TIME = 60000;

  private long msPollInterval;

  private String latencyDistribution;

  private int minScopeLength;

  private Environment environment;

  public MetricsBootstrapConfig(Environment environment) {
    this.environment = environment;
    msPollInterval =
        environment.getProperty(METRICS_WINDOW_TIME, int.class, DEFAULT_METRICS_WINDOW_TIME);
    if (msPollInterval < 1000) {
      msPollInterval = 1000;
    }

    latencyDistribution = environment.getProperty(CONFIG_LATENCY_DISTRIBUTION, String.class);
    minScopeLength = environment.getProperty(
        CONFIG_LATENCY_DISTRIBUTION_MIN_SCOPE_LEN, int.class, 7);
  }

  public Environment getEnvironment() {
    return environment;
  }

  public long getMsPollInterval() {
    return msPollInterval;
  }

  public String getLatencyDistribution() {
    return latencyDistribution;
  }

  public int getMinScopeLength() {
    return minScopeLength;
  }
}
