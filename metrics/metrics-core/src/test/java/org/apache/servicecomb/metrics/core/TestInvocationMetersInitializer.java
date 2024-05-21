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
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.publish.MeasurementGroupConfig;
import org.apache.servicecomb.foundation.metrics.publish.MeasurementTree;
import org.apache.servicecomb.metrics.core.meter.invocation.MeterInvocationConst;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import com.google.common.eventbus.EventBus;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@ExtendWith(MockitoExtension.class)
public class TestInvocationMetersInitializer {
  EventBus eventBus = new EventBus();

  MeterRegistry registry = new SimpleMeterRegistry();

  InvocationMetersInitializer invocationMetersInitializer = new InvocationMetersInitializer();

  @Mock
  Invocation invocation;

  Environment environment = Mockito.mock(Environment.class);

  @BeforeEach
  public void setup() {
    Mockito.when(environment.getProperty(METRICS_WINDOW_TIME, int.class, DEFAULT_METRICS_WINDOW_TIME))
        .thenReturn(DEFAULT_METRICS_WINDOW_TIME);
    Mockito.when(environment.getProperty(
            CONFIG_LATENCY_DISTRIBUTION_MIN_SCOPE_LEN, int.class, 7))
        .thenReturn(7);
    invocationMetersInitializer.init(registry, eventBus, new MetricsBootstrapConfig(environment));
  }

