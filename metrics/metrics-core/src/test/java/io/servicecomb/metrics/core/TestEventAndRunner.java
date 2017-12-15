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

package io.servicecomb.metrics.core;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.core.metrics.InvocationFinishedEvent;
import io.servicecomb.core.metrics.InvocationStartProcessingEvent;
import io.servicecomb.core.metrics.InvocationStartedEvent;
import io.servicecomb.foundation.common.utils.EventUtils;
import io.servicecomb.metrics.core.event.DefaultEventListenerManager;
import io.servicecomb.metrics.core.model.RegistryMetricsModel;
import io.servicecomb.metrics.core.schedule.DefaultStatisticsRunner;
import io.servicecomb.swagger.invocation.InvocationType;

public class TestEventAndRunner {

  @Test
  public void test() throws InterruptedException {

    DefaultEventListenerManager manager = new DefaultEventListenerManager();

    EventUtils.triggerEvent(new InvocationStartedEvent("fun1", System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun1", InvocationType.PRODUCER, System.nanoTime(),
            TimeUnit.MILLISECONDS.toNanos(100)));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun1", InvocationType.PRODUCER, System.nanoTime(),
            TimeUnit.MILLISECONDS.toNanos(200), TimeUnit.MILLISECONDS.toNanos(300)));

    EventUtils.triggerEvent(new InvocationStartedEvent("fun1", System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun1", InvocationType.PRODUCER, System.nanoTime(),
            TimeUnit.MILLISECONDS.toNanos(300)));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun1", InvocationType.PRODUCER, System.nanoTime(),
            TimeUnit.MILLISECONDS.toNanos(400), TimeUnit.MILLISECONDS.toNanos(700)));

    EventUtils.triggerEvent(new InvocationStartedEvent("fun12", System.nanoTime()));
    EventUtils.triggerEvent(
        new InvocationStartProcessingEvent("fun12", InvocationType.PRODUCER, System.nanoTime(),
            TimeUnit.MILLISECONDS.toNanos(500)));
    EventUtils
        .triggerEvent(new InvocationFinishedEvent("fun12", InvocationType.PRODUCER, System.nanoTime(),
            TimeUnit.MILLISECONDS.toNanos(600), TimeUnit.MILLISECONDS.toNanos(1100)));

    EventUtils.triggerEvent(new InvocationStartedEvent("fun11", System.nanoTime()));

    DefaultStatisticsRunner runner = new DefaultStatisticsRunner();

    runner.run();

    RegistryMetricsModel model = runner.getRegistryModel();

    Assert.assertEquals(model.getInvocations().get("fun1").getCountInQueue(), 0);
    Assert.assertEquals(model.getInvocations().get("fun11").getCountInQueue(), 1);
    Assert.assertEquals(model.getInstanceModel().getCountInQueue(), 1);

    Assert.assertEquals(model.getInvocations().get("fun1").getLifeTimeInQueueMin(), TimeUnit.MILLISECONDS.toNanos(100),
        0);
    Assert.assertEquals(model.getInvocations().get("fun1").getLifeTimeInQueueMax(), TimeUnit.MILLISECONDS.toNanos(300),
        0);
    Assert.assertEquals(
        model.getInvocations().get("fun1").getLifeTimeInQueueAverage(), TimeUnit.MILLISECONDS.toNanos(200), 0);
    Assert.assertEquals(model.getInstanceModel().getLifeTimeInQueueMin(), TimeUnit.MILLISECONDS.toNanos(100), 0);
    Assert.assertEquals(model.getInstanceModel().getLifeTimeInQueueMax(), TimeUnit.MILLISECONDS.toNanos(500), 0);
    Assert.assertEquals(model.getInstanceModel().getLifeTimeInQueueAverage(), TimeUnit.MILLISECONDS.toNanos(300), 0);

    Assert
        .assertEquals(model.getInvocations().get("fun1").getExecutionTimeMin(), TimeUnit.MILLISECONDS.toNanos(200), 0);
    Assert
        .assertEquals(model.getInvocations().get("fun1").getExecutionTimeMax(), TimeUnit.MILLISECONDS.toNanos(400), 0);
    Assert
        .assertEquals(model.getInvocations().get("fun1").getExecutionTimeAverage(), TimeUnit.MILLISECONDS.toNanos(300),
            0);
    Assert.assertEquals(model.getInstanceModel().getExecutionTimeMin(), TimeUnit.MILLISECONDS.toNanos(200), 0);
    Assert.assertEquals(model.getInstanceModel().getExecutionTimeMax(), TimeUnit.MILLISECONDS.toNanos(600), 0);
    Assert.assertEquals(model.getInstanceModel().getExecutionTimeAverage(), TimeUnit.MILLISECONDS.toNanos(400), 0);
  }
}
