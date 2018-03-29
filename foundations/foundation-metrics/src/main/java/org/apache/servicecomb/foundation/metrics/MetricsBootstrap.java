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
package org.apache.servicecomb.foundation.metrics;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.netflix.spectator.api.CompositeRegistry;
import com.netflix.spectator.api.Meter;

public class MetricsBootstrap {
  private CompositeRegistry globalRegistry;

  private EventBus eventBus;

  private MetricsBootstrapConfig config = new MetricsBootstrapConfig();

  private ScheduledExecutorService executorService;

  public void start(CompositeRegistry globalRegistry, EventBus eventBus) {
    this.globalRegistry = globalRegistry;
    this.eventBus = eventBus;
    this.executorService = Executors.newScheduledThreadPool(1,
        new ThreadFactoryBuilder()
            .setNameFormat("spectator-poller-%d")
            .build());

    loadMetricsInitializers();
    startPoll();
  }

  public void shutdown() {
    executorService.shutdown();
  }

  protected void loadMetricsInitializers() {
    SPIServiceUtils.getSortedService(MetricsInitializer.class).forEach(initializer -> {
      initializer.init(globalRegistry, eventBus, config);
    });
  }

  protected void startPoll() {
    executorService.scheduleAtFixedRate(this::pollMeters,
        0,
        config.getMsPollInterval(),
        TimeUnit.MILLISECONDS);
  }

  protected void pollMeters() {
    List<Meter> meters = Lists.newArrayList(globalRegistry.iterator());
    PolledEvent event = new PolledEvent(meters);

    eventBus.post(event);
  }
}
