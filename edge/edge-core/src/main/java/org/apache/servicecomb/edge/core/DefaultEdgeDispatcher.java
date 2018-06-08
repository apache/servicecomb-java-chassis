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

import java.util.Map;

import com.netflix.config.DynamicPropertyFactory;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CookieHandler;

/**
 * Provide an easy mapping dispatcher that starts with a common prefix pattern.
 */
public class DefaultEdgeDispatcher extends AbstractEdgeDispatcher {
  private static final String KEY_ENABLED = "servicecomb.http.dispatcher.edge.default.enabled";

  private static final String KEY_PREFIX = "servicecomb.http.dispatcher.edge.default.prefix";

  private static final String KEY_WITH_VERSION = "servicecomb.http.dispatcher.edge.default.withVersion";

  private static final String KEY_PREFIX_SEGMENT_COUNT = "servicecomb.http.dispatcher.edge.default.prefixSegmentCount";

  private CompatiblePathVersionMapper versionMapper = new CompatiblePathVersionMapper();

  private String prefix;

  private boolean withVersion;

  private int prefixSegmentCount;

  @Override
  public int getOrder() {
    return 20000;
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
    String regex;
    if (withVersion) {
      regex = "/" + prefix + "/([^\\\\/]+)/([^\\\\/]+)/(.*)";
    } else {
      regex = "/" + prefix + "/([^\\\\/]+)/(.*)";
    }
    router.routeWithRegex(regex).handler(CookieHandler.create());
    router.routeWithRegex(regex).handler(createBodyHandler());
    router.routeWithRegex(regex).failureHandler(this::onFailure).handler(this::onRequest);
  }

  protected void onRequest(RoutingContext context) {
    Map<String, String> pathParams = context.pathParams();
    String microserviceName = pathParams.get("param0");
    String path = Utils.findActualPath(context.request().path(), prefixSegmentCount);

    EdgeInvocation edgeInvocation = new EdgeInvocation();
    if (withVersion) {
      String pathVersion = pathParams.get("param1");
      edgeInvocation.setVersionRule(versionMapper.getOrCreate(pathVersion).getVersionRule());
    }
    edgeInvocation.init(microserviceName, context, path, httpServerFilters);
    edgeInvocation.edgeInvoke();
  }
}
