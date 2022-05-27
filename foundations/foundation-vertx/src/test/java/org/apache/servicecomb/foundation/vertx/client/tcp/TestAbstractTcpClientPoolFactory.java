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

import org.junit.jupiter.api.Assertions;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestAbstractTcpClientPoolFactory {
  private final TcpClientConfig normalClientConfig = new TcpClientConfig();

  private final TcpClientConfig sslClientConfig = new TcpClientConfig();

  TcpClientPoolFactory factory = new TcpClientPoolFactory(normalClientConfig, sslClientConfig);

  @Test
  public void createClientPool() {
    Vertx vertx = Mockito.mock(Vertx.class);
    Context context = Mockito.mock(Context.class);
    Mockito.when(context.owner()).thenReturn(vertx);
    TcpClientConnectionPool pool = factory.createClientPool(context);

    Assertions.assertSame(normalClientConfig, pool.netClientWrapper.getClientConfig(false));
    Assertions.assertSame(sslClientConfig, pool.netClientWrapper.getClientConfig(true));
  }
}
