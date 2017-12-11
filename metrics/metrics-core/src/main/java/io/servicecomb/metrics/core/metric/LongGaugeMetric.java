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

import com.netflix.servo.monitor.LongGauge;

public class LongGaugeMetric extends AbstractMetric implements WritableMetric{

  private final LongGauge gauge;

  public LongGaugeMetric(LongGauge gauge) {
    super(gauge.getConfig().getName());
    this.gauge = gauge;
  }

  @Override
  public void update(Number num) {
    gauge.set(num.longValue());
  }

  @Override
  public void increment() {
    gauge.getNumber().incrementAndGet();
  }

  @Override
  public void decrement() {
    gauge.getNumber().decrementAndGet();
  }

  @Override
  public Number get(String tag) {
    return gauge.getValue();
  }
}