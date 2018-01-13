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
package org.apache.servicecomb.springboot.starter.transport;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.transport.rest.servlet.RestServlet;
import org.apache.servicecomb.transport.rest.servlet.RestServletInjector;
import org.apache.servicecomb.transport.rest.servlet.ServletConfig;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.netflix.config.DynamicPropertyFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NetUtils.class})
public class TestRestServletInitializer {

  private static final String LISTEN_ADDRESS = "127.0.0.1";

  private static final int TEST_PORT = 8080;

  @BeforeClass
  public static void beforeClass() {
    ArchaiusUtils.resetConfig();

    DynamicPropertyFactory.getInstance();
  }

  @AfterClass
  public static void afterClass() {
    ArchaiusUtils.resetConfig();
  }

  @Before
  public void setUp() throws Exception {
    Configuration configuration = (Configuration) DynamicPropertyFactory.getBackingConfigurationSource();
    configuration.clearProperty(ServletConfig.KEY_SERVLET_URL_PATTERN);
    configuration.setProperty(ServletConfig.KEY_CSE_REST_ADDRESS, LISTEN_ADDRESS);

    PowerMockito.mockStatic(NetUtils.class);
    PowerMockito.when(NetUtils.parseIpPortFromURI(anyString())).thenReturn(new IpPort(LISTEN_ADDRESS, TEST_PORT));
    PowerMockito.when(NetUtils.canTcpListen(anyObject(), anyInt())).thenReturn(false);
  }

  @Test
  public void testOnStartup() throws Exception {
    Configuration configuration = (Configuration) DynamicPropertyFactory.getBackingConfigurationSource();
    String urlPattern = "/rest/*";
    configuration.setProperty(ServletConfig.KEY_SERVLET_URL_PATTERN, urlPattern);

    ServletContext servletContext = mock(ServletContext.class);
    Dynamic dynamic = mock(Dynamic.class);
    when(servletContext.addServlet(RestServletInjector.SERVLET_NAME, RestServlet.class)).thenReturn(dynamic);

    RestServletInitializer restServletInitializer = new RestServletInitializer();
    restServletInitializer.setPort(TEST_PORT);
    restServletInitializer.onStartup(servletContext);

    verify(dynamic).setAsyncSupported(true);
    verify(dynamic).addMapping(urlPattern);
    verify(dynamic).setLoadOnStartup(0);
  }

  @Test
  public void testOnStartupWhenUrlPatternNotSet() throws ServletException {
    ServletContext servletContext = mock(ServletContext.class);
    Dynamic dynamic = mock(Dynamic.class);
    when(servletContext.addServlet(RestServletInjector.SERVLET_NAME, RestServlet.class)).thenReturn(dynamic);

    RestServletInitializer restServletInitializer = new RestServletInitializer();
    restServletInitializer.setPort(TEST_PORT);
    restServletInitializer.onStartup(servletContext);

    verify(dynamic).setAsyncSupported(true);
    verify(dynamic).addMapping(ServletConfig.DEFAULT_URL_PATTERN);
    verify(dynamic).setLoadOnStartup(0);
  }
}
