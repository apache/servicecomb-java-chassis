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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.eventbus.EventBus;

import io.servicecomb.serviceregistry.ArchaiusUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.client.ServiceRegistryClient;
import io.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import io.servicecomb.serviceregistry.task.event.ShutdownEvent;
import mockit.Deencapsulation;
import mockit.Mocked;

public class TestServiceCenterTask {
    private EventBus eventBus = new EventBus();

    @Mocked
    private MicroserviceServiceCenterTask microserviceServiceCenterTask;

    private ServiceCenterTask serviceCenterTask;

    @BeforeClass
    public static void initClass() {
        ArchaiusUtils.resetConfig();
    }

    @AfterClass
    public static void teardownClass() {
        ArchaiusUtils.resetConfig();
    }

    @Before
    public void init() {
        serviceCenterTask =
            new ServiceCenterTask(eventBus, ServiceRegistryConfig.INSTANCE, microserviceServiceCenterTask);
    }

    @Test
    public void testLifeCycle() {
        serviceCenterTask.init();

        eventBus.post(new ShutdownEvent());
        Assert.assertFalse(Deencapsulation.getField(serviceCenterTask, "running"));
    }

    @Test
    public void testCalcSleepInterval(@Mocked ServiceRegistryClient srClient,
            @Mocked Microservice microservice, @Mocked MicroserviceInstanceHeartbeatTask heartbeatTask,
            @Mocked MicroserviceInstanceRegisterTask registerTask) {
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(1, serviceCenterTask.getInterval());
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(2, serviceCenterTask.getInterval());
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(3, serviceCenterTask.getInterval());
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(10, serviceCenterTask.getInterval());
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(20, serviceCenterTask.getInterval());
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(30, serviceCenterTask.getInterval());
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(40, serviceCenterTask.getInterval());
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(50, serviceCenterTask.getInterval());
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(60, serviceCenterTask.getInterval());
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(60, serviceCenterTask.getInterval());

        eventBus.post(heartbeatTask);
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(60, serviceCenterTask.getInterval());

        // recover and exception again
        registerTask.taskStatus = TaskStatus.FINISHED;
        eventBus.post(registerTask);
        registerTask.taskStatus = TaskStatus.INIT;
        eventBus.post(registerTask);
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(1, serviceCenterTask.getInterval());

        registerTask.taskStatus = TaskStatus.FINISHED;
        eventBus.post(registerTask);
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(30, serviceCenterTask.getInterval());

        registerTask.taskStatus = TaskStatus.INIT;
        eventBus.post(registerTask);
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(1, serviceCenterTask.getInterval());
    }
}
