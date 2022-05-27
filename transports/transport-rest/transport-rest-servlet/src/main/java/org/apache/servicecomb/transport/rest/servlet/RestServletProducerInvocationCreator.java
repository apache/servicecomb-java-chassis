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
package org.apache.servicecomb.transport.rest.servlet;

import javax.annotation.Nonnull;

import org.apache.servicecomb.common.rest.HttpTransportContext;
import org.apache.servicecomb.common.rest.RestProducerInvocationCreator;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;

public class RestServletProducerInvocationCreator extends RestProducerInvocationCreator {
  public RestServletProducerInvocationCreator(@Nonnull MicroserviceMeta microserviceMeta, @Nonnull Endpoint endpoint,
      @Nonnull HttpServletRequestEx requestEx, @Nonnull HttpServletResponseEx responseEx) {
    super(microserviceMeta, endpoint, requestEx, responseEx);
  }

  @Override
  protected void initTransportContext(Invocation invocation) {
    HttpTransportContext transportContext = new HttpTransportContext(requestEx, responseEx, produceProcessor);
    invocation.setTransportContext(transportContext);
  }
}
