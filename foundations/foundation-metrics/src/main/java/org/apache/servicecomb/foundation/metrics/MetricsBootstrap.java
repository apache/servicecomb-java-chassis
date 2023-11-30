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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.foundation.metrics.meter.PeriodMeter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.micrometer.core.instrument.MeterRegistry;

public class MetricsBootstrap {
  private static final Logger LOGGER = LoggerFactory.getLogger(MetricsBootstrap.class);

  private final MetricsBootstrapConfig config;

  private MeterRegistry meterRegistry;

  private EventBus eventBus;

  private ScheduledExecutorService executorService;

  private List<MetricsInitializer> metricsInitializers;

  @Autowired
  public MetricsBootstrap(MetricsBootstrapConfig config) {
    this.config = config;
  }

  @Autowired
  public void setMetricsInitializers(List<MetricsInitializer> metricsInitializers) {
    this.metricsInitializers = metricsInitializers;
  }

  @Autowired
  public void setMeterRegistry(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  public void start(EventBus eventBus) {
    this.eventBus = eventBus;
    this.executorService = Executors.newScheduledThreadPool(1,
        new ThreadFactoryBuilder()
            .setNameFormat("metrics-poller-%d")
            .build());

    metricsInitializers.forEach(initializer -> initializer.init(this.meterRegistry, eventBus, config));
    startPoll();
  }

  public void shutdown() {
    if (executorService != null) {
      executorService.shutdown();
    }

    Collections.reverse(metricsInitializers);
    metricsInitializers.forEach(MetricsInitializer::destroy);
  }

  protected void startPoll() {
    executorService.scheduleAtFixedRate(this::pollMeters,
        config.getMsPollInterval(),
        config.getMsPollInterval(),
        TimeUnit.MILLISECONDS);
  }

  public synchronized void pollMeters() {
    metricsInitializers.forEach(initializer -> {
      if (initializer instanceof PeriodMeter) {
        ((PeriodMeter) initializer).poll(System.currentTimeMillis(), config.getMsPollInterval());
      }
    });
    try {
      PolledEvent polledEvent = new PolledEvent(meterRegistry.getMeters());
      eventBus.post(polledEvent);
    } catch (Throwable e) {
      LOGGER.error("poll meters error. ", e);
    }
  }
}
