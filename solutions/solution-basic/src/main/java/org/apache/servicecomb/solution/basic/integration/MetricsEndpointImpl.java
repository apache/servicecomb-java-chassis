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

package org.apache.servicecomb.solution.basic.integration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.MetricsInitializer;
import org.apache.servicecomb.provider.rest.common.RestSchema;

import com.google.common.eventbus.EventBus;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.MeterRegistry;

@RestSchema(schemaId = MetricsEndpoint.NAME, schemaInterface = MetricsEndpoint.class)
public class MetricsEndpointImpl implements MetricsInitializer, MetricsEndpoint {
  private MeterRegistry meterRegistry;

  @Override
  public void init(MeterRegistry meterRegistry, EventBus eventBus, MetricsBootstrapConfig config) {
    this.meterRegistry = meterRegistry;
  }

  @Override
  public Map<String, Double> measure() {
    Map<String, Double> measurements = new LinkedHashMap<>();
    StringBuilder sb = new StringBuilder();

    for (Meter meter : this.meterRegistry.getMeters()) {
      meter.measure().forEach(measurement -> {
        String key = idToString(meter.getId(), sb);
        measurements.put(key, measurement.getValue());
      });
    }

    return measurements;
  }

  // format id to string:
  // idName(tag1=value1,tag2=value2)
  protected String idToString(Id id, StringBuilder sb) {
    sb.setLength(0);
    sb.append(id.getName()).append('(');
    sb.append(StreamSupport
        .stream(id
            .getTags()
            .spliterator(), false)
        .map(Object::toString)
        .collect(
            Collectors.joining(",")));
    sb.append(')');

    return sb.toString();
  }
}
