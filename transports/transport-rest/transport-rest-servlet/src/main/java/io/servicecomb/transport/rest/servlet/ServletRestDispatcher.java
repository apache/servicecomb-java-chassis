/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.transport.rest.servlet;

import java.util.List;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.servicecomb.common.rest.filter.HttpServerFilter;
import io.servicecomb.core.Const;
import io.servicecomb.core.CseContext;
import io.servicecomb.core.Transport;
import io.servicecomb.foundation.common.utils.SPIServiceUtils;
import io.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import io.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import io.servicecomb.foundation.vertx.http.StandardHttpServletRequestEx;
import io.servicecomb.foundation.vertx.http.StandardHttpServletResponseEx;

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
