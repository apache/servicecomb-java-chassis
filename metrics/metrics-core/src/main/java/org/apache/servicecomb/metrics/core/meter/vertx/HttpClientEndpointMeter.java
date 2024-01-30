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

import org.apache.servicecomb.foundation.vertx.metrics.metric.DefaultClientEndpointMetric;
import org.apache.servicecomb.foundation.vertx.metrics.metric.DefaultEndpointMetric;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

public class HttpClientEndpointMeter extends EndpointMeter {
  public static final String QUEUE_COUNT = "queueCount";

  private final Gauge queueCount;

  private long currentQueueCount;

  public HttpClientEndpointMeter(MeterRegistry meterRegistry, String name, Tags tags, DefaultEndpointMetric metric) {
    super(meterRegistry, name, tags, metric);
    queueCount = Gauge.builder(name, () -> currentQueueCount)
        .tags(tags.and(Tag.of(STATISTIC, QUEUE_COUNT), Tag.of(ADDRESS, metric.getAddress())))
        .register(meterRegistry);
  }

  @Override
  public void destroy() {
    super.destroy();
    this.meterRegistry.remove(queueCount);
  }

  @Override
  public void poll(long msNow, long secondInterval) {
    super.poll(msNow, secondInterval);

    currentQueueCount = ((DefaultClientEndpointMetric) metric).getQueueCount();
  }
}
