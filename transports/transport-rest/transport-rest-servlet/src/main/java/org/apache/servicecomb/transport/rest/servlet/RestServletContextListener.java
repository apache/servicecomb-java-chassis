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

import static java.lang.Boolean.FALSE;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.servicecomb.foundation.common.utils.Log4jUtils;
import org.springframework.context.support.AbstractApplicationContext;

public class RestServletContextListener implements ServletContextListener {
  private AbstractApplicationContext context;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {
      initLog(sce);
      initSpring(sce);
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  public void initLog(ServletContextEvent sce) throws Exception {
    String logMerged = sce.getServletContext().getInitParameter("servicecomb.logging.merged");
    if (!FALSE.toString().equalsIgnoreCase(logMerged)) {
      Log4jUtils.init();
    }
  }

  public AbstractApplicationContext initSpring(ServletContextEvent sce) {
    context = new CseXmlWebApplicationContext(sce.getServletContext());
    context.refresh();
    return context;
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    if (context == null) {
      return;
    }

    context.close();
    context = null;
  }
}
