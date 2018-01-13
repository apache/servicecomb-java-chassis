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

import java.util.Arrays;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration.Dynamic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class RestServletInjector {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestServletInjector.class);

  public static final String SERVLET_NAME = "ServicecombRestServlet";

  public static Dynamic defaultInject(ServletContext servletContext) {
    RestServletInjector injector = new RestServletInjector();

    String urlPattern = ServletConfig.getServletUrlPattern();
    return injector.inject(servletContext, urlPattern);
  }

  public Dynamic inject(ServletContext servletContext, String urlPattern) {
    String[] urlPatterns = splitUrlPattern(urlPattern);
    if (urlPatterns.length == 0) {
      LOGGER.warn("urlPattern is empty, ignore register {}.", SERVLET_NAME);
      return null;
    }

    String listenAddress = ServletConfig.getLocalServerAddress();
    if (!ServletUtils.canPublishEndpoint(listenAddress)) {
      LOGGER.warn("ignore register {}.", SERVLET_NAME);
      return null;
    }

    // dynamic deploy a servlet to handle serviceComb RESTful request
    Dynamic dynamic = servletContext.addServlet(SERVLET_NAME, RestServlet.class);
    dynamic.setAsyncSupported(true);
    dynamic.addMapping(urlPatterns);
    dynamic.setLoadOnStartup(0);
    LOGGER.info("RESTful servlet url pattern: {}.", Arrays.toString(urlPatterns));

    return dynamic;
  }

  private String[] splitUrlPattern(String urlPattern) {
    if (StringUtils.isEmpty(urlPattern)) {
      return new String[] {};
    }

    return ServletUtils.filterUrlPatterns(urlPattern);
  }
}
