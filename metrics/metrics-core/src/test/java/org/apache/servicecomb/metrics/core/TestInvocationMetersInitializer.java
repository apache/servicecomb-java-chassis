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

import static org.junit.Assert.assertEquals;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementGroupConfig;
import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementTree;
import org.apache.servicecomb.foundation.metrics.registry.GlobalRegistry;
import org.apache.servicecomb.metrics.core.meter.invocation.MeterInvocationConst;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.Before;
import org.junit.Test;

import com.google.common.eventbus.EventBus;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.ManualClock;
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

  @Mocked
  Response response;

  @Before
  public void setup() {
    globalRegistry.add(registry);
    invocationMetersInitializer.init(globalRegistry, eventBus, null);
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
        result = Const.RESTFUL;
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
    assertEquals(""
            + "[Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=total:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=total:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=total:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=handlers_request:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=handlers_request:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=handlers_request:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=handlers_response:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=handlers_response:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=handlers_response:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=prepare:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=prepare:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=prepare:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=client_filters_request:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=client_filters_request:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=client_filters_request:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_send_request:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_send_request:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_send_request:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_get_connection:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_get_connection:statistic=totalTime:status=0:transport=rest:type=stage,0,8.0E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_get_connection:statistic=max:status=0:transport=rest:type=stage,0,4.0E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_write_to_buf:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_write_to_buf:statistic=totalTime:status=0:transport=rest:type=stage,0,1.0E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_write_to_buf:statistic=max:status=0:transport=rest:type=stage,0,5.0E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_wait_response:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_wait_response:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_wait_response:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_wake_consumer:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_wake_consumer:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=consumer_wake_consumer:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=client_filters_response:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=client_filters_response:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=client_filters_response:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9)]",
        tree.findChild(MeterInvocationConst.INVOCATION_NAME).getMeasurements().toString());
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
        result = Const.RESTFUL;
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
    assertEquals(
        "[Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=total:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=total:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=total:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=handlers_request:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=handlers_request:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=handlers_request:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=handlers_response:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=handlers_response:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=handlers_response:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=prepare:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=prepare:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=prepare:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=client_filters_request:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=client_filters_request:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=client_filters_request:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_send_request:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_send_request:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_send_request:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_get_connection:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_get_connection:statistic=totalTime:status=0:transport=rest:type=stage,0,8.0E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_get_connection:statistic=max:status=0:transport=rest:type=stage,0,4.0E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_write_to_buf:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_write_to_buf:statistic=totalTime:status=0:transport=rest:type=stage,0,1.0E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_write_to_buf:statistic=max:status=0:transport=rest:type=stage,0,5.0E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_wait_response:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_wait_response:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_wait_response:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_wake_consumer:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_wake_consumer:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=consumer_wake_consumer:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=client_filters_response:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=client_filters_response:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=client_filters_response:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=queue:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=queue:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=queue:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=server_filters_request:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=server_filters_request:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=server_filters_request:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=server_filters_response:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=server_filters_response:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=server_filters_response:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=producer_send_response:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=producer_send_response:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=EDGE:stage=producer_send_response:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9)]",
        tree.findChild(MeterInvocationConst.INVOCATION_NAME).getMeasurements().toString());
  }

  @Test
  public void producerInvocation(@Mocked InvocationFinishEvent event) {
    new Expectations() {
      {
        invocation.isConsumer();
        result = false;
        invocation.getInvocationType();
        result = InvocationType.PRODUCER;
        invocation.getRealTransportName();
        result = Const.RESTFUL;
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
    assertEquals(""
            + "[Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=total:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=total:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=total:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=handlers_request:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=handlers_request:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=handlers_request:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=handlers_response:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=handlers_response:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=handlers_response:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=prepare:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=prepare:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=prepare:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=queue:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=queue:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=queue:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=execution:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=execution:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=execution:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=server_filters_request:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=server_filters_request:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=server_filters_request:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=server_filters_response:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=server_filters_response:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=server_filters_response:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=producer_send_response:statistic=count:status=0:transport=rest:type=stage,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=producer_send_response:statistic=totalTime:status=0:transport=rest:type=stage,0,1.8000000000000002E-8), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=producer_send_response:statistic=max:status=0:transport=rest:type=stage,0,9.000000000000001E-9)]",
        tree.findChild(MeterInvocationConst.INVOCATION_NAME).getMeasurements().toString());
  }
}
