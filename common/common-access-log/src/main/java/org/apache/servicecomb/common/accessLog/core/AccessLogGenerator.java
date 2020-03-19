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

package org.apache.servicecomb.common.accessLog.core;

import java.util.List;

import org.apache.servicecomb.common.accessLog.core.element.AccessLogItem;
import org.apache.servicecomb.common.accessLog.core.parser.AccessLogPatternParser;
import org.apache.servicecomb.common.accessLog.core.parser.impl.VertxRestAccessLogPatternParser;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;

import com.google.common.collect.Iterables;

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

  private AccessLogPatternParser<RoutingContext> logPatternParser = new VertxRestAccessLogPatternParser();

  @SuppressWarnings("unchecked")
  public AccessLogGenerator(String rawPattern) {
    List<AccessLogItem<RoutingContext>> accessLogItemList = logPatternParser.parsePattern(rawPattern);
    accessLogItems = Iterables.toArray(accessLogItemList, AccessLogItem.class);
  }

  public String generateServerLog(ServerAccessLogEvent accessLogEvent) {
    StringBuilder log = new StringBuilder(128);
    for (AccessLogItem<RoutingContext> accessLogItem : getAccessLogItems()) {
      accessLogItem.appendServerFormattedItem(accessLogEvent, log);
    }
    return log.toString();
  }

  public String generateClientLog(InvocationFinishEvent finishEvent) {
    StringBuilder log = new StringBuilder(128);
    for (AccessLogItem<RoutingContext> accessLogItem : getAccessLogItems()) {
      accessLogItem.appendClientFormattedItem(finishEvent, log);
    }
    return log.toString();
  }

  private AccessLogItem<RoutingContext>[] getAccessLogItems() {
    return accessLogItems;
  }
}
