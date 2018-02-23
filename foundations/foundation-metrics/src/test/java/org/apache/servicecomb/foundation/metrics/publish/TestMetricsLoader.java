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

package org.apache.servicecomb.foundation.metrics.publish;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class TestMetricsLoader {
  @Test
  public void test() {
    Map<String, Double> metrics = new HashMap<>();
    metrics.put("X(K1=1,K2=2,K3=3)", 100.0);
    metrics.put("X(K1=1,K2=20,K3=30)", 200.0);
    metrics.put("X(K1=2,K2=200,K3=300)", 300.0);
    metrics.put("X(K1=2,K2=2000,K3=3000)", 400.0);

    metrics.put("Y(K1=1,K2=2,K3=3)", 500.0);
    metrics.put("Y(K1=10,K2=20,K3=30)", 600.0);
    metrics.put("Y(K1=100,K2=200,K3=300)", 700.0);
    metrics.put("Y(K1=1000,K2=2000,K3=3000)", 800.0);

    MetricsLoader loader = new MetricsLoader(metrics);

    Assert.assertEquals(200.0, loader.getFirstMatchMetricValue("X", "K3", "30"), 0);

    MetricNode node = loader.getMetricTree("X", "K1");

    Assert.assertEquals(2, node.getChildren().size());

    MetricNode node_k1 = node.getChildrenNode("1");

    Assert.assertEquals(200, node_k1.getFirstMatchMetricValue("K2", "20", "K3", "30"), 0);

    Assert.assertEquals(100, node_k1.getFirstMatchMetricValue("K2", "2"), 0);

    MetricNode newGroup = new MetricNode(node_k1.getMetrics(), "K2", "K3");

    Assert.assertEquals(1, newGroup.getChildrenNode("2").getChildrenNode("3").getMetrics().size(), 0);
  }
}
