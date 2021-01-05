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

package org.apache.servicecomb.demo.zeroconfig.server;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestSchema(schemaId = "GovernanceEndpoint")
@RequestMapping("/governance")
public class GovernanceEndpoint {
  private static final Logger LOGGER = LoggerFactory.getLogger(GovernanceEndpoint.class);

  private Map<String, Integer> retryTimes = new HashMap<>();

  @GetMapping("/hello")
  public String sayHello() {
    return "Hello world!";
  }

  @GetMapping("/retry")
  @ApiResponses({
      @ApiResponse(code = 200, response = String.class, message = ""),
      @ApiResponse(code = 502, response = String.class, message = "")})
  public String retry(@RequestParam(name = "invocationID") String invocationID) {
    LOGGER.info("invoke service: {}", invocationID);
    retryTimes.putIfAbsent(invocationID, 0);
    retryTimes.put(invocationID, retryTimes.get(invocationID) + 1);

    int retry = retryTimes.get(invocationID);

    if (retry == 3) {
      return "try times: " + retry;
    }
    throw new InvocationException(502, "retry", "retry");
  }

  @GetMapping("/circuitBreaker")
  public String circuitBreaker() {
    throw new RuntimeException("circuitBreaker by provider.");
  }
}
