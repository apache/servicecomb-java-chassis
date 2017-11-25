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

package io.servicecomb.foundation.metrics.output.servo;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.netflix.servo.publish.BasicMetricFilter;
import com.netflix.servo.publish.CounterToRateMetricTransform;
import com.netflix.servo.publish.MetricObserver;
import com.netflix.servo.publish.MonitorRegistryMetricPoller;
import com.netflix.servo.publish.PollRunnable;
import com.netflix.servo.publish.PollScheduler;

import io.servicecomb.foundation.common.utils.RollingFileAppenderExt;
import io.servicecomb.foundation.metrics.output.MetricsFileOutput;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;

@Component
public class RollingMetricsFileOutput extends MetricsFileOutput {
  private final Map<String, RollingFileAppenderExt> metricsAppenders = new HashMap<>();
  private final MetricsContentConvertor convertor;
  private final MetricsContentFormatter formatter;
  private final String fileNameHeader;

  //auto init when as spring bean
  @Autowired
  public RollingMetricsFileOutput(MetricsContentConvertor convertor, MetricsContentFormatter formatter) {
    this(convertor, formatter, true);
  }

  public RollingMetricsFileOutput(MetricsContentConvertor convertor, MetricsContentFormatter formatter,
      boolean autoInit) {
    if (RegistryUtils.getServiceRegistry() != null) {
      Microservice microservice = RegistryUtils.getMicroservice();
      fileNameHeader = String.join(".", microservice.getAppId(), microservice.getServiceName());
    } else {
      fileNameHeader = String.join(".", "local", "test");
    }

    this.convertor = convertor;
    this.formatter = formatter;
    if (autoInit) {
      this.init();
    }
  }

  @Override
  public void init() {
    PollScheduler scheduler = PollScheduler.getInstance();
    if (!scheduler.isStarted()) {
      scheduler.start();
    }

    if (isEnabled()) {
      MetricObserver fileObserver = new FileOutputMetricObserver("fileOutputObserver", this, convertor, formatter);
      MetricObserver fileTransform = new CounterToRateMetricTransform(fileObserver, getMetricPoll(), TimeUnit.SECONDS);
      PollRunnable fileTask = new PollRunnable(new MonitorRegistryMetricPoller(), BasicMetricFilter.MATCH_ALL,
          fileTransform);
      scheduler.addPoller(fileTask, getMetricPoll(), TimeUnit.SECONDS);
    }
  }

  @Override
  public void output(Map<String, String> metrics) {
    for (String metricName : metrics.keySet()) {
      final String fileName = String.join(".", this.fileNameHeader, metricName, "dat");
      RollingFileAppenderExt appender = metricsAppenders.computeIfAbsent(metricName, (key) -> {
        String finalPath = Paths.get(getRollingRootFilePath(), fileName).toString();
        RollingFileAppenderExt fileAppender = new RollingFileAppenderExt();
        fileAppender.setLogPermission("rw-------");
        fileAppender.setFile(finalPath);
        fileAppender.setLayout(new PatternLayout("%m%n"));
        fileAppender.setAppend(true);
        fileAppender.setMaxFileSize(getMaxRollingFileSize());
        fileAppender.setMaxBackupIndex(getMaxRollingFileCount());
        fileAppender.activateOptions();
        return fileAppender;
      });

      LoggingEvent event = new LoggingEvent(fileName, Logger.getLogger(fileName), Level.ALL,
          metrics.get(metricName), null);
      appender.append(event);
    }
  }
}
