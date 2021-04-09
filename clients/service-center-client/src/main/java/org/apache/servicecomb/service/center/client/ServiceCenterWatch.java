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

package org.apache.servicecomb.service.center.client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.servicecomb.http.client.auth.RequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpConfiguration.SSLProperties;
import org.apache.servicecomb.http.client.common.WebSocketListener;
import org.apache.servicecomb.http.client.common.WebSocketTransport;
import org.apache.servicecomb.service.center.client.DiscoveryEvents.PullInstanceEvent;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

public class ServiceCenterWatch implements WebSocketListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCenterWatch.class);

  private static final String HTTP = "http://";

  private static final String HTTPS = "https://";

  private static final String WS = "ws://";

  private static final String WSS = "wss://";

  private static final String WATCH = "/v4/%s/registry/microservices/%s/watcher";

  private static final long SLEEP_BASE = 3000;

  private static final long SLEEP_MAX = 10 * 60 * 10000;

  private AddressManager addressManager;

  private SSLProperties sslProperties;

  private RequestAuthHeaderProvider requestAuthHeaderProvider;

  private String tenantName;

  private Map<String, String> extraGlobalHeaders;

  private WebSocketTransport webSocketTransport;

  private String project;

  private String serviceId;

  private int continuousError = 0;

  private AtomicBoolean reconnecting = new AtomicBoolean(false);

  private EventBus eventBus;

  private ExecutorService connector = Executors.newFixedThreadPool(1, (r) -> new
      Thread(r, "web-socket-connector"));

  public ServiceCenterWatch(AddressManager addressManager,
      SSLProperties sslProperties,
      RequestAuthHeaderProvider requestAuthHeaderProvider,
      String tenantName,
      Map<String, String> extraGlobalHeaders,
      EventBus eventBus) {
    this.addressManager = addressManager;
    this.sslProperties = sslProperties;
    this.requestAuthHeaderProvider = requestAuthHeaderProvider;
    this.tenantName = tenantName;
    this.extraGlobalHeaders = extraGlobalHeaders;
    this.eventBus = eventBus;
  }

  public void startWatch(String project, String serviceId) {
    this.project = project;
    this.serviceId = serviceId;

    startWatch();
  }

  private void startWatch() {
    connector.submit(() -> {
      backOff();

      try {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-domain-name", this.tenantName);
        headers.putAll(this.extraGlobalHeaders);
        headers.putAll(this.requestAuthHeaderProvider.loadAuthHeader(null));
        LOGGER.info("start watch to address {}", addressManager.address());
        webSocketTransport = new WebSocketTransport(convertAddress(), sslProperties,
            headers, this);
        webSocketTransport.connectBlocking();
      } catch (Exception e) {
        LOGGER.error("start watch failed. ", e);
      }
    });
  }

  private String convertAddress() {
    String address = addressManager.address();
    String url = String.format(WATCH, project, serviceId);
    if (address.startsWith(HTTP)) {
      return WS + address.substring(HTTP.length()) + url;
    }

    if (address.startsWith(HTTPS)) {
      return WSS + address.substring(HTTPS.length()) + url;
    }

    return address + url;
  }

  public void stop() {
    if (webSocketTransport != null) {
      webSocketTransport.close();
    }
  }

  private void reconnect() {
    if (reconnecting.getAndSet(true)) {
      return;
    }
    continuousError++;
    if (webSocketTransport != null) {
      webSocketTransport.close();
    }
    addressManager.changeAddress();
    startWatch();
  }

  private void backOff() {
    if (this.continuousError <= 0) {
      return;
    }
    try {
      Thread.sleep(Math.min(SLEEP_MAX, this.continuousError * this.continuousError * SLEEP_BASE));
    } catch (InterruptedException e) {
      // do not care
    }
  }

  @Override
  public void onMessage(String s) {
    LOGGER.info("web socket receive message [{}], start query instance", s);
    this.eventBus.post(new PullInstanceEvent());
  }

  @Override
  public void onError(Exception e) {
    LOGGER.warn("web socket receive error [{}], will restart.", e.getMessage());
    reconnect();
  }

  @Override
  public void onClose(int code, String reason, boolean remote) {
    LOGGER.warn("web socket closed, code={}, reason={}.", code, reason);
  }

  @Override
  public void onOpen(ServerHandshake serverHandshake) {
    LOGGER.info("web socket connected to server {}, status={}, message={}", addressManager.address(),
        serverHandshake.getHttpStatus(),
        serverHandshake.getHttpStatusMessage());
    continuousError = 0;
    reconnecting.set(false);
  }
}
