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

import org.apache.servicecomb.governance.handler.RateLimitingHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.policy.RateLimitingPolicy;
import org.apache.servicecomb.governance.properties.RateLimitProperties;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/*.xml", initializers = ConfigFileApplicationContextInitializer.class)
public class FlowControlTest {
  @Autowired
  private RateLimitingHandler rateLimitingHandler;

  @Autowired
  private RateLimitProperties rateLimitProperties;

  @Autowired
  private MatchersManager matchersManager;

  @Test
  public void test_rate_limiting_work() throws Throwable {
    DecorateCheckedSupplier<Object> ds = Decorators.ofCheckedSupplier(() -> {
      return "test";
    });

    GovernanceRequest request = new GovernanceRequest();
    request.setUri("/hello");
    RateLimitingPolicy policy = matchersManager.match(request, rateLimitProperties.getParsedEntity());
    Assert.assertNotNull(policy);
    RateLimiter rateLimiter = rateLimitingHandler.getActuator(policy);
    ds.withRateLimiter(rateLimiter);

    Assert.assertEquals("test", ds.get());

    // flow control
    CountDownLatch cd = new CountDownLatch(10);
    AtomicBoolean expected = new AtomicBoolean(false);
    AtomicBoolean notExpected = new AtomicBoolean(false);
    for (int i = 0; i < 10; i++) {
      new Thread() {
        public void run() {
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
        }
      }.start();
    }
    cd.await(1, TimeUnit.SECONDS);
    Assert.assertTrue(expected.get());
    Assert.assertFalse(notExpected.get());
  }
}
