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
package org.apache.servicecomb.metrics.core;

import org.apache.servicecomb.core.transport.AbstractTransport;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.MetricsInitializer;
import org.apache.servicecomb.foundation.metrics.registry.GlobalRegistry;
import org.apache.servicecomb.metrics.core.meter.vertx.HttpClientEndpointsMeter;
import org.apache.servicecomb.metrics.core.meter.vertx.ServerEndpointsMeter;
import org.apache.servicecomb.metrics.core.meter.vertx.VertxEndpointsMeter;

import com.google.common.eventbus.EventBus;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.SpectatorUtils;

public class VertxMetersInitializer implements MetricsInitializer {
  public static final String VERTX_ENDPOINTS = "servicecomb.vertx.endpoints";

  public static final String ENDPOINTS_TYPE = "type";

  public static final String ENDPOINTS_CLINET = "client";

  public static final String ENDPOINTS_SERVER = "server";

  @Override
  public void init(GlobalRegistry globalRegistry, EventBus eventBus, MetricsBootstrapConfig config) {
    Registry registry = globalRegistry.getDefaultRegistry();

    Id endpointsId = registry.createId(VERTX_ENDPOINTS);
    VertxEndpointsMeter clientMeter = new HttpClientEndpointsMeter(
        endpointsId.withTag(ENDPOINTS_TYPE, ENDPOINTS_CLINET),
        AbstractTransport
            .getTransportVertxFactory()
            .getMetricsFactory()
            .getVertxMetrics()
            .getClientEndpointMetricManager()
            .getClientEndpointMetricMap());
    SpectatorUtils.registerMeter(registry, clientMeter);

    VertxEndpointsMeter serverMeter = new ServerEndpointsMeter(
        endpointsId.withTag(ENDPOINTS_TYPE, ENDPOINTS_SERVER),
        AbstractTransport
            .getTransportVertxFactory()
            .getMetricsFactory()
            .getVertxMetrics()
            .getServerEndpointMetricMap());
    SpectatorUtils.registerMeter(registry, serverMeter);
  }
}
