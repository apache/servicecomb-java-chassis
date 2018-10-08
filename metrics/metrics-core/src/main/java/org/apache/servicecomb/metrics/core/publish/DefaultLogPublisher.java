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

  //sample
  private static final String SIMPLE_HEADER = "%s:\n  simple:\n"
      + "    status          tps           latency                                    operation\n";

  private static final String FIRST_LINE_SIMPLE_FORMAT = "    %-15s %-13s %-42s %s\n";

  private static final String SIMPLE_FORMAT = "                    %-13s %-42s %s\n";

  //details
  private static final String PRODUCER_DETAILS_FORMAT =
      "        prepare: %-22s queue       : %-22s filtersReq : %-22s handlersReq: %s\n"
          + "        execute: %-22s handlersResp: %-22s filtersResp: %-22s sendResp   : %s\n";

  private static final String CONSUMER_DETAILS_FORMAT =
      "        prepare          : %-22s handlersReq : %-22s clientFiltersReq: %-22s sendReq     : %s\n"
          + "        getConnect       : %-22s writeBuf    : %-22s waitResp        : %-22s wakeConsumer: %s\n"
          + "        clientFiltersResp: %-22s handlersResp: %s\n";

  private static final String EDGE_DETAILS_FORMAT =
      "        prepare          : %-22s queue       : %-22s serverFiltersReq : %-22s handlersReq : %s\n"
          + "        clientFiltersReq : %-22s sendReq     : %-22s getConnect       : %-22s writeBuf    : %s\n"
          + "        waitResp         : %-22s wakeConsumer: %-22s clientFiltersResp: %-22s handlersResp: %s\n"
          + "        serverFiltersResp: %-22s sendResp    : %s\n";

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

    StringBuilder detailsBuilder = new StringBuilder();
    //print sample
    for (Map<String, OperationPerfGroup> statusMap : edgePerf.getGroups().values()) {
      for (OperationPerfGroup perfGroup : statusMap.values()) {
        //append sample
        sb.append(printSamplePerf(perfGroup));
        //append details
        detailsBuilder.append(printEdgeDetailsPerf(perfGroup));
      }
    }

    sb.append("  details:\n")
        .append(detailsBuilder);
  }


  protected void printConsumerLog(DefaultPublishModel model, StringBuilder sb) {
    OperationPerfGroups consumerPerf = model.getConsumer().getOperationPerfGroups();
    if (consumerPerf == null) {
      return;
    }

    sb.append(String.format(SIMPLE_HEADER, "consumer"));

    StringBuilder detailsBuilder = new StringBuilder();
    //print sample
    for (Map<String, OperationPerfGroup> statusMap : consumerPerf.getGroups().values()) {
      for (OperationPerfGroup perfGroup : statusMap.values()) {
        //append sample
        sb.append(printSamplePerf(perfGroup));
        //append details
        detailsBuilder.append(printConsumerDetailsPerf(perfGroup));
      }
    }
    sb.append("  details:\n")
        .append(detailsBuilder);
  }


  protected void printProducerLog(DefaultPublishModel model, StringBuilder sb) {
    OperationPerfGroups producerPerf = model.getProducer().getOperationPerfGroups();

    if (producerPerf == null) {
      return;
    }
    sb.append(String.format(SIMPLE_HEADER, "producer"));
    // use detailsBuilder, we can traverse the map only once
    StringBuilder detailsBuilder = new StringBuilder();
    //print sample
    for (Map<String, OperationPerfGroup> statusMap : producerPerf.getGroups().values()) {
      for (OperationPerfGroup perfGroup : statusMap.values()) {
        //append sample
        sb.append(printSamplePerf(perfGroup));
        //append details
        detailsBuilder.append(printProducerDetailsPerf(perfGroup));
      }
    }
    //print details
    sb.append("  details:\n")
        .append(detailsBuilder);
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
            getDetailsFromPerf(stageTotal), operationPerf.getOperation()));
      } else {
        sb.append(String
            .format(SIMPLE_FORMAT, stageTotal.getTps(), getDetailsFromPerf(stageTotal), operationPerf.getOperation()));
      }
    }
    //print summary
    OperationPerf summaryOperation = perfGroup.getSummary();
    PerfInfo stageSummaryTotal = summaryOperation.findStage(MeterInvocationConst.STAGE_TOTAL);
    sb.append(
        String.format(SIMPLE_FORMAT, stageSummaryTotal.getTps(), getDetailsFromPerf(stageSummaryTotal), "(summary)"));
    return sb;
  }

  private StringBuilder printProducerDetailsPerf(OperationPerfGroup perfGroup) {
    StringBuilder sb = new StringBuilder();
    //append rest.200:
    sb.append("    ")
        .append(perfGroup.getTransport())
        .append(".")
        .append(perfGroup.getStatus())
        .append(":\n");
    PerfInfo prepare, queue, filtersReq, handlersReq, execute, handlersResp, filtersResp, sendResp;
    for (OperationPerf operationPerf : perfGroup.getOperationPerfs()) {

      prepare = operationPerf.findStage(MeterInvocationConst.STAGE_PREPARE);
      queue = operationPerf.findStage(MeterInvocationConst.STAGE_EXECUTOR_QUEUE);
      filtersReq = operationPerf.findStage(MeterInvocationConst.STAGE_SERVER_FILTERS_REQUEST);
      handlersReq = operationPerf.findStage(MeterInvocationConst.STAGE_HANDLERS_REQUEST);
      execute = operationPerf.findStage(MeterInvocationConst.STAGE_EXECUTION);
      handlersResp = operationPerf.findStage(MeterInvocationConst.STAGE_HANDLERS_RESPONSE);
      filtersResp = operationPerf.findStage(MeterInvocationConst.STAGE_SERVER_FILTERS_RESPONSE);
      sendResp = operationPerf.findStage(MeterInvocationConst.STAGE_PRODUCER_SEND_RESPONSE);

      sb.append("      ")
          .append(operationPerf.getOperation())
          .append(":\n")
          .append(String.format(PRODUCER_DETAILS_FORMAT,
              getDetailsFromPerf(prepare),
              getDetailsFromPerf(queue),
              getDetailsFromPerf(filtersReq),
              getDetailsFromPerf(handlersReq),
              getDetailsFromPerf(execute),
              getDetailsFromPerf(handlersResp),
              getDetailsFromPerf(filtersResp),
              getDetailsFromPerf(sendResp)
          ));
    }

    return sb;
  }

  private StringBuilder printConsumerDetailsPerf(OperationPerfGroup perfGroup) {
    StringBuilder sb = new StringBuilder();
    //append rest.200:
    sb.append("    ")
        .append(perfGroup.getTransport())
        .append(".")
        .append(perfGroup.getStatus())
        .append(":\n");

    PerfInfo prepare, handlersReq, clientFiltersReq, sendReq, getConnect, writeBuf,
        waitResp, wakeConsumer, clientFiltersResp, handlersResp;
    for (OperationPerf operationPerf : perfGroup.getOperationPerfs()) {

      prepare = operationPerf.findStage(MeterInvocationConst.STAGE_PREPARE);
      handlersReq = operationPerf.findStage(MeterInvocationConst.STAGE_HANDLERS_REQUEST);
      clientFiltersReq = operationPerf.findStage(MeterInvocationConst.STAGE_CLIENT_FILTERS_REQUEST);
      sendReq = operationPerf.findStage(MeterInvocationConst.STAGE_CONSUMER_SEND_REQUEST);
      getConnect = operationPerf.findStage(MeterInvocationConst.STAGE_CONSUMER_GET_CONNECTION);
      writeBuf = operationPerf.findStage(MeterInvocationConst.STAGE_CONSUMER_WRITE_TO_BUF);
      waitResp = operationPerf.findStage(MeterInvocationConst.STAGE_CONSUMER_WAIT_RESPONSE);
      wakeConsumer = operationPerf.findStage(MeterInvocationConst.STAGE_CONSUMER_WAKE_CONSUMER);
      clientFiltersResp = operationPerf.findStage(MeterInvocationConst.STAGE_CLIENT_FILTERS_RESPONSE);
      handlersResp = operationPerf.findStage(MeterInvocationConst.STAGE_HANDLERS_RESPONSE);

      sb.append("      ")
          .append(operationPerf.getOperation())
          .append(":\n")
          .append(String.format(CONSUMER_DETAILS_FORMAT,
              getDetailsFromPerf(prepare),
              getDetailsFromPerf(handlersReq),
              getDetailsFromPerf(clientFiltersReq),
              getDetailsFromPerf(sendReq),
              getDetailsFromPerf(getConnect),
              getDetailsFromPerf(writeBuf),
              getDetailsFromPerf(waitResp),
              getDetailsFromPerf(wakeConsumer),
              getDetailsFromPerf(clientFiltersResp),
              getDetailsFromPerf(handlersResp)
          ));
    }

    return sb;
  }

  private StringBuilder printEdgeDetailsPerf(OperationPerfGroup perfGroup) {
    StringBuilder sb = new StringBuilder();
    //append rest.200:
    sb.append("    ")
        .append(perfGroup.getTransport())
        .append(".")
        .append(perfGroup.getStatus())
        .append(":\n");

    PerfInfo prepare, queue, serverFiltersReq, handlersReq, clientFiltersReq, sendReq, getConnect, writeBuf,
        waitResp, wakeConsumer, clientFiltersResp, handlersResp, serverFiltersResp, sendResp;
    for (OperationPerf operationPerf : perfGroup.getOperationPerfs()) {

      prepare = operationPerf.findStage(MeterInvocationConst.STAGE_PREPARE);
      queue = operationPerf.findStage(MeterInvocationConst.STAGE_EXECUTOR_QUEUE);
      serverFiltersReq = operationPerf.findStage(MeterInvocationConst.STAGE_SERVER_FILTERS_REQUEST);
      handlersReq = operationPerf.findStage(MeterInvocationConst.STAGE_HANDLERS_REQUEST);
      clientFiltersReq = operationPerf.findStage(MeterInvocationConst.STAGE_CLIENT_FILTERS_REQUEST);
      sendReq = operationPerf.findStage(MeterInvocationConst.STAGE_CONSUMER_SEND_REQUEST);
      getConnect = operationPerf.findStage(MeterInvocationConst.STAGE_CONSUMER_GET_CONNECTION);
      writeBuf = operationPerf.findStage(MeterInvocationConst.STAGE_CONSUMER_WRITE_TO_BUF);
      waitResp = operationPerf.findStage(MeterInvocationConst.STAGE_CONSUMER_WAIT_RESPONSE);
      wakeConsumer = operationPerf.findStage(MeterInvocationConst.STAGE_CONSUMER_WAKE_CONSUMER);
      clientFiltersResp = operationPerf.findStage(MeterInvocationConst.STAGE_CLIENT_FILTERS_RESPONSE);
      handlersResp = operationPerf.findStage(MeterInvocationConst.STAGE_HANDLERS_RESPONSE);
      serverFiltersResp = operationPerf.findStage(MeterInvocationConst.STAGE_SERVER_FILTERS_RESPONSE);
      sendResp = operationPerf.findStage(MeterInvocationConst.STAGE_PRODUCER_SEND_RESPONSE);

      sb.append("      ")
          .append(operationPerf.getOperation())
          .append(":\n")
          .append(String.format(EDGE_DETAILS_FORMAT,
              getDetailsFromPerf(prepare),
              getDetailsFromPerf(queue),
              getDetailsFromPerf(serverFiltersReq),
              getDetailsFromPerf(handlersReq),
              getDetailsFromPerf(clientFiltersReq),
              getDetailsFromPerf(sendReq),
              getDetailsFromPerf(getConnect),
              getDetailsFromPerf(writeBuf),
              getDetailsFromPerf(waitResp),
              getDetailsFromPerf(wakeConsumer),
              getDetailsFromPerf(clientFiltersResp),
              getDetailsFromPerf(handlersResp),
              getDetailsFromPerf(serverFiltersResp),
              getDetailsFromPerf(sendResp)
          ));
    }

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

  private static String getDetailsFromPerf(PerfInfo perfInfo) {
    String result = "";
    if (perfInfo != null) {
      result = String.format("%.3f/%.3f", perfInfo.calcMsLatency(), perfInfo.getMsMaxLatency());
    }
    return result;
  }
}
