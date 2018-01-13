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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.metrics.output.MetricsFileOutput;
import org.apache.servicecomb.foundation.metrics.output.servo.FileOutputMetricObserver;
import org.apache.servicecomb.foundation.metrics.output.servo.MetricsContentFormatter;
import org.apache.servicecomb.foundation.metrics.output.servo.SimpleMetricsContentConvertor;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.netflix.servo.Metric;
import com.netflix.servo.monitor.MonitorConfig;

public class TestFileOutputMetricObserverAndContentConvertor {
  @SuppressWarnings("unchecked")
  @Test
  public void testMetricObserverUpdateImpl() {

    MetricsFileOutput output = mock(MetricsFileOutput.class);
    SimpleMetricsContentConvertor convertor = new SimpleMetricsContentConvertor();
    MetricsContentFormatter formatter = new MetricsContentFormatter() {
      @Override
      public Map<String, String> format(Map<String, String> input) {
        return input;
      }
    };

    List<Metric> metrics = new ArrayList<>();
    metrics.add(
        new Metric(MonitorConfig.builder("totalRequestsPerProvider INSTANCE_LEVEL").build(), System.currentTimeMillis(),
            "1"));
    metrics.add(new Metric(MonitorConfig.builder("totalFailedRequestsPerProvider INSTANCE_LEVEL").build(),
        System.currentTimeMillis(), "2"));
    metrics.add(
        new Metric(MonitorConfig.builder("totalRequestsPerConsumer INSTANCE_LEVEL").build(), System.currentTimeMillis(),
            "3"));
    metrics.add(new Metric(MonitorConfig.builder("totalFailRequestsPerConsumer INSTANCE_LEVEL").build(),
        System.currentTimeMillis(), "4"));
    metrics.add(
        new Metric(MonitorConfig.builder("totalRequestProvider OPERATIONAL_LEVEL").build(), System.currentTimeMillis(),
            "totalRequestProvider"));
    metrics.add(new Metric(MonitorConfig.builder("totalFailedRequestProvider OPERATIONAL_LEVEL").build(),
        System.currentTimeMillis(), "totalFailedRequestProvider"));
    metrics.add(new Metric(MonitorConfig.builder("RequestQueueRelated").build(), System.currentTimeMillis(),
        "{InstanceLevel={averageServiceExecutionTime=1.0, countInQueue=2, minLifeTimeInQueue=3, averageTimeInQueue=4.0, maxLifeTimeInQueue=5}}"));
    metrics.add(new Metric(MonitorConfig.builder("TPS and Latency").build(), System.currentTimeMillis(),
        "{latency=100, tps=200}"));
    metrics.add(new Metric(MonitorConfig.builder("CPU and Memory").build(), System.currentTimeMillis(),
        "{heapUsed=146120664, nonHeapUsed=55146864, cpuRunningThreads=36, heapMax=3786407936, heapCommit=472907776, nonHeapInit=2555904, nonHeapMax=-1, cpuLoad=-1.0, heapInit=266338304, nonHeapCommit=56623104}"));

    FileOutputMetricObserver observer = new FileOutputMetricObserver(output, convertor, formatter);
    observer.updateImpl(metrics);

    @SuppressWarnings("rawtypes")
    ArgumentCaptor<Map> outputMetrics = ArgumentCaptor.forClass(Map.class);
    verify(output).output(outputMetrics.capture());

    HashMap<String, String> result = (HashMap<String, String>) outputMetrics.getValue();

    Assert.assertTrue(result.containsKey("totalRequestsPerProvider"));
    Assert.assertTrue(result.get("totalRequestsPerProvider").equals("1"));
    Assert.assertTrue(result.containsKey("totalFailedRequestsPerProvider"));
    Assert.assertTrue(result.get("totalFailedRequestsPerProvider").equals("2"));
    Assert.assertTrue(result.containsKey("totalRequestsPerConsumer"));
    Assert.assertTrue(result.get("totalRequestsPerConsumer").equals("3"));
    Assert.assertTrue(result.containsKey("totalFailRequestsPerConsumer"));
    Assert.assertTrue(result.get("totalFailRequestsPerConsumer").equals("4"));

    Assert.assertTrue(result.containsKey("averageServiceExecutionTime"));
    Assert.assertTrue(result.get("averageServiceExecutionTime").equals("1.0"));
    Assert.assertTrue(result.containsKey("countInQueue"));
    Assert.assertTrue(result.get("countInQueue").equals("2"));
    Assert.assertTrue(result.containsKey("minLifeTimeInQueue"));
    Assert.assertTrue(result.get("minLifeTimeInQueue").equals("3"));
    Assert.assertTrue(result.containsKey("averageTimeInQueue"));
    Assert.assertTrue(result.get("averageTimeInQueue").equals("4.0"));
    Assert.assertTrue(result.containsKey("maxLifeTimeInQueue"));
    Assert.assertTrue(result.get("maxLifeTimeInQueue").equals("5"));

    Assert.assertTrue(result.containsKey("latency"));
    Assert.assertTrue(result.get("latency").equals("100"));
    Assert.assertTrue(result.containsKey("tps"));
    Assert.assertTrue(result.get("tps").equals("200"));

    Assert.assertTrue(result.containsKey("heapUsed"));
    Assert.assertTrue(result.get("heapUsed").equals("146120664"));
    Assert.assertTrue(result.containsKey("nonHeapUsed"));
    Assert.assertTrue(result.get("nonHeapUsed").equals("55146864"));
    Assert.assertTrue(result.containsKey("cpuRunningThreads"));
    Assert.assertTrue(result.get("cpuRunningThreads").equals("36"));
    Assert.assertTrue(result.containsKey("heapMax"));
    Assert.assertTrue(result.get("heapMax").equals("3786407936"));
    Assert.assertTrue(result.containsKey("heapCommit"));
    Assert.assertTrue(result.get("heapCommit").equals("472907776"));
    Assert.assertTrue(result.containsKey("nonHeapInit"));
    Assert.assertTrue(result.get("nonHeapInit").equals("2555904"));
    Assert.assertTrue(result.containsKey("nonHeapMax"));
    Assert.assertTrue(result.get("nonHeapMax").equals("-1"));
    Assert.assertTrue(result.containsKey("cpuLoad"));
    Assert.assertTrue(result.get("cpuLoad").equals("-1.0"));
    Assert.assertTrue(result.containsKey("heapInit"));
    Assert.assertTrue(result.get("heapInit").equals("266338304"));
    Assert.assertTrue(result.containsKey("nonHeapCommit"));
    Assert.assertTrue(result.get("nonHeapCommit").equals("56623104"));
  }
}
