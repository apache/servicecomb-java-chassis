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
package org.apache.servicecomb.foundation.vertx.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.foundation.vertx.client.http.HttpClientWithContext;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxImpl;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestClientPoolManager {
  @Mocked
  Vertx vertx;

  @Mocked
  ClientPoolFactory<HttpClientWithContext> factory;

  ClientPoolManager<HttpClientWithContext> poolMgr;

  String id;

  List<HttpClientWithContext> pools;

  Map<Object, Object> contextMap = new HashMap<>();

  @Mocked
  Context context;

  @Before
  public void setup() {
    poolMgr = new ClientPoolManager<>(vertx, factory);
    id = Deencapsulation.getField(poolMgr, "id");
    pools = Deencapsulation.getField(poolMgr, "pools");
    new MockUp<Context>(context) {
      @Mock
      void put(Object key, Object value) {
        contextMap.put(key, value);
      }

      @SuppressWarnings("unchecked")
      @Mock
      <T> T get(Object key) {
        return (T) contextMap.get(key);
      }

      @Mock
      Vertx owner() {
        return vertx;
      }

      @Mock
      boolean isEventLoopContext() {
        return true;
      }
    };
  }

  @Test
  public void createClientPool(@Mocked HttpClientWithContext pool) {
    new Expectations(VertxImpl.class) {
      {
        factory.createClientPool(context);
        result = pool;
      }
    };

    Assertions.assertSame(pool, poolMgr.createClientPool(context));
    Assertions.assertSame(pool, context.get(id));
    MatcherAssert.assertThat(pools, Matchers.contains(pool));
  }

  @Test
  public void findClientPool_sync(@Mocked HttpClientWithContext pool1, @Mocked HttpClientWithContext pool2) {
    new Expectations(poolMgr) {
      {
        poolMgr.findThreadBindClientPool();
        result = pool1;
        poolMgr.findByContext(null);
        result = pool2;
      }
    };

    Assertions.assertSame(pool1, poolMgr.findClientPool(true));
    Assertions.assertSame(pool2, poolMgr.findClientPool(false));
  }

  @Test
  public void findThreadBindClientPool(@Mocked HttpClientWithContext pool1, @Mocked HttpClientWithContext pool2) {
    pools.add(pool1);
    pools.add(pool2);

    new MockUp<Thread>() {
      @Mock
      long getId() {
        return 0;
      }
    };

    Assertions.assertSame(pool1, poolMgr.findThreadBindClientPool());
    // find again, get the same result
    Assertions.assertSame(pool1, poolMgr.findThreadBindClientPool());

    new MockUp<Thread>() {
      @Mock
      long getId() {
        return 1;
      }
    };

    Assertions.assertSame(pool2, poolMgr.findThreadBindClientPool());
    // find again, get the same result
    Assertions.assertSame(pool2, poolMgr.findThreadBindClientPool());
  }

  @Test
  public void findByContext_reactive() {
    HttpClientWithContext notMatchPool1 = new HttpClientWithContext(null, null);
    HttpClientWithContext notMatchPool2 = new HttpClientWithContext(null, null);
    pools.add(notMatchPool1);
    pools.add(notMatchPool2);

    new Expectations() {
      {
        Vertx.currentContext();
        result = context;
      }
    };
    context.put(id, notMatchPool2);

    Assertions.assertSame(notMatchPool2, poolMgr.findByContext());
    // find again, get the same result
    Assertions.assertSame(notMatchPool2, poolMgr.findByContext());
  }

  @Test
  public void findByContext_wrongContext_reverse() {
    HttpClientWithContext pool1 = new HttpClientWithContext(null, null);
    HttpClientWithContext pool2 = new HttpClientWithContext(null, null);
    pools.add(pool1);
    pools.add(pool2);

    new Expectations() {
      {
        Vertx.currentContext();
        result = null;
      }
    };

    AtomicInteger reactiveNextIndex = Deencapsulation.getField(poolMgr, "reactiveNextIndex");
    reactiveNextIndex.set(Integer.MAX_VALUE);
    // each time invoke find, reactiveNextIndex will inc 1
    Assertions.assertSame(pool2, poolMgr.findByContext());
    Assertions.assertSame(pool1, poolMgr.findByContext());
    Assertions.assertSame(pool2, poolMgr.findByContext());
    Assertions.assertSame(pool1, poolMgr.findByContext());
  }

  @Test
  public void findByContext_normalThread() {
    HttpClientWithContext pool = new HttpClientWithContext(null, null);
    pools.add(pool);

    new Expectations() {
      {
        Vertx.currentContext();
        result = null;
      }
    };

    Assertions.assertSame(pool, poolMgr.findByContext());
  }

  @Test
  public void findByContext_otherVertx(@Mocked VertxImpl otherVertx, @Mocked Context otherContext) {
    HttpClientWithContext pool = new HttpClientWithContext(null, null);
    pools.add(pool);

    new Expectations() {
      {
        Vertx.currentContext();
        result = otherContext;
        otherContext.owner();
        result = otherVertx;
      }
    };

    Assertions.assertSame(pool, poolMgr.findByContext());
  }

  @Test
  public void findByContext_worker(@Mocked Context workerContext) {
    HttpClientWithContext pool = new HttpClientWithContext(null, null);
    pools.add(pool);

    new Expectations() {
      {
        Vertx.currentContext();
        result = workerContext;
        workerContext.owner();
        result = vertx;
        workerContext.isEventLoopContext();
        result = false;
      }
    };

    Assertions.assertSame(pool, poolMgr.findByContext());
  }
}
