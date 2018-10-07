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
package org.apache.servicecomb.metrics.core;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.InvocationStartEvent;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.MetricsInitializer;
import org.apache.servicecomb.metrics.core.meter.ConsumerMeters;
import org.apache.servicecomb.metrics.core.meter.EdgeMeters;
import org.apache.servicecomb.metrics.core.meter.ProducerMeters;
import org.apache.servicecomb.metrics.core.meter.invocation.AbstractInvocationMeters;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.netflix.spectator.api.CompositeRegistry;
import com.netflix.spectator.api.Registry;

public class InvocationMetersInitializer implements MetricsInitializer {
  private ConsumerMeters consumerMeters;

  private ProducerMeters producerMeters;

  private EdgeMeters edgeMeters;

  @Override
  public void init(CompositeRegistry globalRegistry, EventBus eventBus, MetricsBootstrapConfig config) {
    DefaultRegistryInitializer defaultRegistryInitializer =
        SPIServiceUtils.getTargetService(MetricsInitializer.class, DefaultRegistryInitializer.class);
    Registry registry = defaultRegistryInitializer.getRegistry();

    consumerMeters = new ConsumerMeters(registry);
    producerMeters = new ProducerMeters(registry);
    edgeMeters = new EdgeMeters(registry);

    eventBus.register(this);
  }

  protected AbstractInvocationMeters findInvocationMeters(Invocation invocation) {
    if (invocation.isConsumer()) {
      if (invocation.isEdge()) {
        return edgeMeters.getInvocationMeters();
      } else {
        return consumerMeters.getInvocationMeters();
      }
    }
    return producerMeters.getInvocationMeters();
  }

  @Subscribe
  @AllowConcurrentEvents
  public void onInvocationStart(InvocationStartEvent event) {
    AbstractInvocationMeters invocationMeters = findInvocationMeters(event.getInvocation());
    invocationMeters.onInvocationStart(event);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void onInvocationFinish(InvocationFinishEvent event) {
    AbstractInvocationMeters invocationMeters = findInvocationMeters(event.getInvocation());
    invocationMeters.onInvocationFinish(event);
  }
}
