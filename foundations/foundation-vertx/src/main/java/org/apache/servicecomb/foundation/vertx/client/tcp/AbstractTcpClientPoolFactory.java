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

import org.apache.servicecomb.foundation.vertx.client.ClientPoolFactory;

import io.vertx.core.Context;
import io.vertx.core.Vertx;

public abstract class AbstractTcpClientPoolFactory<CLIENT_POOL> implements ClientPoolFactory<CLIENT_POOL> {
  protected TcpClientConfig normalClientConfig;

  protected TcpClientConfig sslClientConfig;

  public AbstractTcpClientPoolFactory(TcpClientConfig normalClientConfig, TcpClientConfig sslClientConfig) {
    this.normalClientConfig = normalClientConfig;
    this.sslClientConfig = sslClientConfig;
  }

  @Override
  public CLIENT_POOL createClientPool(Context context) {
    Vertx vertx = context.owner();

    NetClientWrapper netClientWrapper = new NetClientWrapper(vertx, normalClientConfig, sslClientConfig);
    return doCreateClientPool(context, netClientWrapper);
  }

  protected abstract CLIENT_POOL doCreateClientPool(Context context, NetClientWrapper netClientWrapper);
}
