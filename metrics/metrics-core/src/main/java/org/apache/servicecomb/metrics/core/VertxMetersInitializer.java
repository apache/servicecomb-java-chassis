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

import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.MetricsInitializer;
import org.apache.servicecomb.foundation.vertx.SharedVertxFactory;
import org.apache.servicecomb.metrics.core.meter.vertx.HttpClientEndpointsMeter;
import org.apache.servicecomb.metrics.core.meter.vertx.ServerEndpointsMeter;
import org.apache.servicecomb.metrics.core.meter.vertx.VertxEndpointsMeter;

import com.google.common.eventbus.EventBus;

import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

public class VertxMetersInitializer implements MetricsInitializer {
  public static final String VERTX_ENDPOINTS = "servicecomb.vertx.endpoints";

  public static final String ENDPOINTS_TYPE = "type";

  public static final String ENDPOINTS_CLINET = "client";

  public static final String ENDPOINTS_SERVER = "server";

  @Override
  public void init(MeterRegistry meterRegistry, EventBus eventBus, MetricsBootstrapConfig config) {
    Id endpointsId = new Id(VERTX_ENDPOINTS, null, null, null, null);
    VertxEndpointsMeter clientMeter = new HttpClientEndpointsMeter(
        endpointsId.withTag(Tag.of(ENDPOINTS_TYPE, ENDPOINTS_CLINET)),
        SharedVertxFactory.getMetricsFactory(config.getEnvironment())
            .getVertxMetrics()
            .getClientEndpointMetricManager()
            .getClientEndpointMetricMap());

    VertxEndpointsMeter serverMeter = new ServerEndpointsMeter(
        endpointsId.withTag(Tag.of(ENDPOINTS_TYPE, ENDPOINTS_SERVER)),
        SharedVertxFactory.getMetricsFactory(config.getEnvironment())
            .getVertxMetrics()
            .getServerEndpointMetricMap());
  }
}
