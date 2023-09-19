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
package org.apache.servicecomb.metrics.core;

import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.CONFIG_LATENCY_DISTRIBUTION_MIN_SCOPE_LEN;
import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.DEFAULT_METRICS_WINDOW_TIME;
import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.METRICS_WINDOW_TIME;

import java.util.List;

import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementGroupConfig;
import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementTree;
import org.apache.servicecomb.foundation.metrics.registry.GlobalRegistry;
import org.apache.servicecomb.metrics.core.meter.invocation.MeterInvocationConst;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.google.common.eventbus.EventBus;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.ManualClock;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Registry;

import mockit.Expectations;
import mockit.Mocked;

public class TestInvocationMetersInitializer {
  EventBus eventBus = new EventBus();

  GlobalRegistry globalRegistry = new GlobalRegistry(new ManualClock());

  Registry registry = new DefaultRegistry(globalRegistry.getClock());

  InvocationMetersInitializer invocationMetersInitializer = new InvocationMetersInitializer();

  @Mocked
  Invocation invocation;

  Environment environment = Mockito.mock(Environment.class);

  @Before
  public void setup() {
    Mockito.when(environment.getProperty(METRICS_WINDOW_TIME, int.class, DEFAULT_METRICS_WINDOW_TIME))
        .thenReturn(DEFAULT_METRICS_WINDOW_TIME);
    Mockito.when(environment.getProperty(
            CONFIG_LATENCY_DISTRIBUTION_MIN_SCOPE_LEN, int.class, 7))
        .thenReturn(7);
    globalRegistry.add(registry);
    invocationMetersInitializer.init(globalRegistry, eventBus, new MetricsBootstrapConfig(environment));
  }

