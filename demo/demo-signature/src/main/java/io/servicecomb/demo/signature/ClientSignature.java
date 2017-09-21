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

import io.servicecomb.common.rest.filter.HttpClientFilter;
import io.servicecomb.core.Invocation;
import io.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import io.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import io.servicecomb.swagger.invocation.Response;

public class ClientSignature implements HttpClientFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientSignature.class);

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public void beforeSendRequest(Invocation invocation, HttpServletRequestEx requestEx) {
    String signature = SignatureUtils.genSignature(requestEx);
    requestEx.setHeader("signature", signature);
  }

  @Override
  public Response afterReceiveResponse(Invocation invocation, HttpServletResponseEx responseEx) {
    String signature = SignatureUtils.genSignature(responseEx);
    String serverSignature = responseEx.getHeader("signature");

    if (serverSignature != null) {
      LOGGER.info("check response signature, client: {}, server: {}.", signature, serverSignature);
      if (!signature.equals(serverSignature)) {
        LOGGER.error("check response signature failed");
        return Response.create(Status.UNAUTHORIZED, "check response signature failed");
      }
    }
    return null;
  }
}
