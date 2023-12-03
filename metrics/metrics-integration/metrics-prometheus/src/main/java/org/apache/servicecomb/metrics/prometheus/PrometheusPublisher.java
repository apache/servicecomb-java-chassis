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
import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.MetricsInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.google.common.eventbus.EventBus;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;

public class PrometheusPublisher extends Collector implements Collector.Describable, MetricsInitializer {
  private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusPublisher.class);

  public static final String METRICS_PROMETHEUS_ADDRESS = "servicecomb.metrics.prometheus.address";

  private HTTPServer httpServer;

  private MeterRegistry meterRegistry;

  private Environment environment;

  @Autowired
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Override
  public void init(MeterRegistry meterRegistry, EventBus eventBus, MetricsBootstrapConfig config) {
    this.meterRegistry = meterRegistry;

    //prometheus default port allocation is here : https://github.com/prometheus/prometheus/wiki/Default-port-allocations
    String address = environment.getProperty(METRICS_PROMETHEUS_ADDRESS, String.class, "0.0.0.0:9696");

    try {
      InetSocketAddress socketAddress = getSocketAddress(address);
      register();
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
  public List<MetricFamilySamples> describe() {
    List<MetricFamilySamples> familySamples = new ArrayList<>();

    List<Sample> samples = new ArrayList<>();

    for (Meter meter : this.meterRegistry.getMeters()) {
      meter.measure().forEach(measurement -> {
        Sample sample = convertMeasurementToSample(meter, measurement);
        samples.add(sample);
      });
    }

    familySamples.add(new MetricFamilySamples("ServiceComb_Metrics", Type.UNKNOWN, "ServiceComb Metrics", samples));

    return familySamples;
  }

  protected Sample convertMeasurementToSample(Meter meter, Measurement measurement) {
    String prometheusName = meter.getId().getName().replace(".", "_");
    List<String> labelNames = new ArrayList<>();
    List<String> labelValues = new ArrayList<>();

    labelNames.add("appId");
    labelValues.add(BootStrapProperties.readApplication(environment));

    for (Tag tag : meter.getId().getTags()) {
      labelNames.add(tag.getKey());
      labelValues.add(tag.getValue());
    }

    return new Sample(prometheusName, labelNames, labelValues, measurement.getValue());
  }

  @Override
  public List<MetricFamilySamples> collect() {
    return describe();
  }

  @Override
  public void destroy() {
    if (httpServer == null) {
      return;
    }

    httpServer.close();
    httpServer = null;
    LOGGER.info("Prometheus httpServer stopped.");
  }
}