  @Test
  public void consumerInvocation(@Mocked InvocationFinishEvent event) {
    new Expectations() {
      {
        invocation.isConsumer();
        result = true;
        invocation.getInvocationType();
        result = InvocationType.CONSUMER;
        invocation.getRealTransportName();
        result = CoreConst.RESTFUL;
        invocation.getMicroserviceQualifiedName();
        result = "m.s.o";
        invocation.getInvocationStageTrace().calcTotalTime();
        result = 9;
        invocation.getInvocationStageTrace().calcClientFiltersRequestTime();
        result = 9;
        invocation.getInvocationStageTrace().calcSendRequestTime();
        result = 9;
        invocation.getInvocationStageTrace().calcGetConnectionTime();
        result = 4;
        invocation.getInvocationStageTrace().calcWriteToBufferTime();
        result = 5;
        invocation.getInvocationStageTrace().calcWakeConsumer();
        result = 9;
        invocation.getInvocationStageTrace().calcReceiveResponseTime();
        result = 9;
        invocation.getInvocationStageTrace().calcClientFiltersResponseTime();
        result = 9;
        invocation.getInvocationStageTrace().calcInvocationPrepareTime();
        result = 9;
        invocation.getInvocationStageTrace().calcHandlersRequestTime();
        result = 9;
        invocation.getInvocationStageTrace().calcHandlersResponseTime();
        result = 9;

        event.getInvocation();
        result = invocation;
      }
    };

    eventBus.post(event);
    eventBus.post(event);

    globalRegistry.poll(1);

    MeasurementTree tree = new MeasurementTree();
    tree.from(registry.iterator(), new MeasurementGroupConfig(MeterInvocationConst.INVOCATION_NAME));
    List<Measurement> measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME).getMeasurements();
    AssertUtil.assertMeasure(measurements, 0,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=total:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=total:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=total:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, 3,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=handlers_request:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, 4,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=handlers_request:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, 5,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=handlers_request:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, 6,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=handlers_response:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, 7,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=handlers_response:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, 8,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=handlers_response:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, 9,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=prepare:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, 10,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=prepare:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, 11,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=prepare:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, 12,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=client_filters_request:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, 13,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=client_filters_request:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, 14,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=client_filters_request:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, 15,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_send_request:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, 16,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_send_request:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, 17,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_send_request:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, 18,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_get_connection:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, 19,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_get_connection:statistic=totalTime:status=0:transport=rest:type=stage,0,8.0E-9");
    AssertUtil.assertMeasure(measurements, 20,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_get_connection:statistic=max:status=0:transport=rest:type=stage,0,4.0E-9");
    AssertUtil.assertMeasure(measurements, 21,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_write_to_buf:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, 22,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_write_to_buf:statistic=totalTime:status=0:transport=rest:type=stage,0,1.0E-8");
    AssertUtil.assertMeasure(measurements, 23,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_write_to_buf:statistic=max:status=0:transport=rest:type=stage,0,5.0E-9");
    AssertUtil.assertMeasure(measurements, 24,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_wait_response:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, 25,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_wait_response:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, 26,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_wait_response:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, 27,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_wake_consumer:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, 28,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_wake_consumer:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, 29,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_wake_consumer:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, 30,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=client_filters_response:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, 31,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=client_filters_response:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, 32,
        "servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=client_filters_response:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
  }

  @Test
  public void edgeInvocation(@Mocked InvocationFinishEvent event) {
    new Expectations() {
      {
        invocation.isConsumer();
        result = true;
        invocation.isEdge();
        result = true;
        invocation.getRealTransportName();
        result = CoreConst.RESTFUL;
        invocation.getMicroserviceQualifiedName();
        result = "m.s.o";
        invocation.getInvocationStageTrace().calcTotalTime();
        result = 9;
        invocation.getInvocationStageTrace().calcThreadPoolQueueTime();
        result = 9;
        invocation.getInvocationStageTrace().calcClientFiltersRequestTime();
        result = 9;
        invocation.getInvocationStageTrace().calcSendRequestTime();
        result = 9;
        invocation.getInvocationStageTrace().calcGetConnectionTime();
        result = 4;
        invocation.getInvocationStageTrace().calcWriteToBufferTime();
        result = 5;
        invocation.getInvocationStageTrace().calcWakeConsumer();
        result = 9;
        invocation.getInvocationStageTrace().calcReceiveResponseTime();
        result = 9;
        invocation.getInvocationStageTrace().calcClientFiltersResponseTime();
        result = 9;
        invocation.getInvocationStageTrace().calcInvocationPrepareTime();
        result = 9;
        invocation.getInvocationStageTrace().calcHandlersRequestTime();
        result = 9;
        invocation.getInvocationStageTrace().calcHandlersResponseTime();
        result = 9;
        invocation.getInvocationStageTrace().calcSendResponseTime();
        result = 9;
        invocation.getInvocationStageTrace().calcServerFiltersRequestTime();
        result = 9;
        invocation.getInvocationStageTrace().calcServerFiltersResponseTime();
        result = 9;
        event.getInvocation();
        result = invocation;
      }
    };

    eventBus.post(event);
    eventBus.post(event);

    globalRegistry.poll(1);

    MeasurementTree tree = new MeasurementTree();
    tree.from(registry.iterator(), new MeasurementGroupConfig(MeterInvocationConst.INVOCATION_NAME));
    List<Measurement> measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME).getMeasurements();
    int i = 0;
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=total:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=total:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=total:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=handlers_request:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=handlers_request:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=handlers_request:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=handlers_response:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=handlers_response:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=handlers_response:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=prepare:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=prepare:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=prepare:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=client_filters_request:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=client_filters_request:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=client_filters_request:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_send_request:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_send_request:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_send_request:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_get_connection:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_get_connection:statistic=totalTime:status=0:transport=rest:type=stage,0,8.0E-9");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_get_connection:statistic=max:status=0:transport=rest:type=stage,0,4.0E-9");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_write_to_buf:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_write_to_buf:statistic=totalTime:status=0:transport=rest:type=stage,0,1.0E-8");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_write_to_buf:statistic=max:status=0:transport=rest:type=stage,0,5.0E-9");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_wait_response:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_wait_response:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_wait_response:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_wake_consumer:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_wake_consumer:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_wake_consumer:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=client_filters_response:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=client_filters_response:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=client_filters_response:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=queue:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=queue:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=queue:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=server_filters_request:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=server_filters_request:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=server_filters_request:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=server_filters_response:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=server_filters_response:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=server_filters_response:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=producer_send_response:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=producer_send_response:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, i,
        "servicecomb.invocation:operation=m.s.o:role=EDGE:stage=producer_send_response:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
  }

  @Test
  public void producerInvocation(@Mocked InvocationFinishEvent event) {
    new Expectations() {
      {
        invocation.isConsumer();
        result = false;
        invocation.getInvocationType();
        result = InvocationType.PROVIDER;
        invocation.getRealTransportName();
        result = CoreConst.RESTFUL;
        invocation.getMicroserviceQualifiedName();
        result = "m.s.o";
        invocation.getInvocationStageTrace().calcTotalTime();
        result = 9;
        invocation.getInvocationStageTrace().calcInvocationPrepareTime();
        result = 9;
        invocation.getInvocationStageTrace().calcHandlersRequestTime();
        result = 9;
        invocation.getInvocationStageTrace().calcHandlersResponseTime();
        result = 9;
        invocation.getInvocationStageTrace().calcThreadPoolQueueTime();
        result = 9;
        invocation.getInvocationStageTrace().calcBusinessTime();
        result = 9;
        invocation.getInvocationStageTrace().calcServerFiltersRequestTime();
        result = 9;
        invocation.getInvocationStageTrace().calcServerFiltersResponseTime();
        result = 9;
        invocation.getInvocationStageTrace().calcSendResponseTime();
        result = 9;
        event.getInvocation();
        result = invocation;
      }
    };

    eventBus.post(event);
    eventBus.post(event);

    globalRegistry.poll(1);

    MeasurementTree tree = new MeasurementTree();
    tree.from(registry.iterator(), new MeasurementGroupConfig(MeterInvocationConst.INVOCATION_NAME));
    List<Measurement> measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME).getMeasurements();
    int i = 0;
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=total:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=total:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=total:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=handlers_request:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=handlers_request:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=handlers_request:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=handlers_response:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=handlers_response:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=handlers_response:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=prepare:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=prepare:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=prepare:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=queue:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=queue:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=queue:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=execution:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=execution:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=execution:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=server_filters_request:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=server_filters_request:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=server_filters_request:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=server_filters_response:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=server_filters_response:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=server_filters_response:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=producer_send_response:statistic=count:status=0:transport=rest:type=stage,0,2.0");
    AssertUtil.assertMeasure(measurements, i++,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=producer_send_response:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8");
    AssertUtil.assertMeasure(measurements, i,
        "servicecomb.invocation:operation=m.s.o:role=PROVIDER:stage=producer_send_response:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9");
  }
}
