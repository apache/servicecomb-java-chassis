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

public class DefaultTcpSocketMetric {
  protected DefaultEndpointMetric endpointMetric;

  protected boolean connected = true;

  protected long connectedTime = System.nanoTime();

  public DefaultTcpSocketMetric() {
  }

  public DefaultTcpSocketMetric(DefaultEndpointMetric endpointMetric) {
    this.endpointMetric = endpointMetric;
  }

  @SuppressWarnings("unchecked")
  public <T extends DefaultEndpointMetric> T getEndpointMetric() {
    return (T) endpointMetric;
  }

  public DefaultTcpSocketMetric endpointMetric(DefaultEndpointMetric endpointMetric) {
    this.endpointMetric = endpointMetric;
    return this;
  }

  public boolean isConnected() {
    return connected;
  }

  public void onDisconnect() {
    endpointMetric.onDisconnect();
    this.connected = false;
  }

  public long getConnectedTime() {
    return connectedTime;
  }
}
