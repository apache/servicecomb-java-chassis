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

package org.apache.servicecomb.transport.rest.vertx.accesslog.impl;

import java.util.List;

import org.apache.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogPatternParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class AccessLogHandler implements Handler<RoutingContext> {
  private static Logger LOGGER = LoggerFactory.getLogger("accesslog");

  private static AccessLogElement[] accessLogElements;

  public AccessLogHandler(String rawPattern, AccessLogPatternParser accessLogPatternParser) {
    List<AccessLogElementExtraction> extractionList = accessLogPatternParser.parsePattern(rawPattern);

    accessLogElements = new AccessLogElement[extractionList.size()];
    for (int i = 0; i < extractionList.size(); ++i) {
      accessLogElements[i] = extractionList.get(i).getAccessLogElement();
    }
  }

  @Override
  public void handle(RoutingContext context) {
    AccessLogParam accessLogParam = new AccessLogParam().setStartMillisecond(System.currentTimeMillis())
        .setRoutingContext(context);

    context.addBodyEndHandler(v -> log(accessLogParam));

    context.next();
  }

  private void log(AccessLogParam accessLogParam) {
    StringBuilder log = new StringBuilder(128);
    accessLogParam.setEndMillisecond(System.currentTimeMillis());

    AccessLogElement[] accessLogElements = getAccessLogElements();
    for (int i = 0; i < accessLogElements.length; ++i) {
      log.append(accessLogElements[i].getFormattedElement(accessLogParam));
    }

    LOGGER.info(log.toString());
  }

  private AccessLogElement[] getAccessLogElements() {
    return accessLogElements;
  }
}
