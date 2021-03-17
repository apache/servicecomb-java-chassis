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
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.invocation.InvocationCreator;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.foundation.vertx.http.VertxServerRequestToHttpServletRequest;
import org.apache.servicecomb.foundation.vertx.http.VertxServerResponseToHttpServletResponse;
import org.apache.servicecomb.registry.definition.DefinitionConst;

import com.google.common.annotations.VisibleForTesting;
import com.netflix.config.DynamicPropertyFactory;

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

  private static final String KEY_WITH_VERSION = "servicecomb.http.dispatcher.edge.default.withVersion";

  private static final String KEY_PREFIX_SEGMENT_COUNT = "servicecomb.http.dispatcher.edge.default.prefixSegmentCount";

  public static final String MICROSERVICE_NAME = "param0";

  public static final String VERSION = "param1";

  private CompatiblePathVersionMapper versionMapper = new CompatiblePathVersionMapper();

  private String prefix;

  private boolean withVersion;

  private int prefixSegmentCount;

  @Override
  public int getOrder() {
    return DynamicPropertyFactory.getInstance().getIntProperty(KEY_ORDER, 20_000).get();
  }

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance().getBooleanProperty(KEY_ENABLED, false).get();
  }

  @Override
  public void init(Router router) {
    prefix = DynamicPropertyFactory.getInstance().getStringProperty(KEY_PREFIX, "api").get();
    withVersion = DynamicPropertyFactory.getInstance().getBooleanProperty(KEY_WITH_VERSION, true).get();
    prefixSegmentCount = DynamicPropertyFactory.getInstance().getIntProperty(KEY_PREFIX_SEGMENT_COUNT, 1).get();
    String regex = generateRouteRegex(prefix, withVersion);

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
    String versionRule = extractVersionRule(context);
    String path = Utils.findActualPath(context.request().path(), prefixSegmentCount);

    if (isFilterChainEnabled()) {
      requestByFilter(context, microserviceName, versionRule, path);
      return;
    }

    requestByHandler(context, microserviceName, versionRule, path);
  }

  protected boolean isFilterChainEnabled() {
    return SCBEngine.getInstance().isFilterChainEnabled();
  }

  @Nullable
  private String extractMicroserviceName(RoutingContext context) {
    return context.pathParam(MICROSERVICE_NAME);
  }

  private String extractVersionRule(RoutingContext context) {
    if (withVersion) {
      String pathVersion = context.pathParam(VERSION);
      return versionMapper.getOrCreate(pathVersion).getVersionRule();
    }

    return DefinitionConst.VERSION_RULE_ALL;
  }

  protected void requestByFilter(RoutingContext context, String microserviceName, String versionRule, String path) {
    HttpServletRequestEx requestEx = new VertxServerRequestToHttpServletRequest(context);
    HttpServletResponseEx responseEx = new VertxServerResponseToHttpServletResponse(context.response());
    InvocationCreator creator = new EdgeInvocationCreator(context, requestEx, responseEx,
        microserviceName, versionRule, path);
    new RestProducerInvocationFlow(creator, requestEx, responseEx)
        .run();
  }

  private void requestByHandler(RoutingContext context, String microserviceName, String versionRule, String path) {
    EdgeInvocation edgeInvocation = createEdgeInvocation();
    edgeInvocation.setVersionRule(versionRule);
    edgeInvocation.init(microserviceName, context, path, httpServerFilters);
    edgeInvocation.edgeInvoke();
  }
}
