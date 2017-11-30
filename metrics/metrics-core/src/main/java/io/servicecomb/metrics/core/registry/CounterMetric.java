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

package io.servicecomb.metrics.core.registry;

import com.netflix.servo.monitor.Counter;

public class CounterMetric extends AbstractMetric {

  private final Counter counter;

  public CounterMetric(Counter counter) {
    super(counter.getConfig().getName());
    this.counter = counter;
  }

  @Override
  public void update(Number num) {
    counter.increment(num.longValue());
  }

  @Override
  public Number get(String tag) {
    return counter.getValue();
  }
}
