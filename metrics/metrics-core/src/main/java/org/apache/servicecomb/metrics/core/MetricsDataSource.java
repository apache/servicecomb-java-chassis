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

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.metrics.core.monitor.RegistryMonitor;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

import com.google.common.collect.Lists;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.servo.util.Strings;

public class MetricsDataSource {
  private RegistryMonitor registryMonitor;

  private Map<Long, Integer> appliedWindowTimes = new HashMap<>();

  private static final MetricsDataSource INSTANCE = new MetricsDataSource();

  public static MetricsDataSource getInstance() {
    return INSTANCE;
  }

  private MetricsDataSource() {
    this.init(RegistryMonitor.getInstance(), DynamicPropertyFactory
        .getInstance().getStringProperty(MetricsConfig.METRICS_POLLING_TIME, "5000").get());
  }

  public MetricsDataSource(RegistryMonitor registryMonitor, String pollingSettings) {
    this.init(registryMonitor, pollingSettings);
  }

  private void init(RegistryMonitor registryMonitor, String pollingSettings) {
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


  /**  What's the WindowTime ?
   We all know there are two major type of metric :
   1.Time-unrelated,you can get the latest value any time immediately:
   Counter -> increase or decrease
   Guage -> set a certain one value
   2.Time-related,only after a centain time pass you can compute the right value,"a centain time" called WindowTime
   Max & Min -> the max value or min value in a centain time
   Average -> average value, the simplest algorithm is f = sum / count
   Rate -> like TPS,algorithm is f = sum / second

   Will be return "servicecomb.metrics.window_time" setting in microservice.yaml
   */
  public List<Long> getAppliedWindowTime() {
    return Lists.newArrayList(appliedWindowTimes.keySet());
  }

  //same as call measure(getAppliedWindowTime().get(0),false)
  public Map<String, Double> measure() {
    return measure(getAppliedWindowTime().get(0));
  }

  //same as call measure(windowTime,false)
  public Map<String, Double> measure(long windowTime) {
    return measure(windowTime, false);
  }

  /**
   * windowTime usage example:
   * if there is two window time set in "servicecomb.metrics.window_time" like 1000,2000
   * then windowTime = 1000 will return result of the setting 1000(1 second)
   * windowTime = 2000 will return result of the setting 2000(2 second)
   *
   * there are three monitor of max,min,total
   * 0----------1----------2----------3----------  <-time line (second)
   *   100,200    300,400                          <-value record
   *
   *                 ↑ measure(1000) will return max=200 min=100 total=300
   *                   measure(2000) will return max=0 min=0 total=0
   *                             ↑ measure(1000) will return max=300 min=400 total=700
   *                               measure(2000) will return max=400 min=100 total=1000
   *
   * @param windowTime getAppliedWindowTime() item
   * @param calculateLatency need output latency
   * @return Map<String               ,               Double>
   */
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
