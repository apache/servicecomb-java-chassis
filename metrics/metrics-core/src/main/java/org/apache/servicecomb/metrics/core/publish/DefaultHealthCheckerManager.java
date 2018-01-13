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

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.servicecomb.metrics.common.HealthCheckResult;
import org.apache.servicecomb.metrics.common.HealthChecker;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultHealthCheckerManager implements HealthCheckerManager {
  private final Map<String, HealthChecker> healthCheckers;

  @Autowired(required = false)
  public DefaultHealthCheckerManager(List<HealthChecker> springHealthCheckers) {
    this.healthCheckers = new ConcurrentHashMap<>();
    if (springHealthCheckers != null && !springHealthCheckers.isEmpty()) {
      for (HealthChecker checker : springHealthCheckers) {
        this.healthCheckers.put(checker.getName(), checker);
      }
    }
  }

  @Override
  public void register(HealthChecker checker) {
    healthCheckers.put(checker.getName(), checker);
  }

  @Override
  public Map<String, HealthCheckResult> check() {
    return healthCheckers.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> e.getValue().check()));
  }

  @Override
  public HealthCheckResult check(String name) {
    HealthChecker checker = healthCheckers.get(name);
    if (checker != null) {
      return checker.check();
    }
    throw new InvocationException(BAD_REQUEST, "HealthChecker name : " + name + " unregister");
  }
}
