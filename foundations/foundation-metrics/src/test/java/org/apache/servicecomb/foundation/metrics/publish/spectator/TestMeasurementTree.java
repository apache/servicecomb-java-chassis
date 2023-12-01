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
package org.apache.servicecomb.foundation.metrics.publish.spectator;

import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.foundation.metrics.publish.DefaultTagFinder;
import org.apache.servicecomb.foundation.metrics.publish.MeasurementGroupConfig;
import org.apache.servicecomb.foundation.metrics.publish.MeasurementNode;
import org.apache.servicecomb.foundation.metrics.publish.MeasurementTree;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class TestMeasurementTree {
  MeasurementTree tree = new MeasurementTree();

  MeterRegistry registry = new SimpleMeterRegistry();

  Timer timer;

  @Before
  public void setup() {
    timer = registry.timer("id",
        "g1",
        "g1v",
        "g2",
        "g2v",
        "t3",
        "t3v",
        "t4",
        "t4v");
    registry.counter("id_notCare");
  }

  @Test
  public void from() {
    timer.record(10, TimeUnit.MILLISECONDS);
    timer.record(2, TimeUnit.MILLISECONDS);

    MeasurementGroupConfig config = new MeasurementGroupConfig("id", "g1", "g2");
    tree.from(registry.getMeters().iterator(), config);

    Assertions.assertEquals(2, tree.getChildren().size());

    MeasurementNode node = tree.findChild("id", "g1v", "g2v");
    Assertions.assertEquals(2d,
        node.findChild(Statistic.COUNT.name()).getMeasurements().get(0).getValue(), 0);
    Assertions.assertEquals(12d,
        node.findChild(Statistic.TOTAL_TIME.name()).getMeasurements().get(0).getValue(), 0);
    Assertions.assertEquals(0d, tree.findChild("id_notCare").summary(), 0);
  }

  @Test
  public void from_withSkipOnNull() {
    try {
      MeasurementGroupConfig config = new MeasurementGroupConfig("id", new DefaultTagFinder("notExist", true));
      tree.from(registry.getMeters().iterator(), config);
    } catch (Exception e) {
      Assertions.fail("should not throw exception");
    }
  }

  @Test
  public void from_failed() {
    IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () -> {
      MeasurementGroupConfig config = new MeasurementGroupConfig("id", "notExist");
      tree.from(registry.getMeters().iterator(), config);
    });
    Assertions.assertEquals(
        "tag key \"notExist\" not exist in MeterId{name='id', tags=[tag(g1=g1v),tag(g2=g2v),tag(t3=t3v),tag(t4=t4v)]}",
        exception.getMessage());
  }
}
