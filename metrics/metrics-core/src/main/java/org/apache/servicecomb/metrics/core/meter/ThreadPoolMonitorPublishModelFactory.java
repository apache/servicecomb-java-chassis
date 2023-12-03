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
package org.apache.servicecomb.metrics.core.meter;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.metrics.publish.MeasurementNode;
import org.apache.servicecomb.foundation.metrics.publish.MeasurementTree;
import org.apache.servicecomb.metrics.core.meter.pool.ThreadPoolMeter;
import org.apache.servicecomb.metrics.core.publish.model.ThreadPoolPublishModel;

import io.micrometer.core.instrument.Measurement;

public class ThreadPoolMonitorPublishModelFactory {
  interface Setter {
    void set(ThreadPoolPublishModel model, Measurement measurement);
  }

  public static Map<String, ThreadPoolPublishModel> create(MeasurementTree tree) {
    Map<String, ThreadPoolPublishModel> result = new HashMap<>(8);
    readMeasurement(result, tree, ThreadPoolMeter.TASK_COUNT,
        (model, measurement) -> model.setAvgTaskCount(measurement.getValue()));
    readMeasurement(result, tree, ThreadPoolMeter.COMPLETED_TASK_COUNT,
        (model, measurement) -> model.setAvgCompletedTaskCount(measurement.getValue()));
    readMeasurement(result, tree, ThreadPoolMeter.CURRENT_THREADS_BUSY,
        (model, measurement) -> model.setCurrentThreadsBusy((int) measurement.getValue()));
    readMeasurement(result, tree, ThreadPoolMeter.MAX_THREADS,
        (model, measurement) -> model.setMaxThreads((int) measurement.getValue()));
    readMeasurement(result, tree, ThreadPoolMeter.POOL_SIZE,
        (model, measurement) -> model.setPoolSize((int) measurement.getValue()));
    readMeasurement(result, tree, ThreadPoolMeter.CORE_POOL_SIZE,
        (model, measurement) -> model.setCorePoolSize((int) measurement.getValue()));
    readMeasurement(result, tree, ThreadPoolMeter.QUEUE_SIZE,
        (model, measurement) -> model.setQueueSize((int) measurement.getValue()));
    readMeasurement(result, tree, ThreadPoolMeter.REJECTED_COUNT,
        (model, measurement) -> model.setRejected(measurement.getValue()));
    return result;
  }

  protected static void readMeasurement(Map<String, ThreadPoolPublishModel> threadPools, MeasurementTree tree,
      String name, Setter setter) {
    MeasurementNode node = tree.findChild(ThreadPoolMeter.THREAD_POOL_METER);
    if (node == null) {
      return;
    }

    for (String threadPoolName : node.getChildren().keySet()) {
      MeasurementNode measure = node.findChild(threadPoolName, name);
      if (measure == null) {
        continue;
      }
      ThreadPoolPublishModel model = threadPools.computeIfAbsent(threadPoolName, tpn -> new ThreadPoolPublishModel());
      setter.set(model, measure.getMeasurements().get(0));
    }
  }
}
