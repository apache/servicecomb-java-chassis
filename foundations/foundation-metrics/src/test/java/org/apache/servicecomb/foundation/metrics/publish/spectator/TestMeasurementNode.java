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

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import com.netflix.spectator.api.Measurement;

import mockit.Expectations;
import mockit.Mocked;

public class TestMeasurementNode {
  MeasurementNode node = new MeasurementNode("name", null);

  @Test
  public void getName() {
    Assert.assertEquals("name", node.getName());
  }

  @Test
  public void getChildren() {
    Map<String, MeasurementNode> children = new HashMap<>();
    node = new MeasurementNode("name", children);

    Assert.assertSame(children, node.getChildren());
  }

  @Test
  public void findChild_noChildren() {
    Assert.assertNull(node.findChild("child"));
  }

  @Test
  public void findChild_multiLevel_noMiddleChildren(@Mocked Measurement measurement) {
    MeasurementNode c1 = node.addChild("c1", measurement);
    c1.addChild("c2", measurement);

    Assert.assertNull(node.findChild("c1_notExist", "c2"));
  }

  @Test
  public void findChild_multiLevel_ok(@Mocked Measurement measurement) {
    MeasurementNode c1 = node.addChild("c1", measurement);
    MeasurementNode c2 = c1.addChild("c2", measurement);

    Assert.assertSame(c2, node.findChild("c1", "c2"));
  }

  @Test
  public void addChild(@Mocked Measurement measurement) {
    MeasurementNode c1 = node.addChild("c1", measurement);
    MeasurementNode c2 = node.addChild("c2", measurement);

    Assert.assertSame(c1, node.findChild("c1"));
    Assert.assertSame(c2, node.findChild("c2"));
  }

  @Test
  public void getMeasurements(@Mocked Measurement measurement) {
    node.addMeasurement(measurement);

    Assert.assertThat(node.getMeasurements(), Matchers.contains(measurement));
  }

  @Test
  public void summary(@Mocked Measurement measurement) {
    new Expectations() {
      {
        measurement.value();
        result = 10;
      }
    };
    node.addMeasurement(measurement);
    node.addMeasurement(measurement);

    Assert.assertEquals(20, node.summary(), 0);
  }
}
