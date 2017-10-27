/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.edge.core;

import java.util.List;
import java.util.Map;

import io.servicecomb.common.rest.AbstractRestInvocation;
import io.servicecomb.common.rest.RestConst;
import io.servicecomb.common.rest.codec.RestCodec;
import io.servicecomb.common.rest.filter.HttpServerFilter;
import io.servicecomb.common.rest.locator.OperationLocator;
import io.servicecomb.common.rest.locator.ServicePathManager;
import io.servicecomb.core.Const;
import io.servicecomb.core.definition.MicroserviceVersionMeta;
import io.servicecomb.core.definition.MicroserviceVersionMetaFactory;
import io.servicecomb.core.definition.classloader.PrivateMicroserviceClassLoaderFactory;
import io.servicecomb.core.invocation.InvocationFactory;
import io.servicecomb.core.provider.consumer.ReactiveResponseExecutor;
import io.servicecomb.core.provider.consumer.ReferenceConfig;
import io.servicecomb.foundation.common.event.EventManager;
import io.servicecomb.foundation.common.exceptions.ServiceCombException;
import io.servicecomb.foundation.vertx.http.VertxServerRequestToHttpServletRequest;
import io.servicecomb.foundation.vertx.http.VertxServerResponseToHttpServletResponse;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.consumer.AppManager;
import io.servicecomb.serviceregistry.consumer.MicroserviceVersionRule;
import io.servicecomb.serviceregistry.definition.DefinitionConst;
import io.servicecomb.swagger.invocation.Response;
import io.vertx.ext.web.RoutingContext;

public class EdgeInvocation extends AbstractRestInvocation {
  // temp appManager, we will create a global appManager in the further
  private static AppManager appManager = new AppManager(EventManager.eventBus);
  static {
    appManager.setMicroserviceVersionFactory(
        new MicroserviceVersionMetaFactory(PrivateMicroserviceClassLoaderFactory.INSTANCE));
  }

  protected String microserviceName;

  protected MicroserviceVersionRule microserviceVersionRule;

  protected MicroserviceVersionMeta latestMicroserviceVersionMeta;

  protected ReferenceConfig referenceConfig;

  public void init(String microserviceName, RoutingContext context, String path,
      List<HttpServerFilter> httpServerFilters) {
    this.microserviceName = microserviceName;
    this.requestEx = new VertxServerRequestToHttpServletRequest(context, path);
    this.responseEx = new VertxServerResponseToHttpServletResponse(context.response());
    this.httpServerFilters = httpServerFilters;
  }

  @Override
  public void invoke() {
    findMicroserviceVersionMeta();

    ClassLoader orgClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(latestMicroserviceVersionMeta.getMicroserviceMeta().getClassLoader());
    try {
      // not handle edge unknown exception
      // all unknow exception will be handled by io.servicecomb.edge.core.AbstractEdgeDispatcher.onFailure(RoutingContext)
      prepareEdgeInvoke();
      Response response = prepareInvoke();
      if (response != null) {
        sendResponse(response);
        return;
      }
      doInvoke();
    } catch (Throwable e) {
      throw new ServiceCombException("unknown edge exception.", e);
    } finally {
      Thread.currentThread().setContextClassLoader(orgClassLoader);
    }
  }

  protected void findMicroserviceVersionMeta() {
    String versionRule = chooseVersionRule();

    String appId = RegistryUtils.getAppId();;
    int idxAt = microserviceName.indexOf(io.servicecomb.serviceregistry.api.Const.APP_SERVICE_SEPARATOR);
    if (idxAt != -1) {
      appId = microserviceName.substring(0, idxAt);
    }

    microserviceVersionRule = appManager.getOrCreateMicroserviceVersionRule(appId, microserviceName, versionRule);
    latestMicroserviceVersionMeta = microserviceVersionRule.getLatestMicroserviceVersion();

    if (latestMicroserviceVersionMeta == null) {
      throw new ServiceCombException(
          String.format("Failed to find latest MicroserviceVersionMeta, appId=%s, microserviceName=%s, versionRule=%s.",
              appId,
              microserviceName,
              versionRule));
    }
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
    return DefinitionConst.VERSION_RULE_ALL;
  }

  protected void prepareEdgeInvoke() throws Throwable {
    ReferenceConfig referenceConfig = new ReferenceConfig();
    referenceConfig.setMicroserviceMeta(latestMicroserviceVersionMeta.getMicroserviceMeta());
    referenceConfig.setMicroserviceVersionRule(microserviceVersionRule.getVersionRule().getVersionRule());
    referenceConfig.setTransport(Const.ANY_TRANSPORT);

    ServicePathManager servicePathManager =
        ServicePathManager.getServicePathManager(latestMicroserviceVersionMeta.getMicroserviceMeta());
    if (servicePathManager == null) {
      throw new ServiceCombException(String.format("no schema defined for %s:%s",
          latestMicroserviceVersionMeta.getMicroserviceMeta().getAppId(),
          latestMicroserviceVersionMeta.getMicroserviceMeta().getName()));
    }

    OperationLocator locator =
        servicePathManager.consumerLocateOperation(requestEx.getRequestURI(), requestEx.getMethod());
    this.restOperationMeta = locator.getOperation();

    Map<String, String> pathParams = locator.getPathVarMap();
    requestEx.setAttribute(RestConst.PATH_PARAMETERS, pathParams);

    Object args[] = RestCodec.restToArgs(requestEx, restOperationMeta);
    this.invocation = InvocationFactory.forConsumer(referenceConfig,
        restOperationMeta.getOperationMeta(),
        args);
  }

  @Override
  protected void doInvoke() throws Throwable {
    invocation.setResponseExecutor(new ReactiveResponseExecutor());
    invocation.next(resp -> {
      sendResponseQuietly(resp);
    });
  }
}
