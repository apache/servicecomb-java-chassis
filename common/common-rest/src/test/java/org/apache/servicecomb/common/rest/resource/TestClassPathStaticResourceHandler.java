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
package org.apache.servicecomb.common.rest.resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.Part;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.foundation.test.scaffolding.exception.RuntimeExceptionWithoutStackTrace;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestClassPathStaticResourceHandler {
  static ClassPathStaticResourceHandler handler = new ClassPathStaticResourceHandler();

  @BeforeAll
  public static void setup() {
    handler.setWebRoot("web-root/");
  }

  @Test
  public void normal() throws IOException {
    Response response = handler.handle("index.html");
    Part part = response.getResult();

    try (InputStream is = part.getInputStream()) {
      Assertions.assertTrue(IOUtils.toString(is, StandardCharsets.UTF_8).endsWith("<html></html>"));
    }
    Assertions.assertEquals("text/html", part.getContentType());
    Assertions.assertEquals("text/html", response.getHeader(HttpHeaders.CONTENT_TYPE));
    Assertions.assertEquals("inline", response.getHeader(HttpHeaders.CONTENT_DISPOSITION));
  }

  @Test
  public void notExist() {
    Response response = handler.handle("notExist.html");

    InvocationException invocationException = response.getResult();
    Assertions.assertEquals(Status.NOT_FOUND, invocationException.getStatus());
    Assertions.assertEquals(Status.NOT_FOUND.getReasonPhrase(),
        ((CommonExceptionData) invocationException.getErrorData()).getMessage());
    Assertions.assertEquals(404, response.getStatusCode());
    Assertions.assertEquals("Not Found", response.getReasonPhrase());
  }

  @Test
  public void attack() {
    Response response = handler.handle("../microservice.yaml");

    InvocationException invocationException = response.getResult();
    Assertions.assertEquals(Status.NOT_FOUND, invocationException.getStatus());
    Assertions.assertEquals(Status.NOT_FOUND.getReasonPhrase(),
        ((CommonExceptionData) invocationException.getErrorData()).getMessage());
    Assertions.assertEquals(404, response.getStatusCode());
    Assertions.assertEquals("Not Found", response.getReasonPhrase());
  }

  @Test
  public void readContentFailed() throws IOException {
    handler = Mockito.spy(TestClassPathStaticResourceHandler.handler);
    Mockito.when(handler.findResource("web-root/index.html"))
            .thenThrow(new RuntimeExceptionWithoutStackTrace("read content failed."));

    try (LogCollector logCollector = new LogCollector()) {
      Response response = handler.handle("index.html");

      Assertions.assertEquals("failed to process static resource, path=web-root/index.html",
          logCollector.getLastEvents().getMessage());

      InvocationException invocationException = response.getResult();
      Assertions.assertEquals(Status.INTERNAL_SERVER_ERROR, invocationException.getStatus());
      Assertions.assertEquals("failed to process static resource.",
          ((CommonExceptionData) invocationException.getErrorData()).getMessage());
      Assertions.assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode());
      Assertions.assertEquals(Status.INTERNAL_SERVER_ERROR.getReasonPhrase(), response.getReasonPhrase());
    }
  }
}
