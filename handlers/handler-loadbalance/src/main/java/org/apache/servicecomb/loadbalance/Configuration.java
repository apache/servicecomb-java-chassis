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

package org.apache.servicecomb.loadbalance;

import java.util.Map;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;

/**
 * configuration items
 *
 */
public final class Configuration {
  //// 2.1 configuration items
  public static final String ROOT = "servicecomb.loadbalance.";

  public static final String SERVER_EXPIRED_IN_SECONDS = "servicecomb.loadbalance.stats.serverExpiredInSeconds";

  public static final String TIMER_INTERVAL_IN_MILLIS = "servicecomb.loadbalance.stats.timerIntervalInMillis";

  public static final String RULE_STRATEGY_NAME = "strategy.name";

  // 2.0 configuration items
  public static final String ROOT_20 = "ribbon.";

  // retry configurations
  public static final String RETRY_HANDLER = "retryHandler";

  // SessionStickinessRule configruation
  public static final String SESSION_TIMEOUT_IN_SECONDS = "SessionStickinessRule.sessionTimeoutInSeconds";

  public static final String SUCCESSIVE_FAILED_TIMES = "SessionStickinessRule.successiveFailedTimes";

  private static final double PERCENT = 100;

  public static final String FILTER_ISOLATION = "isolation.";

  public static final String FILTER_OPEN = "enabled";

  public static final String FILTER_ERROR_PERCENTAGE = "errorThresholdPercentage";

  public static final String FILTER_ENABLE_REQUEST = "enableRequestThreshold";

  public static final String FILTER_RECOVER_IMMEDIATELY_WHEN_SUCCESS = "recoverImmediatelyWhenSuccess";

  public static final String FILTER_SINGLE_TEST = "singleTestTime";

  public static final String FILTER_MAX_SINGLE_TEST_WINDOW = "maxSingleTestWindow";

  public static final String FILTER_MIN_ISOLATION_TIME = "minIsolationTime";

  public static final String FILTER_CONTINUOUS_FAILURE_THRESHOLD = "continuousFailureThreshold";

  public static final String TRANSACTIONCONTROL_OPTIONS_PREFIX_PATTERN =
      "servicecomb.loadbalance.%s.transactionControl.options";

  public static final Configuration INSTANCE = new Configuration();

  private Configuration() {
  }

  public String getRuleStrategyName(String microservice) {
    return getStringProperty(null,
        ROOT + microservice + "." + RULE_STRATEGY_NAME,
        ROOT + RULE_STRATEGY_NAME);
  }

  public int getSessionTimeoutInSeconds(String microservice) {
    final int defaultValue = 30;
    String p = getStringProperty("30",
        ROOT + microservice + "." + SESSION_TIMEOUT_IN_SECONDS,
        ROOT + SESSION_TIMEOUT_IN_SECONDS);
    try {
      return Integer.parseInt(p); // can be negative
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public int getSuccessiveFailedTimes(String microservice) {
    final int defaultValue = 5;
    String p = getStringProperty("5",
        ROOT + microservice + "." + SUCCESSIVE_FAILED_TIMES,
        ROOT + SUCCESSIVE_FAILED_TIMES);
    try {
      return Integer.parseInt(p); // can be negative
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public boolean isIsolationFilterOpen(String microservice) {
    String p = getStringProperty("true",
        ROOT + microservice + "." + FILTER_ISOLATION + FILTER_OPEN,
        ROOT + FILTER_ISOLATION + FILTER_OPEN);
    return Boolean.parseBoolean(p);
  }

  public int getErrorThresholdPercentage(String microservice) {
    final int defaultValue = 0;
    String p = getStringProperty("0",
        ROOT + microservice + "." + FILTER_ISOLATION + FILTER_ERROR_PERCENTAGE,
        ROOT + FILTER_ISOLATION + FILTER_ERROR_PERCENTAGE);
    try {
      int result = Integer.parseInt(p);
      if (result <= PERCENT && result > 0) {
        return result;
      }
      return defaultValue;
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public int getEnableRequestThreshold(String microservice) {
    return getThreshold(microservice, FILTER_ENABLE_REQUEST);
  }

  public int getSingleTestTime(String microservice) {
    final int defaultValue = 60000;
    String p = getStringProperty("60000",
        ROOT + microservice + "." + FILTER_ISOLATION + FILTER_SINGLE_TEST,
        ROOT + FILTER_ISOLATION + FILTER_SINGLE_TEST);
    try {
      int result = Integer.parseInt(p);
      if (result >= 0) {
        return result;
      }
      return defaultValue;
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public int getMaxSingleTestWindow() {
    final int defaultValue = 60000;
    String p = getStringProperty(Integer.toString(defaultValue),
        ROOT + FILTER_ISOLATION + FILTER_MAX_SINGLE_TEST_WINDOW);
    try {
      int result = Integer.parseInt(p);
      if (result >= 0) {
        return result;
      }
      return defaultValue;
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public int getMinIsolationTime(String microservice) {
    final int defaultValue = 3000; // 3 seconds
    String p = getStringProperty("3000",
        ROOT + microservice + "." + FILTER_ISOLATION + FILTER_MIN_ISOLATION_TIME,
        ROOT + FILTER_ISOLATION + FILTER_MIN_ISOLATION_TIME);
    try {
      int result = Integer.parseInt(p);
      if (result >= 0) {
        return result;
      }
      return defaultValue;
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public boolean isRecoverImmediatelyWhenSuccess(String microservice) {
    String p = getStringProperty("true",
        ROOT + microservice + "." + FILTER_ISOLATION + FILTER_RECOVER_IMMEDIATELY_WHEN_SUCCESS,
        ROOT + FILTER_ISOLATION + FILTER_RECOVER_IMMEDIATELY_WHEN_SUCCESS);
    return Boolean.parseBoolean(p);
  }

  public Map<String, String> getFlowsplitFilterOptions(String microservice) {
    String keyPrefix = String.format(TRANSACTIONCONTROL_OPTIONS_PREFIX_PATTERN, microservice);
    return ConfigUtil.stringPropertiesWithPrefix(LegacyPropertyFactory.getEnvironment(), keyPrefix);
  }

  public static String getStringProperty(String defaultValue, String... keys) {
    String property;
    for (String key : keys) {
      property = LegacyPropertyFactory.getStringProperty(key);
      if (property != null) {
        return property;
      }
    }
    return defaultValue;
  }

  public int getContinuousFailureThreshold(String microservice) {
    return getThreshold(microservice, FILTER_CONTINUOUS_FAILURE_THRESHOLD);
  }

  private int getThreshold(String microservice, String threshold) {
    final int defaultValue = 5;
    String p = getStringProperty("5",
        ROOT + microservice + "." + FILTER_ISOLATION + threshold,
        ROOT + FILTER_ISOLATION + threshold);
    try {
      return Integer.parseInt(p);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }
}
