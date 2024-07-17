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

package org.apache.servicecomb.transport.rest.vertx;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.common.rest.EdgeServerWebSocketInvocationCreator;
import org.apache.servicecomb.common.rest.ProviderServerWebSocketInvocationCreator;
import org.apache.servicecomb.common.rest.route.URLMappedConfigurationItem;
import org.apache.servicecomb.common.rest.route.URLMappedConfigurationLoader;
import org.apache.servicecomb.common.rest.route.Utils;
import org.apache.servicecomb.config.ConfigurationChangedEvent;
import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.invocation.InvocationCreator;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

import com.google.common.eventbus.Subscribe;

import io.vertx.core.http.ServerWebSocket;
import jakarta.ws.rs.core.Response.Status;

public class WebSocketDispatcher {
  private static final String KEY_MAPPING_PREFIX = "servicecomb.http.dispatcher.edge.websocket.mappings";

  private final Object LOCK = new Object();

  private volatile boolean initialized = false;

  private Endpoint endpoint;

  private MicroserviceMeta microserviceMeta;

  private boolean isEdge;

  private Map<String, URLMappedConfigurationItem> configurations = new HashMap<>();

  public WebSocketDispatcher(Endpoint endpoint) {
    this.endpoint = endpoint;
    EventManager.register(this);
  }

  private void loadConfigurations() {
    configurations = URLMappedConfigurationLoader.loadConfigurations(
        LegacyPropertyFactory.getEnvironment(), KEY_MAPPING_PREFIX);
  }

  @Subscribe
  public void onConfigurationChangedEvent(ConfigurationChangedEvent event) {
    for (String changed : event.getChanged()) {
      if (changed.startsWith(KEY_MAPPING_PREFIX)) {
        loadConfigurations();
        break;
      }
    }
  }

  protected void onRequest(ServerWebSocket webSocket) {
    if (!initialized) {
      synchronized (LOCK) {
        if (!initialized) {
          Transport transport = SCBEngine.getInstance().getTransportManager().findTransport(CoreConst.WEBSOCKET);
          this.microserviceMeta = SCBEngine.getInstance().getProducerMicroserviceMeta();
          this.endpoint = new Endpoint(transport, this.endpoint.getEndpoint());
          this.isEdge = TransportConfig.getRestServerVerticle()
              .getName().equals("org.apache.servicecomb.edge.core.EdgeRestServerVerticle");
          if (this.isEdge) {
            loadConfigurations();
          }
        }
        initialized = true;
      }
    }

    InvocationCreator creator;
    if (isEdge) {
      URLMappedConfigurationItem configurationItem = findConfigurationItem(webSocket.path());
      if (configurationItem == null) {
        throw new InvocationException(Status.NOT_FOUND, new CommonExceptionData(
            String.format("path %s not found", webSocket.path())));
      }
      String path = Utils.findActualPath(webSocket.path(), configurationItem.getPrefixSegmentCount());
      creator = new EdgeServerWebSocketInvocationCreator(
          configurationItem.getMicroserviceName(), path, endpoint, webSocket);
    } else {
      creator = new ProviderServerWebSocketInvocationCreator(microserviceMeta,
          endpoint, webSocket);
    }
    new WebSocketProducerInvocationFlow(creator, webSocket).run();
  }

  private URLMappedConfigurationItem findConfigurationItem(String path) {
    for (URLMappedConfigurationItem item : configurations.values()) {
      if (item.getPattern().matcher(path).matches()) {
        return item;
      }
    }
    return null;
  }
}
