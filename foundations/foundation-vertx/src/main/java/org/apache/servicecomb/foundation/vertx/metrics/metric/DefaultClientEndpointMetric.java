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
package org.apache.servicecomb.foundation.vertx.metrics.metric;

import java.util.concurrent.atomic.LongAdder;

/**
 * for one listen address, include multiple httpClient or httpServer
 */
public class DefaultClientEndpointMetric extends DefaultEndpointMetric {
  private LongAdder queue = new LongAdder();

  // control if the metric instance will be expired
  // all invoker about incRefCount/isExpired, must lock: DefaultClientEndpointMetricManager
  // decRefCount no need to lock, because that only cause to be expired later.
  private long lastNanoTime = System.nanoTime();

  public DefaultClientEndpointMetric(String address) {
    super(address);
  }

  public long getLastNanoTime() {
    return lastNanoTime;
  }

  @Override
  public void onDisconnect() {
    super.onDisconnect();
    lastNanoTime = System.nanoTime();
  }

  public long getQueueCount() {
    return queue.longValue();
  }

  public void enqueueRequest() {
    queue.increment();
  }

  public void dequeueRequest() {
    queue.decrement();
  }

  public boolean isExpired(long nsTimeout) {
    return getCurrentConnectionCount() == 0
        && (System.nanoTime() - lastNanoTime) > nsTimeout;
  }
}
