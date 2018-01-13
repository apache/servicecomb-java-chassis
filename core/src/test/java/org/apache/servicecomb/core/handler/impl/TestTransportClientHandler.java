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

package org.apache.servicecomb.core.handler.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.junit.Before;
import org.junit.Test;

public class TestTransportClientHandler {

  private final Endpoint endpoint = mock(Endpoint.class);

  private final TransportClientHandler transportClientHandler = TransportClientHandler.INSTANCE;

  private final Invocation invocation = mock(Invocation.class);

  private final AsyncResponse asyncResp = mock(AsyncResponse.class);

  private final Transport transport = mock(Transport.class);

  @Before
  public void setUp() throws Exception {
    when(transport.getEndpoint()).thenReturn(endpoint);
    when(endpoint.getEndpoint()).thenReturn("rest://localhost:8080");
  }

  @Test
  public void test() throws Exception {
    when(invocation.getTransport()).thenReturn(transport);
    when(invocation.getEndpoint()).thenReturn(endpoint);
    transportClientHandler.handle(invocation, asyncResp);

    verify(transport).send(invocation, asyncResp);
  }
}
