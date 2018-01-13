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

import java.util.List;

import org.apache.servicecomb.metrics.common.MetricsPublisher;
import org.apache.servicecomb.metrics.common.RegistryMetric;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestSchema(schemaId = "metricsEndpoint")
@RequestMapping(path = "/metrics")
public class DefaultMetricsPublisher implements MetricsPublisher {

  private final DataSource dataSource;

  public DefaultMetricsPublisher(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @RequestMapping(path = "/appliedWindowTime", method = RequestMethod.GET)
  @CrossOrigin
  @Override
  public List<Long> getAppliedWindowTime() {
    return dataSource.getAppliedWindowTime();
  }

  @RequestMapping(path = "/", method = RequestMethod.GET)
  @CrossOrigin
  @Override
  public RegistryMetric metrics() {
    return dataSource.getRegistryMetric();
  }

  @ApiResponses({
      @ApiResponse(code = 400, response = String.class, message = "illegal request content"),
  })
  @RequestMapping(path = "/{windowTime}", method = RequestMethod.GET)
  @CrossOrigin
  @Override
  public RegistryMetric metricsWithWindowTime(@PathVariable(name = "windowTime") long windowTime) {
    return dataSource.getRegistryMetric(windowTime);
  }
}
