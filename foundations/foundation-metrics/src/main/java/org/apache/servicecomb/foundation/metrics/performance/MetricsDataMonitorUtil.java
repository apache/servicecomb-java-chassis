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

package org.apache.servicecomb.foundation.metrics.performance;

import org.apache.servicecomb.foundation.metrics.MetricsServoRegistry;

/**
 * Implementation of metric util functions such as preparing average queue times
 * and total requests/fail for producer and instance level.
 */
public final class MetricsDataMonitorUtil {

  /*
   * Sets the total requests per provider and consumer.
   */
  public void setAllReqProviderAndConsumer(String operationPath, String invocationType) {
    MetricsDataMonitor metricsRef = MetricsServoRegistry.getOrCreateLocalMetrics();
    if ("CONSUMER".equals(invocationType)) {
      metricsRef.incrementTotalReqConsumer();
    } else {
      metricsRef.incrementTotalReqProvider();
      // note down metrics for operational level.
      metricsRef.setOperMetTotalReq(operationPath,
          metricsRef.getOperMetTotalReq(operationPath) == null ? 1L : metricsRef.getOperMetTotalReq(operationPath) + 1);
    }
  }

  /*
   * Sets the total failed requests per provider and consumer.
   */
  public void setAllFailReqProviderAndConsumer(String operationPath, String invocationType) {
    MetricsDataMonitor metricsRef = MetricsServoRegistry.getOrCreateLocalMetrics();
    if ("CONSUMER".equals(invocationType)) {
      metricsRef.incrementTotalFailReqConsumer();
    } else {
      metricsRef.incrementTotalFailReqProvider();
      // note down metrics for operational level.
      metricsRef.setOperMetTotalReq(operationPath,
          metricsRef.getOperMetTotalReq(operationPath) == null ? 1L : metricsRef.getOperMetTotalReq(operationPath) + 1);
    }
  }
}
