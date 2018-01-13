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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;

import io.vertx.core.Context;

public abstract class AbstractTcpClientConnectionPool<T extends TcpClientConnection> {
  // 是在哪个context中创建的
  protected Context context;

  protected NetClientWrapper netClientWrapper;

  // key为address
  protected Map<String, T> tcpClientMap = new ConcurrentHashMapEx<>();

  public AbstractTcpClientConnectionPool(Context context,
      NetClientWrapper netClientWrapper) {
    this.context = context;
    this.netClientWrapper = netClientWrapper;

    startCheckTimeout(context);
  }

  protected void startCheckTimeout(Context context) {
    context.owner().setPeriodic(TimeUnit.SECONDS.toMillis(1), this::onCheckTimeout);
  }

  private void onCheckTimeout(Long event) {
    for (TcpClientConnection client : tcpClientMap.values()) {
      client.checkTimeout();
    }
  }

  public T findOrCreateClient(String endpoint) {
    return tcpClientMap.computeIfAbsent(endpoint, this::create);
  }

  protected abstract T create(String endpoint);
}
