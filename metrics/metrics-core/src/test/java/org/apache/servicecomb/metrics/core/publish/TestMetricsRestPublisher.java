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
package org.apache.servicecomb.metrics.core.publish;

import java.util.Map;

import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.registry.GlobalRegistry;
import org.junit.Test;

import com.google.common.eventbus.EventBus;
import com.netflix.spectator.api.Clock;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.ManualClock;
import com.netflix.spectator.api.Registry;
import org.junit.jupiter.api.Assertions;

public class TestMetricsRestPublisher {
  MetricsRestPublisher publisher = new MetricsRestPublisher();

  @Test
  public void measure_globalRegistryNull() {
    Map<String, Double> result = publisher.measure();

    Assertions.assertEquals(0, result.size());
  }

  @Test
  public void measure_normal() {
    Clock clock = new ManualClock();
    GlobalRegistry globalRegistry = new GlobalRegistry();
    Registry registry = new DefaultRegistry(clock);
    registry.timer(registry.createId("name", "t1", "v1", "t2", "v2"));
    globalRegistry.add(registry);

    EventBus eventBus = new EventBus();

    publisher.init(globalRegistry, eventBus, new MetricsBootstrapConfig());
    Map<String, Double> result = publisher.measure();

    Assertions.assertEquals(2, result.size());
    Assertions.assertEquals(0, result.get("name(statistic=count,t1=v1,t2=v2)"), 0);
    Assertions.assertEquals(0, result.get("name(statistic=totalTime,t1=v1,t2=v2)"), 0);
  }
}
