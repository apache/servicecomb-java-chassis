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
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMetricNode {
  private static MetricsLoader loader;

  @BeforeClass
  public static void steup() {
    Map<String, Double> metrics = new HashMap<>();
    metrics.put("X(K1=1,K2=2,K3=3,unit=SECONDS,statistic=A)", 100.0);
    metrics.put("X(K1=1,K2=2,K3=30000,unit=SECONDS,statistic=AA)", 110.0);
    metrics.put("X(K1=1,K2=20,K3=30,unit=SECONDS,statistic=B)", 200.0);
    metrics.put("X(K1=2,K2=200,K3=300,unit=SECONDS,statistic=C)", 300.0);
    metrics.put("X(K1=2,K2=2000,K3=3000,unit=SECONDS,statistic=D)", 400.0);

    metrics.put("Y(K1=1,K2=2,K3=3)", 500.0);
    metrics.put("Y(K1=10,K2=20,K3=30)", 600.0);
    metrics.put("Y(K1=100,K2=200,K3=300)", 700.0);
    metrics.put("Y(K1=1000,K2=2000,K3=3000)", 800.0);

    loader = new MetricsLoader(metrics);
  }

  @Test
  public void checkNodeMetricCount() {
    MetricNode node = loader.getMetricTree("X", "K1");
    MetricNode node_k1 = node.getChildrenNode("1");
    Assert.assertEquals(3, node_k1.getMetricCount());
  }

  @Test
  public void checkGetFirstMatchMetricValueWithSingleTag() {
    MetricNode node = loader.getMetricTree("X", "K1");
    MetricNode node_k1 = node.getChildrenNode("1");
    Assert.assertEquals(100, node_k1.getFirstMatchMetricValue("K2", "2"), 0);
    Assert.assertEquals(100 * 1000, node_k1.getFirstMatchMetricValue(TimeUnit.MILLISECONDS, "K2", "2"), 0);
    Assert.assertEquals(100 * 1000, node_k1.getFirstMatchMetricValue(TimeUnit.MILLISECONDS, "K2", "2"), 0);
  }

  @Test
  public void checkGetFirstMatchMetricValueWithMultiTag() {
    MetricNode node = loader.getMetricTree("X", "K1");
    MetricNode node_k1 = node.getChildrenNode("1");
    Assert.assertEquals(200, node_k1.getFirstMatchMetricValue("K3", "30", "K2", "20"), 0);
    Assert.assertEquals(200 * 1000, node_k1.getFirstMatchMetricValue(TimeUnit.MILLISECONDS, "K3", "30", "K2", "20"), 0);
    Assert.assertEquals(110.0, node_k1.getFirstMatchMetricValue("K2", "2", "K3", "30000"), 0);
    Assert
        .assertEquals(110 * 1000, node_k1.getFirstMatchMetricValue(TimeUnit.MILLISECONDS, "K2", "2", "K3", "30000"), 0);
  }

  @Test
  public void checkGetMatchStatisticMetricValue() {
    MetricNode node = loader.getMetricTree("X", "K1");
    MetricNode node_k1 = node.getChildrenNode("1");
    Assert.assertEquals(100, node_k1.getMatchStatisticMetricValue("A"), 0);
    Assert.assertEquals(100 * 1000, node_k1.getMatchStatisticMetricValue(TimeUnit.MILLISECONDS, "A"), 0);
  }

  @Test
  public void checkGenerateMetricNodeFromExistedNode() {
    MetricNode node = loader.getMetricTree("X", "K1");
    MetricNode node_k1 = node.getChildrenNode("1");
    MetricNode newNode = new MetricNode(node_k1.getMetrics(), "K2", "K3");
    Assert.assertEquals(1, newNode.getChildrenNode("2").getChildrenNode("3").getMetricCount(), 0);
  }
}