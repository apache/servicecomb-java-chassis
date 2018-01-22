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

package org.apache.servicecomb.metrics.overwatch;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.metrics.common.ConsumerInvocationMetric;
import org.apache.servicecomb.metrics.common.MetricsDimension;
import org.apache.servicecomb.metrics.common.RegistryMetric;
import org.springframework.stereotype.Component;

@Component
public class DefaultMetricsConvertor implements MetricsConvertor {
  @Override
  public SystemStatus convert(String serviceName, RegistryMetric metric) {
    Map<String, Map<String, Map<String, InstanceStatus>>> allStatus = new HashMap<>();
    Map<String, Map<String, InstanceStatus>> callServiceStatus = new HashMap<>();
    allStatus.put(serviceName, callServiceStatus);
    for (Entry<String, ConsumerInvocationMetric> entry : metric.getConsumerMetrics().entrySet()) {
      String callServiceName = entry.getKey().split(".")[0];
      Map<String, InstanceStatus> instanceStatus = callServiceStatus
          .computeIfAbsent(callServiceName, s -> new HashMap<>());
      InstanceStatus status = instanceStatus.computeIfAbsent("total", s -> new InstanceStatus(0, 0));
      instanceStatus.put("total", new InstanceStatus(
          (int) (entry.getValue().getConsumerCall().getTpsValue(MetricsDimension.DIMENSION_STATUS,
              MetricsDimension.DIMENSION_STATUS_SUCCESS_FAILED_SUCCESS).getValue() * 60) + status.getRpm(),
          (int) (entry.getValue().getConsumerCall().getTpsValue(MetricsDimension.DIMENSION_STATUS,
              MetricsDimension.DIMENSION_STATUS_SUCCESS_FAILED_FAILED).getValue() * 60) + status.getFpm()));
    }
    return new SystemStatus((int) (System.currentTimeMillis() / 1000), allStatus);
  }
}
