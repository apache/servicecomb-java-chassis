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

package org.apache.servicecomb.transport.rest.vertx.accesslog;

import java.util.List;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogPatternParser;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.impl.VertxRestAccessLogPatternParser;

import io.vertx.ext.web.RoutingContext;

/*
 * Accept {@link AccessLogParam} and generate access log.
 * <br/>
 * Each AccessLogParam for a line of access log.
 */
public class AccessLogGenerator {
  /*
   * traversal this array to generate access log segment.
   */
  private AccessLogItem<RoutingContext>[] accessLogItems;

  private AccessLogPatternParser<RoutingContext> accessLogPatternParser = new VertxRestAccessLogPatternParser();

  @SuppressWarnings("unchecked")
  public AccessLogGenerator(String rawPattern) {
    List<AccessLogItem<RoutingContext>> accessLogItemList = accessLogPatternParser.parsePattern(rawPattern);
    accessLogItems = accessLogItemList.toArray(new AccessLogItem[0]);
  }

  public String generateLog(AccessLogParam<RoutingContext> accessLogParam) {
    StringBuilder log = new StringBuilder(128);
    accessLogParam.setEndMillisecond(System.currentTimeMillis());

    AccessLogItem<RoutingContext>[] accessLogItems = getAccessLogItems();
    for (int i = 0; i < accessLogItems.length; ++i) {
      log.append(accessLogItems[i].getFormattedItem(accessLogParam));
    }

    return log.toString();
  }

  private AccessLogItem<RoutingContext>[] getAccessLogItems() {
    return accessLogItems;
  }
}
