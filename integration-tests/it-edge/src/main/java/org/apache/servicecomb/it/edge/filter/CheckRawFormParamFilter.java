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

package org.apache.servicecomb.it.edge.filter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

/**
 * used for DataTypeJaxrsSchema#stringUrlencodedForm(Map)
 */
public class CheckRawFormParamFilter implements HttpServerFilter {

  @Override
  public int getOrder() {
    return 0;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Response afterReceiveRequest(Invocation invocation, HttpServletRequestEx requestEx) {
    if (!"paramCodec.stringUrlencodedForm".equals(invocation.getOperationMeta().getSchemaQualifiedName())) {
      return null;
    }
    final Object swaggerArgument = invocation.getInvocationArgument("requestMap");

    if (!(swaggerArgument instanceof Map)) {
      return Response.failResp(new InvocationException(Status.BAD_REQUEST, "param is not map"));
    }
    return checkRequestType((Map<Object, Object>) swaggerArgument);
  }

  @Override
  public CompletableFuture<Void> beforeSendResponseAsync(Invocation invocation, HttpServletResponseEx responseEx) {
    return CompletableFuture.completedFuture(null);
  }

  private Response checkRequestType(Map<Object, Object> swaggerArgument) {
    final Object valueA = swaggerArgument.get("A");
    if (null != valueA && !(valueA instanceof String)) {
      return Response.failResp(new InvocationException(Status.BAD_REQUEST, "valueA is not String"));
    }
    final Object valueB = swaggerArgument.get("B");
    if (null != valueB && !(valueB instanceof String)) {
      return Response.failResp(new InvocationException(Status.BAD_REQUEST, "valueB is not String"));
    }

    return null;
  }
}
