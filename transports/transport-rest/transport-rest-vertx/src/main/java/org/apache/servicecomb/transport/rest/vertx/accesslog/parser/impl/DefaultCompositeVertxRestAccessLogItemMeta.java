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

package org.apache.servicecomb.transport.rest.vertx.accesslog.parser.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.CookieItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DatetimeConfigurableItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DurationMillisecondItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DurationSecondItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.FirstLineOfRequestItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.HttpMethodItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.HttpStatusItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.InvocationContextItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.LocalHostItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.LocalPortItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.QueryStringItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.RemoteHostItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.RequestHeaderItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.RequestProtocolItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.ResponseHeaderItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.ResponseSizeItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.TraceIdItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.UrlPathItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.UrlPathWithQueryItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.CompositeVertxRestAccessLogItemMeta;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.VertxRestAccessLogItemMeta;

import io.vertx.ext.web.RoutingContext;

public class DefaultCompositeVertxRestAccessLogItemMeta extends CompositeVertxRestAccessLogItemMeta {
  private static final List<VertxRestAccessLogItemMeta> SUPPORTED_META = new ArrayList<>();

  static {
    final AccessLogItem<RoutingContext> httpMethodItem = new HttpMethodItem();
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%m", config -> httpMethodItem));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("cs-method", config -> httpMethodItem));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%s", config -> new HttpStatusItem()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("sc-status", config -> new HttpStatusItem()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%T", config -> new DurationSecondItem()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%D", config -> new DurationMillisecondItem()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%h", config -> new RemoteHostItem()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%v", config -> new LocalHostItem()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%p", config -> new LocalPortItem()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%B", config -> new ResponseSizeItem("0")));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%b", config -> new ResponseSizeItem("-")));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%r", config -> new FirstLineOfRequestItem()));
    final AccessLogItem<RoutingContext> urlPathItem = new UrlPathItem();
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%U", config -> urlPathItem));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("cs-uri-stem", config -> urlPathItem));
    final AccessLogItem<RoutingContext> queryStringItem = new QueryStringItem();
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%q", config -> queryStringItem));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("cs-uri-query", config -> queryStringItem));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("cs-uri", config -> new UrlPathWithQueryItem()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%H", config -> new RequestProtocolItem()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%t", config -> new DatetimeConfigurableItem()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%SCB-traceId", config -> new TraceIdItem()));

    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%{", "}t", DatetimeConfigurableItem::new));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%{", "}i", RequestHeaderItem::new));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%{", "}o", ResponseHeaderItem::new));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%{", "}C", CookieItem::new));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%{", "}SCB-ctx", InvocationContextItem::new));
  }

  @Override
  public List<VertxRestAccessLogItemMeta> getAccessLogItemMetas() {
    return SUPPORTED_META;
  }
}
