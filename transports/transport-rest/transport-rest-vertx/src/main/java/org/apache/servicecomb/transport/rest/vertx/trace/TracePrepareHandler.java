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

package org.apache.servicecomb.transport.rest.vertx.trace;

import org.apache.commons.lang.StringUtils;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.tracing.BraveTraceIdGenerator;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class TracePrepareHandler implements Handler<RoutingContext> {
  @Override
  public void handle(RoutingContext context) {
    String traceId = context.request().getHeader(Const.TRACE_ID_NAME);
    if (StringUtils.isEmpty(traceId)) {
      traceId = BraveTraceIdGenerator.INSTANCE.generateStringId();
      context.request().headers().add(Const.TRACE_ID_NAME, traceId);
    }

    context.next();
  }
}
