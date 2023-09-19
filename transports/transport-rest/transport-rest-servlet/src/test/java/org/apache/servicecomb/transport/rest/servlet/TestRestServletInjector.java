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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration.Dynamic;
import mockit.Expectations;
import mockit.Mocked;

public class TestRestServletInjector {
  Environment environment = Mockito.mock(Environment.class);

  @Test
  public void testDefaultInjectEmptyUrlPattern(@Mocked ServletContext servletContext, @Mocked Dynamic dynamic) {
    new Expectations(ServletConfig.class) {
      {
        ServletConfig.getServletUrlPattern(environment);
        result = null;
      }
    };

    Assertions.assertNull(RestServletInjector.defaultInject(servletContext, environment));
  }

  @Test
  public void testDefaultInjectNotListen(@Mocked ServletContext servletContext,
      @Mocked Dynamic dynamic) throws UnknownHostException, IOException {
    try (ServerSocket ss = new ServerSocket(0, 0, InetAddress.getByName("127.0.0.1"))) {
      int port = ss.getLocalPort();

      new Expectations(ServletConfig.class) {
        {
          ServletConfig.getServletUrlPattern(environment);
          result = "/*";
          ServletConfig.getLocalServerAddress(environment);
          result = "127.0.0.1:" + port;
        }
      };
    }

    Assertions.assertNull(RestServletInjector.defaultInject(servletContext, environment));
  }

  @Test
  public void testDefaultInjectListen(@Mocked ServletContext servletContext,
      @Mocked Dynamic dynamic) throws UnknownHostException, IOException {
    try (ServerSocket ss = new ServerSocket(0, 0, InetAddress.getByName("127.0.0.1"))) {
      int port = ss.getLocalPort();

      new Expectations(ServletConfig.class) {
        {
          ServletConfig.getServletUrlPattern(environment);
          result = "/rest/*";
          ServletConfig.getLocalServerAddress(environment);
          result = "127.0.0.1:" + port;
        }
      };

      Assertions.assertEquals(dynamic, RestServletInjector.defaultInject(servletContext, environment));
    }
  }
}
