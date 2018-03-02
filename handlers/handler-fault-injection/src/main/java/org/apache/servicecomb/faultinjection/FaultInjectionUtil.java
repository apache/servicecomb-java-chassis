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

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;

/**
 * Handles the count for all request based key[transport + microservice qualified name].
 */
public class FaultInjectionUtil {

  // key is transport+operQualifiedName
  public static Map<String, AtomicLong> requestCount = new ConcurrentHashMapEx<>();

  // key is config paramter
  public static Map<String, AtomicInteger> configCenterValue = new ConcurrentHashMapEx<>();

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
}
