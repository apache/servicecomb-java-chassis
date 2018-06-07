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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;

import org.apache.servicecomb.common.rest.UploadConfig;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.apache.servicecomb.serviceregistry.api.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class ServletUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServletUtils.class);

  public static boolean canPublishEndpoint(String listenAddress) {
    if (StringUtils.isEmpty(listenAddress)) {
      LOGGER.info("listenAddress is null, can not publish.");
      return false;
    }

    IpPort ipPort = NetUtils.parseIpPortFromURI("http://" + listenAddress);
    if (ipPort == null) {
      LOGGER.info("invalid listenAddress {}, can not publish, format should be ip:port.", listenAddress);
      return false;
    }

    if (NetUtils.canTcpListen(ipPort.getSocketAddress().getAddress(), ipPort.getPort())) {
      LOGGER.info("{} is not listened, can not publish.", ipPort.getSocketAddress());
      return false;
    }

    return true;
  }

  // we only support path prefix rule
  // other invalid urlPattern will be check by web container, we do not handle that
  static void checkUrlPattern(String urlPattern) {
    if (!urlPattern.startsWith("/")) {
      throw new ServiceCombException("only support rule like /* or /path/* or /path1/path2/* and so on.");
    }

    int idx = urlPattern.indexOf("/*");
    if (idx < 0 || (idx >= 0 && idx != urlPattern.length() - 2)) {
      throw new ServiceCombException("only support rule like /* or /path/* or /path1/path2/* and so on.");
    }
  }

  static String[] filterUrlPatterns(String... urlPatterns) {
    return filterUrlPatterns(Arrays.asList(urlPatterns));
  }

  static String[] filterUrlPatterns(Collection<String> urlPatterns) {
    return urlPatterns.stream()
        .filter(pattern -> !pattern.trim().isEmpty())
        .filter(pattern -> {
          checkUrlPattern(pattern.trim());
          return true;
        })
        .toArray(String[]::new);
  }

  static String[] collectUrlPatterns(ServletContext servletContext, Class<?> servletCls) {
    List<ServletRegistration> servlets = findServletRegistrations(servletContext, servletCls);
    if (servlets.isEmpty()) {
      return new String[] {};
    }

    ServletRegistration servletRegistration = servlets.get(0);
    Collection<String> mappings = servletRegistration.getMappings();
    if (servlets.size() > 1) {
      LOGGER.info("Found {} {} registered, select the first one, mappings={}.",
          servlets.size(),
          servletCls.getName(),
          mappings);
    }
    return filterUrlPatterns(mappings);
  }

  static List<ServletRegistration> findServletRegistrations(ServletContext servletContext,
      Class<?> servletCls) {
    return servletContext.getServletRegistrations()
        .values()
        .stream()
        .filter(predicate -> predicate.getClassName().equals(servletCls.getName()))
        .collect(Collectors.toList());
  }

  static String collectUrlPrefix(ServletContext servletContext, Class<?> servletCls) {
    String[] urlPatterns = collectUrlPatterns(servletContext, servletCls);
    if (urlPatterns.length == 0) {
      return null;
    }

    // even have multiple urlPattern, we only choose one to set as urlPrefix
    // only make sure sdk can invoke
    String urlPattern = urlPatterns[0];
    return servletContext.getContextPath() + urlPattern.substring(0, urlPattern.length() - 2);
  }

  public static void saveUrlPrefix(ServletContext servletContext) {
    String urlPrefix = collectUrlPrefix(servletContext, RestServlet.class);
    if (urlPrefix == null) {
      LOGGER.info("RestServlet not found, will not save UrlPrefix.");
      return;
    }

    System.setProperty(Const.URL_PREFIX, urlPrefix);
    LOGGER.info("UrlPrefix of this instance is \"{}\".", urlPrefix);
  }

  static File createUploadDir(ServletContext servletContext, String location) {
    // If relative, it is relative to TEMPDIR
    File dir = new File(location);
    if (!dir.isAbsolute()) {
      dir = new File((File) servletContext.getAttribute(ServletContext.TEMPDIR), location).getAbsoluteFile();
    }

    if (!dir.exists()) {
      dir.mkdirs();
    }

    return dir;
  }

  static void setServletParameters(ServletContext servletContext) {
    UploadConfig uploadConfig = new UploadConfig();
    MultipartConfigElement multipartConfig = uploadConfig.toMultipartConfigElement();
    if (multipartConfig == null) {
      return;
    }

    File dir = createUploadDir(servletContext, multipartConfig.getLocation());
    LOGGER.info("set uploads directory to {}.", dir.getAbsolutePath());

    List<ServletRegistration> servlets = findServletRegistrations(servletContext, RestServlet.class);
    for (ServletRegistration servletRegistration : servlets) {
      if (!Dynamic.class.isInstance(servletRegistration)) {
        continue;
      }

      Dynamic dynamic = (Dynamic) servletRegistration;
      dynamic.setMultipartConfig(multipartConfig);
    }
  }

  public static void init(ServletContext servletContext) {
    RestServletInjector.defaultInject(servletContext);
    ServletUtils.saveUrlPrefix(servletContext);
    setServletParameters(servletContext);
  }
}
