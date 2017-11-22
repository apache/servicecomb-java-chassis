/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.provider.springmvc.metrics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.servo.Metric;

import io.servicecomb.foundation.metrics.MetricsServoRegistry;
import io.servicecomb.provider.rest.common.RestSchema;

@RestSchema(schemaId = "metricsService")
@RequestMapping(path = "/metrics", produces = MediaType.APPLICATION_JSON)
public class MetricsController {

  private static final Logger log = LoggerFactory.getLogger(MetricsController.class);

  private final ObjectMapper mapper = new ObjectMapper();

  private MetricsServoRegistry registry = null;

  @Autowired
  public MetricsController(MetricsServoRegistry registry) {
    this.registry = registry;
  }

  @RequestMapping(path = "/", method = RequestMethod.GET)
  public String metrics() {
    List<List<Metric>> metrics = registry.getMemoryObserver().getObservations();
    String response = "{}";
    if (!metrics.isEmpty()) {
      Map<String, String> values = new HashMap<>();
      for (Metric metric : metrics.get(0)) {
        String key = metric.getConfig().getName() +
            (metric.getConfig().getTags().containsKey("statistic") ? "." + metric.getConfig().getTags()
                .getValue("statistic") : "");
        values.put(key, metric.getValue().toString());
      }
      try {
        response = mapper.writeValueAsString(values);
      } catch (JsonProcessingException e) {
        log.error("failed parse metrics info");
      }
    }
    return response;
  }
}


