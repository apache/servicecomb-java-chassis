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

package io.servicecomb.metrics.core.publish;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.servicecomb.foundation.common.exceptions.ServiceCombException;
import io.servicecomb.foundation.common.utils.JsonUtils;

@Component
public class JsonMetricsPublisher implements MetricsPublisher {

  private final DataSource dataSource;

  public JsonMetricsPublisher(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public String metrics(int pollerIndex) {
    if (pollerIndex >= 0 && pollerIndex < dataSource.getAppliedPollingIntervals().size()) {
      try {
        return JsonUtils.writeValueAsString(dataSource.getRegistryMetric(pollerIndex));
      } catch (JsonProcessingException e) {
        throw new ServiceCombException("serialize metrics failed", e);
      }
    } else {
      return "{}";
    }
  }
}
