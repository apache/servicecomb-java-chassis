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

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementGroupConfig;
import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementTree;
import org.apache.servicecomb.metrics.core.meter.invocation.MeterInvocationConst;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.eventbus.EventBus;
import com.netflix.spectator.api.CompositeRegistry;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.ManualClock;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.SpectatorUtils;

import mockit.Expectations;
import mockit.Mocked;

public class TestDefaultMetricsInitializer {
  EventBus eventBus = new EventBus();

  ManualClock clock = new ManualClock();

  CompositeRegistry globalRegistry = SpectatorUtils.createCompositeRegistry(new ManualClock());

  DefaultMetricsInitializer metricsInitializer = new DefaultMetricsInitializer() {
    protected Registry createRegistry(MetricsBootstrapConfig config) {
      return new DefaultRegistry(new ManualClock());
    };
  };

  MetricsBootstrapConfig config = new MetricsBootstrapConfig();

  @Mocked
  private Invocation invocation;

  @Mocked
  private Response response;


  @Before
  public void setup() {
    metricsInitializer.init(globalRegistry, eventBus, config);
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
        invocation.getStartTime();
        result = 1;
        event.getInvocation();
        result = invocation;
        event.getNanoCurrent();
        result = 10;
      }
    };

    eventBus.post(event);
    eventBus.post(event);

    MeasurementTree tree = new MeasurementTree();
    tree.from(globalRegistry.iterator(), new MeasurementGroupConfig(MeterInvocationConst.INVOCATION_NAME));
    Assert.assertEquals(
        "[Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=total:statistic=count:status=0:transport=rest,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=CONSUMER:stage=total:statistic=totalTime:status=0:transport=rest,0,18.0)]",
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
        invocation.getStartTime();
        result = 1;
        invocation.getStartExecutionTime();
        result = 3;
        event.getNanoCurrent();
        result = 10;
        event.getInvocation();
        result = invocation;
      }
    };

    eventBus.post(event);
    eventBus.post(event);

    MeasurementTree tree = new MeasurementTree();
    tree.from(globalRegistry.iterator(), new MeasurementGroupConfig(MeterInvocationConst.INVOCATION_NAME));
    Assert.assertEquals(
        "[Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=execution:statistic=count:status=0:transport=rest,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=execution:statistic=totalTime:status=0:transport=rest,0,14.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=total:statistic=count:status=0:transport=rest,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=total:statistic=totalTime:status=0:transport=rest,0,18.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=queue:statistic=count:status=0:transport=rest,0,2.0), "
            + "Measurement(servicecomb.invocation:operation=m.s.o:role=PRODUCER:stage=queue:statistic=totalTime:status=0:transport=rest,0,4.0)]",
        tree.findChild(MeterInvocationConst.INVOCATION_NAME).getMeasurements().toString());
  }
}
