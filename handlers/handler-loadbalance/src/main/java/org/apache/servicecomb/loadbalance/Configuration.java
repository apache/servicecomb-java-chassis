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

import org.apache.servicecomb.serviceregistry.config.ConfigurePropertyUtils;

import com.netflix.config.DynamicPropertyFactory;

/**
 * configuration items
 *
 */
public final class Configuration {
  //// 2.1 configuration items
  public static final String PROP_ROOT = "cse.loadbalance.";

  public static final String PROP_POLICY = "NFLoadBalancerRuleClassName";

  public static final String PROP_RULE_STRATEGY_NAME = "strategy.name";

  // 2.0 configuration items
  public static final String PROP_ROOT_20 = "ribbon.";

  // retry configurations
  public static final String PROP_RETRY_HANDLER = "retryHandler";

  public static final String PROP_RETRY_ENABLED = "retryEnabled";

  public static final String PROP_RETRY_ONNEXT = "retryOnNext";

  public static final String PROP_RETRY_ONSAME = "retryOnSame";

  // SessionStickinessRule configruation
  public static final String SESSION_TIMEOUT_IN_SECONDS = "SessionStickinessRule.sessionTimeoutInSeconds";

  public static final String SUCCESSIVE_FAILED_TIMES = "SessionStickinessRule.successiveFailedTimes";

  // Begin: ServerListFilters configurations
  //Enabled filter lists, e.g cse.loadbalance.serverListFilters=a,b,c
  public static final String SERVER_LIST_FILTERS = "cse.loadbalance.serverListFilters";

  //Class name of each filter: e.g cse.loadbalance.serverListFilter.a.className=org.apache.servicecomb.MyServerListFilterExt
  public static final String SERVER_LIST_FILTER_CLASS_HOLDER = "cse.loadbalance.serverListFilter.%s.className";

  //Property of the class: e.g cse.loadbalance.serverListFilter.a.myproperty=sample
  public static final String SERVER_LIST_FILTER_PROPERTY_HOLDER = "cse.loadbalance.serverListFilter.%s.%s";
  //End: ServerListFilters configurations

  private static final double PERCENT = 100;

  public static final String FILTER_ISOLATION = "isolation.";

  public static final String FILTER_OPEN = "enabled";

  public static final String FILTER_ERROR_PERCENTAGE = "errorThresholdPercentage";

  public static final String FILTER_ENABLE_REQUEST = "enableRequestThreshold";

  public static final String FILTER_SINGLE_TEST = "singleTestTime";

  public static final String FILTER_CONTINUOUS_FAILURE_THRESHOLD = "continuousFailureThreshold";

  public static final String TRANSACTIONCONTROL_OPTIONS_PREFIX_PATTERN =
      "cse.loadbalance.%s.transactionControl.options";

  public static final String TRANSACTIONCONTROL_POLICY_KEY_PATTERN = "cse.loadbalance.%s.transactionControl.policy";

  public static final Configuration INSTANCE = new Configuration();

  private Configuration() {
  }

  public String getPolicy(String microservice) {
    return getStringProperty(null,
        PROP_ROOT + microservice + "." + PROP_POLICY,
        PROP_ROOT_20 + microservice + "." + PROP_POLICY,
        PROP_ROOT + PROP_POLICY,
        PROP_ROOT_20 + PROP_POLICY);
  }

  public String getRuleStrategyName(String microservice) {
    return getStringProperty(null,
        PROP_ROOT + microservice + "." + PROP_RULE_STRATEGY_NAME,
        PROP_ROOT + PROP_RULE_STRATEGY_NAME);
  }

