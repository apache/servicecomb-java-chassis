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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import org.junit.jupiter.api.Assertions;

public class TestServletRestDispatcher {
  ServletRestDispatcher dispatcher = new ServletRestDispatcher();

  @Mocked
  HttpServletRequest request;

  @Mocked
  HttpServletResponse response;

  @Mocked
  TransportManager transportManager;

  @Before
  public void setup() {
    SCBBootstrap.createSCBEngineForTest().setTransportManager(transportManager);
  }

  @After
  public void teardown() {
    SCBEngine.getInstance().destroy();
  }

  @Test
  public void service() {
    Holder<Boolean> handled = new Holder<>();

    new MockUp<RestServletProducerInvocation>() {
      @Mock
      void invoke(Transport transport, HttpServletRequestEx requestEx, HttpServletResponseEx responseEx,
          List<HttpServerFilter> httpServerFilters) {
        handled.value = true;
      }
    };

    dispatcher.service(request, response);

    Assertions.assertTrue(handled.value);
  }
}
