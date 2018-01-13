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

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxImpl;
import mockit.Expectations;
import mockit.Mocked;

public class TestAbstractTcpClientPoolFactory {
  private TcpClientConfig normalClientConfig = new TcpClientConfig();

  private TcpClientConfig sslClientConfig = new TcpClientConfig();

  TcpClientPoolFactory factory = new TcpClientPoolFactory(normalClientConfig, sslClientConfig);

  @Test
  public void createClientPool(@Mocked Vertx vertx, @Mocked Context context) {
    new Expectations(VertxImpl.class) {
      {
        VertxImpl.context();
        result = context;
        context.owner();
        result = vertx;
      }
    };
    TcpClientConnectionPool pool = factory.createClientPool();

    Assert.assertSame(normalClientConfig, pool.netClientWrapper.getClientConfig(false));
    Assert.assertSame(sslClientConfig, pool.netClientWrapper.getClientConfig(true));
  }
}
