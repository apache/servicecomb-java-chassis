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
package org.apache.servicecomb.metrics.core.meter.invocation;

import java.util.Map;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.InvocationStartEvent;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.swagger.invocation.Response;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.SpectatorUtils;

public abstract class AbstractInvocationMeters {
  protected Registry registry;

  private final Map<String, AbstractInvocationMeter> metersMap = new ConcurrentHashMapEx<>();

  // not care for concurrency, just for make build key faster
  private int maxKeyLen = 64;

  protected MetricsBootstrapConfig metricsBootstrapConfig;

  public AbstractInvocationMeters(Registry registry, MetricsBootstrapConfig metricsBootstrapConfig) {
    this.registry = registry;
    this.metricsBootstrapConfig = metricsBootstrapConfig;
  }

  protected AbstractInvocationMeter getOrCreateMeters(Invocation invocation, Response response) {
    // build string key is faster then use Id to locate timer directly
    StringBuilder keyBuilder = new StringBuilder(maxKeyLen);
    String invocationName;
    //check edge
    if (invocation.isConsumer() && invocation.isEdge()) {
      invocationName = MeterInvocationConst.EDGE_INVOCATION_NAME;
    } else {
      invocationName = invocation.getInvocationType().name();
    }

    keyBuilder
        .append(invocationName)
        .append(invocation.getRealTransportName())
        .append(invocation.getMicroserviceQualifiedName())
        .append(response.getStatusCode());
    if (keyBuilder.length() > maxKeyLen) {
      maxKeyLen = keyBuilder.length();
    }

    return metersMap.computeIfAbsent(keyBuilder.toString(), k -> {
      Id id = registry.createId(MeterInvocationConst.INVOCATION_NAME,
          MeterInvocationConst.TAG_ROLE,
          invocationName,
          MeterInvocationConst.TAG_TRANSPORT,
          invocation.getRealTransportName(),
          MeterInvocationConst.TAG_OPERATION,
          invocation.getMicroserviceQualifiedName(),
          MeterInvocationConst.TAG_STATUS,
          String.valueOf(response.getStatusCode()));

      AbstractInvocationMeter meter = createMeter(id);
      SpectatorUtils.registerMeter(registry, meter);
      return meter;
    });
  }

  protected abstract AbstractInvocationMeter createMeter(Id id);

  public void onInvocationStart(InvocationStartEvent event) {
  }

  public void onInvocationFinish(InvocationFinishEvent event) {
    AbstractInvocationMeter meters = getOrCreateMeters(event.getInvocation(), event.getResponse());
    meters.onInvocationFinish(event);
  }
}
