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

package org.apache.servicecomb.config.kie.client;

import java.util.Collections;
import java.util.Map;

import org.apache.servicecomb.config.common.ConfigurationChangedEvent;
import org.apache.servicecomb.config.kie.client.model.ConfigurationsRequest;
import org.apache.servicecomb.config.kie.client.model.ConfigurationsResponse;
import org.apache.servicecomb.http.client.task.AbstractTask;
import org.apache.servicecomb.http.client.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

public class KieConfigManager extends AbstractTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(KieConfigManager.class);

  private static long POLL_INTERVAL = 1000;

  private KieConfigOperation configKieClient;

  private final EventBus eventBus;

  private ConfigurationsRequest configurationsRequest;

  private Map<String, Object> lastConfiguration;

  public KieConfigManager(KieConfigOperation configKieClient, EventBus eventBus) {
    this(configKieClient, eventBus, Collections.emptyMap());
  }

  public KieConfigManager(KieConfigOperation configKieClient, EventBus eventBus,
      Map<String, Object> lastConfiguration) {
    super("config-center-configuration-task");
    this.configKieClient = configKieClient;
    this.eventBus = eventBus;
    this.lastConfiguration = lastConfiguration;
  }

  public void setConfigurationsRequest(ConfigurationsRequest configurationsRequest) {
    this.configurationsRequest = configurationsRequest;
  }

  public void startConfigKieManager() {
    this.startTask(new PollConfigurationTask(0));
  }

  class PollConfigurationTask implements Task {
    int failCount = 0;

    public PollConfigurationTask(int failCount) {
      this.failCount = failCount;
    }

    @Override
    public void execute() {
      try {
        ConfigurationsResponse response = configKieClient.queryConfigurations(configurationsRequest);
        if (response.isChanged()) {
          LOGGER.info("The configurations are change, will refresh local configurations.");
          configurationsRequest.setRevision(response.getRevision());
          eventBus.post(ConfigurationChangedEvent.createIncremental(response.getConfigurations(), lastConfiguration));
          lastConfiguration = response.getConfigurations();
        }
        startTask(new BackOffSleepTask(POLL_INTERVAL, new PollConfigurationTask(0)));
      } catch (Exception e) {
        LOGGER.error("get configurations from KieConfigCenter failed, and will try again.", e);
        startTask(new BackOffSleepTask(failCount + 1, new PollConfigurationTask(failCount + 1)));
      }
    }
  }
}
