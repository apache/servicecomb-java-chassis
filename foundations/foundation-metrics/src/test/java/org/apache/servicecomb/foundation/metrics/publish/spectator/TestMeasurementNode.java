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

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Meter.Type;
import io.micrometer.core.instrument.Tags;
import mockit.Expectations;
import mockit.Mocked;

public class TestMeasurementNode {
  Id id = new Id("name", Tags.empty(), null, null, Type.OTHER);

  MeasurementNode node = new MeasurementNode("name", id, null);

  @Test
  public void getName() {
    Assertions.assertEquals("name", node.getName());
  }

  @Test
  public void getChildren() {
    Map<String, MeasurementNode> children = new HashMap<>();
    node = new MeasurementNode("name", id, children);

    Assertions.assertSame(children, node.getChildren());
  }

  @Test
  public void findChild_noChildren() {
    Assertions.assertNull(node.findChild("child"));
  }

  @Test
  public void findChild_multiLevel_noMiddleChildren(@Mocked Measurement measurement) {
    MeasurementNode c1 = node.addChild("c1", id, measurement);
    c1.addChild("c2", id, measurement);

    Assertions.assertNull(node.findChild("c1_notExist", "c2"));
  }

  @Test
  public void findChild_multiLevel_ok(@Mocked Measurement measurement) {
    MeasurementNode c1 = node.addChild("c1", id, measurement);
    MeasurementNode c2 = c1.addChild("c2", id, measurement);

    Assertions.assertSame(c2, node.findChild("c1", "c2"));
  }

  @Test
  public void addChild(@Mocked Measurement measurement) {
    MeasurementNode c1 = node.addChild("c1", id, measurement);
    MeasurementNode c2 = node.addChild("c2", id, measurement);

    Assertions.assertSame(c1, node.findChild("c1"));
    Assertions.assertSame(c2, node.findChild("c2"));
  }

  @Test
  public void getMeasurements(@Mocked Measurement measurement) {
    node.addMeasurement(measurement);

    MatcherAssert.assertThat(node.getMeasurements(), Matchers.contains(measurement));
  }

  @Test
  public void summary(@Mocked Measurement measurement) {
    new Expectations() {
      {
        measurement.getValue();
        result = 10;
      }
    };
    node.addMeasurement(measurement);
    node.addMeasurement(measurement);

    Assertions.assertEquals(20, node.summary(), 0);
  }
}
