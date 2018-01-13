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

package org.apache.servicecomb.foundation.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.servicecomb.foundation.metrics.output.MetricsFileOutput;
import org.apache.servicecomb.foundation.metrics.output.servo.MetricsContentConvertor;
import org.apache.servicecomb.foundation.metrics.output.servo.MetricsContentFormatter;
import org.apache.servicecomb.foundation.metrics.output.servo.MetricsObserverInitializer;
import org.apache.servicecomb.foundation.metrics.output.servo.RollingMetricsFileOutput;
import org.apache.servicecomb.foundation.metrics.output.servo.SimpleMetricsContentConvertor;
import org.apache.servicecomb.foundation.metrics.output.servo.SimpleMetricsContentFormatter;
import org.apache.servicecomb.foundation.metrics.performance.MetricsDataMonitor;
import org.apache.servicecomb.foundation.metrics.performance.QueueMetricsData;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.servo.publish.PollScheduler;

public class TestMetricsServoRegistry {
  MetricsDataMonitor metricsDataMonitor = null;

  MetricsDataMonitor localData = null;

  MetricsServoRegistry metricsRegistry = null;

  MetricsFileOutput fileOutput = null;

  MetricsContentConvertor convertor = null;

  MetricsContentFormatter formatter = null;

  MetricsObserverInitializer observerManager = null;

  @BeforeClass
  public static void staticBeforeClean() {
    BaseConfiguration configuration = new BaseConfiguration();
    configuration.setProperty(MetricsObserverInitializer.METRICS_FILE_ENABLED, true);
    configuration.setProperty(MetricsObserverInitializer.METRICS_POLL_TIME, 1);
    DynamicPropertyFactory.initWithConfigurationSource(configuration);
    MetricsServoRegistry.metricsList.clear();
    MetricsServoRegistry.LOCAL_METRICS_MAP = new ThreadLocal<>();
  }

  @Before
  public void setUp() throws Exception {
    metricsRegistry = new MetricsServoRegistry();
    convertor = new SimpleMetricsContentConvertor();
    formatter = new SimpleMetricsContentFormatter();
    fileOutput = new RollingMetricsFileOutput();
    observerManager = new MetricsObserverInitializer(fileOutput, convertor, formatter, false);
    localData = metricsRegistry.getLocalMetrics();
    metricsDataMonitor = MetricsServoRegistry.getOrCreateLocalMetrics();
  }

  @After
  public void tearDown() throws Exception {
    PollScheduler.getInstance().stop();
    metricsRegistry = null;
    convertor = null;
    formatter = null;
    observerManager = null;
    fileOutput = null;
    localData = null;
    metricsDataMonitor = null;
  }

  @Test
  public void testAllRegistryMetrics() throws Exception {

    metricsDataMonitor.incrementTotalReqProvider();
    metricsDataMonitor.incrementTotalFailReqProvider();
    metricsDataMonitor.incrementTotalReqConsumer();
    metricsDataMonitor.incrementTotalFailReqConsumer();
    metricsDataMonitor.setOperMetTotalReq("sayHi", 20L);
    metricsDataMonitor.setOperMetTotalFailReq("sayHi", 20L);
    localData = metricsRegistry.getLocalMetrics();
    localData.setOperMetTotalReq("sayHi", 10L);
    localData.setOperMetTotalFailReq("sayHi", 10L);

    metricsRegistry.afterPropertiesSet();
    observerManager.init();
    Thread.sleep(1000);
    // get the metrics from local data and compare
    localData = metricsRegistry.getLocalMetrics();
    Assert.assertEquals(1, localData.getTotalReqProvider());
    Assert.assertEquals(1, localData.getTotalFailReqProvider());
    Assert.assertEquals(1, localData.getTotalReqConsumer());
    Assert.assertEquals(1, localData.getTotalFailReqConsumer());
    Assert.assertEquals(20L, localData.getOperMetTotalReq("sayHi").longValue());
    Assert.assertEquals(20L, localData.getOperMetTotalFailReq("sayHi").longValue());

    MetricsDataMonitor localData1 = metricsRegistry.getLocalMetrics();
    Assert.assertEquals(20L, localData1.getOperMetTotalReq("sayHi").longValue());
    Assert.assertEquals(20L, localData1.getOperMetTotalFailReq("sayHi").longValue());
  }

