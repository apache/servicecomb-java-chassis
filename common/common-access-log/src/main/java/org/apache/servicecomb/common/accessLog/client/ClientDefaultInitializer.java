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

package org.apache.servicecomb.common.accessLog.client;

import org.apache.servicecomb.common.accessLog.AccessLogConfig;
import org.apache.servicecomb.common.accessLog.AccessLogInitializer;
import org.apache.servicecomb.common.accessLog.core.AccessLogGenerator;
import org.apache.servicecomb.core.event.InvocationFinishEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class ClientDefaultInitializer implements AccessLogInitializer {
  private static Logger LOGGER = LoggerFactory.getLogger("requestlog");

  private AccessLogGenerator accessLogGenerator;

  @Override
  public void init(EventBus eventBus, AccessLogConfig accessLogConfig) {
    if (!accessLogConfig.isClientLogEnabled()) {
      return;
    }
    accessLogGenerator = new AccessLogGenerator(accessLogConfig.getClientLogPattern());
    eventBus.register(this);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void onRequestOut(InvocationFinishEvent finishEvent) {
    LOGGER.info(accessLogGenerator.generateClientLog(finishEvent));
  }
}
