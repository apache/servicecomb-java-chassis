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

package io.servicecomb.metrics.example.ewf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.netflix.servo.Metric;

@Component
public class SimpleMetricsContentConvertor implements MetricsContentConvertor {
  @Override
  public Map<String, String> convert(Map<String,Number> metrics) {
    Map<String, String> pickedMetrics = new HashMap<>();
    

    return pickedMetrics;
  }

  private boolean isTotalRequestInstanceLevelMetric(String metricName) {
    return "totalRequestsPerProvider INSTANCE_LEVEL".equals(metricName) ||
        "totalFailedRequestsPerProvider INSTANCE_LEVEL".equals(metricName) ||
        "totalRequestsPerConsumer INSTANCE_LEVEL".equals(metricName) ||
        "totalFailRequestsPerConsumer INSTANCE_LEVEL".equals(metricName);
  }

  private boolean isSystemOrTPSAndLatencyMetric(String metricName) {
    return "CPU and Memory".equals(metricName) ||
        "TPS and Latency".equals(metricName);
  }
}
