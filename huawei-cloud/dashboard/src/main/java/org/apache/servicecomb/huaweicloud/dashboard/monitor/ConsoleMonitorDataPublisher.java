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

package org.apache.servicecomb.huaweicloud.dashboard.monitor;

import io.vertx.core.json.Json;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.event.ConsoleMonitorDataEvent;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.model.MonitorDaraProvider;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.model.MonitorDataPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleMonitorDataPublisher implements MonitorDataPublisher {
  private static final int END_INDEX = 100;

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleMonitorDataPublisher.class);

  @Override
  public void publish(MonitorDaraProvider provider) {
    Object data = provider.getData();
    if (data == null) {
      return;
    }
    String reqString = Json.encode(data);
    LOGGER.info(reqString.length() > END_INDEX ? reqString.substring(0, END_INDEX) : reqString);
    EventManager.post(new ConsoleMonitorDataEvent(reqString));
  }
}
