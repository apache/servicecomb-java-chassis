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

package org.apache.servicecomb.serviceregistry.registry;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.foundation.common.concurrency.SuppressedRunnableWrapper;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.event.HeartbeatFailEvent;
import org.apache.servicecomb.serviceregistry.event.HeartbeatSuccEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class RemoteServiceRegistry extends AbstractServiceRegistry {
  private static final Logger LOGGER = LoggerFactory.getLogger(RemoteServiceRegistry.class);

  private ScheduledThreadPoolExecutor taskPool;

  private List<ServiceRegistryTaskInitializer> taskInitializers = SPIServiceUtils
      .getOrLoadSortedService(ServiceRegistryTaskInitializer.class);

  private AtomicBoolean heartBeatStatus = new AtomicBoolean(true);

  public RemoteServiceRegistry(EventBus eventBus, ServiceRegistryConfig serviceRegistryConfig,
      Configuration configuration) {
    super(eventBus, serviceRegistryConfig, configuration);
  }

  @Override
  public void init() {
    super.init();
    taskPool = new ScheduledThreadPoolExecutor(3,
        new ThreadFactory() {
          private int taskId = 0;

          @Override
          public Thread newThread(Runnable r) {
            Thread thread = new Thread(r,
                RemoteServiceRegistry.super.getName() + " Service Center Task [" + (taskId++) + "]");
            thread.setUncaughtExceptionHandler(
                (t, e) -> LOGGER.error("Service Center Task Thread is terminated! thread: [{}]", t, e));
            return thread;
          }
        },
        (task, executor) -> LOGGER.warn("Too many pending tasks, reject " + task.toString())
    );
    executorService = taskPool;
  }

  @Override
  protected ServiceRegistryClient createServiceRegistryClient() {
    return serviceRegistryConfig.createServiceRegistryClient(this);
  }

  @Override
  public void run() {
    super.run();

    taskPool.scheduleAtFixedRate(serviceCenterTask,
        serviceRegistryConfig.getHeartbeatInterval(),
        serviceRegistryConfig.getHeartbeatInterval(),
        TimeUnit.SECONDS);

    taskPool.scheduleAtFixedRate(
        new SuppressedRunnableWrapper(() -> {
          if (!heartBeatStatus.get()) {
            LOGGER.warn("heart beat currently not success, pause for pulling instance.");
            return;
          }
          serviceRegistryCache.refreshCache();
        }),
        serviceRegistryConfig.getInstancePullInterval(),
        serviceRegistryConfig.getInstancePullInterval(),
        TimeUnit.SECONDS);

    for (ServiceRegistryTaskInitializer initializer : taskInitializers) {
      initializer.init(this);
    }
  }

  public ScheduledThreadPoolExecutor getTaskPool() {
    return this.taskPool;
  }

  @Subscribe
  public void onHeartbeatSuccEvent(HeartbeatSuccEvent heartbeatSuccEvent) {
    // 可以考虑多等待一个心跳周期，这样的好处是尽可能避免provider滞后于consumer注册的情况，consumer访问provider失败
    // 但是这样的坏处是当服务中心两个实例，一个可用，另外一个不可用的情况下，长期处于不查询实例状态。 目前按照一次的实际效果
    // 还是挺好的，也是和版本一直的实现一致。
    heartBeatStatus.set(true);
  }

  @Subscribe
  public void onHeartbeatFailEvent(HeartbeatFailEvent heartbeatFailEvent) {
    heartBeatStatus.set(false);
  }
}
