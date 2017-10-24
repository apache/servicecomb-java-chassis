/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicecomb.serviceregistry.registry;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import io.servicecomb.serviceregistry.client.ServiceRegistryClient;
import io.servicecomb.serviceregistry.client.http.ServiceRegistryClientImpl;
import io.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import io.servicecomb.serviceregistry.definition.MicroserviceDefinition;
import io.servicecomb.serviceregistry.task.InstancePullTask;
import io.servicecomb.serviceregistry.task.event.PeriodicPullEvent;
import io.servicecomb.serviceregistry.task.event.PullMicroserviceVersionsInstancesEvent;
import io.servicecomb.serviceregistry.task.event.ShutdownEvent;

public class RemoteServiceRegistry extends AbstractServiceRegistry {
  private static final Logger LOGGER = LoggerFactory.getLogger(RemoteServiceRegistry.class);

  private ScheduledThreadPoolExecutor taskPool;

  private InstancePullTask pullTask;

  public RemoteServiceRegistry(EventBus eventBus, ServiceRegistryConfig serviceRegistryConfig,
      MicroserviceDefinition microserviceDefinition) {
    super(eventBus, serviceRegistryConfig, microserviceDefinition);
  }

  @Override
  public void init() {
    super.init();
    taskPool = new ScheduledThreadPoolExecutor(2, new ThreadFactory() {
      @Override
      public Thread newThread(Runnable task) {
        return new Thread(task, "Service Center Task");
      }
    }, new RejectedExecutionHandler() {
      @Override
      public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
        LOGGER.warn("Too many pending tasks, reject " + task.getClass().getName());
      }
    });
    pullTask = new InstancePullTask(serviceRegistryConfig.getInstancePullInterval(), this.instanceCacheManager);
  }

  @Override
  protected ServiceRegistryClient createServiceRegistryClient() {
    return new ServiceRegistryClientImpl(ipPortManager);
  }

  @Subscribe
  public void onShutdown(ShutdownEvent event) {
    LOGGER.info("service center task is shutdown.");
    taskPool.shutdownNow();
  }

  @Override
  public void run() {
    super.run();

    ipPortManager.initAutoDiscovery();

    taskPool.scheduleAtFixedRate(serviceCenterTask,
        serviceRegistryConfig.getHeartbeatInterval(),
        serviceRegistryConfig.getHeartbeatInterval(),
        TimeUnit.SECONDS);
    if (isNeedPull()) {
      taskPool.scheduleAtFixedRate(pullTask,
          serviceRegistryConfig.getInstancePullInterval(),
          serviceRegistryConfig.getInstancePullInterval(),
          TimeUnit.SECONDS);
      taskPool.scheduleAtFixedRate(() -> eventBus.post(new PeriodicPullEvent()),
          serviceRegistryConfig.getInstancePullInterval(),
          serviceRegistryConfig.getInstancePullInterval(),
          TimeUnit.SECONDS);
    }
  }

  private boolean isNeedPull() {
    return !serviceRegistryConfig.isWatch();
  }

  @Subscribe
  public void onPullMicroserviceVersionsInstancesEvent(PullMicroserviceVersionsInstancesEvent event) {
    taskPool.schedule(event.getMicroserviceVersions()::pullInstances, event.getMsDelay(), TimeUnit.MILLISECONDS);
  }

  // for testing
  ScheduledThreadPoolExecutor getTaskPool() {
    return this.taskPool;
  }
}
