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
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;
import org.apache.servicecomb.swagger.invocation.Response;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class ResponseHeaderAccessItem implements AccessLogItem<RoutingContext> {

  public static final String RESULT_NOT_FOUND = "-";

  private final String varName;

  public ResponseHeaderAccessItem(String varName) {
    this.varName = varName;
  }

  @Override
  public void appendServerFormattedItem(ServerAccessLogEvent accessLogEvent, StringBuilder builder) {
    HttpServerResponse response = accessLogEvent.getRoutingContext().response();
    if (null == response || null == response.headers() || StringUtils.isEmpty(response.headers().get(varName))) {
      builder.append(RESULT_NOT_FOUND);
      return;
    }
    builder.append(response.headers().get(varName));
  }

  @Override
  public void appendClientFormattedItem(InvocationFinishEvent finishEvent, StringBuilder builder) {
    Response response = finishEvent.getResponse();
    String value = response != null ? response.getHeader(varName) : null;
    if (null == value) {
      builder.append(RESULT_NOT_FOUND);
      return;
    }
    builder.append(value);
  }

  public String getVarName() {
    return varName;
  }
}
