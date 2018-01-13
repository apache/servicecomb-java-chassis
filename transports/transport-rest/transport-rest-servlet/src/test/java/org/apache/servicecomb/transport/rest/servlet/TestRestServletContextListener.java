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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class TestRestServletContextListener {
  @Test
  public void testcontextInitializedException() {
    boolean status = true;
    RestServletContextListener listener = new RestServletContextListener();
    ServletContextEvent sce = Mockito.mock(ServletContextEvent.class);

    try {
      listener.contextInitialized(sce);
    } catch (Exception | Error e) {
      status = false;
    }
    Assert.assertFalse(status);
  }

  @Test
  public void testInitSpring() {
    boolean status = true;
    RestServletContextListener listener = new RestServletContextListener();
    ServletContextEvent sce = Mockito.mock(ServletContextEvent.class);
    ServletContext context = Mockito.mock(ServletContext.class);
    Mockito.when(sce.getServletContext()).thenReturn(context);
    Mockito.when(sce.getServletContext().getInitParameter("contextConfigLocation")).thenReturn("locations");
    try {
      listener.initSpring(sce);
    } catch (Exception e) {
      status = false;
    }
    Assert.assertFalse(status);
  }
}
