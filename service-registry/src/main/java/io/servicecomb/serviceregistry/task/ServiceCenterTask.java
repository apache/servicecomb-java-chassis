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
package io.servicecomb.serviceregistry.task;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import io.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import io.servicecomb.serviceregistry.task.event.ShutdownEvent;

public class ServiceCenterTask implements Runnable {
    private EventBus eventBus;

    private ServiceRegistryConfig serviceRegistryConfig;

    private CompositeTask compositeTask = new CompositeTask();

    // microservice name
    private Set<String> registerInstanceSuccessMap = new HashSet<>();

    private volatile boolean running = true;

    // for fast recovery, register interval is different with heartbeat interval
    private int[] registerIntervalSeconds = new int[] {1, 2, 3, 10, 20, 30, 40, 50, 60};

    private int registerRetryCount;

    private int interval;

    public ServiceCenterTask(EventBus eventBus, ServiceRegistryConfig serviceRegistryConfig) {
        this.eventBus = eventBus;
        this.serviceRegistryConfig = serviceRegistryConfig;

        this.eventBus.register(this);
    }

    public int getInterval() {
        return interval;
    }

    @Subscribe
    public void onShutdown(ShutdownEvent event) {
        this.running = false;
    }

    @Subscribe
    public void onHeartbeatEvent(MicroserviceInstanceHeartbeatTask task) {
        if (task.isNeedRegisterInstance()) {
            registerInstanceSuccessMap.remove(task.getMicroservice().getServiceName());
            registerRetryCount = 0;
        }
    }

    @Subscribe
    public void onInstanceRegisterEvent(MicroserviceInstanceRegisterTask task) {
        if (task.isRegistered()) {
            registerInstanceSuccessMap.add(task.getMicroservice().getServiceName());
        } else {
            registerInstanceSuccessMap.remove(task.getMicroservice().getServiceName());
            registerRetryCount = 0;
        }
    }

    public void addMicroserviceTask(MicroserviceServiceCenterTask task) {
        compositeTask.addTask(task);
    }

    public void init() {
        compositeTask.run();
    }

    @Override
    public void run() {
        while (running) {
            compositeTask.run();

            calcSleepInterval();
            try {
                TimeUnit.SECONDS.sleep(interval);
            } catch (InterruptedException e) {
                continue;
            }
        }
    }

    public void calcSleepInterval() {
        if (registerInstanceSuccessMap.size() == compositeTask.getTaskCount()) {
            interval = serviceRegistryConfig.getHeartbeatInterval();
            return;
        }

        if (registerRetryCount >= registerIntervalSeconds.length) {
            registerRetryCount = registerIntervalSeconds.length - 1;
        }
        interval = registerIntervalSeconds[registerRetryCount];
        registerRetryCount++;
    }
}
