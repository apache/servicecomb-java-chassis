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

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.common.accessLog.core.element.AccessLogItem;
import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.codec.param.RestClientRequestImpl;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;

import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;

public class RequestHeaderAccessItem implements AccessLogItem<RoutingContext> {
  public static final String RESULT_NOT_FOUND = "-";

  private final String varName;

  public RequestHeaderAccessItem(String varName) {
    this.varName = varName;
  }

  @Override
  public void appendServerFormattedItem(ServerAccessLogEvent accessLogEvent, StringBuilder builder) {
    MultiMap headers = accessLogEvent.getRoutingContext().request().headers();
    if (null == headers || StringUtils.isEmpty(headers.get(varName))) {
      builder.append(RESULT_NOT_FOUND);
      return;
    }
    builder.append(headers.get(varName));
  }

  @Override
  public void appendClientFormattedItem(InvocationFinishEvent clientLogEvent, StringBuilder builder) {
    RestClientRequestImpl restRequestImpl = (RestClientRequestImpl) clientLogEvent.getInvocation().getHandlerContext()
        .get(RestConst.INVOCATION_HANDLER_REQUESTCLIENT);
    if (null == restRequestImpl || null == restRequestImpl.getRequest()
        || null == restRequestImpl.getRequest().headers()
        || StringUtils.isEmpty(restRequestImpl.getRequest().headers().get(varName))) {
      builder.append(RESULT_NOT_FOUND);
      return;
    }
    builder.append(restRequestImpl.getRequest().headers().get(varName));
  }

  public String getVarName() {
    return varName;
  }
}
