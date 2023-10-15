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
package org.apache.servicecomb.solution.basic.health;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.provider.consumer.InvokerUtils;
import org.apache.servicecomb.registry.discovery.InstancePing;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance;
import org.apache.servicecomb.registry.discovery.TelnetInstancePing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.CollectionUtils;

public class HealthInstancePing implements InstancePing {
  private static final Logger LOGGER = LoggerFactory.getLogger(HealthInstancePing.class);

  private SCBEngine scbEngine;

  private TelnetInstancePing telnetInstancePing;

  @Autowired
  @Lazy
  public void setScbEngine(SCBEngine scbEngine) {
    this.scbEngine = scbEngine;
  }

  @Autowired
  @Lazy
  public void setTelnetInstancePing(@Qualifier("telnetInstancePing") TelnetInstancePing telnetInstancePing) {
    this.telnetInstancePing = telnetInstancePing;
  }

  @Override
  public boolean ping(StatefulDiscoveryInstance instance) {
    if (CollectionUtils.isEmpty(instance.getEndpoints())) {
      return false;
    }

    // hard coded here, not very nice
    if ("local-registry".equals(instance.getRegistryName())) {
      return telnetInstancePing.ping(instance);
    }

    Map<String, Object> args = new HashMap<>(2);
    args.put("instanceId", instance.getInstanceId());
    args.put("registryName", instance.getRegistryName());

    for (String endpoint : instance.getEndpoints()) {
      URI uri = URI.create(endpoint);
      String transportName = uri.getScheme();
      Transport transport = scbEngine.getTransportManager().findTransport(transportName);
      if (transport == null) {
        continue;
      }
      Invocation invocation = InvokerUtils.createInvocation(instance.getServiceName(), transportName,
          "HealthEndpoint", "health",
          args, boolean.class);
      invocation.setEndpoint(new Endpoint(transport, endpoint, instance));
      boolean result;
      try {
        result = (boolean) InvokerUtils.syncInvoke(invocation);
      } catch (Exception e) {
        LOGGER.warn("ping instance {}/{}/{}/{} endpoint {} failed. {}",
            instance.getApplication(),
            instance.getServiceName(),
            instance.getRegistryName(),
            instance.getInstanceId(), endpoint, e.getMessage());
        continue;
      }
      if (result) {
        return true;
      }
      LOGGER.warn("ping instance {}/{}/{}/{} endpoint {} failed",
          instance.getApplication(),
          instance.getServiceName(),
          instance.getRegistryName(),
          instance.getInstanceId(), endpoint);
    }

    return false;
  }

  @Override
  public int getOrder() {
    return -10000;
  }
}
