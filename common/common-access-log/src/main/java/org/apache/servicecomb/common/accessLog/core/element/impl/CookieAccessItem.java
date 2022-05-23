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

package org.apache.servicecomb.common.accessLog.core.element.impl;

import java.util.Map.Entry;
import java.util.Set;

import org.apache.servicecomb.common.accessLog.core.element.AccessLogItem;
import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.codec.param.RestClientRequestImpl;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;

import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;

public class CookieAccessItem implements AccessLogItem<RoutingContext> {

  public static final String RESULT_NOT_FOUND = "-";

  private final String varName;

  public CookieAccessItem(String varName) {
    this.varName = varName;
  }

  @Override
  public void appendServerFormattedItem(ServerAccessLogEvent accessLogEvent, StringBuilder builder) {
    Set<Cookie> cookies = accessLogEvent.getRoutingContext().request().cookies();
    if (null == cookies) {
      builder.append(RESULT_NOT_FOUND);
      return;
    }
    for (Cookie cookie : cookies) {
      if (varName.equals(cookie.getName())) {
        builder.append(cookie.getValue());
        return;
      }
    }
    builder.append(RESULT_NOT_FOUND);
  }

  @Override
  public void appendClientFormattedItem(InvocationFinishEvent finishEvent, StringBuilder builder) {
    RestClientRequestImpl restRequestImpl = (RestClientRequestImpl) finishEvent.getInvocation().getHandlerContext()
      .get(RestConst.INVOCATION_HANDLER_REQUESTCLIENT);
    if (null == restRequestImpl || null == restRequestImpl.getCookieMap()) {
      builder.append(RESULT_NOT_FOUND);
      return;
    }
    for (Entry<String, String> entry : restRequestImpl.getCookieMap().entrySet()) {
      if (entry.getKey().equals(varName)) {
        builder.append(entry.getValue());
        return;
      }
    }
    builder.append(RESULT_NOT_FOUND);
  }

  public String getVarName() {
    return varName;
  }
}
