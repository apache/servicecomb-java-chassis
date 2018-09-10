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
package org.apache.servicecomb.core.transport;

import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.foundation.vertx.metrics.DefaultVertxMetricsFactory;
import org.apache.servicecomb.foundation.vertx.metrics.MetricsOptionsEx;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

public class TransportVertxFactory {
  private VertxOptions vertxOptions = new VertxOptions();

  private DefaultVertxMetricsFactory metricsFactory = new DefaultVertxMetricsFactory();

  private MetricsOptionsEx metricsOptionsEx = (MetricsOptionsEx) metricsFactory.newOptions();

  private Vertx transportVertx;

  public TransportVertxFactory() {
    vertxOptions.setMetricsOptions(metricsOptionsEx);
    transportVertx = VertxUtils.getOrCreateVertxByName("transport", vertxOptions);
  }

  public DefaultVertxMetricsFactory getMetricsFactory() {
    return metricsFactory;
  }

  public Vertx getTransportVertx() {
    return transportVertx;
  }
}
