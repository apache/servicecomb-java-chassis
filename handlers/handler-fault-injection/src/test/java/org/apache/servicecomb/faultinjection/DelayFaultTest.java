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

package org.apache.servicecomb.faultinjection;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.netflix.config.DynamicProperty;

import io.vertx.core.Vertx;

public class DelayFaultTest {
  private Invocation invocation;

  @SuppressWarnings("unchecked")
  @BeforeEach
  public void before() {
    ArchaiusUtils.resetConfig();
    FaultInjectionConfig.getCfgCallback().clear();
    FaultInjectionUtil.getRequestCount().clear();
    FaultInjectionUtil.getConfigCenterValue().clear();

    invocation = Mockito.mock(Invocation.class);
    Transport transport = Mockito.mock(Transport.class);
    Mockito.when(invocation.getMicroserviceQualifiedName()).thenReturn("MicroserviceQualifiedName12");
    Mockito.when(invocation.getTransport()).thenReturn(transport);
    Mockito.when(transport.getName()).thenReturn("rest");
    Mockito.when(invocation.getOperationName()).thenReturn("sayBye4");
    Mockito.when(invocation.getSchemaId()).thenReturn("testSchema4");
    Mockito.when(invocation.getMicroserviceName()).thenReturn("carts6");
  }

  @AfterEach
  public void after() {
    ArchaiusUtils.resetConfig();
  }

  @AfterAll
  public static void classTeardown() {
    VertxUtils.blockCloseVertxByName("faultinjectionTest");
  }

  @Test
  public void injectFaultVertxDelay() throws InterruptedException {
    ArchaiusUtils
        .setProperty("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.delay.fixedDelay", "10");
    ArchaiusUtils
        .setProperty("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.delay.percent", "100");

    Assertions.assertEquals("10", DynamicProperty
        .getInstance("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.delay.fixedDelay")
        .getString());
    Assertions.assertEquals("100", DynamicProperty
        .getInstance("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.delay.percent")
        .getString());

    DelayFault delayFault = new DelayFault();
    FaultParam faultParam = new FaultParam(1);
    Vertx vertx = VertxUtils.getOrCreateVertxByName("faultinjectionTest", null);
    faultParam.setVertx(vertx);

    Holder<String> resultHolder = new Holder<>();
    CountDownLatch latch = new CountDownLatch(1);
    delayFault.injectFault(invocation, faultParam, response -> {
      resultHolder.value = response.getResult();
      latch.countDown();
    });

    latch.await(10, TimeUnit.SECONDS);
    AtomicLong count = FaultInjectionUtil.getOperMetTotalReq("restMicroserviceQualifiedName12");
    Assertions.assertEquals(1, count.get());
    Assertions.assertEquals("success", resultHolder.value);
  }

  @Test
  public void injectFaultSystemDelay() throws InterruptedException {
    ArchaiusUtils
        .setProperty("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.delay.fixedDelay", "10");
    ArchaiusUtils
        .setProperty("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.delay.percent", "100");

    Assertions.assertEquals("10", DynamicProperty
        .getInstance("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.delay.fixedDelay")
        .getString());
    Assertions.assertEquals("100", DynamicProperty
        .getInstance("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.delay.percent")
        .getString());

    DelayFault delayFault = new DelayFault();
    FaultParam faultParam = new FaultParam(1);

    Holder<String> resultHolder = new Holder<>();
    CountDownLatch latch = new CountDownLatch(1);
    delayFault.injectFault(invocation, faultParam, response -> {
      resultHolder.value = response.getResult();
      latch.countDown();
    });

    latch.await(10, TimeUnit.SECONDS);
    AtomicLong count = FaultInjectionUtil.getOperMetTotalReq("restMicroserviceQualifiedName12");
    Assertions.assertEquals(1, count.get());
    Assertions.assertEquals("success", resultHolder.value);
  }

  @Test
  public void injectFaultNotDelay() throws InterruptedException {
    ArchaiusUtils
        .setProperty("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.delay.fixedDelay", "10");
    ArchaiusUtils.setProperty("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.delay.percent", "0");

    Assertions.assertEquals("10", DynamicProperty
        .getInstance("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.delay.fixedDelay")
        .getString());
    Assertions.assertEquals("0", DynamicProperty
        .getInstance("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.delay.percent")
        .getString());

    DelayFault delayFault = new DelayFault();
    FaultParam faultParam = new FaultParam(1);
    Vertx vertx = VertxUtils.getOrCreateVertxByName("faultinjectionTest", null);
    faultParam.setVertx(vertx);

    Holder<String> resultHolder = new Holder<>();
    CountDownLatch latch = new CountDownLatch(1);
    delayFault.injectFault(invocation, faultParam, response -> {
      resultHolder.value = response.getResult();
      latch.countDown();
    });

    latch.await(3, TimeUnit.SECONDS);
    AtomicLong count = FaultInjectionUtil.getOperMetTotalReq("restMicroserviceQualifiedName12");
    Assertions.assertEquals(1, count.get());
    Assertions.assertEquals("success", resultHolder.value);
  }

  @Test
  public void injectFaultNoPercentageConfig() {
    ArchaiusUtils
        .setProperty("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.delay.percent", null);

    Assertions.assertNull(DynamicProperty
        .getInstance("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.delay.percent")
        .getString());

    DelayFault delayFault = new DelayFault();
    FaultParam faultParam = new FaultParam(1);
    Vertx vertx = VertxUtils.getOrCreateVertxByName("faultinjectionTest", null);
    faultParam.setVertx(vertx);

    Holder<String> resultHolder = new Holder<>();
    delayFault.injectFault(invocation, faultParam, response -> resultHolder.value = response.getResult());

    AtomicLong count = FaultInjectionUtil.getOperMetTotalReq("restMicroserviceQualifiedName12");
    Assertions.assertEquals(1, count.get());
    Assertions.assertEquals("success", resultHolder.value);
  }

  @Test
  public void injectFaultNoDelayMsConfig() {
    ArchaiusUtils
        .setProperty("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.delay.percent", "10");

    Assertions.assertEquals("10", DynamicProperty
        .getInstance("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.delay.percent")
        .getString());

    DelayFault delayFault = new DelayFault();
    FaultParam faultParam = new FaultParam(10);
    Vertx vertx = VertxUtils.getOrCreateVertxByName("faultinjectionTest", null);
    faultParam.setVertx(vertx);

    Holder<String> resultHolder = new Holder<>();
    delayFault.injectFault(invocation, faultParam, response -> resultHolder.value = response.getResult());

    AtomicLong count = FaultInjectionUtil.getOperMetTotalReq("restMicroserviceQualifiedName12");
    Assertions.assertEquals(1, count.get());
    Assertions.assertEquals("success", resultHolder.value);
  }
}
