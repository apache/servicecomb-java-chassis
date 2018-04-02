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

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.ManualClock;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.Statistic;
import com.netflix.spectator.api.Timer;

public class TestMeasurementTree {
  MeasurementTree tree = new MeasurementTree();

  ManualClock clock = new ManualClock();

  Registry registry = new DefaultRegistry(clock);

  Timer timer;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

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
    timer.record(10, TimeUnit.NANOSECONDS);
    timer.record(2, TimeUnit.NANOSECONDS);

    MeasurementGroupConfig config = new MeasurementGroupConfig("id", "g1", "g2", Statistic.count.key());
    tree.from(registry.iterator(), config);

    Assert.assertEquals(2, tree.getChildren().size());

    MeasurementNode node = tree.findChild("id", "g1v", "g2v");
    Assert.assertEquals(2d, node.findChild(Statistic.count.value()).getMeasurements().get(0).value(), 0);
    Assert.assertEquals(12d, node.findChild(Statistic.totalTime.value()).getMeasurements().get(0).value(), 0);
    Assert.assertEquals(0d, tree.findChild("id_notCare").summary(), 0);
  }

  @Test
  public void from_failed() {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers
        .is("tag key \"notExist\" not exist in Measurement(id:g1=g1v:g2=g2v:statistic=count:t3=t3v:t4=t4v,0,0.0)"));

    MeasurementGroupConfig config = new MeasurementGroupConfig("id", "notExist");
    tree.from(registry.iterator(), config);
  }
}
