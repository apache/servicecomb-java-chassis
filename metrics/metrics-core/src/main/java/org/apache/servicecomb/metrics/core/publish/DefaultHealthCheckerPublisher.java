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

import java.util.Map;

import org.apache.servicecomb.metrics.common.HealthCheckResult;
import org.apache.servicecomb.metrics.common.HealthCheckerPublisher;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestSchema(schemaId = "healthEndpoint")
@RequestMapping(path = "/health")
public class DefaultHealthCheckerPublisher implements HealthCheckerPublisher {

  private final HealthCheckerManager manager;

  public DefaultHealthCheckerPublisher(HealthCheckerManager manager) {
    this.manager = manager;
  }

  @RequestMapping(path = "/", method = RequestMethod.GET)
  @CrossOrigin
  @Override
  public Map<String, HealthCheckResult> health() {
    return manager.check();
  }

  @ApiResponses({
      @ApiResponse(code = 400, response = String.class, message = "illegal request content"),
  })
  @RequestMapping(path = "/{name}", method = RequestMethod.GET)
  @CrossOrigin
  @Override
  public HealthCheckResult healthWithName(@PathVariable(name = "name") String name) {
    return manager.check(name);
  }
}
