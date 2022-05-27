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

package org.apache.servicecomb.springboot2.starter.servlet;

import java.io.IOException;
import java.net.ServerSocket;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.transport.rest.servlet.ServletConfig;
import org.apache.servicecomb.transport.rest.servlet.ServletUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.server.AbstractConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;

@Component
public class RestServletInitializer
    implements WebServerFactoryCustomizer<AbstractConfigurableWebServerFactory>, ServletContextInitializer {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestServletInitializer.class);

  private AbstractConfigurableWebServerFactory factory = null;

  @Override
  public void customize(AbstractConfigurableWebServerFactory factory) {
    this.factory = factory;
  }

  @Override
  @SuppressWarnings("try")
  public void onStartup(ServletContext servletContext) throws ServletException {
    if (StringUtils.isEmpty(ServletConfig.getServletUrlPattern())) {
      // ensure the servlet will be instantiated
      Configuration configuration = (Configuration) DynamicPropertyFactory.getBackingConfigurationSource();
      configuration.setProperty(ServletConfig.KEY_SERVLET_URL_PATTERN, ServletConfig.DEFAULT_URL_PATTERN);
    }

    if (this.factory == null) {
      // when running in external tomcat, WebServerFactoryCustomizer will not be available, but now tomcat
      // is already listening and we can call ServletUtils.init directly.
      ServletUtils.init(servletContext);
      return;
    }

    if (factory.getPort() == 0) {
      LOGGER.warn(
          "spring boot embedded web container listen port is 0, ServiceComb will not use container's port to handler REST request.");
      return;
    }

    // when running in embedded tomcat, web container did not listen now. Call ServletUtils.init needs server is ready,
    // so mock to listen, and then close.
    try (ServerSocket ss = new ServerSocket(factory.getPort(), 0, factory.getAddress())) {
      ServletUtils.init(servletContext);
    } catch (IOException e) {
      throw new ServletException(e);
    }
  }
}
