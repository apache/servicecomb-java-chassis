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

package org.apache.servicecomb.metrics.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.metrics.MetricsConst;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.servo.BasicMonitorRegistry;
import com.netflix.servo.MonitorRegistry;
import com.netflix.servo.monitor.BasicCounter;
import com.netflix.servo.monitor.BasicGauge;
import com.netflix.servo.monitor.BasicTimer;
import com.netflix.servo.monitor.Counter;
import com.netflix.servo.monitor.Gauge;
import com.netflix.servo.monitor.MaxGauge;
import com.netflix.servo.monitor.Monitor;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.MonitorConfig.Builder;
import com.netflix.servo.monitor.Timer;
import com.netflix.servo.tag.Tag;
import com.netflix.servo.tag.TagList;

public class MonitorManager {

  private final Map<String, Counter> counters;

  private final Map<String, MaxGauge> maxGauges;

  private final Map<String, Gauge<?>> gauges;

  private final Map<String, Timer> timers;

  private final MonitorRegistry basicMonitorRegistry;

  private static final char[] forbiddenCharacter = new char[] {'(', ')', '=', ','};

  private static final MonitorManager INSTANCE = new MonitorManager();

  public static MonitorManager getInstance() {
    return INSTANCE;
  }

  private MonitorManager() {
    this.counters = new ConcurrentHashMapEx<>();
    this.maxGauges = new ConcurrentHashMapEx<>();
    this.gauges = new ConcurrentHashMapEx<>();
    this.timers = new ConcurrentHashMapEx<>();
    this.basicMonitorRegistry = new BasicMonitorRegistry();
    setupWindowTime();
    registerSystemMetrics();
  }

  private void setupWindowTime() {
    int time = DynamicPropertyFactory.getInstance().getIntProperty(MetricsConfig.METRICS_WINDOW_TIME, 5000).get();
    System.getProperties().setProperty("servo.pollers", time > 0 ? String.valueOf(time) : "5000");
  }

  public Counter getCounter(String name, String... tags) {
    validateMonitorNameAndTags(name, tags);
    return counters.computeIfAbsent(getMonitorKey(name, tags), f -> {
      Counter counter = new BasicCounter(getConfig(name, tags));
      basicMonitorRegistry.register(counter);
      return counter;
    });
  }

  public Counter getCounter(Function<MonitorConfig, Counter> function, String name, String... tags) {
    validateMonitorNameAndTags(name, tags);
    return counters.computeIfAbsent(getMonitorKey(name, tags), f -> {
      Counter counter = function.apply(getConfig(name, tags));
      basicMonitorRegistry.register(counter);
      return counter;
    });
  }

  public MaxGauge getMaxGauge(String name, String... tags) {
    validateMonitorNameAndTags(name, tags);
    return maxGauges.computeIfAbsent(getMonitorKey(name, tags), f -> {
      MaxGauge maxGauge = new MaxGauge(getConfig(name, tags));
      basicMonitorRegistry.register(maxGauge);
      return maxGauge;
    });
  }

  public <V extends Number> Gauge<?> getGauge(Callable<V> callable, String name, String... tags) {
    validateMonitorNameAndTags(name, tags);
    return gauges.computeIfAbsent(getMonitorKey(name, tags), f -> {
      Gauge<?> gauge = new BasicGauge<>(getConfig(name, tags), callable);
      basicMonitorRegistry.register(gauge);
      return gauge;
    });
  }

  public Timer getTimer(String name, String... tags) {
    validateMonitorNameAndTags(name, tags);
    return timers.computeIfAbsent(getMonitorKey(name, tags), f -> {
      Timer timer = new BasicTimer(getConfig(name, tags));
      basicMonitorRegistry.register(timer);
      return timer;
    });
  }

  public Map<String, Double> measure() {
    Map<String, Double> measurements = new HashMap<>();
    for (Monitor<?> monitor : basicMonitorRegistry.getRegisteredMonitors()) {
      measurements.put(getMonitorKey(monitor.getConfig()),
          ((Number) monitor.getValue(0)).doubleValue());
    }
    return measurements;
  }

  private MonitorConfig getConfig(String name, String... tags) {
    validateMonitorNameAndTags(name, tags);
    Builder builder = MonitorConfig.builder(name);
    for (int i = 0; i < tags.length; i += 2) {
      builder.withTag(tags[i], tags[i + 1]);
    }
    return builder.build();
  }

  private String getMonitorKey(String name, String... tags) {
    validateMonitorNameAndTags(name, tags);
    if (tags.length != 0) {
      SortedMap<String, String> tagMap = new TreeMap<>();
      for (int i = 0; i < tags.length; i += 2) {
        tagMap.put(tags[i], tags[i + 1]);
      }
      StringBuilder builder = new StringBuilder("(");
      for (Entry<String, String> entry : tagMap.entrySet()) {
        builder.append(String.format("%s=%s,", entry.getKey(), entry.getValue()));
      }
      builder.deleteCharAt(builder.length() - 1);
      builder.append(")");
      return name + builder.toString();
    }
    return name;
  }

  private String getMonitorKey(MonitorConfig config) {
    TagList tagList = config.getTags();
    List<String> tags = new ArrayList<>();
    for (Tag tag : tagList) {
      if (!"type".equals(tag.getKey())) {
        tags.add(tag.getKey());
        tags.add(tag.getValue());
      }
    }
    return getMonitorKey(config.getName(), tags.toArray(new String[0]));
  }


  private void registerSystemMetrics() {
    SystemMetrics resource = new SystemMetrics();
    registerSystemMetricItem(resource::getCpuLoad, "cpuLoad");
    registerSystemMetricItem(resource::getCpuRunningThreads, "cpuRunningThreads");
    registerSystemMetricItem(resource::getHeapInit, "heapInit");
    registerSystemMetricItem(resource::getHeapCommit, "heapCommit");
    registerSystemMetricItem(resource::getHeapUsed, "heapUsed");
    registerSystemMetricItem(resource::getHeapMax, "heapMax");
    registerSystemMetricItem(resource::getNonHeapInit, "nonHeapInit");
    registerSystemMetricItem(resource::getNonHeapCommit, "nonHeapCommit");
    registerSystemMetricItem(resource::getNonHeapUsed, "nonHeapUsed");
    registerSystemMetricItem(resource::getNonHeapMax, "nonHeapMax");
  }

  private <V extends Number> void registerSystemMetricItem(Callable<V> callable, String name) {
    this.getGauge(callable, MetricsConst.JVM, MetricsConst.TAG_STATISTIC, "gauge", MetricsConst.TAG_NAME, name);
  }

  private void validateMonitorNameAndTags(String name, String... tags) {
    boolean passed = StringUtils.isNotEmpty(name) && tags.length % 2 == 0;
    if (passed) {
      if (StringUtils.containsNone(name, forbiddenCharacter)) {
        for (String tag : tags) {
          if (StringUtils.containsAny(tag, forbiddenCharacter)) {
            throw new ServiceCombException(
                "validate name and tags failed name = " + name + " tags = " + String.join(",", tags));
          }
        }
      } else {
        throw new ServiceCombException(
            "validate name and tags failed name = " + name + " tags = " + String.join(",", tags));
      }
    } else {
      throw new ServiceCombException(
          "validate name and tags failed name = " + name + " tags = " + String.join(",", tags));
    }
  }
}