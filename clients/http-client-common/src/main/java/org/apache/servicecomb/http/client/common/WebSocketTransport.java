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

package org.apache.servicecomb.http.client.common;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

import org.apache.servicecomb.foundation.ssl.SSLManager;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

public class WebSocketTransport extends WebSocketClient {
  public static final int CONNECT_TIMEOUT = 5000;

  private WebSocketListener webSocketListener;

  public WebSocketTransport(String serverUri, HttpConfiguration.SSLProperties sslProperties,
      Map<String, String> headers, WebSocketListener webSocketListener)
      throws URISyntaxException {
    super(new URI(serverUri), new Draft_6455(), headers, CONNECT_TIMEOUT);

    if (sslProperties.isEnabled()) {
      SSLSocketFactory sslSocketFactory = SSLManager
          .createSSLSocketFactory(sslProperties.getSslOption(), sslProperties.getSslCustom());
      URI uri = new URI(serverUri);
      setSocketFactory(new SSLSocketFactoryExt(sslSocketFactory, uri.getHost(), uri.getPort()));
    }

    this.webSocketListener = webSocketListener;
  }

  @Override
  public void onOpen(ServerHandshake serverHandshake) {
    this.webSocketListener.onOpen(serverHandshake);
  }

  @Override
  public void onMessage(String s) {
    this.webSocketListener.onMessage(s);
  }

  @Override
  public void onClose(int code, String reason, boolean remote) {
    this.webSocketListener.onClose(code, reason, remote);
  }

  @Override
  public void onError(Exception e) {
    this.webSocketListener.onError(e);
  }
}
