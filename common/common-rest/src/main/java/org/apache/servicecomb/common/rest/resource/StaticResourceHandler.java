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
import java.net.URI;

import javax.servlet.http.Part;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: LRU cache for small resource in jar?
public abstract class StaticResourceHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(StaticResourceHandler.class);

  private String webRoot = "webroot/";

  public void setWebRoot(String webRoot) {
    this.webRoot = webRoot;
  }

  protected abstract Part findResource(String path) throws IOException;

  public Response handle(String path) {
    path = URI.create(webRoot + path).normalize().getPath();
    if (!path.startsWith(webRoot)) {
      // maybe request of attack, just return 404
      return Response.failResp(new InvocationException(Status.NOT_FOUND, Status.NOT_FOUND.getReasonPhrase()));
    }

    Part part;
    try {
      part = findResource(path);
    } catch (Throwable e) {
      LOGGER.error("failed to process static resource, path={}", path, e);
      return Response
          .failResp(new InvocationException(Status.INTERNAL_SERVER_ERROR, "failed to process static resource."));
    }

    if (part == null) {
      return Response.failResp(new InvocationException(Status.NOT_FOUND, Status.NOT_FOUND.getReasonPhrase()));
    }

    return handler(part);
  }

  public Response handler(Part part) {
    // todo: cache control
    Response response = Response.ok(part);
    response.getHeaders().addHeader(HttpHeaders.CONTENT_TYPE, part.getContentType());
    response.getHeaders().addHeader(HttpHeaders.CONTENT_DISPOSITION, "inline");
    return response;
  }
}
