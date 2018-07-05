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
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class CseXmlWebApplicationContext extends XmlWebApplicationContext {
  private static final Logger LOGGER = LoggerFactory.getLogger(CseXmlWebApplicationContext.class);

  static final String KEY_LOCATION = "contextConfigLocation";

  private String defaultBeanResource = BeanUtils.DEFAULT_BEAN_RESOURCE;

  public CseXmlWebApplicationContext() {
  }

  public CseXmlWebApplicationContext(ServletContext servletContext) {
    setServletContext(servletContext);
  }

  public void setDefaultBeanResource(String defaultBeanResource) {
    this.defaultBeanResource = defaultBeanResource;
  }

  @Override
  protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
    super.invokeBeanFactoryPostProcessors(beanFactory);

    // inject servlet after config installed and before transport init
    ServletUtils.init(getServletContext());
  }

  @Override
  public String[] getConfigLocations() {
    String contextConfigLocation = getServletContext().getInitParameter(KEY_LOCATION);
    String[] locationArray = splitLocations(contextConfigLocation);

    LOGGER.info("init spring context: {}.", Arrays.toString(locationArray));
    return locationArray;
  }

  private String[] splitLocations(String locations) {
    Set<String> locationSet = new LinkedHashSet<>();
    if (!StringUtils.isEmpty(locations)) {
      for (String location : locations.split("[,\n]")) {
        location = location.trim();
        if (StringUtils.isEmpty(location)) {
          continue;
        }

        locationSet.add(location);
      }
    }

    if (!StringUtils.isEmpty(defaultBeanResource)) {
      locationSet.add(defaultBeanResource);
    }

    return locationSet.toArray(new String[locationSet.size()]);
  }
}
