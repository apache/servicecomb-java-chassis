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

import java.util.Map;

import com.netflix.servo.monitor.BasicCounter;
import com.netflix.servo.monitor.DoubleGauge;
import com.netflix.servo.monitor.LongGauge;
import com.netflix.servo.monitor.MonitorConfig;

import rx.functions.Func0;

public class DefaultMetricFactory implements MetricFactory {
  @Override
  public Metric createCounter(String name) {
    return new CounterMetric(new BasicCounter(MonitorConfig.builder(name).build()));
  }

  @Override
  public Metric createDoubleGauge(String name) {
    return new DoubleGaugeMetric(new DoubleGauge(MonitorConfig.builder(name).build()));
  }

  @Override
  public Metric createLongGauge(String name) {
    return new LongGaugeMetric(new LongGauge(MonitorConfig.builder(name).build()));
  }

  @Override
  public Metric createTimer(String name) {
    return new BasicTimerMetric(name);
  }

  @Override
  public Metric createCustom(String name, Func0<Number> getCallback) {
    return new CustomMetric(name, getCallback);
  }

  @Override
  public Metric createCustomMulti(String name, Func0<Map<String, Number>> getCallback) {
    return new CustomMultiMetric(name, getCallback);
  }

  @Override
  public Metric createBackground(String name, Func0<Map<String, Number>> getCallback, long reloadInterval) {
    return new BackgroundMetric(name, getCallback, reloadInterval);
  }
}
