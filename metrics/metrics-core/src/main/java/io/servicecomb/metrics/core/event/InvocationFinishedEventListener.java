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

import io.servicecomb.foundation.metrics.event.InvocationFinishedEvent;
import io.servicecomb.foundation.metrics.event.MetricsEvent;
import io.servicecomb.foundation.metrics.event.MetricsEventListener;
import io.servicecomb.metrics.core.EmbeddedMetricsName;
import io.servicecomb.metrics.core.metric.Metric;
import io.servicecomb.metrics.core.metric.MetricFactory;
import io.servicecomb.metrics.core.registry.MetricsRegistry;

public class InvocationFinishedEventListener implements MetricsEventListener {

  private final MetricsRegistry registry;

  private final MetricFactory factory;

  public InvocationFinishedEventListener(MetricsRegistry registry,
      MetricFactory factory) {
    this.registry = registry;
    this.factory = factory;
  }

  @Override
  public Class<? extends MetricsEvent> getConcernedEvent() {
    return InvocationFinishedEvent.class;
  }

  @Override
  public void process(MetricsEvent data) {
    InvocationFinishedEvent event = (InvocationFinishedEvent) data;

    String executionTimeName = String.format(EmbeddedMetricsName.QUEUE_EXECUTION_TIME, event.getOperationName());
    Metric metric = registry.getMetric(executionTimeName);
    if (metric == null) {
      metric = registry.getOrCreateMetric(factory.createTimer(executionTimeName));
    }
    String instanceExecutionTimeName = String.format(EmbeddedMetricsName.QUEUE_EXECUTION_TIME, "instance");
    Metric instanceMetric = registry.getMetric(instanceExecutionTimeName);

    metric.update(event.getTimeProcess());
    instanceMetric.update(event.getTimeProcess());
  }
}
