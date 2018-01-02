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

package io.servicecomb.metrics.prometheus;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import io.servicecomb.foundation.common.exceptions.ServiceCombException;

@Component
public class MetricsPublisher {
  private static final String METRICS_PROMETHEUS_PORT = "servicecomb.metrics.prometheus.port";

  private final MetricsCollector metricsCollector;

  private final HTTPServer httpServer;

  @Autowired
  public MetricsPublisher(MetricsCollector metricsCollector) {
    //prometheus default port allocation is here : https://github.com/prometheus/prometheus/wiki/Default-port-allocations
    int publishPort = DynamicPropertyFactory.getInstance().getIntProperty(METRICS_PROMETHEUS_PORT, 9696).get();
    this.metricsCollector = metricsCollector;
    this.metricsCollector.register();
    try {
      this.httpServer = new HTTPServer(new InetSocketAddress(publishPort), CollectorRegistry.defaultRegistry, true);
    } catch (IOException e) {
      throw new ServiceCombException("create http publish server failed", e);
    }
  }
}
