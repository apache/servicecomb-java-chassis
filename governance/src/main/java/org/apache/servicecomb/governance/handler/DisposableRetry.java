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

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;

public class DisposableRetry extends Disposable<Retry> {
  private final String key;

  private final RetryRegistry registry;

  private final Retry retry;

  public DisposableRetry(String key, RetryRegistry registry, Retry retry) {
    this.key = key;
    this.registry = registry;
    this.retry = retry;
  }

  @Override
  public void dispose() {
    registry.remove(key);
  }

  @Override
  public Retry getValue() {
    return retry;
  }

  @Override
  public String getKey() {
    return key;
  }
}
