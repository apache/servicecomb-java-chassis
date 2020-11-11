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
package org.apache.servicecomb.match;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.match.marker.GovHttpRequest;
import org.apache.servicecomb.match.service.MatchersService;
import org.apache.servicecomb.match.service.MatchersServiceImpl;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;

public class RequestMarkHandler implements Handler {

  private MatchersService matchersService = new MatchersServiceImpl();

  public static final String MARK_KEY = "X-Mark";

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    if (invocation.getHandlerIndex() > 0) {
      invocation.next(asyncResp);
      return;
    }
    HttpServletRequestEx req = invocation.getRequestEx();
    if (req == null) {
      invocation.next(asyncResp);
      return;
    }
    GovHttpRequest govReq = new GovHttpRequest();
    govReq.setHeaders(covertMap(req));
    govReq.setUri(req.getPathInfo());
    govReq.setMethod(req.getMethod());
    // 获取流量标记到的 match
    List<String> match = matchersService.getMatchStr(govReq);
    invocation.getContext().put(MARK_KEY, String.join(",", match));
    // invocation.next(asyncResp);
  }

  private Map<String, String> covertMap(HttpServletRequestEx req) {
    Enumeration<String> headerKeys = req.getHeaderNames();
    Map<String, String> headerMap = new HashMap<>();
    while (headerKeys.hasMoreElements()) {
      String key = headerKeys.nextElement();
      headerMap.put(key, req.getHeader(key));
    }
    return headerMap;
  }
}
