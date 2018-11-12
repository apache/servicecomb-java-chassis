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

import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementNode;
import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementTree;

public abstract class AbstractMeasurementNodeLogPublisher {
  protected StringBuilder sb;

  protected MeasurementNode measurementNode;

  private boolean exists;

  public AbstractMeasurementNodeLogPublisher(MeasurementTree tree, StringBuilder sb, String... childNames) {
    this.sb = sb;
    measurementNode = tree.findChild(childNames);
    exists = measurementNode != null && !measurementNode.getMeasurements().isEmpty();
  }

  public boolean isExists() {
    return exists;
  }

  public abstract void print(boolean printDetail);
}
