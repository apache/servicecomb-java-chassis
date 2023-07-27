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

package org.apache.servicecomb.demo.edge.service;

import java.util.Map;

import org.apache.servicecomb.common.rest.RestProducerInvocationFlow;
import org.apache.servicecomb.core.invocation.InvocationCreator;
import org.apache.servicecomb.edge.core.AbstractEdgeDispatcher;
import org.apache.servicecomb.edge.core.EdgeInvocationCreator;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.foundation.vertx.http.VertxServerRequestToHttpServletRequest;
import org.apache.servicecomb.foundation.vertx.http.VertxServerResponseToHttpServletResponse;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class EdgeDispatcher extends AbstractEdgeDispatcher {
  @Override
  public int getOrder() {
    return 10000;
  }

  @Override
  public void init(Router router) {
    String regex = "/api/([^\\\\/]+)/([^\\\\/]+)/(.*)";
    // cookies handler are enabled by default start from 3.8.3
    router.routeWithRegex(regex).handler(createBodyHandler());
    router.routeWithRegex(regex).failureHandler(this::onFailure).handler(this::onRequest);
  }

  protected void onRequest(RoutingContext context) {
    Map<String, String> pathParams = context.pathParams();
    String microserviceName = pathParams.get("param0");
    String pathVersion = pathParams.get("param1");
    String path = context.request().path().substring(4);

    requestByFilter(context, microserviceName, path);
  }

  protected void requestByFilter(RoutingContext context, String microserviceName, String path) {
    HttpServletRequestEx requestEx = new VertxServerRequestToHttpServletRequest(context);
    HttpServletResponseEx responseEx = new VertxServerResponseToHttpServletResponse(context.response());
    InvocationCreator creator = new EdgeInvocationCreator(context, requestEx, responseEx,
        microserviceName, path);
    new RestProducerInvocationFlow(creator, requestEx, responseEx)
        .run();
  }
}
