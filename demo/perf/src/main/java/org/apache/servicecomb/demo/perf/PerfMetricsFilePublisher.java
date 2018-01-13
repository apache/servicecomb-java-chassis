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

import org.apache.servicecomb.metrics.common.ConsumerInvocationMetric;
import org.apache.servicecomb.metrics.common.ProducerInvocationMetric;
import org.apache.servicecomb.metrics.common.RegistryMetric;
import org.apache.servicecomb.metrics.core.publish.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    double cpu = metric.getInstanceMetric().getSystemMetric().getCpuLoad();
    // can not get cpu usage in windows, so skip this information
    if (cpu >= 0) {
      sb.append("cpu: ")
          .append((long) cpu * Runtime.getRuntime().availableProcessors())
          .append("%\n");
    }

    sb.append("consumer:\n"
        + "  total           tps             latency(ms)     name\n");
    for (Entry<String, ConsumerInvocationMetric> entry : metric.getConsumerMetrics().entrySet()) {
      String opName = entry.getKey();
      sb.append(String
          .format("  %-16d%-16d%-16.3f%s\n",
              entry.getValue().getConsumerCall().getTotal(),
              (long) entry.getValue().getConsumerCall().getTps(),
              entry.getValue().getConsumerLatency().getAverage(),
              opName));
    }

    sb.append("producer:\n"
        + "  total           tps             latency(ms)     queue(ms)       execute(ms)     name\n");
    for (Entry<String, ProducerInvocationMetric> entry : metric.getProducerMetrics().entrySet()) {
      String opName = entry.getKey();
      sb.append(
          String.format("  %-16d%-16d%-16.3f%-16.3f%-16.3f%s\n",
              entry.getValue().getProducerCall().getTotal(),
              (long) entry.getValue().getProducerCall().getTps(),
              entry.getValue().getProducerLatency().getAverage(),
              entry.getValue().getLifeTimeInQueue().getAverage(),
              entry.getValue().getExecutionTime().getAverage(),
              opName));
    }

    sb.setLength(sb.length() - 1);

    LOGGER.info(sb.toString());
  }
}
