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
import org.apache.servicecomb.governance.handler.RateLimitingHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.injection.Fault;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;

@SpringBootTest
@ContextConfiguration(classes = {GovernanceConfiguration.class, MockConfiguration.class})
public class FlowControlTest {
  private RateLimitingHandler rateLimitingHandler;

  private FaultInjectionHandler faultInjectionHandler;

  @Autowired
  public void setRateLimitingHandler(RateLimitingHandler rateLimitingHandler) {
    this.rateLimitingHandler = rateLimitingHandler;
  }

  @Autowired
  public void setFaultInjectionHandler(FaultInjectionHandler faultInjectionHandler) {
    this.faultInjectionHandler = faultInjectionHandler;
  }

  public FlowControlTest() {
  }

  @Test
  public void test_rate_limiting_work() throws Throwable {
    DecorateCheckedSupplier<Object> ds = Decorators.ofCheckedSupplier(() -> "test");

    GovernanceRequest request = new GovernanceRequest();
    request.setUri("/hello");

    RateLimiter rateLimiter = rateLimitingHandler.getActuator(request);
    ds.withRateLimiter(rateLimiter);

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
        } catch (Throwable e) {
          if (e instanceof RequestNotPermitted) {
            expected.set(true);
          } else {
            notExpected.set(true);
          }
        }
        cd.countDown();
      }).start();
    }
    cd.await(1, TimeUnit.SECONDS);
    Assertions.assertTrue(expected.get());
    Assertions.assertFalse(notExpected.get());
  }

  @Test
  public void test_rate_limiting_service_name_work() throws Throwable {
    DecorateCheckedSupplier<Object> ds = Decorators.ofCheckedSupplier(() -> "test");

    GovernanceRequest request = new GovernanceRequest();
    request.setUri("/helloServiceName");
    request.setServiceName("srcService");

    RateLimiter rateLimiter = rateLimitingHandler.getActuator(request);
    ds.withRateLimiter(rateLimiter);

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
        } catch (Throwable e) {
          if (e instanceof RequestNotPermitted) {
            expected.set(true);
          } else {
            notExpected.set(true);
          }
        }
        cd.countDown();
      }).start();
    }
    cd.await(1, TimeUnit.SECONDS);
    Assertions.assertTrue(expected.get());
    Assertions.assertFalse(notExpected.get());
  }

  @Test
  public void test_delay_fault_injection_service_name_work() throws Throwable {
    DecorateCheckedSupplier<Object> ds = Decorators.ofCheckedSupplier(() -> "test");

    GovernanceRequest request = new GovernanceRequest();
    request.setUri("/faultInjectDelay");
    request.setServiceName("srcService");

    Fault fault = faultInjectionHandler.getActuator(request);

    Assertions.assertEquals("test", ds.get());

    // flow control
    CountDownLatch cd = new CountDownLatch(10);
    AtomicBoolean notExpected = new AtomicBoolean(false);
    AtomicBoolean delayExpected = new AtomicBoolean(false);
    AtomicBoolean abortExpected = new AtomicBoolean(false);
    for (int i = 0; i < 10; i++) {
      new Thread(() -> {
        fault.injectFault(faultResponse -> {
          if (faultResponse.isDelay()) {
            delayExpected.set(true);
          } else if (!faultResponse.isSuccess()) {
            abortExpected.set(true);
          } else {
            try {
              Object result = ds.get();
              if (!"test".equals(result)) {
                notExpected.set(true);
              }
            } catch (Throwable e) {
              notExpected.set(true);
            }
          }
          cd.countDown();
        });
      }).start();
    }
    //timeout should be bigger than delayTime
    cd.await(10, TimeUnit.SECONDS);
    Assertions.assertFalse(notExpected.get());
    Assertions.assertFalse(abortExpected.get());
    Assertions.assertTrue(delayExpected.get());
  }

  @Test
  public void test_abort_fault_injection_service_name_work() throws Throwable {
    DecorateCheckedSupplier<Object> ds = Decorators.ofCheckedSupplier(() -> "test");

    GovernanceRequest request = new GovernanceRequest();
    request.setUri("/faultInjectAbort");
    request.setServiceName("srcService");

    Fault fault = faultInjectionHandler.getActuator(request);

    Assertions.assertEquals("test", ds.get());

    // flow control
    CountDownLatch cd = new CountDownLatch(10);
    AtomicBoolean notExpected = new AtomicBoolean(false);
    AtomicBoolean delayExpected = new AtomicBoolean(false);
    AtomicBoolean abortExpected = new AtomicBoolean(false);
    for (int i = 0; i < 10; i++) {
      new Thread(() -> {
        fault.injectFault(faultResponse -> {
          if (faultResponse.isDelay()) {
            delayExpected.set(true);
          } else if (!faultResponse.isSuccess()) {
            abortExpected.set(true);
          } else {
            try {
              Object result = ds.get();
              if (!"test".equals(result)) {
                notExpected.set(true);
              }
            } catch (Throwable e) {
              notExpected.set(true);
            }
          }
          cd.countDown();
        });
      }).start();
    }
    cd.await(1000, TimeUnit.SECONDS);
    Assertions.assertFalse(notExpected.get());
    Assertions.assertTrue(abortExpected.get());
    Assertions.assertFalse(delayExpected.get());
  }
}
