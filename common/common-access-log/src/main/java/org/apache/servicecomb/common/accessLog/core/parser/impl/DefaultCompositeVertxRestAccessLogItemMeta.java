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

package org.apache.servicecomb.common.accessLog.core.parser.impl;

import java.util.ArrayList;
import java.util.List;


import org.apache.servicecomb.common.accessLog.core.element.AccessLogItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.CookieAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.ConfigurableDatetimeAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.DurationMillisecondAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.DurationSecondAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.FirstLineOfRequestAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.HttpMethodAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.HttpStatusAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.InvocationContextAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.LocalHostAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.LocalPortAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.QueryStringAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.RemoteHostAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.RequestHeaderAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.RequestProtocolAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.ResponseHeaderAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.ResponseSizeAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.TraceIdAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.TransportAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.UrlPathAccessItem;
import org.apache.servicecomb.common.accessLog.core.element.impl.UrlPathWithQueryAccessItem;
import org.apache.servicecomb.common.accessLog.core.parser.CompositeVertxRestAccessLogItemMeta;
import org.apache.servicecomb.common.accessLog.core.parser.VertxRestAccessLogItemMeta;

import io.vertx.ext.web.RoutingContext;

public class DefaultCompositeVertxRestAccessLogItemMeta extends CompositeVertxRestAccessLogItemMeta {
  private static final List<VertxRestAccessLogItemMeta> SUPPORTED_META = new ArrayList<>();

  static {
    final AccessLogItem<RoutingContext> httpMethodItem = new HttpMethodAccessItem();
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%m", config -> httpMethodItem));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("cs-method", config -> httpMethodItem));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%s", config -> new HttpStatusAccessItem()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("sc-status", config -> new HttpStatusAccessItem()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%T", config -> new DurationSecondAccessItem()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%D", config -> new DurationMillisecondAccessItem()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%h", config -> new RemoteHostAccessItem()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%v", config -> new LocalHostAccessItem()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%p", config -> new LocalPortAccessItem()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%B", config -> new ResponseSizeAccessItem("0")));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%b", config -> new ResponseSizeAccessItem("-")));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%r", config -> new FirstLineOfRequestAccessItem()));
    final AccessLogItem<RoutingContext> urlPathItem = new UrlPathAccessItem();
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%U", config -> urlPathItem));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("cs-uri-stem", config -> urlPathItem));
    final AccessLogItem<RoutingContext> queryStringItem = new QueryStringAccessItem();
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%q", config -> queryStringItem));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("cs-uri-query", config -> queryStringItem));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("cs-uri", config -> new UrlPathWithQueryAccessItem()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%H", config -> new RequestProtocolAccessItem()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%t", config -> new ConfigurableDatetimeAccessItem()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%SCB-traceId", config -> new TraceIdAccessItem()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%SCB-transport", config -> new TransportAccessItem()));

    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%{", "}t", ConfigurableDatetimeAccessItem::new));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%{", "}i", RequestHeaderAccessItem::new));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%{", "}o", ResponseHeaderAccessItem::new));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%{", "}C", CookieAccessItem::new));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%{", "}SCB-ctx", InvocationContextAccessItem::new));
  }

  @Override
  public List<VertxRestAccessLogItemMeta> getAccessLogItemMetas() {
    return SUPPORTED_META;
  }
}
