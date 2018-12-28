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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.metrics.registry.GlobalRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class MetricsBootstrap {
  private static final Logger LOGGER = LoggerFactory.getLogger(MetricsBootstrap.class);

  private GlobalRegistry globalRegistry;

  private EventBus eventBus;

  private MetricsBootstrapConfig config = new MetricsBootstrapConfig();

  private ScheduledExecutorService executorService;

  public void start(GlobalRegistry globalRegistry, EventBus eventBus) {
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
    if (executorService != null) {
      executorService.shutdown();
    }

    List<MetricsInitializer> initializers = new ArrayList<>(SPIServiceUtils.getSortedService(MetricsInitializer.class));
    Collections.reverse(initializers);
    initializers.forEach(initializer -> initializer.destroy());
  }

  protected void loadMetricsInitializers() {
    SPIServiceUtils.getSortedService(MetricsInitializer.class)
        .forEach(initializer -> initializer.init(globalRegistry, eventBus, config));
  }

  protected void startPoll() {
    executorService.scheduleAtFixedRate(this::pollMeters,
        0,
        config.getMsPollInterval(),
        TimeUnit.MILLISECONDS);
  }

  public synchronized void pollMeters() {
    try {
      long secondInterval = TimeUnit.MILLISECONDS.toSeconds(config.getMsPollInterval());
      PolledEvent polledEvent = globalRegistry.poll(secondInterval);
      eventBus.post(polledEvent);
    } catch (Throwable e) {
      LOGGER.error("poll meters error. ", e);
    }
  }
}
