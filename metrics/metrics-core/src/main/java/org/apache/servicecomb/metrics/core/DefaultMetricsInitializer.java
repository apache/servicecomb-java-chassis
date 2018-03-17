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
import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.MetricsInitializer;
import org.apache.servicecomb.metrics.core.meter.ConsumerMeters;
import org.apache.servicecomb.metrics.core.meter.ProducerMeters;
import org.apache.servicecomb.metrics.core.meter.invocation.AbstractInvocationMeters;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.netflix.spectator.api.CompositeRegistry;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.servo.ServoRegistry;

public class DefaultMetricsInitializer implements MetricsInitializer {
  public static final String METRICS_WINDOW_TIME = "servicecomb.metrics.window_time";

  public static final int DEFAULT_METRICS_WINDOW_TIME = 5000;

  private Registry registry;

  private ConsumerMeters consumerMeters;

  private ProducerMeters producerMeters;

  @Override
  public void init(CompositeRegistry globalRegistry, EventBus eventBus, MetricsBootstrapConfig config) {
    registry = createRegistry(config);

    this.consumerMeters = new ConsumerMeters(registry);
    this.producerMeters = new ProducerMeters(registry);

    globalRegistry.add(registry);
    eventBus.register(this);
  }

  protected Registry createRegistry(MetricsBootstrapConfig config) {
    System.getProperties().setProperty("servo.pollers", String.valueOf(config.getMsPollInterval()));
    return new ServoRegistry();
  }

  protected AbstractInvocationMeters findInvocationMeters(Invocation invocation) {
    if (invocation.isConsumer()) {
      return consumerMeters.getInvocationMeters();
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
