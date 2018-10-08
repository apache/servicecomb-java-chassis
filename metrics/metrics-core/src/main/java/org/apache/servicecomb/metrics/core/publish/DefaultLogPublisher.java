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
package org.apache.servicecomb.metrics.core.publish;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.MetricsInitializer;
import org.apache.servicecomb.foundation.metrics.PolledEvent;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.metrics.core.meter.invocation.MeterInvocationConst;
import org.apache.servicecomb.metrics.core.publish.model.DefaultPublishModel;
import org.apache.servicecomb.metrics.core.publish.model.ThreadPoolPublishModel;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerf;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerfGroup;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerfGroups;
import org.apache.servicecomb.metrics.core.publish.model.invocation.PerfInfo;
import org.apache.servicecomb.metrics.core.publish.statistics.MeterDetailStatisticsModel;
import org.apache.servicecomb.metrics.core.publish.statistics.MeterStatisticsManager;
import org.apache.servicecomb.metrics.core.publish.statistics.MeterStatisticsMeterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.spectator.api.CompositeRegistry;
import com.netflix.spectator.api.Meter;

import io.vertx.core.impl.VertxImplEx;

public class DefaultLogPublisher implements MetricsInitializer {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLogPublisher.class);

  public static final String ENABLED = "servicecomb.metrics.publisher.defaultLog.enabled";

  private static final String SIMPLE_HEADER = "%s:\n  simple:\n"
      + "    status          tps           latency                                    operation\n";

  private static final String FIRST_LINE_SIMPLE_FORMAT = "    %-15s %-13s %-42s %s\n";

  private static final String SIMPLE_FORMAT = "                    %-13s %-42s %s\n";


  @Override
  public void init(CompositeRegistry globalRegistry, EventBus eventBus, MetricsBootstrapConfig config) {
    if (!DynamicPropertyFactory.getInstance()
        .getBooleanProperty(ENABLED, false)
        .get()) {
      return;
    }

    eventBus.register(this);
  }

  @Subscribe
  public void onPolledEvent(PolledEvent event) {
    try {
      printLog(event.getMeters());
    } catch (Throwable e) {
      // make development easier
      LOGGER.error("Failed to print perf log.", e);
    }
  }

  protected void printLog(List<Meter> meters) {
    StringBuilder sb = new StringBuilder();
    sb.append("\n");

    printVertxMetrics(sb);

    PublishModelFactory factory = new PublishModelFactory(meters);
    DefaultPublishModel model = factory.createDefaultPublishModel();

    printThreadPoolMetrics(model, sb);

    printConsumerLog(model, sb);
    printProducerLog(model, sb);
    printEdgeLog(model, sb);

    LOGGER.info(sb.toString());
  }

  protected void printThreadPoolMetrics(DefaultPublishModel model, StringBuilder sb) {
    if (model.getThreadPools().isEmpty()) {
      return;
    }

    sb.append("threadPool:\n");
    sb.append("  corePoolSize maxThreads poolSize currentThreadsBusy queueSize taskCount completedTaskCount name\n");
    for (Entry<String, ThreadPoolPublishModel> entry : model.getThreadPools().entrySet()) {
      ThreadPoolPublishModel threadPoolPublishModel = entry.getValue();
      sb.append(String.format("  %-12d %-10d %-8d %-18d %-9d %-9.1f %-18.1f %s\n",
          threadPoolPublishModel.getCorePoolSize(),
          threadPoolPublishModel.getMaxThreads(),
          threadPoolPublishModel.getPoolSize(),
          threadPoolPublishModel.getCurrentThreadsBusy(),
          threadPoolPublishModel.getQueueSize(),
          threadPoolPublishModel.getAvgTaskCount(),
          threadPoolPublishModel.getAvgCompletedTaskCount(),
          entry.getKey()));
    }
  }

  protected void printEdgeLog(DefaultPublishModel model, StringBuilder sb) {
    OperationPerfGroups edgePerf = model.getEdge().getOperationPerfGroups();
    if (edgePerf == null) {
      return;
    }
    sb.append(String.format(SIMPLE_HEADER, "edge"));
    Map<String, MeterDetailStatisticsModel> statisticsModelMap = new HashMap<>();

    StringBuilder sampleBuilder = new StringBuilder();
    //print sample
    for (Map<String, OperationPerfGroup> statusMap : edgePerf.getGroups().values()) {
      for (OperationPerfGroup perfGroup : statusMap.values()) {
        //append sample
        sampleBuilder.append(printSamplePerf(perfGroup));
        //load details
        MeterStatisticsManager
            .loadMeterDetailStatisticsModelFromPerfGroup(perfGroup, MeterStatisticsMeterType.EDGE, statisticsModelMap);
      }
    }
    sb.append(sampleBuilder)
        .append("  details:\n");
    statisticsModelMap.values()
        .forEach(details -> sb.append(details.getFormatDetails()));
  }


  protected void printConsumerLog(DefaultPublishModel model, StringBuilder sb) {
    OperationPerfGroups consumerPerf = model.getConsumer().getOperationPerfGroups();
    if (consumerPerf == null) {
      return;
    }

    sb.append(String.format(SIMPLE_HEADER, "consumer"));
    Map<String, MeterDetailStatisticsModel> statisticsModelMap = new HashMap<>();

    StringBuilder sampleBuilder = new StringBuilder();
    //print sample
    for (Map<String, OperationPerfGroup> statusMap : consumerPerf.getGroups().values()) {
      for (OperationPerfGroup perfGroup : statusMap.values()) {
        //append sample
        sampleBuilder.append(printSamplePerf(perfGroup));
        //load details
        MeterStatisticsManager
            .loadMeterDetailStatisticsModelFromPerfGroup(perfGroup, MeterStatisticsMeterType.CONSUMER,
                statisticsModelMap);
      }
    }
    sb.append(sampleBuilder)
        .append("  details:\n");
    statisticsModelMap.values()
        .forEach(details -> sb.append(details.getFormatDetails()));
  }


  protected void printProducerLog(DefaultPublishModel model, StringBuilder sb) {
    OperationPerfGroups producerPerf = model.getProducer().getOperationPerfGroups();

    if (producerPerf == null) {
      return;
    }
    sb.append(String.format(SIMPLE_HEADER, "producer"));
    Map<String, MeterDetailStatisticsModel> statisticsModelMap = new HashMap<>();

    StringBuilder sampleBuilder = new StringBuilder();
    //print sample
    for (Map<String, OperationPerfGroup> statusMap : producerPerf.getGroups().values()) {
      for (OperationPerfGroup perfGroup : statusMap.values()) {
        //append sample
        sampleBuilder.append(printSamplePerf(perfGroup));
        //load details
        MeterStatisticsManager
            .loadMeterDetailStatisticsModelFromPerfGroup(perfGroup, MeterStatisticsMeterType.PRODUCER,
                statisticsModelMap);
      }
    }
    //print details
    sb.append(sampleBuilder)
        .append("  details:\n");
    statisticsModelMap.values().forEach(details -> sb.append(details.getFormatDetails()));
  }


  private StringBuilder printSamplePerf(OperationPerfGroup perfGroup) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < perfGroup.getOperationPerfs().size(); i++) {
      OperationPerf operationPerf = perfGroup.getOperationPerfs().get(i);
      PerfInfo stageTotal = operationPerf.findStage(MeterInvocationConst.STAGE_TOTAL);
      if (i == 0) {
        // first line
        String status = perfGroup.getTransport() + "." + perfGroup.getStatus();
        sb.append(String.format(FIRST_LINE_SIMPLE_FORMAT, status, stageTotal.getTps(),
            MeterStatisticsManager.getDetailsFromPerf(stageTotal),
            operationPerf.getOperation()));
      } else {
        sb.append(String
            .format(SIMPLE_FORMAT, stageTotal.getTps(), MeterStatisticsManager.getDetailsFromPerf(stageTotal),
                operationPerf.getOperation()));
      }
    }
    //print summary
    OperationPerf summaryOperation = perfGroup.getSummary();
    PerfInfo stageSummaryTotal = summaryOperation.findStage(MeterInvocationConst.STAGE_TOTAL);
    sb.append(
        String.format(SIMPLE_FORMAT, stageSummaryTotal.getTps(),
            MeterStatisticsManager.getDetailsFromPerf(stageSummaryTotal), "(summary)"));
    return sb;
  }


  protected void printVertxMetrics(StringBuilder sb) {
    sb.append("vertx:\n")
        .append("  name       eventLoopContext-created\n");
    for (Entry<String, VertxImplEx> entry : VertxUtils.getVertxMap().entrySet()) {
      sb.append(String.format("  %-10s %d\n",
          entry.getKey(),
          entry.getValue().getEventLoopContextCreatedCount()));
    }
  }
}
