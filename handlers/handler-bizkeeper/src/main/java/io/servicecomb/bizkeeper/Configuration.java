/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.bizkeeper;

import com.netflix.config.DynamicPropertyFactory;

public final class Configuration {
  //isolation
  private static final String ISOLATION = "cse.isolation.";

  private static final String ISOLATION_TIMEOUTINMILLISECONDS = "timeoutInMilliseconds";

  private static final String ISOLATION_TIMEOUTINMILLISECONDS_OLD =
      ".businessKeeper.command.execution.isolation.thread.timeoutInMilliseconds";

  private static final String ISOLATION_TIMEOUTENABLED = "timeout.enabled";

  private static final String ISOLATION_MAXCONCURRENTREQUESTS = "maxConcurrentRequests";

  //circuit breaker
  private static final String CIRCUIT_BREAKER = "cse.circuitBreaker.";

  private static final String CIRCUIT_BREAKER_ENABLED = "enabled";

  private static final String CIRCUIT_BREAKER_FORCEOPEN = "forceOpen";

  private static final String CIRCUIT_BREAKER_FORCECLOSED = "forceClosed";

  private static final String CIRCUIT_BREAKER_SLEEPWINDOWINMILLISECONDS = "sleepWindowInMilliseconds";

  private static final String CIRCUIT_BREAKER_REQUESTVOLUMETHRESHOLD = "requestVolumeThreshold";

  private static final String CIRCUIT_BREAKER_ERRORTHRESHOLDPERCENTAGE = "errorThresholdPercentage";

  // fallback
  // following items only supports consumer
  private static final String FALLBACK = "cse.fallback.";

  private static final String FALLBACK_ENABLED = "enabled";

  private static final String FALLBACK_FORCE = "force";

  private static final String FALLBACK_MAXCONCURRENTREQUESTS = "maxConcurrentRequests";

  // fallbackpolicy
  private static final String FALLBACKPOLICY = "cse.fallbackpolicy.";

  private static final String FALLBACKPOLICY_POLICY = "policy";

  public static final String FALLBACKPOLICY_POLICY_THROW = "throwexception";

  public static final String FALLBACKPOLICY_POLICY_RETURN = "returnnull";

  private static final int DEFAULT_ISOLATION_TIMEOUT = 30000;

  private static final int DEFAULT_MAX_CONCURRENT_REQUESTS = 10;

  private static final int DEFAULT_SLEEP_WINDOW = 15000;

  private static final int DEFAULT_VOLUME_THRESHOLD = 20;

  private static final int DEFAULT_THRESHOLD_PERCENTAGE = 50;

  public static final Configuration INSTANCE = new Configuration();

  private Configuration() {

  }

  public int getIsolationTimeoutInMilliseconds(String type, String microserviceName,
      String qualifiedOperationName) {
    int timeout;
    String p = getProperty("30000",
        ISOLATION + type + "." + qualifiedOperationName + "." + ISOLATION_TIMEOUTINMILLISECONDS,
        ISOLATION + type + "." + microserviceName + "." + ISOLATION_TIMEOUTINMILLISECONDS,
        ISOLATION + type + "." + ISOLATION_TIMEOUTINMILLISECONDS,
        // 2.0 compatible
        type + "." + microserviceName + ISOLATION_TIMEOUTINMILLISECONDS_OLD,
        type + ".default" + ISOLATION_TIMEOUTINMILLISECONDS_OLD);
    try {
      timeout = Integer.parseInt(p);
    } catch (NumberFormatException e) {
      return DEFAULT_ISOLATION_TIMEOUT;
    }
    if (timeout > 0) {
      return timeout;
    }
    return DEFAULT_ISOLATION_TIMEOUT;
  }

  public boolean getIsolationTimeoutEnabled(String type, String microserviceName,
      String qualifiedOperationName) {
    String p = getProperty("false",
        ISOLATION + type + "." + qualifiedOperationName + "." + ISOLATION_TIMEOUTENABLED,
        ISOLATION + type + "." + microserviceName + "." + ISOLATION_TIMEOUTENABLED,
        ISOLATION + type + "." + ISOLATION_TIMEOUTENABLED);
    return Boolean.parseBoolean(p);
  }

  public int getIsolationMaxConcurrentRequests(String type, String microserviceName,
      String qualifiedOperationName) {
    int concurrentRequests;
    String p = getProperty("10",
        ISOLATION + type + "." + qualifiedOperationName + "." + ISOLATION_MAXCONCURRENTREQUESTS,
        ISOLATION + type + "." + microserviceName + "." + ISOLATION_MAXCONCURRENTREQUESTS,
        ISOLATION + type + "." + ISOLATION_MAXCONCURRENTREQUESTS);
    try {
      concurrentRequests = Integer.parseInt(p);
    } catch (NumberFormatException e) {
      return DEFAULT_MAX_CONCURRENT_REQUESTS;
    }
    if (concurrentRequests > 0) {
      return concurrentRequests;
    }
    return DEFAULT_MAX_CONCURRENT_REQUESTS;
  }

  public boolean isCircuitBreakerEnabled(String type, String microserviceName, String qualifiedOperationName) {
    String p = getProperty("true",
        CIRCUIT_BREAKER + type + "." + qualifiedOperationName + "." + CIRCUIT_BREAKER_ENABLED,
        CIRCUIT_BREAKER + type + "." + microserviceName + "." + CIRCUIT_BREAKER_ENABLED,
        CIRCUIT_BREAKER + type + "." + CIRCUIT_BREAKER_ENABLED);
    return Boolean.parseBoolean(p);
  }

