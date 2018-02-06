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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.metrics.common.MetricsConst;
import org.apache.servicecomb.metrics.common.MetricsUtils;
import org.apache.servicecomb.metrics.core.publish.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.netflix.servo.monitor.Pollers;

import io.vertx.core.impl.VertxImplEx;

public class PerfMetricsFilePublisher {
  private static final Logger LOGGER = LoggerFactory.getLogger(PerfMetricsFilePublisher.class);

  private DataSource dataSource;

  public PerfMetricsFilePublisher(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void onCycle() {
    Map<String, Double> metrics = dataSource.getMetrics(Pollers.getPollingIntervals().get(0), true);

    StringBuilder sb = new StringBuilder();
    sb.append("\n");

    collectSystemMetrics(metrics, sb);
    collectVertxMetrics(metrics, sb);
    collectMetrics(metrics, sb);

    LOGGER.info(sb.toString());
  }

  private void collectSystemMetrics(Map<String, Double> metrics, StringBuilder sb) {
    double cpu = MetricsUtils.getFirstMatchMetricValue(metrics, MetricsConst.JVM,
        Lists.newArrayList(MetricsConst.TAG_NAME), Lists.newArrayList("cpuLoad"));
    // can not get cpu usage in windows, so skip this information
    if (cpu >= 0) {
      sb.append("cpu: ")
          .append((long) cpu * Runtime.getRuntime().availableProcessors())
          .append("%\n");
    }
  }

  private void collectVertxMetrics(Map<String, Double> metrics, StringBuilder sb) {
    sb.append("vertx:\n")
        .append("  name       eventLoopContext-created\n");
    for (Entry<String, VertxImplEx> entry : VertxUtils.getVertxMap().entrySet()) {
      sb.append(String.format("  %-10s %-19d\n",
          entry.getKey(),
          entry.getValue().getEventLoopContextCreatedCount()));
    }
  }

  private void collectMetrics(Map<String, Double> metrics, StringBuilder sb) {
    Map<String, OperationMetrics> consumerMetrics = new HashMap<>();
    Map<String, OperationMetrics> producerMetrics = new HashMap<>();

    for (Entry<String, Double> metric : metrics.entrySet()) {
      String[] nameAndTag = metric.getKey().split("\\(");
      Map<String, String> tags = new HashMap<>();
      String[] tagAnValues = nameAndTag[1].split("[=,)]");
      for (int i = 0; i < tagAnValues.length; i += 2) {
        tags.put(tagAnValues[i], tagAnValues[i + 1]);
      }
      if (MetricsConst.SERVICECOMB_INVOCATION.equals(nameAndTag[0])) {
        if (MetricsConst.ROLE_CONSUMER.equals(tags.get(MetricsConst.TAG_ROLE))) {
          if (MetricsConst.STAGE_WHOLE.equals(tags.get(MetricsConst.TAG_STAGE))) {
            setStatisticValue(metric, tags, getOperationMetrics(consumerMetrics, tags.get(MetricsConst.TAG_OPERATION)));
          }
        } else {
          if (MetricsConst.STAGE_WHOLE.equals(tags.get(MetricsConst.TAG_STAGE))) {
            setStatisticValue(metric, tags, getOperationMetrics(producerMetrics, tags.get(MetricsConst.TAG_OPERATION)));
          } else if (MetricsConst.STAGE_QUEUE.equals(tags.get(MetricsConst.TAG_STAGE))) {
            if ("latency".equals(tags.get(MetricsConst.TAG_STATISTIC))) {
              getOperationMetrics(producerMetrics, tags.get(MetricsConst.TAG_OPERATION)).setQueue(metric.getValue());
            }
          } else if (MetricsConst.STAGE_EXECUTION.equals(tags.get(MetricsConst.TAG_STAGE))) {
            if ("latency".equals(tags.get(MetricsConst.TAG_STATISTIC))) {
              getOperationMetrics(producerMetrics, tags.get(MetricsConst.TAG_OPERATION)).setExecute(metric.getValue());
            }
          }
        }
      }
    }

    if (consumerMetrics.size() != 0) {
      sb.append("consumer:\n"
          + "  total               tps     latency(ms) name\n");
      for (Entry<String, OperationMetrics> entry : consumerMetrics.entrySet()) {
        String opName = entry.getKey();
        sb.append(String
            .format("  %-19d %-7.3f %-11.3f %s\n",
                (long) entry.getValue().getTotal(),
                entry.getValue().getTps(),
                entry.getValue().getLatency(),
                opName));
      }
    }

    if (producerMetrics.size() != 0) {
      sb.append("producer:\n"
          + "  total               tps     latency(ms) queue(ms) execute(ms) name\n");
      for (Entry<String, OperationMetrics> entry : producerMetrics.entrySet()) {
        String opName = entry.getKey();
        sb.append(
            String.format("  %-19d %-7.3f %-11.3f %-9.3f %-11.3f %s\n",
                (long) entry.getValue().getTotal(),
                entry.getValue().getTps(),
                entry.getValue().getLatency(),
                entry.getValue().getQueue(),
                entry.getValue().getExecute(),
                opName));
      }
    }
  }

  private void setStatisticValue(Entry<String, Double> metric, Map<String, String> tags, OperationMetrics opMetrics) {
    if ("tps".equals(tags.get(MetricsConst.TAG_STATISTIC))) {
      opMetrics.setTps(metric.getValue());
    } else if ("count".equals(tags.get(MetricsConst.TAG_STATISTIC))) {
      opMetrics.setTotal(metric.getValue());
    } else if ("latency".equals(tags.get(MetricsConst.TAG_STATISTIC))) {
      opMetrics.setLatency(metric.getValue());
    }
  }

  private OperationMetrics getOperationMetrics(Map<String, OperationMetrics> operationMetrics, String name) {
    return operationMetrics.computeIfAbsent(name, f -> new OperationMetrics());
  }

  class OperationMetrics {
    private double total;

    private double tps;

    private double latency;

    private double queue;

    private double execute;

    public double getTotal() {
      return total;
    }

    public void setTotal(double total) {
      this.total = total;
    }

    public double getTps() {
      return tps;
    }

    public void setTps(double tps) {
      this.tps = tps;
    }

    public double getLatency() {
      return latency;
    }

    public void setLatency(double latency) {
      this.latency = latency;
    }

    public double getQueue() {
      return queue;
    }

    public void setQueue(double queue) {
      this.queue = queue;
    }

    public double getExecute() {
      return execute;
    }

    public void setExecute(double execute) {
      this.execute = execute;
    }
  }
}
