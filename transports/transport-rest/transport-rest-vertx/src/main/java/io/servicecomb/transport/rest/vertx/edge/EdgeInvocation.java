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

package io.servicecomb.transport.rest.vertx.edge;

import java.util.List;
import java.util.Map;

import io.servicecomb.common.rest.AbstractRestInvocation;
import io.servicecomb.common.rest.RestConst;
import io.servicecomb.common.rest.codec.RestCodec;
import io.servicecomb.common.rest.filter.HttpServerFilter;
import io.servicecomb.common.rest.locator.OperationLocator;
import io.servicecomb.common.rest.locator.ServicePathManager;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.invocation.InvocationFactory;
import io.servicecomb.core.provider.consumer.ReactiveResponseExecutor;
import io.servicecomb.core.provider.consumer.ReferenceConfig;
import io.servicecomb.core.provider.consumer.ReferenceConfigUtils;
import io.servicecomb.foundation.common.exceptions.ServiceCombException;
import io.servicecomb.foundation.vertx.http.VertxServerRequestToHttpServletRequest;
import io.servicecomb.foundation.vertx.http.VertxServerResponseToHttpServletResponse;
import io.vertx.ext.web.RoutingContext;

public class EdgeInvocation extends AbstractRestInvocation {
  protected String microserviceName;

  protected ReferenceConfig referenceConfig;

  public void init(String microserviceName, RoutingContext context, String path,
      List<HttpServerFilter> httpServerFilters) {
    this.microserviceName = microserviceName;
    this.requestEx = new VertxServerRequestToHttpServletRequest(context, path);
    this.responseEx = new VertxServerResponseToHttpServletResponse(context.response());
    this.httpServerFilters = httpServerFilters;
  }

  @Override
  protected void prepareInvoke() throws Throwable {
    prepareEdgeInvoke();

    super.prepareInvoke();
  }

  protected void prepareEdgeInvoke() throws Throwable {
    referenceConfig = ReferenceConfigUtils.getForInvoke(microserviceName);

    MicroserviceMeta microserviceMeta = referenceConfig.getMicroserviceMeta();
    ServicePathManager servicePathManager = ServicePathManager.getServicePathManager(microserviceMeta);
    if (servicePathManager == null) {
      throw new ServiceCombException(String.format("no schema defined for %s:%s",
          microserviceMeta.getAppId(),
          microserviceMeta.getName()));
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
