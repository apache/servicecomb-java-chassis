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
package org.apache.servicecomb.metrics.core.meter.vertx;

import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.foundation.vertx.metrics.metric.DefaultEndpointMetric;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

public class EndpointMeter {
  private static final double SNV_MILLI_SECONDS = 1.0 / TimeUnit.MILLISECONDS.toNanos(1L);

  public static final String ADDRESS = "address";

  public static final String STATISTIC = "statistic";

  public static final String CONNECT_COUNT = "connectCount";

  public static final String DISCONNECT_COUNT = "disconnectCount";

  public static final String CONNECTIONS = "connections";

  public static final String BYTES_READ = "bytesRead";

  public static final String BYTES_WRITTEN = "bytesWritten";

  public static final String REQUESTS = "requests";

  public static final String LATENCY = "latency";

  protected DefaultEndpointMetric metric;

  private final MeterRegistry meterRegistry;

  private final Gauge connectCount;

  private final Gauge disconnectCount;

  private final Gauge connections;

  private final Gauge bytesRead;

  private final Gauge bytesWritten;

  private final Gauge requests;

  private final Gauge latency;

  private long lastConnectCount;

  private long lastDisconnectCount;

  private long lastBytesRead;

  private long lastBytesWritten;

  private long lastRequests;

  private long lastRequestsForLatency;

  private long lastLatency;

  public EndpointMeter(MeterRegistry meterRegistry, String name, Tags tags, DefaultEndpointMetric metric) {
    this.meterRegistry = meterRegistry;

    tags = tags.and(Tag.of(ADDRESS, metric.getAddress()));
    connectCount = Gauge.builder(name, () -> {
          long current = metric.getConnectCount();
          long result = current - lastConnectCount;
          lastConnectCount = current;
          return result;
        })
        .tags(tags.and(Tag.of(STATISTIC, CONNECT_COUNT)))
        .register(meterRegistry);
    disconnectCount = Gauge.builder(name, () -> {
          long current = metric.getDisconnectCount();
          long result = current - lastDisconnectCount;
          lastDisconnectCount = current;
          return result;
        }).tags(tags.and(Tag.of(STATISTIC, DISCONNECT_COUNT)))
        .register(meterRegistry);
    connections = Gauge.builder(name, () -> metric.getConnectCount() - metric.getDisconnectCount())
        .tags(tags.and(Tag.of(STATISTIC, CONNECTIONS)))
        .register(meterRegistry);
    bytesRead = Gauge.builder(name, () -> {
          long current = metric.getBytesRead();
          long result = current - lastBytesRead;
          lastBytesRead = current;
          return result;
        }).tags(tags.and(Tag.of(STATISTIC, BYTES_READ)))
        .register(meterRegistry);
    bytesWritten = Gauge.builder(name, () -> {
          long current = metric.getBytesWritten();
          long result = current - lastBytesWritten;
          lastBytesWritten = current;
          return result;
        }).tags(tags.and(Tag.of(STATISTIC, BYTES_WRITTEN)))
        .register(meterRegistry);
    requests = Gauge.builder(name, () -> {
          long current = metric.getRequests();
          long result = current - lastRequests;
          lastRequests = current;
          return result;
        }).tags(tags.and(Tag.of(STATISTIC, REQUESTS)))
        .register(meterRegistry);
    latency = Gauge.builder(name, () -> {
          long currentLatency = metric.getLatency();
          long currentRequests = metric.getRequests();
          double result = currentRequests - lastRequestsForLatency == 0 ? 0 :
              (currentLatency - lastLatency) / ((double) (currentRequests - lastRequestsForLatency)) * SNV_MILLI_SECONDS;
          lastRequestsForLatency = currentRequests;
          lastLatency = currentLatency;
          return result;
        }).tags(tags.and(Tag.of(STATISTIC, LATENCY)))
        .register(meterRegistry);
  }

  public DefaultEndpointMetric getMetric() {
    return metric;
  }

  public void destroy() {
    this.meterRegistry.remove(connectCount);
    this.meterRegistry.remove(disconnectCount);
    this.meterRegistry.remove(connections);
    this.meterRegistry.remove(bytesRead);
    this.meterRegistry.remove(bytesWritten);
    this.meterRegistry.remove(requests);
    this.meterRegistry.remove(latency);
  }
}
