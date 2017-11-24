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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Category;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.servo.publish.BasicMetricFilter;
import com.netflix.servo.publish.CounterToRateMetricTransform;
import com.netflix.servo.publish.MetricObserver;
import com.netflix.servo.publish.MonitorRegistryMetricPoller;
import com.netflix.servo.publish.PollRunnable;
import com.netflix.servo.publish.PollScheduler;

import io.servicecomb.foundation.common.utils.RollingFileAppenderExt;
import io.servicecomb.foundation.metrics.MetricsServoRegistry;
import io.servicecomb.foundation.metrics.output.MetricsOutput;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;

@Component
public class MetricsServoFileOutput extends MetricsOutput {

  private static final Logger logger = LoggerFactory.getLogger(MetricsServoFileOutput.class);

  private final String applicationName;
  private final ObjectMapper mapper = new ObjectMapper();
  private final Map<String, RollingFileAppenderExt> metricsAppenders;
  private final MetricsServoRegistry registry;
  private String hostName;


  @Autowired
  public MetricsServoFileOutput(MetricsServoRegistry registry) {
    this.metricsAppenders = new HashMap<>();
    this.registry = registry;
    try {
      InetAddress localHost = InetAddress.getLocalHost();
      hostName = localHost.getHostName();
    } catch (UnknownHostException e) {
      e.printStackTrace();
      hostName = "UnknownHost";
    }

    if (RegistryUtils.getServiceRegistry() != null) {
      Microservice microservice = RegistryUtils.getMicroservice();
      applicationName = String.join(".", microservice.getAppId(), microservice.getServiceName());
    } else {
      applicationName = String.join(".", hostName, "test");
    }

    this.init();
  }

  @Override
  public void init() {
    PollScheduler scheduler = PollScheduler.getInstance();
    if (!scheduler.isStarted()) {
      scheduler.start();
    }

    if (isEnabled()) {
      MetricObserver fileObserver = new SeparatedMetricObserver(applicationName, this, registry);
      MetricObserver fileTransform = new CounterToRateMetricTransform(fileObserver, getMetricPoll(), TimeUnit.SECONDS);
      PollRunnable fileTask = new PollRunnable(new MonitorRegistryMetricPoller(), BasicMetricFilter.MATCH_ALL,
          fileTransform);
      scheduler.addPoller(fileTask, getMetricPoll(), TimeUnit.SECONDS);
    }
  }

  @Override
  public void output(Map<String, String> metrics) {

    for (String metricName : metrics.keySet()) {
      final String fileName = String.join(".", this.applicationName, metricName, "dat");
      RollingFileAppenderExt appender = metricsAppenders.computeIfAbsent(metricName, (key) -> {
        String finalPath = Paths.get(getRollingRootFilePath(), fileName).toString();
        RollingFileAppenderExt fileAppender = new RollingFileAppenderExt();
        fileAppender.setLogPermission("rw-------");
        fileAppender.setFile(finalPath);
        fileAppender.setLayout(new PatternLayout("%m%n"));
        fileAppender.setThreshold(Priority.FATAL);
        fileAppender.setAppend(true);
        fileAppender.setMaxFileSize(getMaxRollingFileSize());
        fileAppender.setMaxBackupIndex(getMaxRollingFileCount());
        fileAppender.activateOptions();
        return fileAppender;
      });

      LoggingEvent event = null;
      try {
        SeparatedOutputData outputData = new SeparatedOutputData(this.applicationName, hostName, metricName,
            metrics.get(metricName));
        event = new LoggingEvent(fileName, Category.getInstance(fileName), Priority.FATAL,
            mapper.writeValueAsString(outputData), null);
      } catch (JsonProcessingException e) {
        logger.error("parse metric data error");
      }
      appender.append(event);
    }
  }

  class SeparatedOutputData {
    private String plugin_id = null;
    private Map<String, Object> metric = null;

    public String getPlugin_id() {
      return plugin_id;
    }

    public Map<String, Object> getMetric() {
      return metric;
    }

    public SeparatedOutputData(String plugin_id, String hostName, String metricName, String metricValue) {
      this.plugin_id = plugin_id;
      this.metric = new HashMap<>();
      this.metric.put("node", hostName);
      this.metric.put("scope_name", "");
      this.metric.put("timestamp", System.currentTimeMillis());
      this.metric.put("inface_name", "");
      this.metric.put(metricName, metricValue);
    }
  }
}