  @Test
  public void consumerInvocation() {
    InvocationFinishEvent event = Mockito.mock(InvocationFinishEvent.class);
    Mockito.when(invocation.isConsumer()).thenReturn(true);
    Mockito.when(invocation.getInvocationType()).thenReturn(InvocationType.CONSUMER);
    Mockito.when(invocation.getRealTransportName()).thenReturn(CoreConst.RESTFUL);
    Mockito.when(invocation.getMicroserviceQualifiedName()).thenReturn("m.s.o");
    InvocationStageTrace invocationStageTrace = Mockito.mock(InvocationStageTrace.class);
    Mockito.when(invocation.getInvocationStageTrace()).thenReturn(invocationStageTrace);
    Mockito.when(invocationStageTrace.calcTotal()).thenReturn(9L);
    Mockito.when(invocationStageTrace.calcPrepare()).thenReturn(9L);
    Mockito.when(invocationStageTrace.calcConnection()).thenReturn(9L);
    Mockito.when(invocationStageTrace.calcConsumerEncodeRequest()).thenReturn(4L);
    Mockito.when(invocationStageTrace.calcConsumerSendRequest()).thenReturn(5L);
    Mockito.when(invocationStageTrace.calcWait()).thenReturn(9L);
    Mockito.when(invocationStageTrace.calcConsumerDecodeResponse()).thenReturn(9L);
    Mockito.when(event.getInvocation()).thenReturn(invocation);
    Response mockResponse = Mockito.spy(Response.class);
    Mockito.when(event.getResponse()).thenReturn(mockResponse);
    Mockito.doReturn(0).when(mockResponse).getStatusCode();

    eventBus.post(event);
    eventBus.post(event);

    MeasurementTree tree = new MeasurementTree();
    tree.from(registry.getMeters().iterator(),
        new MeasurementGroupConfig(MeterInvocationConst.INVOCATION_NAME, "stage"));
    List<Measurement> measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "total")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "prepare")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "consumer-send")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.0E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=5.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "connection")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "consumer-encode")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=8.0E-9");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=4.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "connection")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "wait")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "consumer-decode")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");
  }

  @Test
  public void edgeInvocation() {
    InvocationFinishEvent event = Mockito.mock(InvocationFinishEvent.class);
    Mockito.when(invocation.getInvocationType()).thenReturn(InvocationType.EDGE);
    Mockito.when(invocation.isEdge()).thenReturn(true);
    Mockito.when(invocation.getRealTransportName()).thenReturn(CoreConst.RESTFUL);
    Mockito.when(invocation.getMicroserviceQualifiedName()).thenReturn("m.s.o");
    InvocationStageTrace invocationStageTrace = Mockito.mock(InvocationStageTrace.class);
    Mockito.when(invocation.getInvocationStageTrace()).thenReturn(invocationStageTrace);
    Mockito.when(invocationStageTrace.calcTotal()).thenReturn(9L);
    Mockito.when(invocationStageTrace.calcPrepare()).thenReturn(9L);
    Mockito.when(invocationStageTrace.calcProviderDecodeRequest()).thenReturn(9L);
    Mockito.when(invocationStageTrace.calcConnection()).thenReturn(9L);
    Mockito.when(invocationStageTrace.calcConsumerEncodeRequest()).thenReturn(4L);
    Mockito.when(invocationStageTrace.calcConsumerSendRequest()).thenReturn(5L);
    Mockito.when(invocationStageTrace.calcConsumerDecodeResponse()).thenReturn(8L);
    Mockito.when(invocationStageTrace.calcWait()).thenReturn(9L);
    Mockito.when(invocationStageTrace.calcProviderEncodeResponse()).thenReturn(9L);
    Mockito.when(invocationStageTrace.calcProviderSendResponse()).thenReturn(9L);
    Mockito.when(event.getInvocation()).thenReturn(invocation);
    Response mockResponse = Mockito.spy(Response.class);
    Mockito.when(event.getResponse()).thenReturn(mockResponse);
    Mockito.doReturn(0).when(mockResponse).getStatusCode();

    eventBus.post(event);
    eventBus.post(event);

    MeasurementTree tree = new MeasurementTree();
    tree.from(registry.getMeters().iterator(),
        new MeasurementGroupConfig(MeterInvocationConst.INVOCATION_NAME, "stage"));

    List<Measurement> measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "total")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "prepare")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "consumer-send")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.0E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=5.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "connection")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "consumer-encode")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=8.0E-9");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=4.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "wait")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "consumer-decode")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.6E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=8.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "provider-decode")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "provider-encode")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "provider-send")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");
  }

  @Test
  public void producerInvocation() {
    InvocationFinishEvent event = Mockito.mock(InvocationFinishEvent.class);
    Mockito.when(invocation.isConsumer()).thenReturn(false);
    Mockito.when(invocation.getInvocationType()).thenReturn(InvocationType.PROVIDER);
    Mockito.when(invocation.getRealTransportName()).thenReturn(CoreConst.RESTFUL);
    Mockito.when(invocation.getMicroserviceQualifiedName()).thenReturn("m.s.o");
    InvocationStageTrace invocationStageTrace = Mockito.mock(InvocationStageTrace.class);
    Mockito.when(invocation.getInvocationStageTrace()).thenReturn(invocationStageTrace);
    Mockito.when(invocationStageTrace.calcTotal()).thenReturn(9L);
    Mockito.when(invocationStageTrace.calcPrepare()).thenReturn(9L);
    Mockito.when(invocationStageTrace.calcProviderDecodeRequest()).thenReturn(9L);
    Mockito.when(invocationStageTrace.calcQueue()).thenReturn(9L);
    Mockito.when(invocationStageTrace.calcBusinessExecute()).thenReturn(9L);
    Mockito.when(invocationStageTrace.calcProviderEncodeResponse()).thenReturn(9L);
    Mockito.when(invocationStageTrace.calcProviderSendResponse()).thenReturn(9L);
    Mockito.when(event.getInvocation()).thenReturn(invocation);
    Response mockResponse = Mockito.spy(Response.class);
    Mockito.when(event.getResponse()).thenReturn(mockResponse);
    Mockito.doReturn(0).when(mockResponse).getStatusCode();

    eventBus.post(event);
    eventBus.post(event);

    MeasurementTree tree = new MeasurementTree();
    tree.from(registry.getMeters().iterator(),
        new MeasurementGroupConfig(MeterInvocationConst.INVOCATION_NAME, "stage"));

    List<Measurement> measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "total")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "prepare")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "queue")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "execute")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "provider-decode")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "provider-encode")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");

    measurements = tree.findChild(MeterInvocationConst.INVOCATION_NAME, "provider-send")
        .getMeasurements();
    Assertions.assertEquals(3, measurements.size());
    AssertUtil.assertMeasure(measurements, 0,
        "statistic='COUNT', value=2.0");
    AssertUtil.assertMeasure(measurements, 1,
        "statistic='TOTAL_TIME', value=1.8E-8");
    AssertUtil.assertMeasure(measurements, 2,
        "statistic='MAX', value=9.0E-9");
  }
}
