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

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.metrics.MetricsInitializer;
import org.apache.servicecomb.metrics.core.meter.os.CpuMeter;
import org.apache.servicecomb.metrics.core.meter.os.NetMeter;
import org.apache.servicecomb.metrics.core.meter.os.NetMeter.InterfaceInfo;
import org.apache.servicecomb.metrics.core.meter.os.OsMeter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.eventbus.EventBus;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.ManualClock;
import com.netflix.spectator.api.Registry;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestOsMeterInitializer {
  private Registry registry = new DefaultRegistry(new ManualClock());

  @Mocked
  private DefaultRegistryInitializer defaultRegistryInitializer;

  @Mocked
  private EventBus eventBus;

  private boolean isLinux;

  @Before
  public void beforeTest() {
    isLinux = SystemUtils.IS_OS_LINUX;
  }

  @Test
  public void init(@Mocked Runtime runtime) {
    ReflectUtils.setField(SystemUtils.class, null, "IS_OS_LINUX", true);
    new Expectations(SPIServiceUtils.class) {
      {
        SPIServiceUtils.getTargetService(MetricsInitializer.class, DefaultRegistryInitializer.class);
        result = defaultRegistryInitializer;
        defaultRegistryInitializer.getRegistry();
        result = registry;
      }
    };
    List<String> list = new ArrayList<>();
    list.add("cpu  1 1 1 1 1 1 1 1 0 0");
    list.add("useless");
    list.add("eth0: 0 0    0    0    0     0          0          0         0 0    0      0     0     0    0    0");
    new MockUp<FileUtils>() {
      @Mock
      public List<String> readLines(File file, Charset encoding) {
        return list;
      }
    };
    new MockUp<Runtime>() {
      @Mock
      public Runtime getRuntime() {
        return runtime;
      }
    };
    new Expectations() {
      {
        runtime.availableProcessors();
        result = 2;
      }
    };
    OsMetersInitializer osMetersInitializer = new OsMetersInitializer();
    osMetersInitializer.init(null, eventBus, null);
    OsMeter osMeter = osMetersInitializer.getOsMeter();
    Assert.assertNotNull(osMeter);
    Assert.assertNotNull(osMeter.getCpuMeter());
    Assert.assertNotNull(osMeter.getNetMeter());
    CpuMeter cpuMeter = osMeter.getCpuMeter();
    NetMeter netMeter = osMeter.getNetMeter();
    Assert.assertEquals(2, cpuMeter.getCpuNum());
    Assert.assertEquals(1L, cpuMeter.getLastIdleTime());
    Assert.assertEquals(8L, cpuMeter.getLastTotalTime());
    Map<String, InterfaceInfo> interfaceInfoMap = netMeter.getInterfaceInfoMap();
    Assert.assertEquals(1, interfaceInfoMap.size());
    InterfaceInfo eth0 = interfaceInfoMap.get("eth0");
    Assert.assertEquals(0L, eth0.getLastRxBytes());
    Assert.assertEquals(0L, eth0.getLastTxBytes());
    Assert.assertEquals(0.0, eth0.getSendRate(), 0.0);
    Assert.assertEquals(0.0, eth0.getReceiveRate(), 0.0);
  }

  @Test
  public void initFail() {
    OsMetersInitializer osMetersInitializer = new OsMetersInitializer();
    ReflectUtils.setField(SystemUtils.class, null, "IS_OS_LINUX", false);
    osMetersInitializer.init(null, eventBus, null);
    Assert.assertNull(osMetersInitializer.getOsMeter());
  }

  @After
  public void afterTest() {
    ReflectUtils.setField(SystemUtils.class, null, "IS_OS_LINUX", isLinux);
  }
}
