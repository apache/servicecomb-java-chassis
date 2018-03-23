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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.netflix.spectator.api.Measurement;

public class MeasurementNode {
  private String name;

  private List<Measurement> measurements = new ArrayList<>();

  private Map<String, MeasurementNode> children;

  public MeasurementNode(String name, Map<String, MeasurementNode> children) {
    this.name = name;
    this.children = children;
  }

  public String getName() {
    return name;
  }

  public Map<String, MeasurementNode> getChildren() {
    return children;
  }

  public MeasurementNode findChild(String childName) {
    if (children == null) {
      return null;
    }
    return children.get(childName);
  }

  public MeasurementNode findChild(String... childNames) {
    MeasurementNode node = this;
    for (String childName : childNames) {
      if (node == null) {
        return null;
      }

      node = node.findChild(childName);
    }
    return node;
  }

  public MeasurementNode addChild(String childName, Measurement measurement) {
    if (children == null) {
      children = new HashMap<>();
    }

    MeasurementNode node = children.computeIfAbsent(childName, name -> {
      return new MeasurementNode(name, null);
    });
    node.addMeasurement(measurement);

    return node;
  }

  public List<Measurement> getMeasurements() {
    return measurements;
  }

  public void addMeasurement(Measurement measurement) {
    measurements.add(measurement);
  }

  public double summary() {
    double result = 0;
    for (Measurement measurement : measurements) {
      result += measurement.value();
    }

    return result;
  }
}