  public boolean isCircuitBreakerForceOpen(String type, String microserviceName, String qualifiedOperationName) {
    String p = getProperty("false",
        CIRCUIT_BREAKER + type + "." + qualifiedOperationName + "." + CIRCUIT_BREAKER_FORCEOPEN,
        CIRCUIT_BREAKER + type + "." + microserviceName + "." + CIRCUIT_BREAKER_FORCEOPEN,
        CIRCUIT_BREAKER + type + "." + CIRCUIT_BREAKER_FORCEOPEN);
    return Boolean.parseBoolean(p);
  }

  public boolean isCircuitBreakerForceClosed(String type, String microserviceName, String qualifiedOperationName) {
    String p = getProperty("false",
        CIRCUIT_BREAKER + type + "." + qualifiedOperationName + "." + CIRCUIT_BREAKER_FORCECLOSED,
        CIRCUIT_BREAKER + type + "." + microserviceName + "." + CIRCUIT_BREAKER_FORCECLOSED,
        CIRCUIT_BREAKER + type + "." + CIRCUIT_BREAKER_FORCECLOSED);
    return Boolean.parseBoolean(p);
  }

  public int getCircuitBreakerSleepWindowInMilliseconds(String type, String microserviceName,
      String qualifiedOperationName) {
    String p = getProperty("15000",
        CIRCUIT_BREAKER + type + "." + qualifiedOperationName + "."
            + CIRCUIT_BREAKER_SLEEPWINDOWINMILLISECONDS,
        CIRCUIT_BREAKER + type + "." + microserviceName + "." + CIRCUIT_BREAKER_SLEEPWINDOWINMILLISECONDS,
        CIRCUIT_BREAKER + type + "." + CIRCUIT_BREAKER_SLEEPWINDOWINMILLISECONDS);
    try {
      return Integer.parseInt(p);
    } catch (NumberFormatException e) {
      return DEFAULT_SLEEP_WINDOW;
    }
  }

  public int getCircuitBreakerRequestVolumeThreshold(String type, String microserviceName,
      String qualifiedOperationName) {
    String p = getProperty("20",
        CIRCUIT_BREAKER + type + "." + qualifiedOperationName + "."
            + CIRCUIT_BREAKER_REQUESTVOLUMETHRESHOLD,
        CIRCUIT_BREAKER + type + "." + microserviceName + "." + CIRCUIT_BREAKER_REQUESTVOLUMETHRESHOLD,
        CIRCUIT_BREAKER + type + "." + CIRCUIT_BREAKER_REQUESTVOLUMETHRESHOLD);
    try {
      return Integer.parseInt(p);
    } catch (NumberFormatException e) {
      return DEFAULT_VOLUME_THRESHOLD;
    }
  }

  public int getCircuitBreakerErrorThresholdPercentage(String type, String microserviceName,
      String qualifiedOperationName) {
    String p = getProperty("50",
        CIRCUIT_BREAKER + type + "." + qualifiedOperationName + "."
            + CIRCUIT_BREAKER_ERRORTHRESHOLDPERCENTAGE,
        CIRCUIT_BREAKER + type + "." + microserviceName + "." + CIRCUIT_BREAKER_ERRORTHRESHOLDPERCENTAGE,
        CIRCUIT_BREAKER + type + "." + CIRCUIT_BREAKER_ERRORTHRESHOLDPERCENTAGE);
    try {
      return Integer.parseInt(p);
    } catch (NumberFormatException e) {
      return DEFAULT_THRESHOLD_PERCENTAGE;
    }
  }

  public boolean isFallbackEnabled(String type, String microserviceName, String qualifiedOperationName) {
    String p = getProperty("true",
        FALLBACK + type + "." + qualifiedOperationName + "." + FALLBACK_ENABLED,
        FALLBACK + type + "." + microserviceName + "." + FALLBACK_ENABLED,
        FALLBACK + type + "." + FALLBACK_ENABLED);
    return Boolean.parseBoolean(p);
  }

  public boolean isFallbackForce(String type, String microserviceName, String qualifiedOperationName) {
    String p = getProperty("false",
        FALLBACK + type + "." + qualifiedOperationName + "." + FALLBACK_FORCE,
        FALLBACK + type + "." + microserviceName + "." + FALLBACK_FORCE,
        FALLBACK + type + "." + FALLBACK_FORCE);
    return Boolean.parseBoolean(p);
  }

  public int getFallbackMaxConcurrentRequests(String type, String microserviceName, String qualifiedOperationName) {
    String p = getProperty("10",
        FALLBACK + type + "." + qualifiedOperationName + "." + FALLBACK_MAXCONCURRENTREQUESTS,
        FALLBACK + type + "." + microserviceName + "." + FALLBACK_MAXCONCURRENTREQUESTS,
        FALLBACK + type + "." + FALLBACK_MAXCONCURRENTREQUESTS);
    try {
      return Integer.parseInt(p);
    } catch (NumberFormatException e) {
      return DEFAULT_MAX_CONCURRENT_REQUESTS;
    }
  }

  public String getFallbackPolicyPolicy(String type, String microserviceName, String qualifiedOperationName) {
    return getProperty(null,
        FALLBACKPOLICY + type + "." + qualifiedOperationName + "." + FALLBACKPOLICY_POLICY,
        FALLBACKPOLICY + type + "." + microserviceName + "." + FALLBACKPOLICY_POLICY,
        FALLBACKPOLICY + type + "." + FALLBACKPOLICY_POLICY);
  }

  private String getProperty(String defaultValue, String... keys) {
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
}
