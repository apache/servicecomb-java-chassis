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

package io.servicecomb.metrics.core.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.servicecomb.foundation.common.exceptions.ServiceCombException;
import io.servicecomb.foundation.common.utils.JsonUtils;
import io.servicecomb.metrics.core.health.HealthCheckResult;
import io.servicecomb.metrics.core.registry.HealthCheckRegistry;

public class DefaultHealthCheckerPublisher implements HealthCheckerPublisher {

  private final HealthCheckRegistry registry;

  public DefaultHealthCheckerPublisher(HealthCheckRegistry registry) {
    this.registry = registry;
  }

  @Override
  public Map<String, String> health() {
    Map<String, String> output = new HashMap<>();
    for (Entry<String, HealthCheckResult> entry : registry.checkAllStatus().entrySet()) {
      try {
        output.put(entry.getKey(), JsonUtils.writeValueAsString(entry.getValue()));
      } catch (JsonProcessingException e) {
        throw new ServiceCombException("parse health check result failed", e);
      }
    }
    return output;
  }

  @Override
  public String health(String name) {
    try {
      return JsonUtils.writeValueAsString(registry.checkStatus(name));
    } catch (JsonProcessingException e) {
      throw new ServiceCombException("parse health check result failed", e);
    }
  }
}
