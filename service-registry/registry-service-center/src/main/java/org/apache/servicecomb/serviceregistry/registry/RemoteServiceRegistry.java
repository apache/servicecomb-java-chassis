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

import org.apache.servicecomb.foundation.common.concurrency.SuppressedRunnableWrapper;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.registry.definition.MicroserviceDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

public class RemoteServiceRegistry extends AbstractServiceRegistry {
  private static final Logger LOGGER = LoggerFactory.getLogger(RemoteServiceRegistry.class);

  private ScheduledThreadPoolExecutor taskPool;

  private List<ServiceRegistryTaskInitializer> taskInitializers = SPIServiceUtils
      .getOrLoadSortedService(ServiceRegistryTaskInitializer.class);

  public RemoteServiceRegistry(EventBus eventBus, ServiceRegistryConfig serviceRegistryConfig,
      MicroserviceDefinition microserviceDefinition) {
    super(eventBus, serviceRegistryConfig, microserviceDefinition);
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
}
