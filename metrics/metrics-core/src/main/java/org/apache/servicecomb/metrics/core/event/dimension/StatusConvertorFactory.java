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

package org.apache.servicecomb.metrics.core.event.dimension;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.servicecomb.metrics.common.MetricsDimension;
import org.apache.servicecomb.metrics.core.MetricsConfig;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StatusConvertorFactory {

  private final Map<String, Supplier<StatusConvertor>> suppliers;

  public StatusConvertorFactory() {
    this.suppliers = new HashMap<>();
    this.suppliers.put(MetricsDimension.DIMENSION_STATUS_OUTPUT_LEVEL_CODE, CodeStatusConvertor::new);
    this.suppliers.put(MetricsDimension.DIMENSION_STATUS_OUTPUT_LEVEL_CODE_GROUP, CodeGroupStatusConvertor::new);
    this.suppliers
        .put(MetricsDimension.DIMENSION_STATUS_OUTPUT_LEVEL_SUCCESS_FAILED, SuccessFailedStatusConvertor::new);
  }

  public StatusConvertor getConvertor(String outputLevel) {
    if (suppliers.containsKey(outputLevel)) {
      return suppliers.get(outputLevel).get();
    }
    LoggerFactory.getLogger(StatusConvertorFactory.class).error("unknown config value of " +
        MetricsConfig.METRICS_DIMENSION_STATUS_OUTPUT_LEVEL + " : " + outputLevel
        + ", use default level : " +
        MetricsDimension.DIMENSION_STATUS_OUTPUT_LEVEL_SUCCESS_FAILED + " replace it");
    //return default
    return suppliers.get(MetricsDimension.DIMENSION_STATUS_OUTPUT_LEVEL_SUCCESS_FAILED).get();
  }
}
