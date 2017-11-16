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

package io.servicecomb.foundation.metrics.performance;

import io.servicecomb.core.Invocation;
import io.servicecomb.foundation.metrics.MetricsServoRegistry;
import io.servicecomb.swagger.invocation.InvocationType;

/**
 * Implementation of metric util functions such as preparing average queue times
 * and total requests/fail for producer and instance level.
 */
public final class MetricsDataMonitorUtil {

  /**
   * Sets the total requests per provider and consumer.
   * @param invocation invocation of request
   */
  public void setAllReqProviderAndConsumer(Invocation invocation) {
    MetricsDataMonitor metricsRef = MetricsServoRegistry.getOrCreateLocalMetrics();
    String operPath = invocation.getOperationMeta().getMicroserviceQualifiedName();

    if (InvocationType.CONSUMER.equals(invocation.getInvocationType())) {
      metricsRef.incrementTotalReqConsumer();
    } else {
      metricsRef.incrementTotalReqProvider();
      // note down metrics for operational level.
      metricsRef.setOperMetTotalReq(operPath,
          metricsRef.getOperMetTotalReq(operPath) == null ? 1L : metricsRef.getOperMetTotalReq(operPath) + 1);
    }
  }

  /**
   * Sets the total failed requests per provider and consumer.
   * @param invocation invocation of request
   */
  public void setAllFailReqProviderAndConsumer(Invocation invocation) {
    MetricsDataMonitor metricsRef = MetricsServoRegistry.getOrCreateLocalMetrics();
    String operPath = invocation.getOperationMeta().getMicroserviceQualifiedName();

    if (InvocationType.CONSUMER.equals(invocation.getInvocationType())) {
      metricsRef.incrementTotalFailReqConsumer();
    } else {
      metricsRef.incrementTotalFailReqProvider();
      // note down metrics for operational level.
      metricsRef.setOperMetTotalReq(operPath,
          metricsRef.getOperMetTotalReq(operPath) == null ? 1L : metricsRef.getOperMetTotalReq(operPath) + 1);
    }
  }
}
