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
package io.vertx.core.impl;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestVertxImplEx {
  @Test
  public void testContextCreatedCount(@Mocked EventLoopContext context) {
    new MockUp<VertxImpl>() {
      @Mock
      EventLoopContext createEventLoopContext(String deploymentID, WorkerPool workerPool, JsonObject config,
          ClassLoader tccl) {
        return context;
      }
    };

    VertxImplEx vertx = new VertxImplEx("test", new VertxOptions());

    vertx.createEventLoopContext(null, null, null, null);
    Assert.assertEquals(1, vertx.getEventLoopContextCreatedCount());

    vertx.createEventLoopContext(null, null, null, null);
    Assert.assertEquals(2, vertx.getEventLoopContextCreatedCount());
  }
}
