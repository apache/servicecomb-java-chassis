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

package io.servicecomb.metrics.core.event;

import io.servicecomb.core.metrics.InvocationStartProcessingEvent;
import io.servicecomb.foundation.common.event.Event;
import io.servicecomb.foundation.common.event.EventListener;
import io.servicecomb.metrics.core.monitor.InvocationMonitor;
import io.servicecomb.metrics.core.monitor.RegistryMonitor;
import io.servicecomb.swagger.invocation.InvocationType;

public class InvocationStartProcessingEventListener implements EventListener {

  private final RegistryMonitor registryMonitor;

  public InvocationStartProcessingEventListener(RegistryMonitor registryMonitor) {
    this.registryMonitor = registryMonitor;
  }

  @Override
  public Class<? extends Event> getConcernedEvent() {
    return InvocationStartProcessingEvent.class;
  }

  @Override
  public void process(Event data) {
    InvocationStartProcessingEvent event = (InvocationStartProcessingEvent) data;
    InvocationMonitor monitor = registryMonitor.getInvocationMonitor(event.getOperationName());
    //TODO:current java chassis unable know invocation type before starting process,so all type WaitInQueue increment(-1) (decrement)
    monitor.getWaitInQueue().increment(-1);
    if (InvocationType.PRODUCER.equals(event.getInvocationType())) {
      monitor.getLifeTimeInQueue().update(event.getInQueueNanoTime());
      monitor.getProducerCall().increment();
    } else {
      monitor.getConsumerCall().increment();
    }
  }
}
