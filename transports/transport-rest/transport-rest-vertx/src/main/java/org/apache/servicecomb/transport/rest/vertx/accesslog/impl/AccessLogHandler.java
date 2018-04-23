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

import org.apache.servicecomb.transport.rest.vertx.accesslog.AccessLogGenerator;
import org.apache.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogPatternParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class AccessLogHandler implements Handler<RoutingContext> {
  private static Logger LOGGER = LoggerFactory.getLogger("accesslog");

  private AccessLogGenerator accessLogGenerator;

  public AccessLogHandler(String rawPattern, AccessLogPatternParser accessLogPatternParser) {
    accessLogGenerator = new AccessLogGenerator(rawPattern, accessLogPatternParser);
  }

  @Override
  public void handle(RoutingContext context) {
    AccessLogParam<RoutingContext> accessLogParam = new AccessLogParam<>();
    accessLogParam.setStartMillisecond(System.currentTimeMillis()).setContextData(context);

    context.response().endHandler(event -> LOGGER.info(accessLogGenerator.generateLog(accessLogParam)));

    context.next();
  }
}
