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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.client.ServiceRegistryClient;

public abstract class AbstractRegisterTask extends AbstractTask {
    protected boolean registered;

    public AbstractRegisterTask(EventBus eventBus, ServiceRegistryClient srClient, Microservice microservice) {
        super(eventBus, srClient, microservice);
    }

    @Subscribe
    public void onHeartbeatEvent(MicroserviceInstanceHeartbeatTask task) {
        if (task.isNeedRegisterInstance() && isSameMicroservice(task.getMicroservice())) {
            this.registered = false;
        }
    }

    public boolean isRegistered() {
        return registered;
    }

    @Override
    public void run() {
        if (registered) {
            return;
        }

        registered = doRegister();
        eventBus.post(this);
    }

    protected abstract boolean doRegister();
}
