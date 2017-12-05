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

package io.servicecomb.metrics.core.extra;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixEventType;

public class DefaultHystrixCollector implements HystrixCollector {

  private static final String TPS_TOTAL_FORMAT = "servicecomb.%s.tps.total";

  private static final String TPS_FAILED_FORMAT = "servicecomb.%s.tps.failed";

  private static final String TPS_INSTANCE_TOTAL = String.format(TPS_TOTAL_FORMAT, "instance");

  private static final String TPS_INSTANCE_FAILED = String.format(TPS_FAILED_FORMAT, "instance");

  private static final String LATENCY_AVERAGE_FORMAT = "servicecomb.%s.latency.average";

  private static final String LATENCY_INSTANCE_AVERAGE = String.format(LATENCY_AVERAGE_FORMAT, "instance");

  @Override
  public Map<String, Number> collect() {
    return calculateData(getData());
  }

  private List<HystrixTpsAndLatencyData> getData() {
    return HystrixCommandMetrics.getInstances().stream().map(instance ->
        new HystrixTpsAndLatencyData(
            instance.getCommandKey().name(),
            instance.getRollingCount(HystrixEventType.SUCCESS),
            instance.getRollingCount(HystrixEventType.FAILURE),
            instance.getExecutionTimeMean(),
            instance.getProperties().metricsRollingStatisticalWindowInMilliseconds().get()))
        .collect(Collectors.toList());
  }

  protected Map<String, Number> calculateData(List<HystrixTpsAndLatencyData> tpsAndLatencyData) {
    Map<String, Number> tpsAndLatency = new HashMap<>();
    double totalInstanceLatency = 0;
    long totalInstanceCount = 0;
    double totalInstanceTps = 0;
    double totalFailedTps = 0;

    for (HystrixTpsAndLatencyData data : tpsAndLatencyData) {
      long totalCount = data.getSuccessCount() + data.getFailureCount();
      totalInstanceCount += totalCount;
      totalInstanceLatency += data.getOperationLatency() * totalCount;
      double windowTime = (double) data.getWindowInMilliseconds() / (double) 1000;
      double totalTps = (double) (totalCount) / windowTime;
      double failedTps = (double) (data.getFailureCount()) / windowTime;
      totalInstanceTps += totalTps;
      totalFailedTps += failedTps;

      tpsAndLatency.put(String.format(TPS_TOTAL_FORMAT, data.getOperationName()), totalTps);
      tpsAndLatency.put(String.format(TPS_FAILED_FORMAT, data.getOperationName()), failedTps);
      tpsAndLatency.put(String.format(LATENCY_AVERAGE_FORMAT, data.getOperationName()), data.getOperationLatency());
    }

    tpsAndLatency.put(LATENCY_INSTANCE_AVERAGE, totalInstanceLatency / (double) totalInstanceCount);
    tpsAndLatency.put(TPS_INSTANCE_TOTAL, totalInstanceTps);
    tpsAndLatency.put(TPS_INSTANCE_FAILED, totalFailedTps);
    return tpsAndLatency;
  }
}
