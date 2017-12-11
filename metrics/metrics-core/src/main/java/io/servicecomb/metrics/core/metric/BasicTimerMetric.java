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

package io.servicecomb.metrics.core.metric;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.netflix.servo.monitor.BasicTimer;
import com.netflix.servo.monitor.Monitor;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.util.Strings;

import io.servicecomb.foundation.common.exceptions.ServiceCombException;

public class BasicTimerMetric extends AbstractMetric {

  //it is a StepCounter and div by second
  public static final String TOTAL = "totalTime";

  public static final String MAX = "max";

  public static final String MIN = "min";

  //it is a StepCounter and div by second
  public static final String COUNT = "count";

  public static final String AVERAGE = "average";

  public static final String[] ALL_TAG = new String[] {TOTAL, MAX, MIN, COUNT, AVERAGE};

  private final Map<String, Monitor> extraTagMetrics;

  private final BasicTimer timer;

  public BasicTimerMetric(String name) {
    super(name);
    this.timer = new BasicTimer(MonitorConfig.builder(name).build());
    this.extraTagMetrics = new HashMap<>();
    for (Monitor monitor : timer.getMonitors()) {
      String key = monitor.getConfig().getTags().getValue("statistic");
      extraTagMetrics.put(key, monitor);
    }

    Monitor average = new Monitor() {
      @Override
      public Object getValue() {
        double count = get("count").doubleValue();
        if (count != 0) {
          return get("totalTime").doubleValue() / get("count").doubleValue();
        } else {
          return 0;
        }
      }

      @Override
      public Object getValue(int pollerIndex) {
        return getValue();
      }

      @Override
      public MonitorConfig getConfig() {
        return MonitorConfig.builder(name).withTag("statistic", AVERAGE).build();
      }
    };
    extraTagMetrics.put(AVERAGE, average);
  }


  @Override
  public void update(Number num) {
    timer.record(num.longValue(), TimeUnit.NANOSECONDS);
  }

  @Override
  public Number get(String tag) {
    if (Strings.isNullOrEmpty(tag)) {
      return timer.getValue(0);
    } else {
      if (extraTagMetrics.containsKey(tag)) {
        return (Number) extraTagMetrics.get(tag).getValue(0);
      } else {
        throw new ServiceCombException("can't find tag in " + getName() + " metric");
      }
    }
  }

  @Override
  public Map<String, Number> getAll() {
    Map<String, Number> values = new HashMap<>(6);
    values.put(getName(), get(null));
    for (String tag : ALL_TAG) {
      values.put(String.join(".", getName(), tag), get(tag));
    }
    return values;
  }
}
