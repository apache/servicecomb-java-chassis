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
import org.apache.servicecomb.common.accessLog.core.element.impl.CookieItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.DatetimeConfigurableItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.DurationMillisecondItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.DurationSecondItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.FirstLineOfRequestItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.HttpMethodItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.HttpStatusItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.InvocationContextItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.LocalHostItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.LocalPortItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.QueryStringItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.RemoteHostItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.RequestHeaderItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.RequestProtocolItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.ResponseHeaderItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.ResponseSizeItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.TraceIdItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.TransportItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.UrlPathItemAccess;
import org.apache.servicecomb.common.accessLog.core.element.impl.UrlPathWithQueryItemAccess;
import org.apache.servicecomb.common.accessLog.core.parser.CompositeVertxRestAccessLogItemMeta;
import org.apache.servicecomb.common.accessLog.core.parser.VertxRestAccessLogItemMeta;

import io.vertx.ext.web.RoutingContext;

public class DefaultCompositeVertxRestAccessLogItemMeta extends CompositeVertxRestAccessLogItemMeta {
  private static final List<VertxRestAccessLogItemMeta> SUPPORTED_META = new ArrayList<>();

  static {
    final AccessLogItem<RoutingContext> httpMethodItem = new HttpMethodItemAccess();
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%m", config -> httpMethodItem));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("cs-method", config -> httpMethodItem));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%s", config -> new HttpStatusItemAccess()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("sc-status", config -> new HttpStatusItemAccess()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%T", config -> new DurationSecondItemAccess()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%D", config -> new DurationMillisecondItemAccess()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%h", config -> new RemoteHostItemAccess()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%v", config -> new LocalHostItemAccess()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%p", config -> new LocalPortItemAccess()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%B", config -> new ResponseSizeItemAccess("0")));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%b", config -> new ResponseSizeItemAccess("-")));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%r", config -> new FirstLineOfRequestItemAccess()));
    final AccessLogItem<RoutingContext> urlPathItem = new UrlPathItemAccess();
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%U", config -> urlPathItem));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("cs-uri-stem", config -> urlPathItem));
    final AccessLogItem<RoutingContext> queryStringItem = new QueryStringItemAccess();
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%q", config -> queryStringItem));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("cs-uri-query", config -> queryStringItem));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("cs-uri", config -> new UrlPathWithQueryItemAccess()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%H", config -> new RequestProtocolItemAccess()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%t", config -> new DatetimeConfigurableItemAccess()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%SCB-traceId", config -> new TraceIdItemAccess()));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%SCB-transport", config -> new TransportItemAccess()));

    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%{", "}t", DatetimeConfigurableItemAccess::new));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%{", "}i", RequestHeaderItemAccess::new));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%{", "}o", ResponseHeaderItemAccess::new));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%{", "}C", CookieItemAccess::new));
    SUPPORTED_META.add(new VertxRestAccessLogItemMeta("%{", "}SCB-ctx", InvocationContextItemAccess::new));
  }

  @Override
  public List<VertxRestAccessLogItemMeta> getAccessLogItemMetas() {
    return SUPPORTED_META;
  }
}
