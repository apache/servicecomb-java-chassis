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

package org.apache.servicecomb.demo.edge.service.encrypt;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.demo.edge.authentication.encrypt.Hcr;
import org.apache.servicecomb.edge.core.AbstractEdgeDispatcher;
import org.apache.servicecomb.edge.core.CompatiblePathVersionMapper;
import org.apache.servicecomb.provider.pojo.Invoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CookieHandler;

public class EncryptEdgeDispatcher extends AbstractEdgeDispatcher {
  private static final Logger LOGGER = LoggerFactory.getLogger(EncryptEdgeDispatcher.class);

  private CompatiblePathVersionMapper versionMapper = new CompatiblePathVersionMapper();

  private Encrypt encrypt = Invoker.createProxy("auth", "encrypt", Encrypt.class);

  private String prefix = "encryptApi";

  @Override
  public int getOrder() {
    return 10000;
  }

  @Override
  public void init(Router router) {
    {
      String regex = "/" + prefix + "/([^\\\\/]+)/([^\\\\/]+)/(.*)";
      router.routeWithRegex(regex).handler(CookieHandler.create());
      router.routeWithRegex(regex).handler(createBodyHandler());
      router.routeWithRegex(regex).failureHandler(this::onFailure).handler(this::onRequest);
    }
  }

  protected void onRequest(RoutingContext context) {
    HttpServerRequest httpServerRequest = context.request();

    // queryUserId always success
    CompletableFuture<String> userIdFuture = queryUserId(httpServerRequest);
    queryHcr(httpServerRequest).thenCombine(userIdFuture, (hcr, userId) -> {
      // hcr and userId all success
      routeToBackend(context, hcr, userId);
      return null;
    }).whenComplete((v, e) -> {
      // failed to query hcr
      if (e != null) {
        context.response().end("failed to query hcr: " + e.getMessage());
        return;
      }
    });
  }

  private CompletableFuture<String> queryUserId(HttpServerRequest httpServerRequest) {
    String serviceToken = httpServerRequest.getParam("serviceToken");
    if (serviceToken == null) {
      // no need to query userId
      return CompletableFuture.completedFuture(null);
    }

    CompletableFuture<String> future = new CompletableFuture<>();
    encrypt.queryUserId(serviceToken).whenComplete((userId, e) -> {
      if (e != null) {
        // treat as success, just userId is null
        LOGGER.error("Failed to query userId, serviceToken={}.", serviceToken, e);
      }

      future.complete(userId);
    });

    return future;
  }

  private CompletableFuture<Hcr> queryHcr(HttpServerRequest httpServerRequest) {
    String hcrId = httpServerRequest.getParam("hcrId");
    return encrypt.queryHcr(hcrId);
  }

  private void routeToBackend(RoutingContext context, Hcr hcr, String userId) {
    Map<String, String> pathParams = context.pathParams();
    String microserviceName = pathParams.get("param0");
    String pathVersion = pathParams.get("param1");
    String path = context.request().path().substring(prefix.length() + 1);

    EncryptEdgeInvocation edgeInvocation = new EncryptEdgeInvocation(new EncryptContext(hcr, userId));
    edgeInvocation.setVersionRule(versionMapper.getOrCreate(pathVersion).getVersionRule());

    edgeInvocation.init(microserviceName, context, path, httpServerFilters);
    edgeInvocation.edgeInvoke();
  }
}
