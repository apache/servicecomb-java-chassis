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
package com.netflix.spectator.api.patterns;

import java.util.Map;

import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementNode;
import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementTree;
import org.apache.servicecomb.metrics.core.publish.model.ThreadPoolPublishModel;

import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Utils;

public class ThreadPoolMonitorPublishModelFactory {
  interface Setter {
    void set(ThreadPoolPublishModel model, Measurement measurement);
  }

  private MeasurementTree tree;

  private Map<String, ThreadPoolPublishModel> threadPools;

  public ThreadPoolMonitorPublishModelFactory(MeasurementTree tree,
      Map<String, ThreadPoolPublishModel> threadPools) {
    this.tree = tree;
    this.threadPools = threadPools;
  }

  public static void create(MeasurementTree tree,
      Map<String, ThreadPoolPublishModel> threadPools) {
    new ThreadPoolMonitorPublishModelFactory(tree, threadPools).create();
  }

  public void create() {
    readMeasurement(ThreadPoolMonitor.TASK_COUNT,
        (model, measurement) -> {
          model.setAvgTaskCount(measurement.value());
        });
    readMeasurement(ThreadPoolMonitor.COMPLETED_TASK_COUNT,
        (model, measurement) -> {
          model.setAvgCompletedTaskCount(measurement.value());
        });
    readMeasurement(ThreadPoolMonitor.CURRENT_THREADS_BUSY,
        (model, measurement) -> {
          model.setCurrentThreadsBusy((int) measurement.value());
        });
    readMeasurement(ThreadPoolMonitor.MAX_THREADS,
        (model, measurement) -> {
          model.setMaxThreads((int) measurement.value());
        });
    readMeasurement(ThreadPoolMonitor.POOL_SIZE,
        (model, measurement) -> {
          model.setPoolSize((int) measurement.value());
        });
    readMeasurement(ThreadPoolMonitor.CORE_POOL_SIZE,
        (model, measurement) -> {
          model.setCorePoolSize((int) measurement.value());
        });
    readMeasurement(ThreadPoolMonitor.QUEUE_SIZE,
        (model, measurement) -> {
          model.setQueueSize((int) measurement.value());
        });
  }

  protected void readMeasurement(String name, Setter setter) {
    MeasurementNode node = tree.findChild(name);
    if (node == null) {
      return;
    }

    for (Measurement measurement : node.getMeasurements()) {
      String threadPoolName = Utils.getTagValue(measurement.id(), ThreadPoolMonitor.ID_TAG_NAME);
      if (threadPoolName == null) {
        continue;
      }

      ThreadPoolPublishModel model = threadPools.computeIfAbsent(threadPoolName, tpn -> {
        return new ThreadPoolPublishModel();
      });

      setter.set(model, measurement);
    }
  }
}
