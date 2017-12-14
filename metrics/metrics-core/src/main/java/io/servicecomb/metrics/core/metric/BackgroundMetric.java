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

import io.servicecomb.foundation.common.exceptions.ServiceCombException;
import rx.functions.Func0;

public class BackgroundMetric extends AbstractMetric {

  private Map<String, Number> lastUpdateValues = new HashMap<>();

  private final Func0<Map<String, Number>> getCallback;

  public BackgroundMetric(String name, Func0<Map<String, Number>> getCallback) {
    super(name);
    this.getCallback = getCallback;
  }

  @Override
  public Number get() {
    throw new ServiceCombException("unsupport single get without key");
  }

  public Number get(String key) {
    if (lastUpdateValues.containsKey(key)) {
      return lastUpdateValues.get(key);
    } else {
      throw new ServiceCombException("can't find " + key + " in " + getName() + " metric");
    }
  }


  public void call(){
    lastUpdateValues = this.getCallback.call();
  }

  public Map<String, Number> getAll() {
    return lastUpdateValues;
  }
}
