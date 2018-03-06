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

import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.foundation.metrics.MetricsConst;

import com.netflix.servo.monitor.BasicTimer;
import com.netflix.servo.monitor.Monitor;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.tag.Tags;

//latency and max should return by default millisecond unit value for easy use
//because Timer is extends NumericMonitor<Long> so only can return Long result value and lost precision (value after the decimal point will be remove)
//warp BasicTimer, return custom Monitor<Double> for calculate double millisecond value
public class HighPrecisionBasicTimer extends BasicTimer {
  private final MonitorForTimerValue timerValueMonitor;

  private final MonitorForTimerMax timerMaxMonitor;

  public MonitorForTimerValue getTimerValueMonitor() {
    return timerValueMonitor;
  }

  public MonitorForTimerMax getTimerMaxMonitor() {
    return timerMaxMonitor;
  }

  public HighPrecisionBasicTimer(MonitorConfig config) {
    super(config, TimeUnit.NANOSECONDS);
    this.timerValueMonitor = new MonitorForTimerValue(this);
    this.timerMaxMonitor = new MonitorForTimerMax(this);
  }

  class MonitorForTimerValue implements Monitor<Double> {

    private final BasicTimer timer;

    public MonitorForTimerValue(BasicTimer timer) {
      this.timer = timer;
    }

    @Override
    public Double getValue() {
      return this.getValue(0);
    }

    @Override
    public Double getValue(int pollerIndex) {
      //we need direct div for keep value after the decimal point
      long nanoValue = timer.getValue();
      return (double) nanoValue / (double) 1000000;
    }

    @Override
    public MonitorConfig getConfig() {
      return timer.getConfig().withAdditionalTag(Tags.newTag(MetricsConst.TAG_STATISTIC, "latency"));
    }
  }

  class MonitorForTimerMax implements Monitor<Double> {

    private final BasicTimer timer;

    public MonitorForTimerMax(BasicTimer timer) {
      this.timer = timer;
    }

    @Override
    public Double getValue() {
      return getValue(0);
    }

    @Override
    public Double getValue(int pollerIndex) {
      //we need direct div for keep value after the decimal point
      return timer.getMax() / (double) 1000000;
    }

    @Override
    public MonitorConfig getConfig() {
      return timer.getConfig().withAdditionalTag(Tags.newTag(MetricsConst.TAG_STATISTIC, "max"));
    }
  }
}
