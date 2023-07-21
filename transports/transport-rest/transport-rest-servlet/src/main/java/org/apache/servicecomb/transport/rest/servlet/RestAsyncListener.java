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

package org.apache.servicecomb.transport.rest.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

public class RestAsyncListener implements AsyncListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestAsyncListener.class);

  private static String TIMEOUT_MESSAGE;

  static {
    try {
      TIMEOUT_MESSAGE = JsonUtils.writeValueAsString(new CommonExceptionData("Timeout when processing the request."));
    } catch (JsonProcessingException e) {
      LOGGER.error("Failed to init timeout message.", e);
    }
  }

  @Override
  public void onComplete(AsyncEvent event) throws IOException {
  }

  @Override
  public void onTimeout(AsyncEvent event) throws IOException {
    // in this time, maybe:
    // 1.invocation in executor's queue
    // 2.already executing in executor
    // 3.already send response
    // to avoid concurrent, must lock request
    ServletRequest request = event.getSuppliedRequest();
    HttpServletRequestEx requestEx = (HttpServletRequestEx) request.getAttribute(RestConst.REST_REQUEST);
    LOGGER.error("Rest request timeout, method {}, path {}.", requestEx.getMethod(), requestEx.getRequestURI());

    // Waiting till executing in executor done. This operation may block container pool and make timeout requests in executor's
    // queue getting executed, and will cause bad performance. So default timeout is setting to -1 to disable timeout.
    synchronized (requestEx) {
      ServletResponse response = event.getAsyncContext().getResponse();
      if (!response.isCommitted()) {
        // invocation in executor's queue
        response.setContentType(MediaType.APPLICATION_JSON);

        // we don't know if developers declared one statusCode in contract
        // so we use cse inner statusCode here
        ((HttpServletResponse) response).setStatus(Status.INTERNAL_SERVER_ERROR.getStatusCode());
        PrintWriter out = response.getWriter();
        out.write(TIMEOUT_MESSAGE);
        response.flushBuffer();
      }

      request.removeAttribute(RestConst.REST_REQUEST);
    }

    LOGGER.error("Rest request timeout committed, method {}, path {}.", requestEx.getMethod(), requestEx.getRequestURI());
  }

  @Override
  public void onError(AsyncEvent event) throws IOException {
    // 未使用
  }

  @Override
  public void onStartAsync(AsyncEvent event) throws IOException {
    // 未使用
  }
}
