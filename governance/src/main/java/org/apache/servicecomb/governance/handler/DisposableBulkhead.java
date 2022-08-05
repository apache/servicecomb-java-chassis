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

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;

public class DisposableBulkhead extends Disposable<Bulkhead> {
  private final String key;

  private final BulkheadRegistry bulkheadRegistry;

  private final Bulkhead bulkhead;

  public DisposableBulkhead(String key, BulkheadRegistry bulkheadRegistry,
      Bulkhead bulkhead) {
    this.key = key;
    this.bulkheadRegistry = bulkheadRegistry;
    this.bulkhead = bulkhead;
  }

  @Override
  public void dispose() {
    bulkheadRegistry.remove(key);
  }

  @Override
  public Bulkhead getValue() {
    return bulkhead;
  }

  @Override
  public String getKey() {
    return key;
  }
}
