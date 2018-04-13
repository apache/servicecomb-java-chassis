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

import java.util.List;

import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.eventbus.EventBus;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.spectator.api.CompositeRegistry;
import com.netflix.spectator.api.ManualClock;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.SpectatorUtils;
import com.netflix.spectator.servo.ServoRegistry;

import mockit.Deencapsulation;

public class TestDefaultRegistryInitializer {
  CompositeRegistry globalRegistry = SpectatorUtils.createCompositeRegistry(new ManualClock());

  List<Registry> registries = Deencapsulation.getField(globalRegistry, "registries");

  DefaultRegistryInitializer registryInitializer = new DefaultRegistryInitializer();

  @Test
  public void init() {
    registryInitializer.init(globalRegistry, new EventBus(), new MetricsBootstrapConfig());

    Assert.assertEquals(-10, registryInitializer.getOrder());
    Assert.assertThat(registryInitializer.getRegistry(), Matchers.instanceOf(ServoRegistry.class));
    Assert.assertEquals(1, registries.size());
    Assert.assertEquals(1, DefaultMonitorRegistry.getInstance().getRegisteredMonitors().size());

    registryInitializer.uninit();

    Assert.assertEquals(0, registries.size());
    Assert.assertEquals(0, DefaultMonitorRegistry.getInstance().getRegisteredMonitors().size());
  }
}
