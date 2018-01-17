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

package org.apache.servicecomb.metrics.core.event;

import org.apache.servicecomb.core.metrics.InvocationFinishedEvent;
import org.apache.servicecomb.foundation.common.event.Event;
import org.apache.servicecomb.foundation.common.event.EventListener;
import org.apache.servicecomb.metrics.common.MetricsDimension;
import org.apache.servicecomb.metrics.core.event.dimension.StatusConvertor;
import org.apache.servicecomb.metrics.core.monitor.ConsumerInvocationMonitor;
import org.apache.servicecomb.metrics.core.monitor.ProducerInvocationMonitor;
import org.apache.servicecomb.metrics.core.monitor.RegistryMonitor;
import org.apache.servicecomb.swagger.invocation.InvocationType;

public class InvocationFinishedEventListener implements EventListener {
  private final RegistryMonitor registryMonitor;

  private final StatusConvertor convertor;

  public InvocationFinishedEventListener(RegistryMonitor registryMonitor, StatusConvertor convertor) {
    this.registryMonitor = registryMonitor;
    this.convertor = convertor;
  }

  @Override
  public Class<? extends Event> getConcernedEvent() {
    return InvocationFinishedEvent.class;
  }

  @Override
  public void process(Event data) {
    InvocationFinishedEvent event = (InvocationFinishedEvent) data;
    String statusDimensionValue = convertor.convert(event.isSuccess(), event.getStatusCode());
    if (InvocationType.PRODUCER.equals(event.getInvocationType())) {
      ProducerInvocationMonitor monitor = registryMonitor.getProducerInvocationMonitor(event.getOperationName());
      monitor.getExecutionTime().update(event.getProcessElapsedNanoTime());
      monitor.getProducerLatency().update(event.getTotalElapsedNanoTime());
      monitor.getProducerCall().increment(MetricsDimension.DIMENSION_STATUS, statusDimensionValue);
    } else {
      ConsumerInvocationMonitor monitor = registryMonitor.getConsumerInvocationMonitor(event.getOperationName());
      monitor.getConsumerLatency().update(event.getTotalElapsedNanoTime());
      monitor.getConsumerCall().increment(MetricsDimension.DIMENSION_STATUS, statusDimensionValue);
    }
  }
}
