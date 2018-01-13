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

package org.apache.servicecomb.foundation.metrics.performance;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestMetricsDataMonitor {
  MetricsDataMonitor metricsDataMonitor = null;

  @Before
  public void setUp() throws Exception {
    metricsDataMonitor = new MetricsDataMonitor();
  }

  @After
  public void tearDown() throws Exception {
    metricsDataMonitor = null;
  }

  @Test
  public void testReqMapValues() {
    QueueMetricsData reqQueue = new QueueMetricsData();
    reqQueue.setCountInQueue(1L);
    reqQueue.setMaxLifeTimeInQueue(5L);
    reqQueue.setMinLifeTimeInQueue(1L);
    reqQueue.toString();

    metricsDataMonitor.setQueueMetrics("/sayHi", reqQueue);
    QueueMetricsData reqQueueTest = metricsDataMonitor.getOrCreateQueueMetrics("/sayHi");
    QueueMetricsData reqQueueTestAbsent = metricsDataMonitor.getOrCreateQueueMetrics("");
    Assert.assertEquals(1, reqQueueTest.getCountInQueue().longValue());
    Assert.assertEquals(5, reqQueueTest.getMaxLifeTimeInQueue().longValue());
    Assert.assertEquals(1, reqQueueTest.getMinLifeTimeInQueue().longValue());
    Assert.assertEquals(0, reqQueueTestAbsent.getCountInQueue().longValue());
  }

  @Test
  public void testOperationMetricsMap() {
    QueueMetrics reqQueue = new QueueMetrics();
    QueueMetricsData queueMetrics = new QueueMetricsData();
    queueMetrics.incrementCountInQueue();
    reqQueue.setQueueStartTime(200);
    reqQueue.setEndOperTime(250);
    reqQueue.setQueueEndTime(300);
    reqQueue.setOperQualifiedName("name");
    queueMetrics.setTotalTime(100L);
    queueMetrics.setTotalServExecutionTime(200L);
    queueMetrics.setTotalCount(100L);
    queueMetrics.setTotalServExecutionCount(200L);

    // increment the count to 5.
    queueMetrics.incrementTotalCount();
    queueMetrics.incrementTotalCount();
    queueMetrics.incrementTotalCount();
    queueMetrics.incrementTotalCount();
    queueMetrics.incrementTotalCount();
    queueMetrics.incrementTotalServExecutionCount();
    queueMetrics.incrementTotalServExecutionCount();
    queueMetrics.incrementTotalServExecutionCount();
    queueMetrics.incrementTotalServExecutionCount();
    queueMetrics.incrementTotalServExecutionCount();

    queueMetrics.setMinLifeTimeInQueue(1);
    queueMetrics.resetMinLifeTimeInQueue();
    Assert.assertEquals(0, queueMetrics.getMinLifeTimeInQueue().longValue());

    queueMetrics.setMaxLifeTimeInQueue(1);
    queueMetrics.resetMaxLifeTimeInQueue();
    Assert.assertEquals(0, queueMetrics.getMaxLifeTimeInQueue().longValue());

    metricsDataMonitor.setQueueMetrics("/sayHi", queueMetrics);

    //Assert.assertEquals(1, reqQueueTest.getConuntInQueue());
    Assert.assertEquals(300, reqQueue.getQueueEndTime());
    Assert.assertEquals(250, reqQueue.getEndOperTime());
    Assert.assertEquals(200, reqQueue.getQueueStartTime());
    Assert.assertEquals("name", reqQueue.getOperQualifiedName());
    Assert.assertEquals(100L, queueMetrics.getTotalTime().longValue());
    Assert.assertEquals(105L, queueMetrics.getTotalCount().longValue());
    Assert.assertEquals(200, queueMetrics.getTotalServExecutionTime().longValue());
    Assert.assertEquals(205L, queueMetrics.getTotalServExecutionCount().longValue());
    queueMetrics.decrementCountInQueue();
    Assert.assertEquals(0, queueMetrics.getCountInQueue().longValue());
  }

  @Test
  public void testHystrixAvgTimes() {

    // total request for provider
    metricsDataMonitor.incrementTotalReqProvider();
    metricsDataMonitor.incrementTotalFailReqProvider();
    Assert.assertEquals(1, metricsDataMonitor.getTotalReqProvider());
    Assert.assertEquals(1, metricsDataMonitor.getTotalFailReqProvider());
    metricsDataMonitor.incrementTotalReqProvider();
    metricsDataMonitor.incrementTotalFailReqProvider();
    Assert.assertEquals(2, metricsDataMonitor.getTotalReqProvider());
    Assert.assertEquals(2, metricsDataMonitor.getTotalFailReqProvider());

    // total request for consumer
    metricsDataMonitor.incrementTotalReqConsumer();
    metricsDataMonitor.incrementTotalFailReqConsumer();
    Assert.assertEquals(1, metricsDataMonitor.getTotalReqConsumer());
    Assert.assertEquals(1, metricsDataMonitor.getTotalFailReqConsumer());
    metricsDataMonitor.incrementTotalReqConsumer();
    metricsDataMonitor.incrementTotalFailReqConsumer();
    Assert.assertEquals(2, metricsDataMonitor.getTotalReqConsumer());
    Assert.assertEquals(2, metricsDataMonitor.getTotalFailReqConsumer());

    metricsDataMonitor.setOperMetTotalReq("/sayHi", 10L);
    metricsDataMonitor.setOperMetTotalFailReq("/sayHi", 20L);
    Assert.assertEquals(10L, metricsDataMonitor.getOperMetTotalReq("/sayHi").longValue());
    Assert.assertEquals(20L, metricsDataMonitor.getOperMetTotalFailReq("/sayHi").longValue());
  }
}
