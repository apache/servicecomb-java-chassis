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

package org.apache.servicecomb.governance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.servicecomb.governance.handler.FaultInjectionHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.processor.injection.Fault;
import org.apache.servicecomb.governance.processor.injection.FaultInjectionDecorators;
import org.apache.servicecomb.governance.processor.injection.FaultInjectionDecorators.FaultInjectionDecorateCheckedSupplier;
import org.apache.servicecomb.governance.processor.injection.FaultInjectionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {GovernanceConfiguration.class, MockConfiguration.class})
public class FaultInjectionTest {
  private FaultInjectionHandler faultInjectionHandler;

  private FaultInjectionHandler faultInjectionHandler2;

  @Autowired
  public void setFaultInjectionHandler(FaultInjectionHandler faultInjectionHandler,
      @Qualifier("faultInjectionHandler2") FaultInjectionHandler faultInjectionHandler2) {
    this.faultInjectionHandler = faultInjectionHandler;
    this.faultInjectionHandler2 = faultInjectionHandler2;
  }

  public FaultInjectionTest() {
  }

  @Test
  public void test_delay_fault_injection_service_name_work() throws Throwable {
    FaultInjectionDecorateCheckedSupplier<Object> ds =
        FaultInjectionDecorators.ofCheckedSupplier(() -> "test");

    GovernanceRequest request = new GovernanceRequest();
    request.setUri("/faultInjectDelay");
    request.setServiceName("srcService");

    Fault fault = faultInjectionHandler.getActuator(request);
    ds.withFaultInjection(fault);

    Assertions.assertEquals("test", ds.get());

    // flow control
    CountDownLatch cd = new CountDownLatch(10);
    AtomicBoolean expected = new AtomicBoolean(false);
    AtomicBoolean notExpected = new AtomicBoolean(false);
    for (int i = 0; i < 10; i++) {
      new Thread(() -> {
        try {
          long startTime = System.currentTimeMillis();
          Object result = ds.get();
          if (!"test".equals(result)) {
            notExpected.set(true);
          }
          // delayTime is 2S
          if (System.currentTimeMillis() - startTime > 1000) {
            expected.set(true);
          }
        } catch (Throwable e) {
          notExpected.set(true);
        }
        cd.countDown();
      }).start();
    }
    //timeout should be bigger than delayTime
    cd.await(10, TimeUnit.SECONDS);
    Assertions.assertFalse(notExpected.get());
    Assertions.assertTrue(expected.get());
  }

  @Test
  public void test_abort_fault_injection_service_name_work() throws Throwable {
    FaultInjectionDecorateCheckedSupplier<Object> ds =
        FaultInjectionDecorators.ofCheckedSupplier(() -> "test");

    GovernanceRequest request = new GovernanceRequest();
    request.setUri("/faultInjectAbort");
    request.setServiceName("srcService");

    Fault fault = faultInjectionHandler.getActuator(request);
    ds.withFaultInjection(fault);

    Assertions.assertEquals("test", ds.get());

    // flow control
    CountDownLatch cd = new CountDownLatch(10);
    AtomicBoolean expected = new AtomicBoolean(false);
    AtomicBoolean notExpected = new AtomicBoolean(false);
    for (int i = 0; i < 10; i++) {
      new Thread(() -> {
        try {
          Object result = ds.get();
          if (!"test".equals(result)) {
            notExpected.set(true);
          }
        } catch (FaultInjectionException e) {
          expected.set(true);
        } catch (Throwable e) {
          notExpected.set(true);
        }
        cd.countDown();
      }).start();
    }
    cd.await(1, TimeUnit.SECONDS);
    Assertions.assertFalse(notExpected.get());
    Assertions.assertTrue(expected.get());
  }

  @Test
  public void test_fallback_returnNull_work() throws Throwable {
    FaultInjectionDecorateCheckedSupplier<Object> ds =
        FaultInjectionDecorators.ofCheckedSupplier(() -> "test");

    GovernanceRequest request = new GovernanceRequest();
    request.setUri("/returnNull");
    request.setServiceName("returnNull");

    Fault fault = faultInjectionHandler.getActuator(request);
    ds.withFaultInjection(fault);
    Assertions.assertEquals(null, ds.get());
  }

  @Test
  public void test_fallback_ThrowException_work() throws Throwable {
    FaultInjectionDecorateCheckedSupplier<Object> ds =
        FaultInjectionDecorators.ofCheckedSupplier(() -> "test");

    GovernanceRequest request = new GovernanceRequest();
    request.setUri("/throwException");
    request.setServiceName("ThrowException");

    Fault fault = faultInjectionHandler.getActuator(request);
    ds.withFaultInjection(fault);
    boolean expected = false;
    try {
      ds.get();
    } catch (FaultInjectionException e) {
      if (e.getFaultResponse().getErrorCode() == 500) {
        expected = true;
      }
    }
    Assertions.assertEquals(true, expected);
  }

  @Test
  public void test_fallback_forceClosed_work() throws Throwable {
    FaultInjectionDecorateCheckedSupplier<Object> ds =
        FaultInjectionDecorators.ofCheckedSupplier(() -> "test");

    GovernanceRequest request = new GovernanceRequest();
    request.setUri("/forceClosed");
    request.setServiceName("forceClosed");

    Fault fault = faultInjectionHandler.getActuator(request);
    ds.withFaultInjection(fault);
    Assertions.assertEquals("test", ds.get());
  }

  @Test
  public void test_fallback_ThrowException_work_handler2() throws Throwable {
    FaultInjectionDecorateCheckedSupplier<Object> ds =
        FaultInjectionDecorators.ofCheckedSupplier(() -> "test");

    GovernanceRequest request = new GovernanceRequest();
    request.setUri("/throwException");
    request.setServiceName("ThrowException");

    Fault fault = faultInjectionHandler2.getActuator(request);
    ds.withFaultInjection(fault);
    boolean expected = false;
    try {
      ds.get();
    } catch (FaultInjectionException e) {
      if (e.getFaultResponse().getErrorCode() == 500) {
        expected = true;
      }
    }
    Assertions.assertEquals(true, expected);
  }
}