  @Test
  public void testOperationalProviderMetrics() throws Exception {
    MetricsDataMonitor metricsDataMonitor = MetricsServoRegistry.getOrCreateLocalMetrics();
    metricsDataMonitor.setOperMetTotalReq("sayHi", 20L);
    metricsDataMonitor.setOperMetTotalFailReq("sayHi", 20L);
    localData = metricsRegistry.getLocalMetrics();

    metricsRegistry.afterPropertiesSet();
    observerManager.init();
    Thread.sleep(1000);
    // get the metrics from local data and compare
    localData = metricsRegistry.getLocalMetrics();
    Assert.assertEquals(1, localData.getTotalReqProvider());
    Assert.assertEquals(1, localData.getTotalFailReqProvider());
    Assert.assertEquals(1, localData.getTotalReqConsumer());
    Assert.assertEquals(1, localData.getTotalFailReqConsumer());
    Assert.assertEquals(20L, localData.getOperMetTotalReq("sayHi").longValue());
    Assert.assertEquals(20L, localData.getOperMetTotalFailReq("sayHi").longValue());

    MetricsDataMonitor localData1 = metricsRegistry.getLocalMetrics();
    Assert.assertEquals(20L, localData1.getOperMetTotalReq("sayHi").longValue());
    Assert.assertEquals(20L, localData1.getOperMetTotalFailReq("sayHi").longValue());
  }

  @Test
  public void testQueueMetrics() throws Exception {
    QueueMetricsData reqQueue1 = new QueueMetricsData();
    reqQueue1.setCountInQueue(1);
    reqQueue1.setMaxLifeTimeInQueue(2);
    reqQueue1.setMinLifeTimeInQueue(1);
    reqQueue1.setTotalCount(10);
    reqQueue1.setTotalTime(100);
    reqQueue1.setTotalServExecutionCount(5);
    reqQueue1.setTotalServExecutionTime(50);
    metricsDataMonitor.setQueueMetrics("/sayBye", reqQueue1);

    metricsRegistry.afterPropertiesSet();
    observerManager.init();
    Thread.sleep(1000);
    // get the metrics from local data and compare
    Map<String, QueueMetricsData> localMap = localData.getQueueMetrics();
    QueueMetricsData reqQueue2 = localMap.get("/sayBye");

    Assert.assertEquals(1L, reqQueue2.getCountInQueue().longValue());
    Assert.assertEquals(1L, reqQueue2.getMinLifeTimeInQueue().longValue());
    Assert.assertEquals(2L, reqQueue2.getMaxLifeTimeInQueue().longValue());
    Assert.assertEquals(10L, reqQueue2.getTotalCount().longValue());
    Assert.assertEquals(100L, reqQueue2.getTotalTime().longValue());
    Assert.assertEquals(5, reqQueue2.getTotalServExecutionCount().longValue());
    Assert.assertEquals(50, reqQueue2.getTotalServExecutionTime().longValue());
  }

  @Test
  public void testQueueMetrics1() throws Exception {
    QueueMetricsData reqQueue1 = new QueueMetricsData();
    reqQueue1.setCountInQueue(10);
    reqQueue1.setMaxLifeTimeInQueue(2);
    reqQueue1.setMinLifeTimeInQueue(2);
    reqQueue1.setTotalCount(1);
    reqQueue1.setTotalTime(10);
    reqQueue1.setTotalServExecutionCount(1);
    reqQueue1.setTotalServExecutionTime(1);
    localData.setQueueMetrics("/sayBye", reqQueue1);

    QueueMetricsData reqQueue2 = new QueueMetricsData();
    reqQueue2.setCountInQueue(10);
    reqQueue2.setMaxLifeTimeInQueue(2);
    reqQueue2.setMinLifeTimeInQueue(2);
    reqQueue2.setTotalCount(10);
    reqQueue2.setTotalTime(100);
    reqQueue2.setTotalServExecutionCount(5);
    reqQueue2.setTotalServExecutionTime(50);
    metricsDataMonitor.setQueueMetrics("/sayBye", reqQueue2);

    metricsRegistry.afterPropertiesSet();
    observerManager.init();
    Thread.sleep(1000);
    // get the metrics from local data and compare
    Map<String, QueueMetricsData> localMap = localData.getQueueMetrics();
    QueueMetricsData reqQueue3 = localMap.get("/sayBye");

    Assert.assertEquals(10L, reqQueue3.getCountInQueue().longValue());
    Assert.assertEquals(2L, reqQueue3.getMinLifeTimeInQueue().longValue());
    Assert.assertEquals(2L, reqQueue3.getMaxLifeTimeInQueue().longValue());
    Assert.assertEquals(10L, reqQueue3.getTotalCount().longValue());
    Assert.assertEquals(100L, reqQueue3.getTotalTime().longValue());
    Assert.assertEquals(5L, reqQueue3.getTotalServExecutionCount().longValue());
    Assert.assertEquals(50L, reqQueue3.getTotalServExecutionTime().longValue());
  }

  @Test
  public void testCalculateTps() {
    observerManager.init();
    List<TpsAndLatencyData> tpsAndLatencyData = new ArrayList<>();
    tpsAndLatencyData.add(new TpsAndLatencyData(1, 1, 100, 100));
    tpsAndLatencyData.add(new TpsAndLatencyData(2, 2, 200, 200));
    String results = metricsRegistry.calculateTpsAndLatency(tpsAndLatencyData);
    Assert.assertTrue("{tps=40.0, latency=166.7}".equals(results));
  }
}
