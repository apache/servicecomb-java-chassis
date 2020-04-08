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

import java.io.IOException;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.netflix.config.DynamicBooleanProperty;
import io.prometheus.client.exporter.common.TextFormat;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.MetricsInitializer;
import org.apache.servicecomb.foundation.metrics.registry.GlobalRegistry;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Meter;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.Tag;

import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/prometheus")
public class PrometheusPublisher extends Collector implements Collector.Describable, MetricsInitializer {
  private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusPublisher.class);

  static final DynamicBooleanProperty METRICS_PROMETHEUS_REST = DynamicPropertyFactory.getInstance()
          .getBooleanProperty("servicecomb.metrics.prometheus.rest", false);

  static final String METRICS_PROMETHEUS_ADDRESS = "servicecomb.metrics.prometheus.address";

  private HTTPServer httpServer;

  private GlobalRegistry globalRegistry;

  @Override
  public void init(GlobalRegistry globalRegistry, EventBus eventBus, MetricsBootstrapConfig config) {
    this.globalRegistry = globalRegistry;
    if (METRICS_PROMETHEUS_REST.get()) {
        register();
        LOGGER.info("Prometheus restful enabled.");
        return;
    }

    //prometheus default port allocation is here : https://github.com/prometheus/prometheus/wiki/Default-port-allocations
    String address =
        DynamicPropertyFactory.getInstance().getStringProperty(METRICS_PROMETHEUS_ADDRESS, "0.0.0.0:9696").get();

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
    if (globalRegistry == null) {
      return familySamples;
    }

    List<Sample> samples = new ArrayList<>();
    for (Registry registry : globalRegistry.getRegistries()) {
      for (Meter meter : registry) {
        meter.measure().forEach(measurement -> {
          Sample sample = convertMeasurementToSample(measurement);
          samples.add(sample);
        });
      }
    }

    familySamples.add(new MetricFamilySamples("ServiceComb_Metrics", Type.UNTYPED, "ServiceComb Metrics", samples));

    return familySamples;
  }

  protected Sample convertMeasurementToSample(Measurement measurement) {
    String prometheusName = measurement.id().name().replace(".", "_");
    List<String> labelNames = new ArrayList<>();
    List<String> labelValues = new ArrayList<>();

    labelNames.add("appId");
    labelValues.add(RegistryUtils.getAppId());

    for (Tag tag : measurement.id().tags()) {
      labelNames.add(tag.key());
      labelValues.add(tag.value());
    }

    return new Sample(prometheusName, labelNames, labelValues, measurement.value());
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

    httpServer.stop();
    httpServer = null;
    LOGGER.info("Prometheus httpServer stopped.");
  }

  @ApiResponses({
          @ApiResponse(code = 400, response = String.class, message = "illegal request content"),
  })
  @GET
  @Path("/")
  @Produces(MediaType.TEXT_PLAIN)
  public String prometheus(HttpServletRequest request) throws IOException {
    String query = request != null ? request.getQueryString() : null;
    Set<String> names = HTTPServerEx.parseQuery(query);
    StringWriter sw = new StringWriter(40960);
    TextFormat.write004(sw, CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(names));
    return sw.toString();
  }

  static class HTTPServerEx extends HTTPServer {
    public HTTPServerEx(int port) throws IOException {
      super(port);
    }
    public static Set<String> parseQuery(String query) throws IOException {
      return HTTPServer.parseQuery(query);
    }
  }
}
