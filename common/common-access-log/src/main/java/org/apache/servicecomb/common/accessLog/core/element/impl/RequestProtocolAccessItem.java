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


import org.apache.servicecomb.common.accessLog.core.element.AccessLogItem;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.RoutingContext;

public class RequestProtocolAccessItem implements AccessLogItem<RoutingContext> {
  public static final String EMPTY_RESULT = "-";

  @Override
  public void appendServerFormattedItem(ServerAccessLogEvent accessLogEvent, StringBuilder builder) {
    HttpServerRequest request = accessLogEvent.getRoutingContext().request();
    if (null == request || null == request.version()) {
      builder.append(EMPTY_RESULT);
      return;
    }
    builder.append(getStringVersion(request.version()));
  }

  @Override
  public void appendClientFormattedItem(InvocationFinishEvent finishEvent, StringBuilder builder) {
    Invocation invocation = finishEvent.getInvocation();
    if (invocation == null || null == invocation.getEndpoint() || null == invocation.getEndpoint().getAddress()
        || !(invocation.getEndpoint().getAddress() instanceof URIEndpointObject)
        || !((URIEndpointObject) invocation.getEndpoint().getAddress()).isHttp2Enabled()) {
      builder.append("HTTP/1.1");
      return;
    }
    builder.append("HTTP/2.0");
  }

  private String getStringVersion(HttpVersion version) {
    switch (version) {
      case HTTP_2:
        return "HTTP/2.0";
      case HTTP_1_0:
        return "HTTP/1.0";
      case HTTP_1_1:
        return "HTTP/1.1";
      default:
        return EMPTY_RESULT;
    }
  }
}
