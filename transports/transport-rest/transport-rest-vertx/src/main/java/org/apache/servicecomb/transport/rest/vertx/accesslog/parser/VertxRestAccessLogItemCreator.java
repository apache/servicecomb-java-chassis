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

package org.apache.servicecomb.transport.rest.vertx.accesslog.parser;

import java.util.List;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogItem;

import io.vertx.ext.web.RoutingContext;

/**
 * The {@linkplain VertxRestAccessLogItemCreator}s are able to instantiate a group of {@linkplain AccessLogItem}.
 */
public interface VertxRestAccessLogItemCreator {
  /**
   * @return A list of {@linkplain AccessLogItemMeta} to show that what kinds of {@linkplain AccessLogItem}
   * this creator is able to instantiate.
   */
  List<AccessLogItemMeta> getAccessLogItemMeta();

  /**
   * Create an instance of {@linkplain AccessLogItem} which is specified by {@linkplain AccessLogItemMeta} and config.
   * @param accessLogItemMeta determine which kind of {@linkplain AccessLogItem} is created.
   * @param config
   * e.g. For {@linkplain org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.CookieItem CookieItem},
   * the pattern may be "%{varName}C", and it's config is "varName". Some {@linkplain AccessLogItem} with no configurable
   * pattern (like "%m") will receive {@code null} as config.
   */
  AccessLogItem<RoutingContext> createItem(AccessLogItemMeta accessLogItemMeta, String config);
}
