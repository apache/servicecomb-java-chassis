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

package org.apache.servicecomb.demo.zeroconfig.client;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@RestSchema(schemaId = "GovernanceEndpoint")
@RequestMapping("/governance")
public class GovernanceEndpoint {
  private static final String SERVER = "servicecomb://demo-zeroconfig-schemadiscovery-registry-server";

  private RestTemplate restTemplate = RestTemplateBuilder.create();

  private int count = 0;

  @GetMapping("/hello")
  public String hello() {
    return restTemplate.getForObject(SERVER + "/governance/hello", String.class);
  }

  @GetMapping("/retry")
  public String retry(@RequestParam(name = "invocationID") String invocationID) {
    return restTemplate
        .getForObject(SERVER + "/governance/retry?invocationID={1}", String.class,
            invocationID);
  }

  @GetMapping("/circuitBreaker")
  public String circuitBreaker() throws Exception {
    count++;
    if (count % 3 == 0) {
      return "ok";
    }
    throw new RuntimeException("test error");
  }

  @GetMapping("/bulkhead")
  public String bulkhead() {
    return restTemplate.getForObject(SERVER + "/governance/hello", String.class);
  }
}