  public int getSessionTimeoutInSeconds() {
    final int defaultValue = 30;
    // do not support MicroService level now
    String p = getStringProperty("30",
        PROP_ROOT + SESSION_TIMEOUT_IN_SECONDS);
    try {
      return Integer.parseInt(p); // can be negative
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public int getSuccessiveFailedTimes() {
    final int defaultValue = 5;
    // do not support MicroService level now
    String p = getStringProperty("5",
        PROP_ROOT + SUCCESSIVE_FAILED_TIMES);
    try {
      return Integer.parseInt(p); // can be negative
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public String getRetryHandler(String microservice) {
    return getStringProperty("default",
        PROP_ROOT + microservice + "." + PROP_RETRY_HANDLER,
        PROP_ROOT + PROP_RETRY_HANDLER);
  }

  public boolean isRetryEnabled(String microservice) {
    String p = getStringProperty("false",
        PROP_ROOT + microservice + "." + PROP_RETRY_ENABLED,
        PROP_ROOT + PROP_RETRY_ENABLED);
    return Boolean.parseBoolean(p);
  }

  public int getRetryOnNext(String microservice) {
    final int defaultValue = 0;
    String p = getStringProperty("0",
        PROP_ROOT + microservice + "." + PROP_RETRY_ONNEXT,
        PROP_ROOT + PROP_RETRY_ONNEXT);
    try {
      int result = Integer.parseInt(p);
      if (result > 0) {
        return result;
      } else {
        return defaultValue;
      }
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public int getRetryOnSame(String microservice) {
    final int defaultValue = 0;
    String p = getStringProperty("0",
        PROP_ROOT + microservice + "." + PROP_RETRY_ONSAME,
        PROP_ROOT + PROP_RETRY_ONSAME);
    try {
      int result = Integer.parseInt(p);
      if (result > 0) {
        return result;
      } else {
        return defaultValue;
      }
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public boolean isIsolationFilterOpen(String microservice) {
    String p = getStringProperty("false",
        PROP_ROOT + microservice + "." + FILTER_ISOLATION + FILTER_OPEN,
        PROP_ROOT + FILTER_ISOLATION + FILTER_OPEN);
    return Boolean.parseBoolean(p);
  }

  public int getErrorThresholdPercentage(String microservice) {
    final int defaultValue = 20;
    String p = getStringProperty("20",
        PROP_ROOT + microservice + "." + FILTER_ISOLATION + FILTER_ERROR_PERCENTAGE,
        PROP_ROOT + FILTER_ISOLATION + FILTER_ERROR_PERCENTAGE);
    try {
      int result = Integer.parseInt(p);
      if (result <= PERCENT && result > 0) {
        return result;
      } else {
        return defaultValue;
      }
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public int getEnableRequestThreshold(String microservice) {
    final int defaultValue = 20;
    String p = getStringProperty("20",
        PROP_ROOT + microservice + "." + FILTER_ISOLATION + FILTER_ENABLE_REQUEST,
        PROP_ROOT + FILTER_ISOLATION + FILTER_ENABLE_REQUEST);
    try {
      int result = Integer.parseInt(p);
      if (result > 0) {
        return result;
      } else {
        return defaultValue;
      }
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public int getSingleTestTime(String microservice) {
    final int defaultValue = 10000;
    String p = getStringProperty("10000",
        PROP_ROOT + microservice + "." + FILTER_ISOLATION + FILTER_SINGLE_TEST,
        PROP_ROOT + FILTER_ISOLATION + FILTER_SINGLE_TEST);
    try {
      int result = Integer.parseInt(p);
      if (result > 0) {
        return result;
      } else {
        return defaultValue;
      }
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public String getFlowsplitFilterPolicy(String microservice) {
    return getStringProperty("", String.format(TRANSACTIONCONTROL_POLICY_KEY_PATTERN, microservice));
  }

  public Map<String, String> getFlowsplitFilterOptions(String microservice) {
    String keyPrefix = String.format(TRANSACTIONCONTROL_OPTIONS_PREFIX_PATTERN, microservice);
    return ConfigurePropertyUtils.getPropertiesWithPrefix(keyPrefix);
  }

  public static String getStringProperty(String defaultValue, String... keys) {
    String property = null;
    for (String key : keys) {
      property = DynamicPropertyFactory.getInstance().getStringProperty(key, null).get();
      if (property != null) {
        break;
      }
    }

    if (property != null) {
      return property;
    } else {
      return defaultValue;
    }
  }

  public int getContinuousFailureThreshold(String microservice) {
    final int defaultValue = 0;
    String p = getStringProperty("0",
        PROP_ROOT + microservice + "." + FILTER_ISOLATION + FILTER_CONTINUOUS_FAILURE_THRESHOLD,
        PROP_ROOT + FILTER_ISOLATION + FILTER_CONTINUOUS_FAILURE_THRESHOLD);
    try {
      int result = Integer.parseInt(p);
      if (result > 0) {
        return result;
      } else {
        return defaultValue;
      }
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }
}
