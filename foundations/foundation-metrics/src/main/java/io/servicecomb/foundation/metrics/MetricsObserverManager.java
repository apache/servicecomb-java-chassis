/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.foundation.metrics;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.servo.publish.BasicMetricFilter;
import com.netflix.servo.publish.CounterToRateMetricTransform;
import com.netflix.servo.publish.FileMetricObserver;
import com.netflix.servo.publish.MemoryMetricObserver;
import com.netflix.servo.publish.MetricObserver;
import com.netflix.servo.publish.MonitorRegistryMetricPoller;
import com.netflix.servo.publish.PollRunnable;
import com.netflix.servo.publish.PollScheduler;

@Component
public class MetricsObserverManager {

  protected static final String METRICS_POLL_TIME = "cse.metrics.polltime";

  protected static final String MEM_ENABLED = "cse.metrics.mem.enabled";

  protected static final String FILE_ENABLED = "cse.metrics.file.enabled";

  private static final String FILE_NAME = "cse.metrics.file.name";

  private static final String FILE_PATH = "cse.metrics.file.path";

  private int metricPoll = 60;

  private MemoryMetricObserver memoryObserver = null;

  private final MetricsOutput metricsOutput;

  public MetricsObserverManager() {
    initObserverPoller();
    this.metricsOutput = new ServoMetricsOutput(memoryObserver);
  }

  public MetricsObserverManager(MetricsOutput output) {
    initObserverPoller();
    this.metricsOutput = output;
  }

  public MetricsOutput getMetricsOutput() {
    return metricsOutput;
  }

  //TODO: current direct init file observer and memory observer from config setting, will refactor later let user can do more decision
  public void initObserverPoller() {
    PollScheduler scheduler = PollScheduler.getInstance();
    if (!scheduler.isStarted()) {
      scheduler.start();
    }

    metricPoll = DynamicPropertyFactory.getInstance().getIntProperty(METRICS_POLL_TIME, 60).get();
    boolean fileObserverEnabled = DynamicPropertyFactory.getInstance().getBooleanProperty(FILE_ENABLED, false).get();
    if (fileObserverEnabled) {
      String fileName = DynamicPropertyFactory.getInstance().getStringProperty(FILE_NAME, "metrics").get();
      String filePath = DynamicPropertyFactory.getInstance().getStringProperty(FILE_PATH, ".").get();
      MetricObserver fileObserver = new FileMetricObserver(fileName, new File(filePath));
      MetricObserver fileTransform = new CounterToRateMetricTransform(fileObserver, metricPoll, TimeUnit.SECONDS);
      PollRunnable fileTask = new PollRunnable(new MonitorRegistryMetricPoller(), BasicMetricFilter.MATCH_ALL,
          fileTransform);
      scheduler.addPoller(fileTask, metricPoll, TimeUnit.SECONDS);
    }

    boolean memoryObserverEnabled = DynamicPropertyFactory.getInstance().getBooleanProperty(MEM_ENABLED, false).get();
    if (memoryObserverEnabled) {
      memoryObserver = new MemoryMetricObserver("default", 1);
      MetricObserver memoryTransform = new CounterToRateMetricTransform(memoryObserver, metricPoll, TimeUnit.SECONDS);
      PollRunnable memoryTask = new PollRunnable(new MonitorRegistryMetricPoller(), BasicMetricFilter.MATCH_ALL,
          memoryTransform);
      scheduler.addPoller(memoryTask, metricPoll, TimeUnit.SECONDS);
    }
  }
}
