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
package org.apache.servicecomb.metrics.core.meter.os;


import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.ManualClock;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Registry;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestOsMeter {
  Registry registry = new DefaultRegistry(new ManualClock());

  @Test
  public void testCalcMeasurement(@Mocked Runtime runtime) {
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
    OsMeter osMeter = new OsMeter(registry);
    list.clear();
    list.add("cpu  2 2 2 2 2 2 2 2 0 0");
    list.add("useless");
    list.add("eth0: 1 1    0    0    0     0          0          1         1 1    1      0     0     0    0    0");

    osMeter.calcMeasurements(1, 1);
    ArrayList<Measurement> measurements = Lists.newArrayList(osMeter.measure());
    Assert.assertEquals(3, measurements.size());
    Assert.assertEquals(1.75, measurements.get(0).value(), 0.0);
    Assert.assertEquals(1.0, measurements.get(1).value(), 0.0);
    Assert.assertEquals(1.0, measurements.get(2).value(), 0.0);
  }
}
