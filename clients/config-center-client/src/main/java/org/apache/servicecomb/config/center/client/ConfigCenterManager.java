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

package org.apache.servicecomb.config.center.client;

import org.apache.servicecomb.config.center.client.model.QueryConfigurationsRequest;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsResponse;
import org.apache.servicecomb.http.client.task.AbstractTask;
import org.apache.servicecomb.http.client.task.Task;

import com.google.common.eventbus.EventBus;

public class ConfigCenterManager extends AbstractTask {
  private static final long POLL_INTERVAL = 15000;

  private ConfigCenterClient configCenterClient;

  private final EventBus eventBus;

  private QueryConfigurationsRequest queryConfigurationsRequest;

  public ConfigCenterManager(ConfigCenterClient configCenterClient, EventBus eventBus) {
    super("config-center-configuration-task");
    this.configCenterClient = configCenterClient;
    this.eventBus = eventBus;
  }

  public void setQueryConfigurationsRequest(QueryConfigurationsRequest queryConfigurationsRequest) {
    this.queryConfigurationsRequest = queryConfigurationsRequest;
  }

  public void startConfigCenterManager() {
    this.startTask(new PollConfigurationTask());
  }

  class PollConfigurationTask implements Task {
    @Override
    public void execute() {
      QueryConfigurationsResponse response = configCenterClient.queryConfigurations(queryConfigurationsRequest);
      if (response.isChanged()) {
        queryConfigurationsRequest.setRevision(response.getRevision());
        eventBus.post(new ConfigurationChangedEvent(response.getConfigurations()));
      }
      startTask(new BackOffSleepTask(POLL_INTERVAL, new PollConfigurationTask()));
    }
  }
}
