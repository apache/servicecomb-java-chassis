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

package org.apache.servicecomb.metrics.core.publish;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.MetricsInitializer;
import org.apache.servicecomb.foundation.metrics.registry.GlobalRegistry;

import com.google.common.eventbus.EventBus;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Meter;
import com.netflix.spectator.api.Registry;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/metrics")
public class MetricsRestPublisher implements MetricsInitializer {
  private GlobalRegistry globalRegistry;

  @Override
  public void init(GlobalRegistry globalRegistry, EventBus eventBus, MetricsBootstrapConfig config) {
    this.globalRegistry = globalRegistry;
  }

  @ApiResponses({
      @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(type = "string")),
          description = "illegal request content"),
  })
  @GET
  @Path("/")
  public Map<String, Double> measure() {
    Map<String, Double> measurements = new LinkedHashMap<>();
    if (globalRegistry == null) {
      return measurements;
    }

    StringBuilder sb = new StringBuilder();
    for (Registry registry : globalRegistry.getRegistries()) {
      for (Meter meter : registry) {
        meter.measure().forEach(measurement -> {
          String key = idToString(measurement.id(), sb);
          measurements.put(key, measurement.value());
        });
      }
    }

    return measurements;
  }

  // format id to string:
  // idName(tag1=value1,tag2=value2)
  protected String idToString(Id id, StringBuilder sb) {
    sb.setLength(0);
    sb.append(id.name()).append('(');
    sb.append(StreamSupport
        .stream(id
            .tags()
            .spliterator(), false)
        .map(Object::toString)
        .collect(
            Collectors.joining(",")));
    sb.append(')');

    return sb.toString();
  }
}
