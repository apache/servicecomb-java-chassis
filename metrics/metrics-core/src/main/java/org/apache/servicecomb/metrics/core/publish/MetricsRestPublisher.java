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

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.MetricsInitializer;

import com.google.common.eventbus.EventBus;
import com.netflix.spectator.api.CompositeRegistry;
import com.netflix.spectator.api.Id;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/metrics")
public class MetricsRestPublisher implements MetricsInitializer {
  private CompositeRegistry globalRegistry;

  @Override
  public void init(CompositeRegistry globalRegistry, EventBus eventBus, MetricsBootstrapConfig config) {
    this.globalRegistry = globalRegistry;
  }

  @ApiResponses({
      @ApiResponse(code = 400, response = String.class, message = "illegal request content"),
  })
  @GET
  @Path("/")
  public Map<String, Double> measure() {
    Map<String, Double> measurements = new LinkedHashMap<>();
    if (globalRegistry == null) {
      return measurements;
    }

    StringBuilder sb = new StringBuilder();
    globalRegistry
        .iterator()
        .forEachRemaining(meter -> {
          meter.measure().forEach(measurement -> {
            String key = idToString(measurement.id(), sb);
            measurements.put(key, measurement.value());
          });
        });

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
