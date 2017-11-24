/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.foundation.metrics;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.servicecomb.foundation.metrics.output.MetricsFileOutput;
import io.servicecomb.foundation.metrics.output.servo.SeparatedMetricObserver;

public class TestSeparatedMetricObserver {
  @Test
  public void testMetricObserverUpdateImpl() {

    MetricsServoRegistry registry = mock(MetricsServoRegistry.class);
    MetricsFileOutput output = mock(MetricsFileOutput.class);

    Map<String, String> tpsAndLatency = new HashMap<>();
    tpsAndLatency.put("tps", "100");
    when(registry.calculateTPSAndLatencyMetrics()).thenReturn(tpsAndLatency);

    when(registry.calculateQueueMetrics()).thenReturn(new HashMap<>());
    when(registry.getSystemMetrics()).thenReturn(new HashMap<>());

    SeparatedMetricObserver observer = new SeparatedMetricObserver("test", output, registry);

    observer.updateImpl(new ArrayList<>());

    ArgumentCaptor<Map> metrics = ArgumentCaptor.forClass(Map.class);
    verify(output).output(metrics.capture());

    HashMap<String, String> outputMetrics = (HashMap<String, String>) metrics.getValue();

    Assert.assertTrue(outputMetrics.containsKey("tps"));
  }
}
