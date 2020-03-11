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

package org.apache.servicecomb.foundation.log.core;

import java.util.List;

import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;
import org.apache.servicecomb.foundation.log.core.element.LogItem;
import org.apache.servicecomb.foundation.log.core.parser.LogPatternParser;
import org.apache.servicecomb.foundation.log.core.parser.impl.VertxRestLogPatternParser;

import com.google.common.collect.Iterables;

import io.vertx.ext.web.RoutingContext;

/*
 * Accept {@link AccessLogParam} and generate access log.
 * <br/>
 * Each AccessLogParam for a line of access log.
 */
public class LogGenerator {
  /*
   * traversal this array to generate access log segment.
   */
  private LogItem<RoutingContext>[] logItems;

  private LogPatternParser<RoutingContext> logPatternParser = new VertxRestLogPatternParser();

  @SuppressWarnings("unchecked")
  public LogGenerator(String rawPattern) {
    List<LogItem<RoutingContext>> logItemList = logPatternParser.parsePattern(rawPattern);
    logItems = Iterables.toArray(logItemList, LogItem.class);
  }

  public String generateServerLog(ServerAccessLogEvent accessLogEvent) {
    StringBuilder log = new StringBuilder(128);
    for (LogItem<RoutingContext> logItem : getLogItems()) {
      logItem.appendFormattedItem(accessLogEvent, log);
    }
    return log.toString();
  }

  public String generateClientLog(InvocationFinishEvent finishEvent) {
    StringBuilder log = new StringBuilder(128);
    for (LogItem<RoutingContext> logItem : getLogItems()) {
      logItem.appendFormattedItem(finishEvent, log);
    }
    return log.toString();
  }

  private LogItem<RoutingContext>[] getLogItems() {
    return logItems;
  }
}
