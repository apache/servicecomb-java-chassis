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
package org.apache.servicecomb.demo.perf;

import java.util.Map.Entry;

import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.metrics.common.ConsumerInvocationMetric;
import org.apache.servicecomb.metrics.common.MetricsDimension;
import org.apache.servicecomb.metrics.common.ProducerInvocationMetric;
import org.apache.servicecomb.metrics.common.RegistryMetric;
import org.apache.servicecomb.metrics.core.publish.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.impl.VertxImplEx;

public class PerfMetricsFilePublisher {
  private static final Logger LOGGER = LoggerFactory.getLogger(PerfMetricsFilePublisher.class);

  private DataSource dataSource;

  public PerfMetricsFilePublisher(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void onCycle() {
    RegistryMetric metric = dataSource.getRegistryMetric();

    StringBuilder sb = new StringBuilder();
    sb.append("\n");

    collectSystemMetrics(metric, sb);
    collectVertxMetrics(metric, sb);
    collectConsumerMetrics(metric, sb);
    collectProducerMetrics(metric, sb);

    LOGGER.info(sb.toString());
  }

  protected void collectSystemMetrics(RegistryMetric metric, StringBuilder sb) {
    double cpu = metric.getInstanceMetric().getSystemMetric().getCpuLoad();
    // can not get cpu usage in windows, so skip this information
    if (cpu >= 0) {
      sb.append("cpu: ")
          .append((long) cpu * Runtime.getRuntime().availableProcessors())
          .append("%\n");
    }
  }

  protected void collectVertxMetrics(RegistryMetric metric, StringBuilder sb) {
    sb.append("vertx:\n")
        .append("  name       eventLoopContext-created\n");
    for (Entry<String, VertxImplEx> entry : VertxUtils.getVertxMap().entrySet()) {
      sb.append(String.format("  %-10s %-19d\n",
          entry.getKey(),
          entry.getValue().getEventLoopContextCreatedCount()));
    }
  }

  protected void collectProducerMetrics(RegistryMetric metric, StringBuilder sb) {
    if (metric.getProducerMetrics().isEmpty()) {
      return;
    }

    sb.append("producer:\n"
        + "  total               tps     latency(ms) queue(ms) execute(ms) name\n");
    for (Entry<String, ProducerInvocationMetric> entry : metric.getProducerMetrics().entrySet()) {
      String opName = entry.getKey();
      sb.append(
          String.format("  %-19d %-7d %-11.3f %-9.3f %-11.3f %s\n",
              entry.getValue()
                  .getProducerCall()
                  .getTotalValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_ALL)
                  .getValue(),
              entry.getValue()
                  .getProducerCall()
                  .getTpsValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_ALL)
                  .getValue()
                  .longValue(),
              entry.getValue().getProducerLatency().getAverage(),
              entry.getValue().getLifeTimeInQueue().getAverage(),
              entry.getValue().getExecutionTime().getAverage(),
              opName));
    }
  }

  protected void collectConsumerMetrics(RegistryMetric metric, StringBuilder sb) {
    if (metric.getConsumerMetrics().isEmpty()) {
      return;
    }

    sb.append("consumer:\n"
        + "  total               tps     latency(ms) name\n");
    for (Entry<String, ConsumerInvocationMetric> entry : metric.getConsumerMetrics().entrySet()) {
      String opName = entry.getKey();
      sb.append(String
          .format("  %-19d %-7d %-11.3f %s\n",
              entry.getValue()
                  .getConsumerCall()
                  .getTotalValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_ALL)
                  .getValue(),
              entry.getValue()
                  .getConsumerCall()
                  .getTpsValue(MetricsDimension.DIMENSION_STATUS, MetricsDimension.DIMENSION_STATUS_ALL)
                  .getValue()
                  .longValue(),
              entry.getValue().getConsumerLatency().getAverage(),
              opName));
    }
  }
}
