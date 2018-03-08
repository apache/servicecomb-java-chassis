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

package org.apache.servicecomb.metrics.prometheus;

import java.net.InetSocketAddress;

import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;

@Component
public class MetricsHttpPublisher implements ApplicationListener<ApplicationEvent> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MetricsHttpPublisher.class);

  private static final String METRICS_PROMETHEUS_ADDRESS = "servicecomb.metrics.prometheus.address";

  private HTTPServer httpServer;

  public MetricsHttpPublisher() {
    //prometheus default port allocation is here : https://github.com/prometheus/prometheus/wiki/Default-port-allocations
    this.init(
        DynamicPropertyFactory.getInstance().getStringProperty(METRICS_PROMETHEUS_ADDRESS, "0.0.0.0:9696").get());
  }

  public MetricsHttpPublisher(String address) {
    this.init(address);
  }

  private void init(String address) {
    try {
      InetSocketAddress socketAddress = getSocketAddress(address);
      MetricsCollector metricsCollector = new MetricsCollector();
      metricsCollector.register();
      this.httpServer = new HTTPServer(socketAddress, CollectorRegistry.defaultRegistry, true);
      LOGGER.info("Prometheus httpServer listened : {}.", address);
    } catch (Exception e) {
      throw new ServiceCombException("create http publish server failed,may bad address : " + address, e);
    }
  }

  private InetSocketAddress getSocketAddress(String address) {
    String[] hostAndPort = address.split(":");
    if (hostAndPort.length == 2) {
      return new InetSocketAddress(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
    }
    throw new ServiceCombException("create http publish server failed,bad address : " + address);
  }

  @Override
  public void onApplicationEvent(ApplicationEvent event) {
    if (!ContextClosedEvent.class.isInstance(event) || httpServer == null) {
      return;
    }

    httpServer.stop();
    httpServer = null;
    LOGGER.info("Prometheus httpServer stopped.");
  }
}
