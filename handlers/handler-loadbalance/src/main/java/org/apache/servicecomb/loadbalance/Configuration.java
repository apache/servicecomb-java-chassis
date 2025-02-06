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
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;

/**
 * configuration items
 */
public final class Configuration {
  //// 2.1 configuration items
  public static final String ROOT = "servicecomb.loadbalance.";

  public static final String RULE_STRATEGY_GLOBAL = "servicecomb.loadbalance.strategy.name";

  public static final String RULE_STRATEGY_NAME = "strategy.name";

  // 2.0 configuration items
  public static final String ROOT_20 = "ribbon.";

  // SessionStickinessRule configuration
  public static final String SESSION_TIMEOUT_IN_SECONDS = "SessionStickinessRule.sessionTimeoutInSeconds";

  public static final String SUCCESSIVE_FAILED_TIMES = "SessionStickinessRule.successiveFailedTimes";

  public static final String FILTER_ISOLATION = "isolation.";

  public static final String FILTER_MAX_SINGLE_TEST_WINDOW = "maxSingleTestWindow";

  public static final String TRANSACTIONCONTROL_OPTIONS_PREFIX_PATTERN =
      "servicecomb.loadbalance.%s.transactionControl.options";

  public static final Configuration INSTANCE = new Configuration();

  public record RuleType(int type, String value) {
    public static final int TYPE_SCHEMA = 1;

    public static final int TYPE_OPERATION = 2;

    public String getValue() {
      return value;
    }

    public int getType() {
      return type;
    }
  }

  private Configuration() {
  }

  public RuleType getRuleStrategyName(Invocation invocation) {
    String value = getStringProperty(null, ROOT + invocation.getMicroserviceName() + "." +
        invocation.getSchemaId() + "." + invocation.getOperationName() + "." + RULE_STRATEGY_NAME);
    if (value != null) {
      return new RuleType(RuleType.TYPE_OPERATION, value);
    }
    value = getStringProperty(null, ROOT + invocation.getMicroserviceName() + "." +
        invocation.getSchemaId() + "." + RULE_STRATEGY_NAME);
    if (value != null) {
      return new RuleType(RuleType.TYPE_SCHEMA, value);
    }
    value = getStringProperty(null, ROOT + invocation.getMicroserviceName() + "." +
        RULE_STRATEGY_NAME);
    if (value != null) {
      return new RuleType(RuleType.TYPE_SCHEMA, value);
    }
    return new RuleType(RuleType.TYPE_SCHEMA,
        getStringProperty("RoundRobin", RULE_STRATEGY_GLOBAL));
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
}
