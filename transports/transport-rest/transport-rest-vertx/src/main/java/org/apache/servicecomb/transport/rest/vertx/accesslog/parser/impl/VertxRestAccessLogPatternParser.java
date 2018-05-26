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
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemMeta;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogPatternParser;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.VertxRestAccessLogItemCreator;

import io.vertx.ext.web.RoutingContext;

/**
 * The parser is used for rest-over-vertx transport.
 */
public class VertxRestAccessLogPatternParser implements AccessLogPatternParser<RoutingContext> {
  private List<VertxRestAccessLogItemCreator> creators = new ArrayList<>();

  /**
   * All of the {@linkplain AccessLogItemMeta} will be wrapped into {@linkplain AccessLogItemMetaWrapper}.
   */
  private List<AccessLogItemMetaWrapper> accessLogItemMetaWrappers = new ArrayList<>();

  public VertxRestAccessLogPatternParser() {
    for (VertxRestAccessLogItemCreator creator : creators) {
      for (AccessLogItemMeta accessLogItemMeta : creator.getAccessLogItemMeta()) {
        accessLogItemMetaWrappers.add(new AccessLogItemMetaWrapper(accessLogItemMeta, creator));
      }
    }
  }

  /**
   * @param rawPattern The access log pattern string specified by users.
   * @return A list of {@linkplain AccessLogItem} which actually generate the content of access log.
   */
  @Override
  public List<AccessLogItem<RoutingContext>> parsePattern(String rawPattern) {
    List<AccessLogItem<RoutingContext>> itemList = new ArrayList<>();
    // the algorithm is unimplemented.
    return itemList;
  }

  public static class AccessLogItemMetaWrapper {
    private AccessLogItemMeta accessLogItemMeta;

    private VertxRestAccessLogItemCreator vertxRestAccessLogItemCreator;

    public AccessLogItemMetaWrapper(AccessLogItemMeta accessLogItemMeta,
        VertxRestAccessLogItemCreator vertxRestAccessLogItemCreator) {
      this.accessLogItemMeta = accessLogItemMeta;
      this.vertxRestAccessLogItemCreator = vertxRestAccessLogItemCreator;
    }

    public AccessLogItemMeta getAccessLogItemMeta() {
      return accessLogItemMeta;
    }

    public VertxRestAccessLogItemCreator getVertxRestAccessLogItemCreator() {
      return vertxRestAccessLogItemCreator;
    }
  }
}
