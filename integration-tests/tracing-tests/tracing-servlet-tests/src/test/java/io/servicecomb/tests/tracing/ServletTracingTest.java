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

package io.servicecomb.tests.tracing;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.After;
import org.junit.Before;

public class ServletTracingTest extends TracingTestBase {

  private Server server;

  @Before
  public void setup() {
    server = new Server(8080);

    WebAppContext context = new WebAppContext();
    context.setContextPath("/");
    context.setWar("src/test/webapp");
    server.setHandler(context);

    try {
      server.start();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to start server.", e);
    }
  }

  @After
  public void tearDown() throws Exception {
    Thread.sleep(1000);

    server.stop();
    server.join();
  }
}
