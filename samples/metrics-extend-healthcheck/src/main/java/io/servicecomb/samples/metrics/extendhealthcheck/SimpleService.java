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

package io.servicecomb.samples.metrics.extendhealthcheck;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.servicecomb.metrics.core.publish.HealthCheckerManager;
import io.servicecomb.provider.rest.common.RestSchema;

//simple service sim
@RestSchema(schemaId = "demoServiceEndpoint")
@RequestMapping(path = "/")
public class SimpleService {

  private final HealthCheckerManager manager;

  @Autowired
  public SimpleService(HealthCheckerManager manager) {
    this.manager = manager;

    //register your custom health check
    this.manager.register(new CustomHealthChecker());
  }

  @GetMapping(path = "/f")
  public String fun() {
    return UUID.randomUUID().toString();
  }
}