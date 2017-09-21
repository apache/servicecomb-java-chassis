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

package io.servicecomb.demo.signature;

import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.common.rest.filter.HttpServerFilter;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import io.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import io.servicecomb.swagger.invocation.Response;

public class ServerSignature implements HttpServerFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServerSignature.class);

  public ServerSignature() {
  }

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public boolean needCacheRequest(OperationMeta operationMeta) {
    return true;
  }

  @Override
  public Response afterReceiveRequest(Invocation invocation, HttpServletRequestEx requestEx) {
    String signature = SignatureUtils.genSignature(requestEx);
    String clientSignature = requestEx.getHeader("signature");
    LOGGER.info("check request signature, client: {}, server: {}.", clientSignature, signature);
    if (!signature.equals(clientSignature)) {
      LOGGER.error("check request signature failed");
      return Response.create(Status.UNAUTHORIZED, "check request signature failed");
    }

    return null;
  }

  @Override
  public void beforeSendResponse(Invocation invocation, HttpServletResponseEx responseEx) {
    String signature = SignatureUtils.genSignature(responseEx);
    responseEx.addHeader("signature", signature);
  }
}
