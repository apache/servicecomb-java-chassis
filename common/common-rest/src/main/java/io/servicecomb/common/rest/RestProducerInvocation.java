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

package io.servicecomb.common.rest;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.common.rest.codec.RestCodec;
import io.servicecomb.common.rest.definition.RestOperationMeta;
import io.servicecomb.common.rest.filter.HttpServerFilter;
import io.servicecomb.common.rest.locator.OperationLocator;
import io.servicecomb.common.rest.locator.ServicePathManager;
import io.servicecomb.core.Const;
import io.servicecomb.core.CseContext;
import io.servicecomb.core.Transport;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.invocation.InvocationFactory;
import io.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import io.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.swagger.invocation.exception.InvocationException;

public class RestProducerInvocation extends AbstractRestInvocation {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestProducerInvocation.class);

  protected Transport transport;

  public void invoke(Transport transport, HttpServletRequestEx requestEx, HttpServletResponseEx responseEx,
      List<HttpServerFilter> httpServerFilters) {
    this.transport = transport;
    this.requestEx = requestEx;
    this.responseEx = responseEx;
    this.httpServerFilters = httpServerFilters;
    requestEx.setAttribute(RestConst.REST_REQUEST, requestEx);

    try {
      this.restOperationMeta = findRestOperation();
    } catch (InvocationException e) {
      sendFailResponse(e);
      return;
    }

    scheduleInvocation();
  }

  protected void scheduleInvocation() {
    OperationMeta operationMeta = restOperationMeta.getOperationMeta();
    operationMeta.getExecutor().execute(() -> {
      synchronized (this.requestEx) {
        try {
          if (requestEx.getAttribute(RestConst.REST_REQUEST) != requestEx) {
            // already timeout
            // in this time, request maybe recycled and reused by web container, do not use requestEx
            LOGGER.error("Rest request already timeout, abandon execute, method {}, operation {}.",
                operationMeta.getHttpMethod(),
                operationMeta.getMicroserviceQualifiedName());
            return;
          }

          requestEx.setAttribute(RestConst.REST_STATE_EXECUTING, true);
          runOnExecutor();
        } catch (Throwable e) {
          LOGGER.error("rest server onRequest error", e);
          sendFailResponse(e);
        }
      }
    });
  }

  protected void runOnExecutor() {
    Object[] args = RestCodec.restToArgs(requestEx, restOperationMeta);
    this.invocation = InvocationFactory.forProvider(transport.getEndpoint(),
        restOperationMeta.getOperationMeta(),
        args);
    invoke();
  }

  protected RestOperationMeta findRestOperation() {
    String targetMicroserviceName = requestEx.getHeader(Const.TARGET_MICROSERVICE);
    if (targetMicroserviceName == null) {
      // for compatible
      targetMicroserviceName = RegistryUtils.getMicroservice().getServiceName();
    }
    MicroserviceMeta selfMicroserviceMeta =
        CseContext.getInstance().getMicroserviceMetaManager().ensureFindValue(targetMicroserviceName);
    ServicePathManager servicePathManager = ServicePathManager.getServicePathManager(selfMicroserviceMeta);
    if (servicePathManager == null) {
      LOGGER.error("No schema in microservice");
      throw new InvocationException(Status.NOT_FOUND, Status.NOT_FOUND.getReasonPhrase());
    }

    OperationLocator locator =
        servicePathManager.producerLocateOperation(requestEx.getRequestURI(), requestEx.getMethod());
    requestEx.setAttribute(RestConst.PATH_PARAMETERS, locator.getPathVarMap());

    return locator.getOperation();
  }

  @Override
  protected void doInvoke() throws Throwable {
    invocation.next(resp -> {
      sendResponseQuietly(resp);
    });
  }
}
