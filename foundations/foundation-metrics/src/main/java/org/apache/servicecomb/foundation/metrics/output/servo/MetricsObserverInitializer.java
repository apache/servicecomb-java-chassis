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

package org.apache.servicecomb.foundation.metrics.output.servo;

import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.foundation.metrics.output.MetricsFileOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.servo.publish.BasicMetricFilter;
import com.netflix.servo.publish.CounterToRateMetricTransform;
import com.netflix.servo.publish.MetricObserver;
import com.netflix.servo.publish.MonitorRegistryMetricPoller;
import com.netflix.servo.publish.PollRunnable;
import com.netflix.servo.publish.PollScheduler;

//manage and init ServoObservers
@Component
public class MetricsObserverInitializer {

  public static final String METRICS_POLL_TIME = "servicecomb.metrics.polltime";

  public static final String METRICS_FILE_ENABLED = "servicecomb.metrics.file.enabled";

  private final int metricPoll;

  private final MetricsFileOutput fileOutput;

  private final MetricsContentConvertor convertor;

  private final MetricsContentFormatter formatter;

  @Autowired
  public MetricsObserverInitializer(MetricsFileOutput fileOutput, MetricsContentConvertor convertor,
      MetricsContentFormatter formatter) {
    this(fileOutput, convertor, formatter, true);
  }

  public MetricsObserverInitializer(MetricsFileOutput fileOutput, MetricsContentConvertor convertor,
      MetricsContentFormatter formatter, boolean autoInit) {
    metricPoll = DynamicPropertyFactory.getInstance().getIntProperty(METRICS_POLL_TIME, 30).get();
    this.fileOutput = fileOutput;
    this.convertor = convertor;
    this.formatter = formatter;
    if (autoInit) {
      this.init();
    }
  }

  public void init() {
    PollScheduler scheduler = PollScheduler.getInstance();
    if (!scheduler.isStarted()) {
      scheduler.start();
    }

    if (isRollingFileEnabled()) {
      MetricObserver fileObserver = new FileOutputMetricObserver(fileOutput, convertor, formatter);
      MetricObserver fileTransform = new CounterToRateMetricTransform(fileObserver, metricPoll, TimeUnit.SECONDS);
      PollRunnable fileTask = new PollRunnable(new MonitorRegistryMetricPoller(), BasicMetricFilter.MATCH_ALL,
          fileTransform);
      scheduler.addPoller(fileTask, metricPoll, TimeUnit.SECONDS);
    }
  }

  public boolean isRollingFileEnabled() {
    return DynamicPropertyFactory.getInstance().getBooleanProperty(METRICS_FILE_ENABLED, false).get();
  }
}
