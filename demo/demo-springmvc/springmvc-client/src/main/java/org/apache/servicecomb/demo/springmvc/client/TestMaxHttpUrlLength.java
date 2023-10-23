/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.apache.servicecomb.demo.springmvc.client;

import static jakarta.ws.rs.core.Response.Status.REQUEST_URI_TOO_LONG;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;

import com.google.common.base.Strings;

@Component
public class TestMaxHttpUrlLength implements CategorizedTestCase {
  @Override
  public void testRestTransport() throws Exception {
    if (LegacyPropertyFactory.getBooleanProperty("servicecomb.test.vert.transport", true)) {
      testUrlNotLongerThan4096();
    }
  }

  private void testUrlNotLongerThan4096() {
    RestOperations restTemplate = RestTemplateBuilder.create();
    // \r doesn't count for url length Since netty 4.1.88.Final See https://github.com/netty/netty/pull/12321
    String q = Strings.repeat("q",
        4096 + 1 - "GET /api/springmvc/controller/sayhi?name=".length() - " HTTP/1.1\r".length());
    TestMgr.check("hi " + q + " [" + q + "]",
        restTemplate.getForObject("cse://springmvc/springmvc/controller/sayhi?name=" + q,
            String.class));

    q = Strings.repeat("q", 4096 + 2 - "GET /api/springmvc/controller/sayhi?name=".length() - " HTTP/1.1\r".length());
    try {
      restTemplate.getForObject("cse://springmvc/springmvc/controller/sayhi?name=" + q,
          String.class);
      TestMgr.check(true, false);
    } catch (InvocationException e) {
      TestMgr.check(REQUEST_URI_TOO_LONG.getStatusCode(), e.getStatusCode());
    }
  }

  @Override
  public void testHighwayTransport() throws Exception {

  }

  @Override
  public void testAllTransport() throws Exception {

  }
}
