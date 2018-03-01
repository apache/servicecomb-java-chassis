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

package org.apache.servicecomb.transport.rest.vertx.accesslog.element.creator;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DatetimeConfigurableItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DurationMillisecondItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DurationSecondItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.FirstLineOfRequestItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.HttpMethodItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.HttpStatusItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.LocalHostItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.LocalPortItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.QueryStringItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.RemoteHostItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.RequestProtocolItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.ResponseSizeItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.TraceIdItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.UrlPathItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.UrlPathWithQueryItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder.AccessLogItemTypeEnum;

import io.vertx.ext.web.RoutingContext;

/**
 * For some access log items, their placeholder contains no modifiable part, like "%s" or "sc-status".
 * So we can build a mapping relationship between the placeholder and item instances, when an item is needed, get it by
 * it's placeholder.
 */
public class SimpleAccessLogItemCreator implements AccessLogItemCreator<RoutingContext> {
  private static final Map<AccessLogItemTypeEnum, AccessLogItem<RoutingContext>> SIMPLE_ACCESSLOG_ITEM_MAP =
      new HashMap<>();

  static {
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.HTTP_METHOD, new HttpMethodItem());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.HTTP_STATUS, new HttpStatusItem());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.DURATION_IN_SECOND, new DurationSecondItem());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.DURATION_IN_MILLISECOND, new DurationMillisecondItem());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.REMOTE_HOSTNAME, new RemoteHostItem());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.LOCAL_HOSTNAME, new LocalHostItem());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.LOCAL_PORT, new LocalPortItem());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.RESPONSE_SIZE, new ResponseSizeItem("0"));
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.RESPONSE_SIZE_CLF, new ResponseSizeItem("-"));
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.FIRST_LINE_OF_REQUEST, new FirstLineOfRequestItem());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.URL_PATH, new UrlPathItem());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.QUERY_STRING, new QueryStringItem());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.URL_PATH_WITH_QUERY, new UrlPathWithQueryItem());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.REQUEST_PROTOCOL, new RequestProtocolItem());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.DATETIME_DEFAULT, new DatetimeConfigurableItem());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.SCB_TRACE_ID, new TraceIdItem());
  }

  @Override
  public AccessLogItem<RoutingContext> create(String rawPattern, AccessLogItemLocation location) {
    return SIMPLE_ACCESSLOG_ITEM_MAP.get(location.getPlaceHolder());
  }
}
