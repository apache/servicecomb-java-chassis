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

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.metrics.MetricsConst;
import org.apache.servicecomb.foundation.metrics.publish.MetricNode;
import org.apache.servicecomb.foundation.metrics.publish.MetricsLoader;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.metrics.core.MonitorManager;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.impl.VertxImplEx;

public class PerfMetricsFilePublisher {
  private static final Logger LOGGER = LoggerFactory.getLogger(PerfMetricsFilePublisher.class);

  public void onCycle() {
    Map<String, Double> metrics = MonitorManager.getInstance().measure();
    MetricsLoader loader = new MetricsLoader(metrics);

    StringBuilder sb = new StringBuilder();
    sb.append("\n");

    collectSystemMetrics(loader, sb);
    collectVertxMetrics(loader, sb);
    collectMetrics(loader, sb);

    LOGGER.info(sb.toString());
  }

  private void collectSystemMetrics(MetricsLoader loader, StringBuilder sb) {
    double cpu = loader.getFirstMatchMetricValue(MetricsConst.JVM, MetricsConst.TAG_NAME, "cpuLoad");
    // can not get cpu usage in windows, so skip this information
    if (cpu >= 0) {
      sb.append("cpu: ")
          .append((long) cpu * Runtime.getRuntime().availableProcessors())
          .append("%\n");
    }
  }

  private void collectVertxMetrics(MetricsLoader loader, StringBuilder sb) {
    sb.append("vertx:\n")
        .append("  name       eventLoopContext-created\n");
    for (Entry<String, VertxImplEx> entry : VertxUtils.getVertxMap().entrySet()) {
      sb.append(String.format("  %-10s %-19d\n",
          entry.getKey(),
          entry.getValue().getEventLoopContextCreatedCount()));
    }
  }

  private void collectMetrics(MetricsLoader loader, StringBuilder sb) {
    MetricNode treeNode;
    try {
      treeNode = loader
          .getMetricTree(MetricsConst.SERVICECOMB_INVOCATION, MetricsConst.TAG_ROLE, MetricsConst.TAG_OPERATION,
              MetricsConst.TAG_STATUS);
    }
    //before receive any request,there are no MetricsConst.SERVICECOMB_INVOCATION,so getMetricTree will throw ServiceCombException
    catch (ServiceCombException ignored) {
      return;
    }

    if (treeNode != null && treeNode.getChildren().size() != 0) {
      MetricNode consumerNode = treeNode.getChildren().get(String.valueOf(InvocationType.CONSUMER));
      if (consumerNode != null) {
        sb.append("consumer:\n");
        sb.append("  tps     latency(ms) status  operation\n");
        for (Entry<String, MetricNode> operationNode : consumerNode.getChildren().entrySet()) {
          for (Entry<String, MetricNode> statusNode : operationNode.getValue().getChildren().entrySet()) {
            sb.append(String.format("  %-7.0f %-11.3f %-9s %s\n",
                statusNode.getValue()
                    .getFirstMatchMetricValue(MetricsConst.TAG_STAGE, MetricsConst.STAGE_TOTAL,
                        MetricsConst.TAG_STATISTIC, "tps"),
                statusNode.getValue()
                    .getFirstMatchMetricValue(TimeUnit.MILLISECONDS, MetricsConst.TAG_STAGE, MetricsConst.STAGE_TOTAL,
                        MetricsConst.TAG_STATISTIC, "latency"),
                statusNode.getKey(), operationNode.getKey()));
          }
        }
      }

      MetricNode producerNode = treeNode.getChildren().get(String.valueOf(InvocationType.PRODUCER));
      if (producerNode != null) {
        sb.append("producer:\n");
        sb.append("  tps     latency(ms) queue(ms) execute(ms) status  operation\n");
        for (Entry<String, MetricNode> operationNode : producerNode.getChildren().entrySet()) {
          for (Entry<String, MetricNode> statusNode : operationNode.getValue().getChildren().entrySet()) {
            sb.append(String.format("  %-7.0f %-11.3f %-9.3f %-11.3f %-7s %s\n",
                statusNode.getValue()
                    .getFirstMatchMetricValue(MetricsConst.TAG_STAGE, MetricsConst.STAGE_TOTAL,
                        MetricsConst.TAG_STATISTIC, "tps"),
                statusNode.getValue()
                    .getFirstMatchMetricValue(TimeUnit.MILLISECONDS, MetricsConst.TAG_STAGE, MetricsConst.STAGE_TOTAL,
                        MetricsConst.TAG_STATISTIC, "latency"),
                statusNode.getValue()
                    .getFirstMatchMetricValue(TimeUnit.MILLISECONDS, MetricsConst.TAG_STAGE, MetricsConst.STAGE_QUEUE,
                        MetricsConst.TAG_STATISTIC, "latency"),
                statusNode.getValue()
                    .getFirstMatchMetricValue(TimeUnit.MILLISECONDS, MetricsConst.TAG_STAGE,
                        MetricsConst.STAGE_EXECUTION,
                        MetricsConst.TAG_STATISTIC, "latency"),
                statusNode.getKey(), operationNode.getKey()));
          }
        }
      }
    }
  }
}
