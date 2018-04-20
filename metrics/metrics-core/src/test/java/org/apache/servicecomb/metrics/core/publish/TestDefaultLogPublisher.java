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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response.Status;
import javax.xml.ws.Holder;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.PolledEvent;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.metrics.core.meter.invocation.MeterInvocationConst;
import org.apache.servicecomb.metrics.core.publish.model.DefaultPublishModel;
import org.apache.servicecomb.metrics.core.publish.model.ThreadPoolPublishModel;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerf;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerfGroup;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerfGroups;
import org.apache.servicecomb.metrics.core.publish.model.invocation.PerfInfo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.eventbus.EventBus;
import com.netflix.spectator.api.CompositeRegistry;

import io.vertx.core.impl.VertxImplEx;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestDefaultLogPublisher {
  CompositeRegistry globalRegistry = null;

  EventBus eventBus = new EventBus();

  DefaultLogPublisher publisher = new DefaultLogPublisher();

  LogCollector collector = new LogCollector();

  @Before
  public void setup() {

  }

  @After
  public void teardown() {
    collector.teardown();
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void init_enabled_default() {
    Holder<Boolean> registered = new Holder<>(false);
    new MockUp<EventBus>(eventBus) {
      @Mock
      void register(Object object) {
        registered.value = true;
      }
    };

    publisher.init(globalRegistry, eventBus, new MetricsBootstrapConfig());
    Assert.assertFalse(registered.value);
  }

  @Test
  public void init_enabled_true() {
    Holder<Boolean> registered = new Holder<>();
    new MockUp<EventBus>(eventBus) {
      @Mock
      void register(Object object) {
        registered.value = true;
      }
    };

    ArchaiusUtils.setProperty(DefaultLogPublisher.ENABLED, true);

    publisher.init(globalRegistry, eventBus, new MetricsBootstrapConfig());
    Assert.assertTrue(registered.value);
  }

  @Test
  public void init_enabled_false() {
    Holder<Boolean> registered = new Holder<>();
    new MockUp<EventBus>(eventBus) {
      @Mock
      void register(Object object) {
        registered.value = true;
      }
    };

    ArchaiusUtils.setProperty(DefaultLogPublisher.ENABLED, false);

    publisher.init(globalRegistry, eventBus, new MetricsBootstrapConfig());
    Assert.assertNull(registered.value);
  }

  @Test
  public void onPolledEvent_failed() {
    publisher.onPolledEvent(null);

    LoggingEvent event = collector.getEvents().get(0);
    Assert.assertEquals("Failed to print perf log.", event.getMessage());
    Assert.assertEquals(NullPointerException.class, event.getThrowableInformation().getThrowable().getClass());
  }

  @Test
  public void onPolledEvent(@Mocked VertxImplEx vertxImplEx) {
    new Expectations(VertxUtils.class) {
      {
        VertxUtils.getVertxMap();
        result = Collections.singletonMap("v", vertxImplEx);
        vertxImplEx.getEventLoopContextCreatedCount();
        result = 1;
      }
    };

    DefaultPublishModel model = new DefaultPublishModel();

    PerfInfo perfTotal = new PerfInfo();
    perfTotal.setTps(10);
    perfTotal.setMsTotalTime(100);

    OperationPerf operationPerf = new OperationPerf();
    operationPerf.setOperation("op");
    operationPerf.getStages().put(MeterInvocationConst.STAGE_TOTAL, perfTotal);
    operationPerf.getStages().put(MeterInvocationConst.STAGE_EXECUTOR_QUEUE, perfTotal);
    operationPerf.getStages().put(MeterInvocationConst.STAGE_EXECUTION, perfTotal);

    OperationPerfGroup operationPerfGroup = new OperationPerfGroup(Const.RESTFUL, Status.OK.name());
    operationPerfGroup.addOperationPerf(operationPerf);

    OperationPerfGroups operationPerfGroups = new OperationPerfGroups();
    operationPerfGroups.getGroups().put(operationPerfGroup.getTransport(),
        Collections.singletonMap(operationPerfGroup.getStatus(), operationPerfGroup));
    model.getConsumer().setOperationPerfGroups(operationPerfGroups);
    model.getProducer().setOperationPerfGroups(operationPerfGroups);

    model.getThreadPools().put("test", new ThreadPoolPublishModel());

    new MockUp<PublishModelFactory>() {
      @Mock
      DefaultPublishModel createDefaultPublishModel() {
        return model;
      }
    };

    publisher.onPolledEvent(new PolledEvent(Collections.emptyList(), Collections.emptyList()));

    List<LoggingEvent> events = collector.getEvents().stream().filter(e -> {
      return DefaultLogPublisher.class.getName().equals(e.getLoggerName());
    }).collect(Collectors.toList());

    LoggingEvent event = events.get(0);
    Assert.assertEquals("\n" +
        "vertx:\n" +
        "  name       eventLoopContext-created\n" +
        "  v          1\n" +
        "threadPool:\n" +
        "  corePoolSize maxThreads poolSize currentThreadsBusy queueSize taskCount completedTaskCount name\n" +
        "  0            0          0        0                  0         0.0       0.0                test\n" +
        "consumer:\n" +
        "  tps     latency(ms) max-latency(ms) operation\n" +
        "  rest.OK:\n" +
        "  10      10.000      0.000           op\n" +
        "  10      10.000      0.000           \n" +
        "producer:\n" +
        "  tps     latency(ms) max-latency(ms) queue(ms) max-queue(ms) execute(ms) max-execute(ms) operation\n" +
        "  rest.OK:\n" +
        "  10      10.000      0.000           10.000    0.000         10.000      0.000           op\n" +
        "  10      10.000      0.000           10.000    0.000         10.000      0.000           \n" +
        "",
        event.getMessage());
  }
}
