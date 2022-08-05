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

package org.apache.servicecomb.governance.handler;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

public class DisposableCircuitBreaker extends Disposable<CircuitBreaker> {
  private final CircuitBreaker circuitBreaker;

  private final CircuitBreakerRegistry circuitBreakerRegistry;

  private final String key;

  public DisposableCircuitBreaker(String key, CircuitBreakerRegistry registry, CircuitBreaker circuitBreaker) {
    this.key = key;
    this.circuitBreaker = circuitBreaker;
    this.circuitBreakerRegistry = registry;
  }

  @Override
  public void dispose() {
    this.circuitBreakerRegistry.remove(key);
  }

  @Override
  public CircuitBreaker getValue() {
    return circuitBreaker;
  }

  @Override
  public String getKey() {
    return key;
  }
}
