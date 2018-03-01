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

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.CookieItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DatetimeConfigurableItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.PlainTextItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.RequestHeaderItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.ResponseHeaderItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder.AccessLogItemTypeEnum;

import io.vertx.ext.web.RoutingContext;

/**
 * Some access log item contains changeable part, so we should get it's configuration from rawPattern, and generate it
 * each time it is needed.
 */
public class PercentagePrefixConfigurableItemCreator implements AccessLogItemCreator<RoutingContext> {
  @Override
  public AccessLogItem<RoutingContext> create(String rawPattern, AccessLogItemLocation location) {
    String config = getConfig(rawPattern, location);
    switch (location.getPlaceHolder()) {
      case DATETIME_CONFIGURABLE:
        return new DatetimeConfigurableItem(config);
      case REQUEST_HEADER:
        return new RequestHeaderItem(config);
      case RESPONSE_HEADER:
        return new ResponseHeaderItem(config);
      case COOKIE:
        return new CookieItem(config);
      case TEXT_PLAIN:
        return new PlainTextItem(config);
      default:
        // unexpected situation
        return null;
    }
  }

  private String getConfig(String rawPattern, AccessLogItemLocation location) {
    if (location.getPlaceHolder() == AccessLogItemTypeEnum.TEXT_PLAIN) {
      return rawPattern.substring(location.getStart(), location.getEnd());
    }
    return rawPattern.substring(location.getStart() + 2, location.getEnd() - 2);
  }
}
