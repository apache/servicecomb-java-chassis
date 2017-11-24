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

package io.servicecomb.foundation.metrics.output.file.servo;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.log4j.Category;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.servo.Metric;
import com.netflix.servo.publish.BaseMetricObserver;
import com.netflix.servo.util.Preconditions;

import io.servicecomb.foundation.common.utils.RollingFileAppenderExt;
import io.servicecomb.foundation.metrics.MetricsServoRegistry;

public class SeparatedMetricObserver extends BaseMetricObserver {
  private final ObjectMapper mapper = new ObjectMapper();
  private final String filePath;
  private final String maxSize;
  private final MetricsServoRegistry registry;
  private final String hostName;
  private final Map<String, RollingFileAppenderExt> metricsAppenders;


  public SeparatedMetricObserver(String name, String filePath, String maxSize,String hostName, MetricsServoRegistry registry) {
    super(name);
    this.filePath = filePath;
    this.maxSize = maxSize;
    this.registry = registry;
    this.hostName = hostName;
    this.metricsAppenders = new HashMap<>();

  }

  @Override
  public void updateImpl(List<Metric> metrics) {
    Preconditions.checkNotNull(metrics, "metrics");
    //这些参数是一次一起计算的，所以不需要将它们转化为独立的Metric，直接取值输出
    Map<String, String> queueMetrics = registry.calculateQueueMetrics();
    Map<String, String> systemMetrics = registry.getSystemMetrics();
    Map<String, String> tpsAndLatencyMetrics = registry.calculateTPSAndLatencyMetrics();

    Map<String, SeparatedOutputData> output = new HashMap<>();

    output.putAll(queueMetrics.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry ->
        new SeparatedOutputData(this.getName(), hostName, entry.getKey(), entry.getValue()))));
    output.putAll(systemMetrics.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry ->
        new SeparatedOutputData(this.getName(), hostName, entry.getKey(), entry.getValue()))));
    output.putAll(tpsAndLatencyMetrics.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry ->
        new SeparatedOutputData(this.getName(), hostName, entry.getKey(), entry.getValue()))));
    for (Metric metric : metrics) {
      output.put(metric.getConfig().getName(),
          new SeparatedOutputData(this.getName(), hostName, metric.getConfig().getName(), metric.getValue().toString()));
    }

    for (String metricName : output.keySet()) {
      final String fileName = String.join(".", this.getName(), metricName, "dat");
      RollingFileAppenderExt appender = metricsAppenders.computeIfAbsent(metricName, (key) -> {
        String finalPath = Paths.get(filePath, fileName).toString();
        RollingFileAppenderExt fileAppender = new RollingFileAppenderExt();
        fileAppender.setLogPermission("rw-------");

        fileAppender.setFile(finalPath);
        fileAppender.setLayout(new PatternLayout("%m%n"));

        fileAppender.setThreshold(Priority.FATAL);
        fileAppender.setAppend(true);
        fileAppender.setMaxFileSize(maxSize);
        fileAppender.activateOptions();
        return fileAppender;
      });

      LoggingEvent event = null;
      try {
        event = new LoggingEvent(fileName, Category.getInstance(fileName), Priority.FATAL,
            mapper.writeValueAsString(output.get(metricName)), null);
      } catch (JsonProcessingException e) {
        e.printStackTrace();
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


