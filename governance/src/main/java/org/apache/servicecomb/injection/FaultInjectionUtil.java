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

package org.apache.servicecomb.injection;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;

/**
 * Handles the count for all request based key[transport + microservice qualified name].
 */
public class FaultInjectionUtil {
  private FaultInjectionUtil() {
  }

  // key is transport+operQualifiedName
  private static Map<String, AtomicLong> requestCount = new ConcurrentHashMapEx<>();

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
