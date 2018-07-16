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
package org.apache.servicecomb.serviceregistry.diagnosis.instance;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.serviceregistry.consumer.AppManager;
import org.apache.servicecomb.serviceregistry.registry.RemoteServiceRegistry;
import org.apache.servicecomb.serviceregistry.registry.ServiceRegistryTaskInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

import io.vertx.core.json.Json;

public class InstanceCacheCheckTask implements ServiceRegistryTaskInitializer {
  private static final Logger LOGGER = LoggerFactory.getLogger(InstanceCacheCheckTask.class);

  private static final int DEFAULT_DIAGNOSE_INSTANCE_CACHE_INTERVAL_IN_HOUR = 24;

  private static final String CONFIG_PREFIX = "servicecomb.service.registry.instance.diagnose.";

  public static final String MANUAL = CONFIG_PREFIX + "manual";

  public static final String AUTO_INTERVAL = CONFIG_PREFIX + "interval";

  // auto task
  private ScheduledFuture<?> scheduledFuture;

  private AppManager appManager;

  private ScheduledThreadPoolExecutor taskPool;

  private EventBus eventBus;

  private DynamicIntProperty autoCheckIntervalProperty;

  private DynamicStringProperty manualCheckProperty;

  private TimeUnit timeUnit = TimeUnit.HOURS;

  // make test easier
  public void setTimeUnit(TimeUnit timeUnit) {
    this.timeUnit = timeUnit;
  }

  public void setAppManager(AppManager appManager) {
    this.appManager = appManager;
  }

  public void setTaskPool(ScheduledThreadPoolExecutor taskPool) {
    this.taskPool = taskPool;
  }

  public void setEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  public DynamicStringProperty getManualCheckProperty() {
    return manualCheckProperty;
  }

  public DynamicIntProperty getAutoCheckIntervalProperty() {
    return autoCheckIntervalProperty;
  }

  @Override
  public void init(RemoteServiceRegistry remoteServiceRegistry) {
    appManager = remoteServiceRegistry.getAppManager();
    taskPool = remoteServiceRegistry.getTaskPool();
    eventBus = remoteServiceRegistry.getEventBus();

    init();
  }

  protected void init() {
    startAutoTask();
    registerManualTask();
  }

  private void registerManualTask() {
    // if manual config item changed, then run task once
    manualCheckProperty = DynamicPropertyFactory.getInstance().getStringProperty(MANUAL, null, this::runTask);
  }

  protected void startAutoTask() {
    autoCheckIntervalProperty = DynamicPropertyFactory.getInstance().getIntProperty(AUTO_INTERVAL,
        DEFAULT_DIAGNOSE_INSTANCE_CACHE_INTERVAL_IN_HOUR,
        this::doStartAutoTask);
    doStartAutoTask();
  }

  private void doStartAutoTask() {
    if (scheduledFuture != null) {
      scheduledFuture.cancel(false);
      scheduledFuture = null;
    }

    int interval = autoCheckIntervalProperty.get();
    if (interval <= 0) {
      LOGGER.info("disable instance cache check task, interval={}.", interval);
      return;
    }

    scheduledFuture = taskPool.scheduleAtFixedRate(this::runTask, interval, interval, timeUnit);
  }

  protected void runTask() {
    try {
      InstanceCacheChecker checker = new InstanceCacheChecker(appManager);
      InstanceCacheSummary instanceCacheSummary = checker.check();
      eventBus.post(instanceCacheSummary);

      LOGGER.info("check instance cache, result={}.", Json.encode(instanceCacheSummary));
    } catch (Throwable e) {
      LOGGER.error("failed check instance cache..", e);
    }
  }
}
