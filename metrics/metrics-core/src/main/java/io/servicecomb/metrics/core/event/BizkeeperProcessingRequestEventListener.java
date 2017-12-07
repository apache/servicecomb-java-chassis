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

package io.servicecomb.metrics.core.event;

import io.servicecomb.foundation.metrics.event.BizkeeperProcessingRequestEvent;
import io.servicecomb.foundation.metrics.event.MetricsEvent;
import io.servicecomb.foundation.metrics.event.MetricsEventListener;
import io.servicecomb.metrics.core.EmbeddedMetricsName;
import io.servicecomb.metrics.core.metric.Metric;
import io.servicecomb.metrics.core.metric.MetricFactory;
import io.servicecomb.metrics.core.registry.MetricsRegistry;
import io.servicecomb.swagger.invocation.InvocationType;

public class BizkeeperProcessingRequestEventListener implements MetricsEventListener {
  private final MetricsRegistry registry;

  private final MetricFactory factory;

  public BizkeeperProcessingRequestEventListener(MetricsRegistry registry, MetricFactory factory) {
    this.registry = registry;
    this.factory = factory;
  }

  @Override
  public Class<? extends MetricsEvent> getConcernedEvent() {
    return BizkeeperProcessingRequestEvent.class;
  }

  @Override
  public void process(MetricsEvent data) {
    BizkeeperProcessingRequestEvent event = (BizkeeperProcessingRequestEvent) data;

    String totalName;
    if (event.getInvocationType().equals(String.valueOf(InvocationType.CONSUMER))) {
      totalName = String
          .format(EmbeddedMetricsName.APPLICATION_TOTAL_REQUEST_COUNT_PER_CONSUMER, event.getOperationName());
    } else {
      totalName = String
          .format(EmbeddedMetricsName.APPLICATION_TOTAL_REQUEST_COUNT_PER_PROVIDER, event.getOperationName());
    }

    Metric metric = registry.getMetric(totalName);
    if (metric == null) {
      metric = registry.getOrCreateMetric(factory.createCounter(totalName));
    }
    metric.update(1);
  }
}
