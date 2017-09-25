/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.transport.rest.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rest Servlet Server, load by web container
 */
public class RestServlet extends HttpServlet {
  private static final long serialVersionUID = 5797523329773923112L;

  private static final Logger LOGGER = LoggerFactory.getLogger(RestServlet.class);

  private ServletRestDispatcher servletRestServer = new ServletRestDispatcher();

  @Override
  public void init() throws ServletException {
    super.init();

    LOGGER.info("Rest Servlet inited");
  }

  @Override
  public void service(final HttpServletRequest request,
      final HttpServletResponse response) throws ServletException, IOException {
    servletRestServer.service(request, response);
  }
}
