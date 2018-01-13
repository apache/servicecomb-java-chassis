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
package org.apache.servicecomb.foundation.vertx.client.tcp;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

// netClient do not like httpClient
// can not support normal and ssl by the same instance
// so we do this wrap
public class NetClientWrapper {
  private TcpClientConfig normalClientConfig;

  private NetClient normalNetClient;

  private TcpClientConfig sslClientConfig;

  private NetClient sslNetClient;

  public NetClientWrapper(Vertx vertx, TcpClientConfig normalClientConfig, TcpClientConfig sslClientConfig) {
    this.normalClientConfig = normalClientConfig;
    this.normalNetClient = vertx.createNetClient(normalClientConfig);

    this.sslClientConfig = sslClientConfig;
    this.sslNetClient = vertx.createNetClient(sslClientConfig);
  }

  public TcpClientConfig getClientConfig(boolean ssl) {
    if (ssl) {
      return sslClientConfig;
    }

    return normalClientConfig;
  }

  public void connect(boolean ssl, int port, String host, Handler<AsyncResult<NetSocket>> connectHandler) {
    if (ssl) {
      sslNetClient.connect(port, host, connectHandler);
      return;
    }

    normalNetClient.connect(port, host, connectHandler);
  }
}
