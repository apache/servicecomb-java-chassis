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

package org.apache.servicecomb.demo.perf;

import java.util.concurrent.CompletableFuture;

import io.vertx.core.AsyncResult;
import io.vertx.redis.RedisClient;

public class RedisSession {
  RedisClient redis;

  String id;

  CompletableFuture<String> future;

  String createResult;

  public RedisSession(RedisClient redis, String id, CompletableFuture<String> future) {
    this.redis = redis;
    this.id = id;
    this.future = future;
  }

  public void query() {
    redis.get(id, this::onGetResponse);
  }

  private void onGetResponse(AsyncResult<String> ar) {
    if (ar.succeeded()) {
      if (ar.result() == null) {
        createCache();
        return;
      }

      future.complete(ar.result());
      return;
    }

    future.completeExceptionally(ar.cause());
  }

  private void createCache() {
    createResult = PerfConfiguration.buildResponse("redis", id);
    redis.set(id, createResult, this::onCreateCacheResponse);
  }

  private void onCreateCacheResponse(AsyncResult<Void> ar) {
    if (ar.succeeded()) {
      future.complete(createResult);
      return;
    }

    future.completeExceptionally(ar.cause());
  }
}
