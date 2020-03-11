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

package org.apache.servicecomb.foundation.log.core.parser.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.foundation.log.core.element.LogItem;
import org.apache.servicecomb.foundation.log.core.element.impl.CookieItem;
import org.apache.servicecomb.foundation.log.core.element.impl.DatetimeConfigurableItem;
import org.apache.servicecomb.foundation.log.core.element.impl.DurationMillisecondItem;
import org.apache.servicecomb.foundation.log.core.element.impl.DurationSecondItem;
import org.apache.servicecomb.foundation.log.core.element.impl.FirstLineOfRequestItem;
import org.apache.servicecomb.foundation.log.core.element.impl.HttpMethodItem;
import org.apache.servicecomb.foundation.log.core.element.impl.HttpStatusItem;
import org.apache.servicecomb.foundation.log.core.element.impl.TransportItem;
import org.apache.servicecomb.foundation.log.core.element.impl.InvocationContextItem;
import org.apache.servicecomb.foundation.log.core.element.impl.LocalHostItem;
import org.apache.servicecomb.foundation.log.core.element.impl.LocalPortItem;
import org.apache.servicecomb.foundation.log.core.element.impl.QueryStringItem;
import org.apache.servicecomb.foundation.log.core.element.impl.RemoteHostItem;
import org.apache.servicecomb.foundation.log.core.element.impl.RequestHeaderItem;
import org.apache.servicecomb.foundation.log.core.element.impl.RequestProtocolItem;
import org.apache.servicecomb.foundation.log.core.element.impl.ResponseHeaderItem;
import org.apache.servicecomb.foundation.log.core.element.impl.ResponseSizeItem;
import org.apache.servicecomb.foundation.log.core.element.impl.TraceIdItem;
import org.apache.servicecomb.foundation.log.core.element.impl.UrlPathItem;
import org.apache.servicecomb.foundation.log.core.element.impl.UrlPathWithQueryItem;
import org.apache.servicecomb.foundation.log.core.parser.CompositeVertxRestLogItemMeta;
import org.apache.servicecomb.foundation.log.core.parser.VertxRestLogItemMeta;

import io.vertx.ext.web.RoutingContext;

public class DefaultCompositeVertxRestLogItemMeta extends CompositeVertxRestLogItemMeta {
  private static final List<VertxRestLogItemMeta> SUPPORTED_META = new ArrayList<>();

  static {
    final LogItem<RoutingContext> httpMethodItem = new HttpMethodItem();
    SUPPORTED_META.add(new VertxRestLogItemMeta("%m", config -> httpMethodItem));
    SUPPORTED_META.add(new VertxRestLogItemMeta("cs-method", config -> httpMethodItem));
    SUPPORTED_META.add(new VertxRestLogItemMeta("%s", config -> new HttpStatusItem()));
    SUPPORTED_META.add(new VertxRestLogItemMeta("sc-status", config -> new HttpStatusItem()));
    SUPPORTED_META.add(new VertxRestLogItemMeta("%T", config -> new DurationSecondItem()));
    SUPPORTED_META.add(new VertxRestLogItemMeta("%D", config -> new DurationMillisecondItem()));
    SUPPORTED_META.add(new VertxRestLogItemMeta("%h", config -> new RemoteHostItem()));
    SUPPORTED_META.add(new VertxRestLogItemMeta("%v", config -> new LocalHostItem()));
    SUPPORTED_META.add(new VertxRestLogItemMeta("%p", config -> new LocalPortItem()));
    SUPPORTED_META.add(new VertxRestLogItemMeta("%B", config -> new ResponseSizeItem("0")));
    SUPPORTED_META.add(new VertxRestLogItemMeta("%b", config -> new ResponseSizeItem("-")));
    SUPPORTED_META.add(new VertxRestLogItemMeta("%r", config -> new FirstLineOfRequestItem()));
    final LogItem<RoutingContext> urlPathItem = new UrlPathItem();
    SUPPORTED_META.add(new VertxRestLogItemMeta("%U", config -> urlPathItem));
    SUPPORTED_META.add(new VertxRestLogItemMeta("cs-uri-stem", config -> urlPathItem));
    final LogItem<RoutingContext> queryStringItem = new QueryStringItem();
    SUPPORTED_META.add(new VertxRestLogItemMeta("%q", config -> queryStringItem));
    SUPPORTED_META.add(new VertxRestLogItemMeta("cs-uri-query", config -> queryStringItem));
    SUPPORTED_META.add(new VertxRestLogItemMeta("cs-uri", config -> new UrlPathWithQueryItem()));
    SUPPORTED_META.add(new VertxRestLogItemMeta("%H", config -> new RequestProtocolItem()));
    SUPPORTED_META.add(new VertxRestLogItemMeta("%t", config -> new DatetimeConfigurableItem()));
    SUPPORTED_META.add(new VertxRestLogItemMeta("%SCB-traceId", config -> new TraceIdItem()));
    SUPPORTED_META.add(new VertxRestLogItemMeta("%SCB-transport", config -> new TransportItem()));

    SUPPORTED_META.add(new VertxRestLogItemMeta("%{", "}t", DatetimeConfigurableItem::new));
    SUPPORTED_META.add(new VertxRestLogItemMeta("%{", "}i", RequestHeaderItem::new));
    SUPPORTED_META.add(new VertxRestLogItemMeta("%{", "}o", ResponseHeaderItem::new));
    SUPPORTED_META.add(new VertxRestLogItemMeta("%{", "}C", CookieItem::new));
    SUPPORTED_META.add(new VertxRestLogItemMeta("%{", "}SCB-ctx", InvocationContextItem::new));
  }

  @Override
  public List<VertxRestLogItemMeta> getAccessLogItemMetas() {
    return SUPPORTED_META;
  }
}
