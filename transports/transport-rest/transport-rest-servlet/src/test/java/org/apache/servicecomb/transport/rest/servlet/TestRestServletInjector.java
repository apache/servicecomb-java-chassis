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

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration.Dynamic;

import org.junit.Assert;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

public class TestRestServletInjector {
  @Test
  public void testDefaultInjectEmptyUrlPattern(@Mocked ServletContext servletContext, @Mocked Dynamic dynamic) {
    new Expectations(ServletConfig.class) {
      {
        ServletConfig.getServletUrlPattern();
        result = null;
      }
    };

    Assert.assertEquals(null, RestServletInjector.defaultInject(servletContext));
  }

  @Test
  public void testDefaultInjectNotListen(@Mocked ServletContext servletContext,
      @Mocked Dynamic dynamic) throws UnknownHostException, IOException {
    try (ServerSocket ss = new ServerSocket(0, 0, InetAddress.getByName("127.0.0.1"))) {
      int port = ss.getLocalPort();

      new Expectations(ServletConfig.class) {
        {
          ServletConfig.getServletUrlPattern();
          result = "/*";
          ServletConfig.getLocalServerAddress();
          result = "127.0.0.1:" + port;
        }
      };
    }

    Assert.assertEquals(null, RestServletInjector.defaultInject(servletContext));
  }

  @Test
  public void testDefaultInjectListen(@Mocked ServletContext servletContext,
      @Mocked Dynamic dynamic) throws UnknownHostException, IOException {
    try (ServerSocket ss = new ServerSocket(0, 0, InetAddress.getByName("127.0.0.1"))) {
      int port = ss.getLocalPort();

      new Expectations(ServletConfig.class) {
        {
          ServletConfig.getServletUrlPattern();
          result = "/rest/*";
          ServletConfig.getLocalServerAddress();
          result = "127.0.0.1:" + port;
        }
      };

      Assert.assertEquals(dynamic, RestServletInjector.defaultInject(servletContext));
    }
  }
}
