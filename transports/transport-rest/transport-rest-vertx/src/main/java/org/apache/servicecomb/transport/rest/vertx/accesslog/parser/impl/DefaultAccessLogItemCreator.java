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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemMeta;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.VertxRestAccessLogItemCreator;

import io.vertx.ext.web.RoutingContext;

public class DefaultAccessLogItemCreator implements VertxRestAccessLogItemCreator {
  private static final Map<String, AccessLogItem<RoutingContext>> SIMPLE_ACCESS_LOG_ITEM_MAP = new HashMap<>();

  private static final List<AccessLogItemMeta> SUPPORTED_META_LIST = new ArrayList<>();

  static {
    AccessLogItem<RoutingContext> item = new HttpMethodItem();
    SIMPLE_ACCESS_LOG_ITEM_MAP.put("%m", item);
    SIMPLE_ACCESS_LOG_ITEM_MAP.put("cs-method", item);
    item = new HttpStatusItem();
    SIMPLE_ACCESS_LOG_ITEM_MAP.put("%s", item);
    SIMPLE_ACCESS_LOG_ITEM_MAP.put("sc-status", item);
    SIMPLE_ACCESS_LOG_ITEM_MAP.put("%T", new DurationSecondItem());
    SIMPLE_ACCESS_LOG_ITEM_MAP.put("%D", new DurationMillisecondItem());
    SIMPLE_ACCESS_LOG_ITEM_MAP.put("%h", new RemoteHostItem());
    SIMPLE_ACCESS_LOG_ITEM_MAP.put("%v", new LocalHostItem());
    SIMPLE_ACCESS_LOG_ITEM_MAP.put("%p", new LocalPortItem());
    SIMPLE_ACCESS_LOG_ITEM_MAP.put("%B", new ResponseSizeItem("0"));
    SIMPLE_ACCESS_LOG_ITEM_MAP.put("%b", new ResponseSizeItem("-"));
    SIMPLE_ACCESS_LOG_ITEM_MAP.put("%r", new FirstLineOfRequestItem());
    item = new UrlPathItem();
    SIMPLE_ACCESS_LOG_ITEM_MAP.put("%U", item);
    SIMPLE_ACCESS_LOG_ITEM_MAP.put("cs-uri-stem", item);
    item = new QueryStringItem();
    SIMPLE_ACCESS_LOG_ITEM_MAP.put("%q", item);
    SIMPLE_ACCESS_LOG_ITEM_MAP.put("cs-uri-query", item);
    SIMPLE_ACCESS_LOG_ITEM_MAP.put("cs-uri", new UrlPathWithQueryItem());
    SIMPLE_ACCESS_LOG_ITEM_MAP.put("%H", new RequestProtocolItem());
    SIMPLE_ACCESS_LOG_ITEM_MAP.put("%t", new DatetimeConfigurableItem());
    SIMPLE_ACCESS_LOG_ITEM_MAP.put("%SCB-traceId", new TraceIdItem());

    SUPPORTED_META_LIST.add(new AccessLogItemMeta("%m", null));
    SUPPORTED_META_LIST.add(new AccessLogItemMeta("cs-method", null));
    SUPPORTED_META_LIST.add(new AccessLogItemMeta("%s", null));
    SUPPORTED_META_LIST.add(new AccessLogItemMeta("sc-status", null));
    SUPPORTED_META_LIST.add(new AccessLogItemMeta("%T", null));
    SUPPORTED_META_LIST.add(new AccessLogItemMeta("%D", null));
    SUPPORTED_META_LIST.add(new AccessLogItemMeta("%h", null));
    SUPPORTED_META_LIST.add(new AccessLogItemMeta("%v", null));
    SUPPORTED_META_LIST.add(new AccessLogItemMeta("%p", null));
    SUPPORTED_META_LIST.add(new AccessLogItemMeta("%B", null));
    SUPPORTED_META_LIST.add(new AccessLogItemMeta("%b", null));
    SUPPORTED_META_LIST.add(new AccessLogItemMeta("%r", null));
    SUPPORTED_META_LIST.add(new AccessLogItemMeta("%U", null));
    SUPPORTED_META_LIST.add(new AccessLogItemMeta("cs-uri-stem", null));
    SUPPORTED_META_LIST.add(new AccessLogItemMeta("%q", null));
    SUPPORTED_META_LIST.add(new AccessLogItemMeta("cs-uri-query", null));
    SUPPORTED_META_LIST.add(new AccessLogItemMeta("cs-uri", null));
    SUPPORTED_META_LIST.add(new AccessLogItemMeta("%H", null));
    SUPPORTED_META_LIST.add(new AccessLogItemMeta("%t", null));
    SUPPORTED_META_LIST.add(new AccessLogItemMeta("%SCB-traceId", null));
    SUPPORTED_META_LIST.add(new AccessLogItemMeta("%{", "}t"));
    SUPPORTED_META_LIST.add(new AccessLogItemMeta("%{", "}i"));
    SUPPORTED_META_LIST.add(new AccessLogItemMeta("%{", "}o"));
    SUPPORTED_META_LIST.add(new AccessLogItemMeta("%{", "}C"));
    SUPPORTED_META_LIST.add(new AccessLogItemMeta("%{", "}SCB-ctx"));
  }

  @Override
  public AccessLogItem<RoutingContext> createItem(AccessLogItemMeta accessLogItemMeta, String config) {
    if (null == accessLogItemMeta.getSuffix()) {
      // For the simple AccessLogItem
      return SIMPLE_ACCESS_LOG_ITEM_MAP.get(accessLogItemMeta.getPrefix());
    }

    // For the configurable AccessLogItem
    switch (accessLogItemMeta.getSuffix()) {
      case "}t":
        return new DatetimeConfigurableItem(config);
      case "}i":
        return new RequestHeaderItem(config);
      case "}o":
        return new ResponseHeaderItem(config);
      case "}C":
        return new CookieItem(config);
      case "}SCB-ctx":
        return new InvocationContextItem(config);
      default:
        // unexpected situation
        return null;
    }
  }

  @Override
  public List<AccessLogItemMeta> getAccessLogItemMeta() {
    return SUPPORTED_META_LIST;
  }
}
