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

import org.junit.Before;
import org.junit.Test;

import io.vertx.core.impl.ContextInternal;
import mockit.Mocked;
import org.junit.jupiter.api.Assertions;

public class TestTcpClientConnectionPool {
  @Mocked
  ContextInternal context;

  @Mocked
  NetClientWrapper netClientWrapper;

  TcpClientConnectionPool pool;

  @Before
  public void setup() {
    pool = new TcpClientConnectionPool(context, netClientWrapper);
  }

  @Test
  public void create() {
    Assertions.assertTrue(pool.create("rest://localhost:8765") instanceof TcpClientConnection);
  }
}
