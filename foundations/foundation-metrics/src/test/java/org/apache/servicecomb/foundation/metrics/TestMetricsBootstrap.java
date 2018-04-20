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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.netflix.spectator.api.CompositeRegistry;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Meter;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

public class TestMetricsBootstrap {
  MetricsBootstrap bootstrap = new MetricsBootstrap();

  @Mocked
  CompositeRegistry globalRegistry;

  EventBus eventBus = new EventBus();

  @Test
  public void loadMetricsInitializers() {
    List<MetricsInitializer> initList = new ArrayList<>();
    MetricsInitializer metricsInitializer = new MetricsInitializer() {
      @Override
      public void init(CompositeRegistry globalRegistry, EventBus eventBus, MetricsBootstrapConfig config) {
        initList.add(this);
      }
    };
    new Expectations(SPIServiceUtils.class) {
      {
        SPIServiceUtils.getSortedService(MetricsInitializer.class);
        result = Arrays.asList(metricsInitializer, metricsInitializer);
      }
    };

    bootstrap.start(globalRegistry, eventBus);
    bootstrap.shutdown();

    Assert.assertThat(initList, Matchers.contains(metricsInitializer, metricsInitializer));
  }

  @Test
  public void pollMeters(@Mocked Meter meter, @Mocked Measurement measurement,
      @Mocked ScheduledExecutorService executor) {
    List<Meter> meters = Arrays.asList(meter);
    new Expectations(Executors.class) {
      {
        Executors.newScheduledThreadPool(1, (ThreadFactory) any);
        result = executor;
        globalRegistry.iterator();
        result = meters.iterator();
        meter.measure();
        result = Arrays.asList(measurement);
      }
    };
    bootstrap.start(globalRegistry, eventBus);

    PolledEvent result = new PolledEvent(null, null);
    eventBus.register(new Object() {
      @Subscribe
      public void onEvent(PolledEvent event) {
        result.setMeters(event.getMeters());
        result.setMeasurements(event.getMeasurements());
      }
    });

    bootstrap.pollMeters();
    bootstrap.shutdown();

    Assert.assertEquals(meters, result.getMeters());
    Assert.assertThat(result.getMeasurements(), Matchers.contains(measurement));
  }

  @Test
  public void shutdown(@Mocked ScheduledExecutorService scheduledExecutorService) {
    List<MetricsInitializer> uninitList = new ArrayList<>();
    MetricsInitializer initializer1 = new MetricsInitializer() {
      @Override
      public int getOrder() {
        return 1;
      }

      @Override
      public void init(CompositeRegistry globalRegistry, EventBus eventBus, MetricsBootstrapConfig config) {
      }

      @Override
      public void uninit() {
        uninitList.add(this);
      }
    };

    MetricsInitializer initializer2 = new MetricsInitializer() {
      @Override
      public int getOrder() {
        return 2;
      }

      @Override
      public void init(CompositeRegistry globalRegistry, EventBus eventBus, MetricsBootstrapConfig config) {
      }

      @Override
      public void uninit() {
        uninitList.add(this);
      }
    };

    new Expectations(SPIServiceUtils.class) {
      {
        SPIServiceUtils.getSortedService(MetricsInitializer.class);
        result = Arrays.asList(initializer1, initializer2);
      }
    };
    Deencapsulation.setField(bootstrap, "executorService", scheduledExecutorService);

    bootstrap.shutdown();

    Assert.assertThat(uninitList, Matchers.contains(initializer2, initializer1));
  }
}
