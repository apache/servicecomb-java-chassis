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

package org.apache.servicecomb.faultinjection;

import static org.apache.servicecomb.faultinjection.FaultInjectionConst.CONSUMER_FAULTINJECTION;
import static org.apache.servicecomb.faultinjection.FaultInjectionConst.CONSUMER_FAULTINJECTION_GLOBAL;
import static org.apache.servicecomb.faultinjection.FaultInjectionConst.CONSUMER_FAULTINJECTION_POLICY_PROTOCOLS;
import static org.apache.servicecomb.faultinjection.FaultInjectionConst.FAULT_INJECTION_DEFAULT_VALUE;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;

import com.google.common.annotations.VisibleForTesting;

/**
 * Handles the count for all request based key[transport + microservice qualified name].
 */
public class FaultInjectionUtil {
  private FaultInjectionUtil() {
  }

  // key is config paramter
  private static final Map<String, AtomicInteger> configCenterValue = new ConcurrentHashMapEx<>();

  /**
   * Returns the map of config parameter key and values.
   * @return value of config parameter map
   */
  public static Map<String, AtomicInteger> getConfigCenterMap() {
    return configCenterValue;
  }

  /**
   * Sets the value for given config parameter.
   * @param key
   * @param value
   */
  public static void setConfigCenterValue(String key, AtomicInteger value) {
    configCenterValue.put(key, value);
  }

  /**
   * Handles the reading fault injection configuration.
   *
   * @param invocation
   *            invocation of request
   * @param key
   *            configuration key
   * @return configuration value
   */
  public static int getFaultInjectionConfig(Invocation invocation, String key) {
    int value = 0;
    String config;

    // get the config base on priority. operationName-->schema-->service-->global
    String operationName = invocation.getOperationName();
    String schema = invocation.getSchemaId();
    String serviceName = invocation.getMicroserviceName();

    config = CONSUMER_FAULTINJECTION + serviceName + ".schemas." + schema + ".operations." + operationName + "."
        + CONSUMER_FAULTINJECTION_POLICY_PROTOCOLS + invocation.getTransport().getName() + "." + key;

    value = getConfigValue(config);
    if ((value != FAULT_INJECTION_DEFAULT_VALUE)) {
      return value;
    }

    config = CONSUMER_FAULTINJECTION + serviceName + ".schemas." + schema + "."
        + CONSUMER_FAULTINJECTION_POLICY_PROTOCOLS + invocation.getTransport().getName() + "." + key;

    value = getConfigValue(config);
    if ((value != FAULT_INJECTION_DEFAULT_VALUE)) {
      return value;
    }

    config = CONSUMER_FAULTINJECTION + serviceName + "." + CONSUMER_FAULTINJECTION_POLICY_PROTOCOLS
        + invocation.getTransport().getName() + "." + key;
    value = getConfigValue(config);
    if ((value != FAULT_INJECTION_DEFAULT_VALUE)) {
      return value;
    }

    config = CONSUMER_FAULTINJECTION_GLOBAL + CONSUMER_FAULTINJECTION_POLICY_PROTOCOLS
        + invocation.getTransport().getName() + "." + key;

    value = getConfigValue(config);
    return value;
  }

  /**
   * Get the configuration value
   * @param config config parameter
   * @return int value
   */
  private static int getConfigValue(String config) {
    int value = 0;
    //first need to check in config center map which has high priority.
    Map<String, AtomicInteger> cfgMap = FaultInjectionUtil.getConfigCenterMap();

    if (cfgMap.containsKey(config)) {
      return cfgMap.get(config).get();
    }

    value = LegacyPropertyFactory.getIntProperty(config, FAULT_INJECTION_DEFAULT_VALUE);
    return value;
  }

  /**
   * It will check the delay/abort condition based on percentage.
   *
   * @return true: delay/abort is needed. false: delay/abort is not needed.
   */
  public static boolean isFaultNeedToInject(int percentage) {
    if (percentage > 0) {
      return ThreadLocalRandom.current().nextInt(100) < percentage;
    }
    return false;
  }

  @VisibleForTesting
  static Map<String, AtomicInteger> getConfigCenterValue() {
    return configCenterValue;
  }
}
