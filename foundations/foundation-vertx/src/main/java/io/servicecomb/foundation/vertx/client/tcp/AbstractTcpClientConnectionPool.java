/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.foundation.vertx.client.tcp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.vertx.core.Context;
import io.vertx.core.net.NetClient;

public abstract class AbstractTcpClientConnectionPool<T extends TcpClientConnection> {
  // 是在哪个context中创建的
  protected Context context;

  protected TcpClientConfig clientConfig;

  protected NetClient netClient;

  // key为address
  protected Map<String, T> tcpClientMap = new ConcurrentHashMap<>();

  public AbstractTcpClientConnectionPool(TcpClientConfig clientConfig, Context context, NetClient netClient) {
    this.clientConfig = clientConfig;
    this.context = context;
    this.netClient = netClient;

    startCheckTimeout(clientConfig, context);
  }

  protected void startCheckTimeout(TcpClientConfig clientConfig, Context context) {
    context.owner().setPeriodic(clientConfig.getRequestTimeoutMillis(), this::onCheckTimeout);
  }

  private void onCheckTimeout(Long event) {
    for (TcpClientConnection client : tcpClientMap.values()) {
      client.checkTimeout();
    }
  }

  public void send(TcpClientConnection tcpClient, AbstractTcpClientPackage tcpClientPackage,
      TcpResonseCallback callback) {
    tcpClient.send(tcpClientPackage, clientConfig.getRequestTimeoutMillis(), callback);
  }

  public T findOrCreateClient(String endpoint) {
    T tcpClient = tcpClientMap.get(endpoint);
    if (tcpClient == null) {
      synchronized (this) {
        tcpClient = tcpClientMap.get(endpoint);
        if (tcpClient == null) {
          tcpClient = create(endpoint);
          tcpClientMap.put(endpoint, tcpClient);
        }
      }
    }

    return tcpClient;
  }

  protected abstract T create(String endpoint);
}
