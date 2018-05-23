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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;

/**
 * Handles the count for all request based key[transport + microservice qualified name].
 */
public class FaultInjectionUtil {

  // key is transport+operQualifiedName
  private static Map<String, AtomicLong> requestCount = new ConcurrentHashMapEx<>();

  // key is config paramter
  private static Map<String, AtomicInteger> configCenterValue = new ConcurrentHashMapEx<>();

  /**
   * Returns total requests per provider for operational level.
   *
   * @param key
   *            transport+operational name
   * @return long total requests
   */
  public static AtomicLong getOperMetTotalReq(String key) {
    return requestCount.computeIfAbsent(key, p -> new AtomicLong(1));
  }

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

    value = FaultInjectionConfig.getConfigVal(config, FAULT_INJECTION_DEFAULT_VALUE);
    return value;
  }

  /**
   * It will check the delay/abort condition based on request count and percentage
   * received.
   *
   * @param reqCount
   * @param percentage
   * @return true: delay/abort is needed. false: delay/abort is not needed.
   */
  public static boolean isFaultNeedToInject(long reqCount, int percentage) {
    /*
     * Example: delay/abort percentage configured is 10% and Get the count(suppose
     * if it is 10th request) from map and calculate resultNew(10th request) and
     * requestOld(9th request). Like this for every request it will calculate
     * current request count and previous count. if both not matched need to add
     * delay/abort otherwise no need to add.
     */

    // calculate the value with current request count.
    long resultNew = (reqCount * percentage) / 100;

    // calculate the value with previous count value.
    long resultOld = ((reqCount - 1) * percentage) / 100;

    // if both are not matching then delay/abort should be added.
    return (resultNew != resultOld);
  }
}
