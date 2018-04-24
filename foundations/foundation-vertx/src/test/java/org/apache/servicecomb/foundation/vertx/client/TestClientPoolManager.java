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
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

  Map<String, Object> contextMap = new HashMap<>();

  Context context;

  @Before
  public void setup() {
    poolMgr = new ClientPoolManager<>(vertx, factory);
    id = Deencapsulation.getField(poolMgr, "id");
    pools = Deencapsulation.getField(poolMgr, "pools");
    context = new MockUp<Context>() {
      @Mock
      void put(String key, Object value) {
        contextMap.put(key, value);
      }

      @SuppressWarnings("unchecked")
      @Mock
      <T> T get(String key) {
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
    }.getMockInstance();
  }

  @Test
  public void createClientPool(@Mocked HttpClientWithContext pool) {
    new Expectations(VertxImpl.class) {
      {
        factory.createClientPool(context);
        result = pool;
      }
    };

    Assert.assertSame(pool, poolMgr.createClientPool(context));
    Assert.assertSame(pool, context.get(id));
    Assert.assertThat(pools, Matchers.contains(pool));
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

    Assert.assertSame(pool1, poolMgr.findClientPool(true));
    Assert.assertSame(pool2, poolMgr.findClientPool(false));
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

    Assert.assertSame(pool1, poolMgr.findThreadBindClientPool());
    // find again, get the same result
    Assert.assertSame(pool1, poolMgr.findThreadBindClientPool());

    new MockUp<Thread>() {
      @Mock
      long getId() {
        return 1;
      }
    };

    Assert.assertSame(pool2, poolMgr.findThreadBindClientPool());
    // find again, get the same result
    Assert.assertSame(pool2, poolMgr.findThreadBindClientPool());
  }

  @Test
  public void findByContext_reactive() {
    HttpClientWithContext notMatchPool = new HttpClientWithContext(null, null);
    pools.add(notMatchPool);

    new Expectations(VertxImpl.class) {
      {
        factory.createClientPool(context);
        result = new HttpClientWithContext(null, null);
        VertxImpl.context();
        result = context;
      }
    };

    HttpClientWithContext result = poolMgr.findByContext();
    Assert.assertNotSame(notMatchPool, result);
    // find again, get the same result
    Assert.assertSame(result, poolMgr.findByContext());
  }

  @Test
  public void findByContext_wrongContext_reverse() {
    HttpClientWithContext pool1 = new HttpClientWithContext(null, null);
    HttpClientWithContext pool2 = new HttpClientWithContext(null, null);
    pools.add(pool1);
    pools.add(pool2);

    new Expectations(VertxImpl.class) {
      {
        VertxImpl.context();
        result = null;
      }
    };

    AtomicInteger reactiveNextIndex = Deencapsulation.getField(poolMgr, "reactiveNextIndex");
    reactiveNextIndex.set(Integer.MAX_VALUE);
    // each time invoke find, reactiveNextIndex will inc 1
    Assert.assertSame(pool2, poolMgr.findByContext());
    Assert.assertSame(pool1, poolMgr.findByContext());
    Assert.assertSame(pool2, poolMgr.findByContext());
    Assert.assertSame(pool1, poolMgr.findByContext());
  }

  @Test
  public void findByContext_normalThread() {
    HttpClientWithContext pool = new HttpClientWithContext(null, null);
    pools.add(pool);

    new Expectations(VertxImpl.class) {
      {
        VertxImpl.context();
        result = null;
      }
    };

    Assert.assertSame(pool, poolMgr.findByContext());
  }

  @Test
  public void findByContext_otherVertx(@Mocked Vertx otherVertx, @Mocked Context otherContext) {
    HttpClientWithContext pool = new HttpClientWithContext(null, null);
    pools.add(pool);

    new Expectations(VertxImpl.class) {
      {
        VertxImpl.context();
        result = otherContext;
        otherContext.owner();
        result = otherVertx;
      }
    };

    Assert.assertSame(pool, poolMgr.findByContext());
  }

  @Test
  public void findByContext_woker(@Mocked Context workerContext) {
    HttpClientWithContext pool = new HttpClientWithContext(null, null);
    pools.add(pool);

    new Expectations(VertxImpl.class) {
      {
        VertxImpl.context();
        result = workerContext;
        workerContext.owner();
        result = vertx;
        workerContext.isEventLoopContext();
        result = false;
      }
    };

    Assert.assertSame(pool, poolMgr.findByContext());
  }
}
