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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response.Status;

import io.servicecomb.common.rest.filter.HttpServerFilter;
import io.servicecomb.core.Invocation;
import io.servicecomb.swagger.invocation.Response;

public class ServerSignature implements HttpServerFilter {
  public ServerSignature() {
  }

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public Response afterReceiveRequest(Invocation invocation, HttpServletRequest request) {
    try {
      String signature = SignatureUtils.genSignature(request.getRequestURI(), invocation.getContext(), request);
      String clientSignature = invocation.getContext("signature");
      if (!signature.equals(clientSignature)) {
        return Response.create(Status.UNAUTHORIZED, "signature failed");
      }

    } catch (IOException e) {
      return Response.producerFailResp(e);
    }

    return null;
  }

  @Override
  public void beforeSendResponse(Invocation invocation, HttpServletResponse response, byte[] bodyBytes, int length) {
    String signature = SignatureUtils.genSignature(bodyBytes, length);
    response.addHeader("signature", signature + "wjm");
  }
}
