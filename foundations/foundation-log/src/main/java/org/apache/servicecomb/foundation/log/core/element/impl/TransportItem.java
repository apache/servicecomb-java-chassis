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

package org.apache.servicecomb.foundation.log.core.element.impl;


import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;
import org.apache.servicecomb.foundation.log.core.element.LogItem;

import io.vertx.ext.web.RoutingContext;

public class TransportItem implements LogItem<RoutingContext> {
  private static final String EMPTY_STR = "-";

  @Override
  public void appendFormattedItem(ServerAccessLogEvent accessLogEvent, StringBuilder builder) {
    builder.append(EMPTY_STR);
  }

  @Override
  public void appendFormattedItem(InvocationFinishEvent finishEvent, StringBuilder builder) {
    String transportName = finishEvent.getInvocation().getConfigTransportName();
    if (!StringUtils.isEmpty(transportName)) {
      builder.append(transportName);
      return;
    }
    Endpoint endpoint = finishEvent.getInvocation().getEndpoint();
    if (endpoint == null || StringUtils.isEmpty(endpoint.getEndpoint())) {
      builder.append(EMPTY_STR);
      return;
    }
    builder.append(endpoint.getEndpoint().split(":")[0]);
  }
}
