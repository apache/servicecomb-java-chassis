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

package io.servicecomb.foundation.vertx.client.tcp;

import io.servicecomb.foundation.vertx.client.AbstractClientVerticle;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.impl.NetClientImpl;

public abstract class AbstractTcpClientVerticle<T extends TcpClientConnection, P extends AbstractTcpClientConnectionPool<T>>
    extends AbstractClientVerticle<P> {
  protected TcpClientConfig clientConfig;

  // 每线程一个实例即可
  protected NetClient netClient;

  @Override
  public void start() throws Exception {
    super.start();
    clientConfig = (TcpClientConfig) config().getValue(CLIENT_OPTIONS);

    // vertx.createNetClient()创建出来的netClient不支持跨线程调用
    netClient = new NetClientImpl((VertxInternal) vertx, clientConfig, false);
  }
}
