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

import java.util.List;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.foundation.vertx.http.StandardHttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.StandardHttpServletResponseEx;

public class ServletRestDispatcher {
  private RestAsyncListener restAsyncListener = new RestAsyncListener();

  private Transport transport;

  private List<HttpServerFilter> httpServerFilters = SPIServiceUtils.getSortedService(HttpServerFilter.class);

  public void service(HttpServletRequest request, HttpServletResponse response) {
    if (transport == null) {
      transport = CseContext.getInstance().getTransportManager().findTransport(Const.RESTFUL);
    }

    // 异步场景
    final AsyncContext asyncCtx = request.startAsync();
    asyncCtx.addListener(restAsyncListener);
    asyncCtx.setTimeout(ServletConfig.getServerTimeout());

    HttpServletRequestEx requestEx = new StandardHttpServletRequestEx(request);
    HttpServletResponseEx responseEx = new StandardHttpServletResponseEx(response);

    RestServletProducerInvocation restProducerInvocation = new RestServletProducerInvocation();
    restProducerInvocation.invoke(transport, requestEx, responseEx, httpServerFilters);
  }
}
