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
import java.util.concurrent.ExecutionException;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.foundation.vertx.client.ClientPoolFactory;
import org.apache.servicecomb.foundation.vertx.client.ClientPoolManager;
import org.apache.servicecomb.foundation.vertx.client.ClientVerticle;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

public class RedisClientUtils {
  private static ClientPoolManager<RedisClient> clientMgr;

  public static void init(Vertx vertx) throws InterruptedException {
    RedisOptions redisOptions = new RedisOptions()
        .setHost(PerfConfiguration.redisHost)
        .setPort(PerfConfiguration.redisPort)
        .setAuth(PerfConfiguration.redisPassword);
    ClientPoolFactory<RedisClient> factory = (ctx) -> {
      return RedisClient.create(vertx, redisOptions);
    };
    clientMgr = new ClientPoolManager<>(vertx, factory);

    DeploymentOptions deployOptions = VertxUtils.createClientDeployOptions(clientMgr,
        PerfConfiguration.redisClientCount);
    VertxUtils.blockDeploy(vertx, ClientVerticle.class, deployOptions);
  }

  public static String syncQuery(String id) {
    CompletableFuture<String> future = doQuery(id, true);
    try {
      return future.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new InvocationException(Status.INTERNAL_SERVER_ERROR.getStatusCode(),
          Status.INTERNAL_SERVER_ERROR.getReasonPhrase(), (Object) "Failed to query from redis.", e);
    }
  }

  public static CompletableFuture<String> asyncQuery(String id) {
    return doQuery(id, false);
  }

  private static CompletableFuture<String> doQuery(String id, boolean sync) {
    CompletableFuture<String> future = new CompletableFuture<>();
    RedisClient redisClient = clientMgr.findClientPool(sync);
    RedisSession session = new RedisSession(redisClient, id, future);
    session.query();
    return future;
  }
}
