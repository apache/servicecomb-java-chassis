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

package org.apache.servicecomb.metrics.core.publish;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.metrics.core.MetricsConfig;
import org.apache.servicecomb.metrics.core.monitor.RegistryMonitor;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.servo.util.Strings;

@Component
public class DefaultDataSource implements DataSource {
  private final RegistryMonitor registryMonitor;

  private final Map<Long, Integer> appliedWindowTimes = new HashMap<>();

  @Autowired
  public DefaultDataSource(RegistryMonitor registryMonitor) {
    this(registryMonitor,
        DynamicPropertyFactory.getInstance().getStringProperty(MetricsConfig.METRICS_POLLING_TIME, "5000").get());
  }

  public DefaultDataSource(RegistryMonitor registryMonitor, String pollingSettings) {
    this.registryMonitor = registryMonitor;

    String[] pollingSettingStrings = pollingSettings.split(",");
    Set<Long> parsePollingSettings = new HashSet<>();
    for (String singlePollingSetting : pollingSettingStrings) {
      try {
        long settingValue = Long.parseLong(singlePollingSetting);
        if (settingValue > 0) {
          parsePollingSettings.add(settingValue);
        } else {
          throw new ServiceCombException(
              "bad format servicecomb.metrics.window_time : " + String.valueOf(settingValue));
        }
      } catch (NumberFormatException e) {
        throw new ServiceCombException("bad format servicecomb.metrics.window_time", e);
      }
    }

    List<Long> sortedPollingSettings = Lists.newArrayList(parsePollingSettings);
    System.getProperties().setProperty("servo.pollers", Strings.join(",", sortedPollingSettings.iterator()));
    for (int i = 0; i < sortedPollingSettings.size(); i++) {
      this.appliedWindowTimes.put(sortedPollingSettings.get(i), i);
    }
  }

  @Override
  public List<Long> getAppliedWindowTime() {
    return Lists.newArrayList(appliedWindowTimes.keySet());
  }

  @Override
  public Map<String, Double> measure() {
    return measure(getAppliedWindowTime().get(0));
  }

  @Override
  public Map<String, Double> measure(long windowTime) {
    return measure(windowTime, false);
  }

  @Override
  public Map<String, Double> measure(long windowTime, boolean calculateLatency) {
    Integer index = appliedWindowTimes.get(windowTime);
    if (index != null) {
      return registryMonitor.measure(index, calculateLatency);
    }
    throw new InvocationException(BAD_REQUEST,
        "windowTime : " + windowTime + " unset in servicecomb.metrics.window_time,current available are : " +
            Strings.join(",", getAppliedWindowTime().iterator()));
  }
}
