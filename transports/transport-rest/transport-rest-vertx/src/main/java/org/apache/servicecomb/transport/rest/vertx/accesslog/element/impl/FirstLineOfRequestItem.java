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

package org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl;

import org.apache.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogItem;

import io.vertx.ext.web.RoutingContext;

public class FirstLineOfRequestItem implements AccessLogItem<RoutingContext> {
  private static final HttpMethodItem METHOD_ELEMENT = new HttpMethodItem();

  private static final UrlPathItem URI_PATH_ONLY_ELEMENT = new UrlPathItem();

  private static final RequestProtocolItem VERSION_OR_PROTOCOL_ELEMENT = new RequestProtocolItem();

  @Override
  public String getFormattedItem(AccessLogParam<RoutingContext> accessLogParam) {
    StringBuilder result = new StringBuilder(64)
        .append("\"")
        .append(METHOD_ELEMENT.getFormattedItem(accessLogParam))
        .append(" ")
        .append(URI_PATH_ONLY_ELEMENT.getFormattedItem(accessLogParam))
        .append(" ")
        .append(VERSION_OR_PROTOCOL_ELEMENT.getFormattedItem(accessLogParam))
        .append("\"");

    return result.toString();
  }
}
