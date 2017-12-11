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

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import io.servicecomb.foundation.common.exceptions.ServiceCombException;
import rx.functions.Func0;

public class DefaultBackgroundMetric extends AbstractMetric implements BackgroundMetric {

  private Map<String, Number> lastUpdateValues = new HashMap<>();

  public DefaultBackgroundMetric(String name, Func0<Map<String, Number>> getCallback, long reloadInterval) {
    super(name);
    final Runnable executor = () -> lastUpdateValues = getCallback.call();
    Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(executor, 0, reloadInterval, MILLISECONDS);
  }

  @Override
  public Number get(String tag) {
    if (lastUpdateValues.containsKey(tag)) {
      return lastUpdateValues.get(tag);
    } else {
      throw new ServiceCombException("can't find tag in " + getName() + " metric");
    }
  }

  @Override
  public Map<String, Number> getAll() {
    return lastUpdateValues;
  }

  @Override
  public Map<String, Number> getAllWithFilter(String prefix) {
    return lastUpdateValues.entrySet().stream().filter(entry -> entry.getKey().startsWith(prefix))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }
}
