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

package org.apache.servicecomb.foundation.vertx;

import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpHeaders;

import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.BodyHandlerImpl;

/**
 * 扩展的BodyHandler
 * 只支持指定ContentType格式的body数据，关闭了文件上传能力
 */
public abstract class SimpleBodyHandler extends BodyHandlerImpl {

  @Override
  public void handle(RoutingContext context) {
    if (this.checkContentType(context)) {
      super.handle(context);
    }
  }

  protected boolean checkContentType(RoutingContext context) {
    String contentType = context.request().getHeader(HttpHeaders.CONTENT_TYPE);
    if (contentTypeSupported(contentType)) {
      return true;
    }
    Status status = Status.UNSUPPORTED_MEDIA_TYPE;
    context.response().setStatusCode(status.getStatusCode()).setStatusMessage(status.getReasonPhrase());
    context.response().end(String.format("Content-Type %s is not supported", contentType));
    return false;
  }

  protected abstract boolean contentTypeSupported(String contentType);
}
