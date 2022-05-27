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
package org.apache.servicecomb.demo.prefix;

import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// demonstrate access log using handler and using Invocation logger to log trace id
public class AccessLogHandler implements Handler {
  private static final Logger LOGGER
      = LoggerFactory.getLogger(AccessLogHandler.class);

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    invocation.getTraceIdLogger().info(LOGGER, "request for operation {} begin", invocation.getInvocationQualifiedName());
     invocation.next((resp) -> {
       invocation.getTraceIdLogger().info(LOGGER, "request for operation {} end", invocation.getInvocationQualifiedName());
       asyncResp.complete(resp);
     });
  }
}
