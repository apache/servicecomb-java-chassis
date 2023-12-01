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
import org.apache.servicecomb.foundation.metrics.publish.MeasurementGroupConfig;
import org.apache.servicecomb.foundation.metrics.publish.MeasurementTree;
import org.apache.servicecomb.metrics.core.meter.invocation.MeterInvocationConst;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.google.common.eventbus.EventBus;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import mockit.Expectations;
import mockit.Mocked;

public class TestInvocationMetersInitializer {
  EventBus eventBus = new EventBus();

  MeterRegistry registry = new SimpleMeterRegistry();

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
    invocationMetersInitializer.init(registry, eventBus, new MetricsBootstrapConfig(environment));
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
        invocation.getInvocationStageTrace().calcTotal();
        result = 9;
        invocation.getInvocationStageTrace().calcPrepare();
        result = 9;
        invocation.getInvocationStageTrace().calcConnection();
        result = 9;
        invocation.getInvocationStageTrace().calcConsumerEncodeRequest();
        result = 4;
        invocation.getInvocationStageTrace().calcConsumerSendRequest();
        result = 5;
        invocation.getInvocationStageTrace().calcWait();
        result = 9;
        invocation.getInvocationStageTrace().calcConsumerDecodeResponse();
        result = 9;

        event.getInvocation();
        result = invocation;
      }
    };

    eventBus.post(event);
    eventBus.post(event);

    MeasurementTree tree = new MeasurementTree();
    tree.from(registry.getMeters().iterator(),
        new MeasurementGroupConfig(MeterInvocationConst.INVOCATION_NAME, "stage"));
    List<Measurement> measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "total")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "prepare")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "consumer-send")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.0E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=5.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "connection")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "consumer-encode")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=8.0E-9");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=4.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "connection")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "wait")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "consumer-decode")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");
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
        invocation.getInvocationStageTrace().calcTotal();
        result = 9;
        invocation.getInvocationStageTrace().calcPrepare();
        result = 9;
        invocation.getInvocationStageTrace().calcProviderDecodeRequest();
        result = 9;
        invocation.getInvocationStageTrace().calcConnection();
        result = 9;
        invocation.getInvocationStageTrace().calcConsumerEncodeRequest();
        result = 4;
        invocation.getInvocationStageTrace().calcConsumerSendRequest();
        result = 5;
        invocation.getInvocationStageTrace().calcConsumerDecodeResponse();
        result = 8;
        invocation.getInvocationStageTrace().calcWait();
        result = 9;
        invocation.getInvocationStageTrace().calcProviderEncodeResponse();
        result = 9;
        invocation.getInvocationStageTrace().calcProviderSendResponse();
        result = 9;
        event.getInvocation();
        result = invocation;
      }
    };

    eventBus.post(event);
    eventBus.post(event);

    MeasurementTree tree = new MeasurementTree();
    tree.from(registry.getMeters().iterator(),
        new MeasurementGroupConfig(MeterInvocationConst.INVOCATION_NAME, "stage"));

    List<Measurement> measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "total")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "prepare")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "consumer-send")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.0E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=5.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "connection")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "consumer-encode")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=8.0E-9");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=4.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "wait")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "consumer-decode")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.6E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=8.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "provider-decode")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "provider-encode")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "provider-send")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");
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
        invocation.getInvocationStageTrace().calcTotal();
        result = 9;
        invocation.getInvocationStageTrace().calcPrepare();
        result = 9;
        invocation.getInvocationStageTrace().calcProviderDecodeRequest();
        result = 9;
        invocation.getInvocationStageTrace().calcQueue();
        result = 9;
        invocation.getInvocationStageTrace().calcBusinessExecute();
        result = 9;
        invocation.getInvocationStageTrace().calcProviderEncodeResponse();
        result = 9;
        invocation.getInvocationStageTrace().calcProviderSendResponse();
        result = 9;
        event.getInvocation();
        result = invocation;
      }
    };

    eventBus.post(event);
    eventBus.post(event);

    MeasurementTree tree = new MeasurementTree();
    tree.from(registry.getMeters().iterator(),
        new MeasurementGroupConfig(MeterInvocationConst.INVOCATION_NAME, "stage"));

    List<Measurement> measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "total")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "prepare")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "queue")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "execute")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "provider-decode")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "provider-encode")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "provider-send")
        .getMeasurements();
    Assert.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");
  }
}
