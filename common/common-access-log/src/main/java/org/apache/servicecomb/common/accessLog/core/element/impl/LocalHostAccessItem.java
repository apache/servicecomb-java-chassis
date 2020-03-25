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

import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

public class LocalHostAccessItem implements AccessLogItem<RoutingContext> {
  public static final String EMPTY_RESULT = "-";

  @Override
  public void appendServerFormattedItem(ServerAccessLogEvent accessLogEvent, StringBuilder builder) {
    builder.append(accessLogEvent.getLocalAddress());
  }

  /**
   * client do not need localhost
   */
  @Override
  public void appendClientFormattedItem(InvocationFinishEvent finishEvent, StringBuilder builder) {
    RestClientRequestImpl restRequestImpl = (RestClientRequestImpl) finishEvent.getInvocation().getHandlerContext()
        .get(RestConst.INVOCATION_HANDLER_REQUESTCLIENT);
    if (null == restRequestImpl || null == restRequestImpl.getRequest()
        || null == restRequestImpl.getRequest().connection()
        || null == restRequestImpl.getRequest().connection().localAddress()
        || StringUtils.isEmpty(restRequestImpl.getRequest().connection().localAddress().host())) {
      builder.append(EMPTY_RESULT);
      return;
    }
    builder.append(restRequestImpl.getRequest().connection().localAddress().host());
  }

  public static String getLocalAddress(RoutingContext context) {
    HttpServerRequest request = context.request();
    if (null == request || null == request.localAddress() || StringUtils.isEmpty(request.localAddress().host())) {
      return EMPTY_RESULT;
    }
    return request.localAddress().host();
  }
}
