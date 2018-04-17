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

  protected void printConsumerLog(DefaultPublishModel model, StringBuilder sb) {
    OperationPerfGroups consumerPerf = model.getConsumer().getOperationPerfGroups();
    if (consumerPerf == null) {
      return;
    }

    sb.append("consumer:\n");
    printConsumerPerfLog(consumerPerf, sb);
  }

  protected void printConsumerPerfLog(OperationPerfGroups consumerPerf, StringBuilder sb) {
    sb.append("  tps     latency(ms) max-latency(ms) operation\n");
    for (Map<String, OperationPerfGroup> statusMap : consumerPerf.getGroups().values()) {
      for (OperationPerfGroup perfGroup : statusMap.values()) {
        sb.append("  ")
            .append(perfGroup.getTransport())
            .append(".")
            .append(perfGroup.getStatus())
            .append(":\n");
        for (OperationPerf operationPerf : perfGroup.getOperationPerfs()) {
          printConsumerOperationPerf(operationPerf, sb);
        }

        printConsumerOperationPerf(perfGroup.getSummary(), sb);
      }
    }
  }

  protected void printConsumerOperationPerf(OperationPerf operationPerf, StringBuilder sb) {
    PerfInfo stageTotal = operationPerf.findStage(MeterInvocationConst.STAGE_TOTAL);
    sb.append(String.format("  %-7d %-11.3f %-15.3f %s\n",
        stageTotal.getTps(),
        stageTotal.calcMsLatency(),
        stageTotal.getMsMaxLatency(),
        operationPerf.getOperation()));
  }

  protected void printProducerLog(DefaultPublishModel model, StringBuilder sb) {
    OperationPerfGroups producerPerf = model.getProducer().getOperationPerfGroups();
    if (producerPerf == null) {
      return;
    }

    sb.append("producer:\n");
    sb.append(
        "  tps     latency(ms) max-latency(ms) queue(ms) max-queue(ms) execute(ms) max-execute(ms) operation\n");
    for (Map<String, OperationPerfGroup> statusMap : producerPerf.getGroups().values()) {
      for (OperationPerfGroup perfGroup : statusMap.values()) {
        sb.append("  ")
            .append(perfGroup.getTransport())
            .append(".")
            .append(perfGroup.getStatus())
            .append(":\n");
        for (OperationPerf operationPerf : perfGroup.getOperationPerfs()) {
          printProducerOperationPerf(operationPerf, sb);
        }

        printProducerOperationPerf(perfGroup.getSummary(), sb);
      }
    }
  }

  protected void printProducerOperationPerf(OperationPerf operationPerf, StringBuilder sb) {
    PerfInfo stageTotal = operationPerf.findStage(MeterInvocationConst.STAGE_TOTAL);
    PerfInfo stageQueue = operationPerf.findStage(MeterInvocationConst.STAGE_EXECUTOR_QUEUE);
    PerfInfo stageExecution = operationPerf.findStage(MeterInvocationConst.STAGE_EXECUTION);
    sb.append(String.format("  %-7d %-11.3f %-15.3f %-9.3f %-13.3f %-11.3f %-15.3f %s\n",
        stageTotal.getTps(),
        stageTotal.calcMsLatency(),
        stageTotal.getMsMaxLatency(),
        stageQueue.calcMsLatency(),
        stageQueue.getMsMaxLatency(),
        stageExecution.calcMsLatency(),
        stageExecution.getMsMaxLatency(),
        operationPerf.getOperation()));
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
