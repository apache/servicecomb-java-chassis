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

package org.apache.servicecomb.common.accessLog.ws;

import org.apache.servicecomb.common.accessLog.AccessLogConfig;
import org.apache.servicecomb.common.accessLog.AccessLogInitializer;
import org.apache.servicecomb.core.event.WebSocketActionEvent;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class WebSocketAccessLogInitializer implements AccessLogInitializer {
  private static final Logger ACCESS_LOG_LOGGER = LoggerFactory.getLogger("ws.accesslog");

  private static final Logger REQUEST_LOG_LOGGER = LoggerFactory.getLogger("ws.requestlog");

  private WebSocketAccessLogGenerator accessLogGenerator;

  private boolean clientLogEnabled;

  private boolean serverLogEnabled;

  @Override
  public void init(EventBus eventBus, AccessLogConfig accessLogConfig) {
    WebSocketAccessLogConfig config = WebSocketAccessLogConfig.INSTANCE;
    clientLogEnabled = config.isClientLogEnabled();
    serverLogEnabled = config.isServerLogEnabled();
    if (clientLogEnabled || serverLogEnabled) {
      accessLogGenerator = new WebSocketAccessLogGenerator();
      eventBus.register(this);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void onRequestReceived(WebSocketActionEvent actionEvent) {
    if (actionEvent == null) {
      return;
    }
    InvocationType invocationType = actionEvent.getInvocationType();
    if (invocationType == null) {
      return;
    }

    switch (invocationType) {
      case CONSUMER:
        if (clientLogEnabled) {
          REQUEST_LOG_LOGGER.info(accessLogGenerator.generateClientLog(actionEvent));
        }
        break;
      case PRODUCER: {
        if (serverLogEnabled) {
          ACCESS_LOG_LOGGER.info(accessLogGenerator.generateServerLog(actionEvent));
        }
        break;
      }
      default:
        throw new IllegalStateException("unexpected websocket invocation type: " + invocationType);
    }
  }
}
