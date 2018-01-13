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

package org.apache.servicecomb.loadbalance;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.cache.CacheEndpoint;

import com.netflix.loadbalancer.Server;

/**
 * 服务器抽象，address只有transport识别， LB模块不识别
 * LB模块不提供服务器状态监测，这块功能是由注册中心进行处理的。
 *
 */
public class CseServer extends Server {
  private final Endpoint endpoint;

  // 所属服务实例
  private final MicroserviceInstance instance;

  private long lastVisitTime = System.currentTimeMillis();

  /**
   * Count the continuous invocation failure. Once invocation successes, set this to zero.
   */
  private AtomicInteger continuousFailureCount = new AtomicInteger(0);

  public long getLastVisitTime() {
    return lastVisitTime;
  }

  public void setLastVisitTime(long lastVisitTime) {
    this.lastVisitTime = lastVisitTime;
  }

  public CseServer(Transport transport, CacheEndpoint cacheEndpoint) {
    super(null);

    endpoint = new Endpoint(transport, cacheEndpoint.getEndpoint(), cacheEndpoint.getInstance());
    instance = cacheEndpoint.getInstance();

    this.setAlive(true);
    this.setReadyToServe(true);
  }

  public Endpoint getEndpoint() {
    return endpoint;
  }

  public MicroserviceInstance getInstance() {
    return instance;
  }

  public String toString() {
    return endpoint.getEndpoint();
  }

  // used in LoadBalancerContext
  public String getHost() {
    return endpoint.getEndpoint();
  }

  public void clearContinuousFailure() {
    continuousFailureCount.set(0);
  }

  public void incrementContinuousFailureCount() {
    if (continuousFailureCount.get() < Integer.MAX_VALUE) {
      continuousFailureCount.incrementAndGet();
    }
  }

  public int getCountinuousFailureCount() {
    return continuousFailureCount.get();
  }

  public boolean equals(Object o) {
    if (o instanceof CseServer) {
      return this.getHost().equals(((CseServer) o).getHost());
    } else {
      return false;
    }
  }

  public int hashCode() {
    return this.getHost().hashCode();
  }
}
