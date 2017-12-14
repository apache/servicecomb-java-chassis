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

package io.servicecomb.metrics.core.schedule;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.Executors;

import com.netflix.config.DynamicPropertyFactory;

public abstract class AbstractStatisticsRunner implements StatisticsRunner {

  private static final String METRICS_STATISTICS_TIME = "servicecomb.metrics.statistics_millisecond";

  private final int statisticsTime;

  public int getStatisticsTime() {
    return statisticsTime;
  }

  public AbstractStatisticsRunner() {
    statisticsTime = DynamicPropertyFactory.getInstance().getIntProperty(METRICS_STATISTICS_TIME, 10000).get();
    final Runnable executor = this::run;
    Executors.newScheduledThreadPool(1)
        .scheduleWithFixedDelay(executor, this.getStatisticsTime(), this.getStatisticsTime(), MILLISECONDS);
  }

  public abstract void run();
}
