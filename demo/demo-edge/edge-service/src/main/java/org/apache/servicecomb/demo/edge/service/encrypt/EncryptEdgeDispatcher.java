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

import org.apache.servicecomb.common.rest.RestProducerInvocationFlow;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.invocation.InvocationCreator;
import org.apache.servicecomb.demo.edge.authentication.encrypt.Hcr;
import org.apache.servicecomb.demo.edge.service.EdgeConst;
import org.apache.servicecomb.edge.core.AbstractEdgeDispatcher;
import org.apache.servicecomb.edge.core.EdgeInvocationCreator;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.foundation.vertx.http.VertxServerRequestToHttpServletRequest;
import org.apache.servicecomb.foundation.vertx.http.VertxServerResponseToHttpServletResponse;
import org.apache.servicecomb.provider.pojo.Invoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class EncryptEdgeDispatcher extends AbstractEdgeDispatcher {
  private static final Logger LOGGER = LoggerFactory.getLogger(EncryptEdgeDispatcher.class);

  private Encrypt encrypt = Invoker.createProxy("auth", "encrypt", Encrypt.class);

  private String prefix = "encryptApi";

  @Override
  public int getOrder() {
    return 10000;
  }

  @Override
  public void init(Router router) {
    String regex = "/" + prefix + "/([^\\\\/]+)/(.*)";
    // cookies handler are enabled by default start from 3.8.3
    router.routeWithRegex(regex).handler(createBodyHandler());
    router.routeWithRegex(regex).failureHandler(this::onFailure).handler(this::onRequest);
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
    String path = context.request().path().substring(prefix.length() + 1);

    requestByFilter(context, microserviceName, path,
        new EncryptContext(hcr, userId));
  }

  protected void requestByFilter(RoutingContext context, String microserviceName, String path
      , EncryptContext encryptContext) {
    HttpServletRequestEx requestEx = new VertxServerRequestToHttpServletRequest(context);
    HttpServletResponseEx responseEx = new VertxServerResponseToHttpServletResponse(context.response());
    InvocationCreator creator = new EdgeInvocationCreator(context, requestEx, responseEx,
        microserviceName, path) {
      @Override
      public CompletableFuture<Invocation> createAsync() {
        CompletableFuture<Invocation> result = super.createAsync();
        return result.whenComplete((invocation, throwable)
            -> {
          if (throwable == null) {
            invocation.getHandlerContext().put(EdgeConst.ENCRYPT_CONTEXT, encryptContext);
          }
        });
      }
    };
    new RestProducerInvocationFlow(creator, requestEx, responseEx)
        .run();
  }
}
