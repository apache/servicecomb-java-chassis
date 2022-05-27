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
package org.apache.servicecomb.common.rest;

import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessor;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.context.TransportContext;

public class HttpTransportContext implements TransportContext {
  private final HttpServletRequestEx requestEx;

  private final HttpServletResponseEx responseEx;

  private final ProduceProcessor produceProcessor;

  public HttpTransportContext(HttpServletRequestEx requestEx, HttpServletResponseEx responseEx,
      ProduceProcessor produceProcessor) {
    this.requestEx = requestEx;
    this.responseEx = responseEx;
    this.produceProcessor = produceProcessor;
  }

  public HttpServletRequestEx getRequestEx() {
    return requestEx;
  }

  public HttpServletResponseEx getResponseEx() {
    return responseEx;
  }

  public ProduceProcessor getProduceProcessor() {
    return produceProcessor;
  }
}
