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
import io.vertx.core.shareddata.Shareable;

public class TransportVertxFactory {
  static class TransportVertxInfo implements Shareable {
    public VertxOptions vertxOptions = new VertxOptions();

    public DefaultVertxMetricsFactory metricsFactory = new DefaultVertxMetricsFactory();

    public MetricsOptionsEx metricsOptionsEx = (MetricsOptionsEx) metricsFactory.newOptions();

    public TransportVertxInfo() {
      vertxOptions.setMetricsOptions(metricsOptionsEx);
    }
  }

  private static final String LOCAL_MAP_NAME = "scb";

  private static final String INFO = "transport-vertx-info";

  public DefaultVertxMetricsFactory getMetricsFactory() {
    TransportVertxInfo info = (TransportVertxInfo) getTransportVertx().sharedData().getLocalMap(LOCAL_MAP_NAME)
        .get(INFO);
    return info.metricsFactory;
  }

  public Vertx getTransportVertx() {
    return VertxUtils.getVertxMap().computeIfAbsent("transport", this::createTransportVertx);
  }

  private Vertx createTransportVertx(String name) {
    TransportVertxInfo info = new TransportVertxInfo();

    Vertx vertx = VertxUtils.init(info.vertxOptions);
    info.metricsFactory.setVertx(vertx, info.vertxOptions);
    vertx.sharedData().getLocalMap(LOCAL_MAP_NAME).put(INFO, info);

    return vertx;
  }
}
