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

import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.CONFIG_LATENCY_DISTRIBUTION_MIN_SCOPE_LEN;
import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.DEFAULT_METRICS_WINDOW_TIME;
import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.METRICS_WINDOW_TIME;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.PolledEvent;
import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementNode;
import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementTree;
import org.apache.servicecomb.foundation.metrics.registry.GlobalRegistry;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.metrics.core.meter.os.OsMeter;
import org.apache.servicecomb.metrics.core.publish.model.DefaultPublishModel;
import org.apache.servicecomb.metrics.core.publish.model.ThreadPoolPublishModel;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerf;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerfGroup;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerfGroups;
import org.apache.servicecomb.metrics.core.publish.model.invocation.PerfInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.google.common.eventbus.EventBus;
import com.netflix.spectator.api.Measurement;

import io.vertx.core.impl.VertxImpl;
import jakarta.ws.rs.core.Response.Status;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestDefaultLogPublisher {
  GlobalRegistry globalRegistry = new GlobalRegistry();

  EventBus eventBus = new EventBus();

  DefaultLogPublisher publisher = new DefaultLogPublisher();

  LogCollector collector = new LogCollector();

  Environment environment = Mockito.mock(Environment.class);

  @Before
  public void setup() {
    Mockito.when(environment.getProperty(METRICS_WINDOW_TIME, int.class, DEFAULT_METRICS_WINDOW_TIME))
        .thenReturn(DEFAULT_METRICS_WINDOW_TIME);
    Mockito.when(environment.getProperty(
            CONFIG_LATENCY_DISTRIBUTION_MIN_SCOPE_LEN, int.class, 7))
        .thenReturn(7);
    publisher.setEnvironment(environment);
  }

  @After
  public void teardown() {
    collector.teardown();
  }

  @Test
  public void init_enabled_default() {
    Mockito.when(environment.getProperty(DefaultLogPublisher.ENABLED, boolean.class, false)).thenReturn(false);
    Holder<Boolean> registered = new Holder<>(false);
    new MockUp<EventBus>(eventBus) {
      @Mock
      void register(Object object) {
        registered.value = true;
      }
    };

    publisher.init(globalRegistry, eventBus, new MetricsBootstrapConfig(environment));
    Assertions.assertFalse(registered.value);
  }

  @Test
  public void init_enabled_true() {
    Mockito.when(environment.getProperty(DefaultLogPublisher.ENABLED, boolean.class, false)).thenReturn(true);
    Holder<Boolean> registered = new Holder<>();
    new MockUp<EventBus>(eventBus) {
      @Mock
      void register(Object object) {
        registered.value = true;
      }
    };

    publisher.init(globalRegistry, eventBus, new MetricsBootstrapConfig(environment));
    Assertions.assertTrue(registered.value);
  }

  @Test
  public void init_enabled_false() {
    Mockito.when(environment.getProperty(DefaultLogPublisher.ENABLED, boolean.class, false)).thenReturn(false);
    Holder<Boolean> registered = new Holder<>();
    new MockUp<EventBus>(eventBus) {
      @Mock
      void register(Object object) {
        registered.value = true;
      }
    };

    publisher.init(globalRegistry, eventBus, new MetricsBootstrapConfig(environment));
    Assertions.assertNull(registered.value);
  }

  @Test
  public void onPolledEvent_failed() {
    publisher.onPolledEvent(null);

    LogEvent event = collector.getEvents().get(0);
    Assertions.assertEquals("Failed to print perf log.", event.getMessage().getFormattedMessage());
    Assertions.assertEquals(NullPointerException.class, event.getThrown().getClass());
  }

  @Test
  public void onPolledEvent(@Injectable VertxImpl vertxImpl, @Injectable MeasurementTree tree,
      @Injectable GlobalRegistry globalRegistry, @Injectable EventBus eventBus) {
    MetricsBootstrapConfig config = Mockito.mock(MetricsBootstrapConfig.class);
    try {
      Mockito.when(environment.getProperty(DefaultLogPublisher.ENABLED, boolean.class, false)).thenReturn(true);
      Mockito.when(config.getLatencyDistribution()).thenReturn("0,1,100");
      publisher.init(globalRegistry, eventBus, config);
      new Expectations(VertxUtils.class) {
        {
          VertxUtils.getVertxMap();
          result = Collections.singletonMap("v", vertxImpl);
        }
      };
      DefaultPublishModel model = new DefaultPublishModel();
      PerfInfo perfTotal = new PerfInfo();
      perfTotal.setTps(10_0000);
      perfTotal.setMsTotalTime(30000L * 1_0000);
      perfTotal.setMsMaxLatency(30000);
      OperationPerf operationPerf = new OperationPerf();
      operationPerf.setOperation("op");
      operationPerf.setLatencyDistribution(new Integer[] {12, 120, 1200});
      operationPerf.getStages().put(InvocationStageTrace.STAGE_TOTAL, perfTotal);
      operationPerf.getStages().put(InvocationStageTrace.STAGE_PROVIDER_QUEUE, perfTotal);
      operationPerf.getStages().put(InvocationStageTrace.STAGE_CONSUMER_ENCODE_REQUEST, perfTotal);
      operationPerf.getStages().put(InvocationStageTrace.STAGE_CONSUMER_DECODE_RESPONSE, perfTotal);
      operationPerf.getStages().put(InvocationStageTrace.STAGE_PROVIDER_DECODE_REQUEST, perfTotal);
      operationPerf.getStages().put(InvocationStageTrace.STAGE_PROVIDER_ENCODE_RESPONSE, perfTotal);
      operationPerf.getStages().put(InvocationStageTrace.STAGE_PROVIDER_BUSINESS, perfTotal);
      operationPerf.getStages().put(InvocationStageTrace.STAGE_PREPARE, perfTotal);
      operationPerf.getStages().put(InvocationStageTrace.STAGE_CONSUMER_WAIT, perfTotal);
      operationPerf.getStages().put(InvocationStageTrace.STAGE_CONSUMER_SEND, perfTotal);
      operationPerf.getStages().put(InvocationStageTrace.STAGE_PROVIDER_SEND, perfTotal);
      operationPerf.getStages().put(InvocationStageTrace.STAGE_CONSUMER_CONNECTION, perfTotal);

      OperationPerfGroup operationPerfGroup = new OperationPerfGroup(CoreConst.RESTFUL, Status.OK.name());
      operationPerfGroup.addOperationPerf(operationPerf);

      OperationPerfGroups operationPerfGroups = new OperationPerfGroups();
      operationPerfGroups.getGroups().put(operationPerfGroup.getTransport(),
          Collections.singletonMap(operationPerfGroup.getStatus(), operationPerfGroup));
      model.getConsumer().setOperationPerfGroups(operationPerfGroups);
      model.getProducer().setOperationPerfGroups(operationPerfGroups);
      model.getEdge().setOperationPerfGroups(operationPerfGroups);

      model.getThreadPools().put("test", new ThreadPoolPublishModel());
      Measurement measurement = new Measurement(null, 0L, 1.0);

      MeasurementNode measurementNodeCpuAll = new MeasurementNode("allProcess", new HashMap<>());
      MeasurementNode measurementNodeCpuProcess = new MeasurementNode("currentProcess", new HashMap<>());
      MeasurementNode measurementNodeSend = new MeasurementNode("send", new HashMap<>());
      MeasurementNode measurementNodeSendPacket = new MeasurementNode("sendPackets", new HashMap<>());
      MeasurementNode measurementNodeRecv = new MeasurementNode("receive", new HashMap<>());
      MeasurementNode measurementNodeRecvPacket = new MeasurementNode("receivePackets", new HashMap<>());
      MeasurementNode measurementNodeEth0 = new MeasurementNode("eth0", new HashMap<>());
      MeasurementNode measurementNodeNet = new MeasurementNode("net", new HashMap<>());
      MeasurementNode measurementNodeOs = new MeasurementNode("os", new HashMap<>());

      measurementNodeSend.getMeasurements().add(measurement);
      measurementNodeRecv.getMeasurements().add(measurement);
      measurementNodeCpuAll.getMeasurements().add(measurement);
      measurementNodeCpuProcess.getMeasurements().add(measurement);
      measurementNodeRecvPacket.getMeasurements().add(measurement);
      measurementNodeSendPacket.getMeasurements().add(measurement);

      measurementNodeEth0.getChildren().put("send", measurementNodeSend);
      measurementNodeEth0.getChildren().put("receive", measurementNodeRecv);
      measurementNodeEth0.getChildren().put("receivePackets", measurementNodeRecvPacket);
      measurementNodeEth0.getChildren().put("sendPackets", measurementNodeSendPacket);

      measurementNodeNet.getChildren().put("eth0", measurementNodeEth0);
      measurementNodeOs.getChildren().put("cpu", measurementNodeCpuAll);
      measurementNodeOs.getChildren().put("processCpu", measurementNodeCpuProcess);
      measurementNodeOs.getChildren().put("net", measurementNodeNet);

      measurementNodeOs.getMeasurements().add(measurement);
      measurementNodeNet.getMeasurements().add(measurement);
      measurementNodeEth0.getMeasurements().add(measurement);

      new MockUp<PublishModelFactory>() {
        @Mock
        DefaultPublishModel createDefaultPublishModel() {
          return model;
        }

        @Mock
        MeasurementTree getTree() {
          return tree;
        }
      };
      new Expectations() {
        {
          tree.findChild(OsMeter.OS_NAME);
          result = measurementNodeOs;
        }
      };
      publisher.onPolledEvent(new PolledEvent(Collections.emptyList(), Collections.emptyList()));
      List<LogEvent> events = collector.getEvents().stream()
          .filter(e -> "scb-metrics".equals(e.getLoggerName())).toList();
      LogEvent event = events.get(0);
      Assertions.assertEquals("""
                            
              os:
                cpu:
                  all usage: 100.00%    all idle: 0.00%    process: 100.00%
                net:
                  send(Bps)    recv(Bps)    send(pps)    recv(pps)    interface
                  1            1            1            1            eth0
              vertx:
                instances:
                  name       eventLoopContext-created
                  v          0
              threadPool:
                coreSize maxThreads poolSize currentBusy rejected queueSize taskCount taskFinished name
                0        0          0        0           NaN      0         0.0       0.0          test
              consumer:
               simple:
                status      tps      latency            [0,1) [1,100) [100,) operation
                rest.OK     100000.0 3000.000/30000.000 12    120     1200   op
                            100000.0 3000.000/30000.000 12    120     1200   (summary)
               details:
                  rest.OK:
                    op:
                      prepare     : 3000.000/30000.000 connection : 3000.000/30000.000 encode-request: 3000.000/30000.000 send     : 3000.000/30000.000
                      wait  : 3000.000/30000.000 decode-response    : 3000.000/30000.000
              producer:
               simple:
                status      tps      latency            [0,1) [1,100) [100,) operation
                rest.OK     100000.0 3000.000/30000.000 12    120     1200   op
                            100000.0 3000.000/30000.000 12    120     1200   (summary)
               details:
                  rest.OK:
                    op:
                      prepare: 3000.000/30000.000 decode-request       : 3000.000/30000.000 queue : 3000.000/30000.000 business-execute: 3000.000/30000.000
                      encode-response: 3000.000/30000.000 send: 3000.000/30000.000
              edge:
               simple:
                status      tps      latency            [0,1) [1,100) [100,) operation
                rest.OK     100000.0 3000.000/30000.000 12    120     1200   op
                            100000.0 3000.000/30000.000 12    120     1200   (summary)
               details:
                  rest.OK:
                    op:
                      prepare     : 3000.000/30000.000 provider-decode       : 3000.000/30000.000 connection : 3000.000/30000.000 consumer-encode : 3000.000/30000.000
                      consumer-send : 3000.000/30000.000 wait     : 3000.000/30000.000 consumer-decode  : 3000.000/30000.000 provider-encode    : 3000.000/30000.000
                      provider-send    : 3000.000/30000.000
              """,
          event.getMessage().getFormattedMessage());
    } catch (Exception e) {
      e.printStackTrace();
      Assertions.fail("unexpected error happen. " + e.getMessage());
    }
  }
}
