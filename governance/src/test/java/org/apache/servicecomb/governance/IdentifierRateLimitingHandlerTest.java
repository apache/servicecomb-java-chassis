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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.servicecomb.governance.handler.IdentifierRateLimitingHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
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
public class IdentifierRateLimitingHandlerTest {
  private IdentifierRateLimitingHandler rateLimitingHandler;

  @Autowired
  public void setRateLimitingHandler(IdentifierRateLimitingHandler rateLimitingHandler) {
    this.rateLimitingHandler = rateLimitingHandler;
  }

  public IdentifierRateLimitingHandlerTest() {
  }

  @Test
  public void test_rate_limiting_work() throws Throwable {
    DecorateCheckedSupplier<Object> ds = Decorators.ofCheckedSupplier(() -> "test");

    GovernanceRequest request = new GovernanceRequest();
    request.setUri("/hello");
    Map<String, String> headers = new HashMap<>();
    headers.put("test", "1234");
    request.setHeaders(headers);

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
    Map<String, String> headers = new HashMap<>();
    headers.put("test", "1234");
    request.setHeaders(headers);

    RateLimiter rateLimiter = rateLimitingHandler.getActuator(request);
    ds.withRateLimiter(rateLimiter);

    Assertions.assertEquals("test", ds.get());

    headers = new HashMap<>();
    headers.put("test", "12345");
    request.setHeaders(headers);

    RateLimiter rateLimiter2 = rateLimitingHandler.getActuator(request);
    DecorateCheckedSupplier<Object> ds2 = Decorators.ofCheckedSupplier(() -> "test2");
    ds2.withRateLimiter(rateLimiter2);
    Assertions.assertEquals("test2", ds2.get());

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
}
