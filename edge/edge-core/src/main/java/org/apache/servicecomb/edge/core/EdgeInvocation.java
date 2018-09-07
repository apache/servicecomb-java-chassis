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

import java.util.List;

import org.apache.servicecomb.common.rest.AbstractRestInvocation;
import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.common.rest.locator.OperationLocator;
import org.apache.servicecomb.common.rest.locator.ServicePathManager;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.definition.MicroserviceVersionMeta;
import org.apache.servicecomb.core.invocation.InvocationFactory;
import org.apache.servicecomb.core.provider.consumer.ReactiveResponseExecutor;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.vertx.http.VertxServerRequestToHttpServletRequest;
import org.apache.servicecomb.foundation.vertx.http.VertxServerResponseToHttpServletResponse;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersionRule;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public class EdgeInvocation extends AbstractRestInvocation {
  public static final String EDGE_INVOCATION_CONTEXT = "edgeInvocationContext";

  protected String microserviceName;

  protected MicroserviceVersionRule microserviceVersionRule;

  protected MicroserviceVersionMeta latestMicroserviceVersionMeta;

  protected ReferenceConfig referenceConfig;

  protected String versionRule = DefinitionConst.VERSION_RULE_ALL;

  protected RoutingContext routingContext;

  public void init(String microserviceName, RoutingContext context, String path,
      List<HttpServerFilter> httpServerFilters) {
    this.microserviceName = microserviceName;
    this.requestEx = new VertxServerRequestToHttpServletRequest(context, path);
    this.responseEx = new VertxServerResponseToHttpServletResponse(context.response());
    this.routingContext = context;
    this.httpServerFilters = httpServerFilters;
    requestEx.setAttribute(RestConst.REST_REQUEST, requestEx);
  }

  public void edgeInvoke() {
    findMicroserviceVersionMeta();
    findRestOperation(latestMicroserviceVersionMeta.getMicroserviceMeta());

    scheduleInvocation();
  }

  protected void findMicroserviceVersionMeta() {
    String versionRule = chooseVersionRule();

    String appId = RegistryUtils.getAppId();
    int idxAt = microserviceName.indexOf(org.apache.servicecomb.serviceregistry.api.Const.APP_SERVICE_SEPARATOR);
    if (idxAt != -1) {
      appId = microserviceName.substring(0, idxAt);
    }

    microserviceVersionRule = RegistryUtils.getServiceRegistry()
        .getAppManager()
        .getOrCreateMicroserviceVersionRule(appId, microserviceName, versionRule);
    latestMicroserviceVersionMeta = microserviceVersionRule.getLatestMicroserviceVersion();

    if (latestMicroserviceVersionMeta == null) {
      throw new ServiceCombException(
          String.format("Failed to find latest MicroserviceVersionMeta, appId=%s, microserviceName=%s, versionRule=%s.",
              appId,
              microserviceName,
              versionRule));
    }
  }

  public void setVersionRule(String versionRule) {
    this.versionRule = versionRule;
  }

  // another possible rule:
  // path is: /msName/version/.....
  // version in path is v1 or v2 and so on
  // map version to VersionRule:
  //   v1->1.0.0-2.0.0
  //   v2->2.0.0-3.0.0
  // that means if a(1.x.x) bigger then b(1.y.y), then a compatible to b
  //        but a(2.x.x) not compatible to b   
  protected String chooseVersionRule() {
    // this will use all instance of the microservice
    // and this required all new version compatible to old version
    return versionRule;
  }

  @Override
  protected OperationLocator locateOperation(ServicePathManager servicePathManager) {
    return servicePathManager.consumerLocateOperation(requestEx.getRequestURI(), requestEx.getMethod());
  }

  @Override
  protected void createInvocation() {
    ReferenceConfig referenceConfig = new ReferenceConfig();
    referenceConfig.setMicroserviceVersionRule(microserviceVersionRule);
    referenceConfig.setTransport(Const.ANY_TRANSPORT);

    this.invocation = InvocationFactory.forConsumer(referenceConfig,
        restOperationMeta.getOperationMeta(),
        null);
    this.invocation.setSync(false);
    this.invocation.setEdge(true);
    this.invocation.getHandlerContext().put(EDGE_INVOCATION_CONTEXT, Vertx.currentContext());
    this.invocation.setResponseExecutor(new ReactiveResponseExecutor());
    this.routingContext.put(RestConst.REST_INVOCATION_CONTEXT, invocation);
  }
}
