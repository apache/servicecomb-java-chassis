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

package org.apache.servicecomb.edge.core;

import org.apache.servicecomb.common.rest.RestProducerInvocationFlow;
import org.apache.servicecomb.core.invocation.InvocationCreator;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.foundation.vertx.http.VertxServerRequestToHttpServletRequest;
import org.apache.servicecomb.foundation.vertx.http.VertxServerResponseToHttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.google.common.annotations.VisibleForTesting;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Provide an easy mapping dispatcher that starts with a common prefix pattern.
 */
public class DefaultEdgeDispatcher extends AbstractEdgeDispatcher {

  private static final String KEY_ENABLED = "servicecomb.http.dispatcher.edge.default.enabled";

  private static final String KEY_ORDER = "servicecomb.http.dispatcher.edge.default.order";

  private static final String KEY_PREFIX = "servicecomb.http.dispatcher.edge.default.prefix";

  private static final String KEY_PREFIX_SEGMENT_COUNT = "servicecomb.http.dispatcher.edge.default.prefixSegmentCount";

  public static final String MICROSERVICE_NAME = "param0";

  private int prefixSegmentCount;

  private Environment environment;

  // though this is an SPI, but add as beans.
  @Autowired
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Override
  public int getOrder() {
    // can not use environment, add as beans is later than instantiate SPI
    return LegacyPropertyFactory.getIntProperty(KEY_ORDER, 20_000);
  }

  @Override
  public boolean enabled() {
    return environment.getProperty(KEY_ENABLED, boolean.class, false);
  }

  @Override
  public void init(Router router) {
    String prefix = environment.getProperty(KEY_PREFIX, "api");
    prefixSegmentCount = environment.getProperty(KEY_PREFIX_SEGMENT_COUNT, int.class, 1);
    String regex = generateRouteRegex(prefix, false);

    // cookies handler are enabled by default start from 3.8.3
    router.routeWithRegex(regex).handler(createBodyHandler());
    router.routeWithRegex(regex).failureHandler(this::onFailure).handler(this::onRequest);
  }

  @VisibleForTesting
  String generateRouteRegex(String prefix, boolean withVersion) {
    String version = withVersion ? "/([^\\\\/]+)" : "";
    return String.format("/%s/([^\\\\/]+)%s/(.*)", prefix, version);
  }

  protected void onRequest(RoutingContext context) {
    String microserviceName = extractMicroserviceName(context);
    String path = Utils.findActualPath(context.request().path(), prefixSegmentCount);

    requestByFilter(context, microserviceName, path);
  }

  @Nullable
  private String extractMicroserviceName(RoutingContext context) {
    return context.pathParam(MICROSERVICE_NAME);
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
