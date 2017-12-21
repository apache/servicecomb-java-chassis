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

package io.servicecomb.metrics.core;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.metrics.core.monitor.RegistryMonitor;
import io.servicecomb.metrics.core.publish.DefaultDataSource;
import io.servicecomb.metrics.core.publish.JsonMetricsPublisher;

public class TestPublisher {

  @Test
  public void test() {
    RegistryMonitor registryMonitor = new RegistryMonitor();
    DefaultDataSource dataSource = new DefaultDataSource(registryMonitor);
    JsonMetricsPublisher publisher = new JsonMetricsPublisher(dataSource);
    String content = publisher.metrics(0);
    Assert
        .assertEquals(content,
            "{\"instanceMetric\":{\"waitInQueue\":0,\"lifeTimeInQueue\":{\"total\":0,\"count\":0,\"average\":0.0,"+
                "\"min\":0,\"max\":0},\"executionTime\":{\"total\":0,\"count\":0,\"average\":0.0,\"min\":0,\"max\":0},"+
                "\"consumerLatency\":{\"total\":0,\"count\":0,\"average\":0.0,\"min\":0,\"max\":0},\"producerLatency"+
                "\":{\"total\":0,\"count\":0,\"average\":0.0,\"min\":0,\"max\":0}},\"invocationMetrics\":{}}");
  }
}
