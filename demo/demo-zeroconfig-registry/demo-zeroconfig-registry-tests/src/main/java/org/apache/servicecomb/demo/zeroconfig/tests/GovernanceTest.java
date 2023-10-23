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

package org.apache.servicecomb.demo.zeroconfig.tests;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;

@Component
public class GovernanceTest implements CategorizedTestCase {
  String url = "servicecomb://demo-zeroconfig-registry-client/governance";

  RestOperations template = RestTemplateBuilder.create();

  @Override
  public void testRestTransport() throws Exception {
    testCircuitBreaker();
    testBulkhead();
    testRateLimitingRest();
    testRateLimitingRpc();
    testRetryRest();
    testRetryRestNoPrefix();
    testRetryRpc();
  }

  private void testRetryRestNoPrefix() {
    testRetry("/noPrefixRetry");
  }

  private void testRetryRest() {
    testRetry("/retry");
  }

  private void testRetryRpc() {
    testRetry("/retryRpc");
  }

  private void testRetry(String operation) {
    String invocationID = UUID.randomUUID().toString();
    String result = template.getForObject(url + operation + "?invocationID={1}", String.class, invocationID);
    TestMgr.check(result, "try times: 3");
  }

  private void testCircuitBreaker() throws Exception {
    CountDownLatch latch = new CountDownLatch(100);
    AtomicBoolean expectedFailed = new AtomicBoolean(false);
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);

    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        String name = "t-" + i + "-" + j;
        new Thread(name) {
          public void run() {
            try {
              String result = template.getForObject(url + "/circuitBreaker", String.class);
              if (!"ok".equals(result)) {
                notExpectedFailed.set(true);
              }
            } catch (Exception e) {
              if ("InvocationException: code=429;msg=CommonExceptionData [message=circuitBreaker is open.]"
                  .equals(e.getMessage())) {
                expectedFailed.set(true);
              } else if ("InvocationException: code=503;msg=CommonExceptionData [message=test error]"
                  .equals(e.getMessage())) {
                // by pass
              } else {
                notExpectedFailed.set(true);
              }
            }
            latch.countDown();
          }
        }.start();
      }
      Thread.sleep(100);
    }

    latch.await(20, TimeUnit.SECONDS);
    TestMgr.check(true, expectedFailed.get());
    TestMgr.check(false, notExpectedFailed.get());
  }

  private void testBulkhead() throws Exception {
    CountDownLatch latch = new CountDownLatch(100);
    AtomicBoolean expectedFailed = new AtomicBoolean(false);
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);

    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        String name = "t-" + i + "-" + j;
        new Thread(name) {
          public void run() {
            try {
              String result = template.getForObject(url + "/bulkhead", String.class);
              if (!"Hello world!".equals(result)) {
                notExpectedFailed.set(true);
              }
            } catch (Exception e) {
              if (!"InvocationException: code=429;msg=CommonExceptionData [message=bulkhead is full and does not permit further calls.]"
                  .equals(e.getMessage())) {
                notExpectedFailed.set(true);
              }
              expectedFailed.set(true);
            }
            latch.countDown();
          }
        }.start();
      }
      Thread.sleep(100);
    }

    latch.await(20, TimeUnit.SECONDS);
    TestMgr.check(true, expectedFailed.get());
    TestMgr.check(false, notExpectedFailed.get());
  }

  private void testRateLimitingRest() throws Exception {
    testRateLimiting("/hello");
  }

  private void testRateLimitingRpc() throws Exception {
    testRateLimiting("/helloRpc");
  }

  private void testRateLimiting(String operation) throws Exception {
    CountDownLatch latch = new CountDownLatch(100);
    AtomicBoolean expectedFailed = new AtomicBoolean(false);
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);

    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        String name = "t-" + i + "-" + j;
        new Thread(name) {
          public void run() {
            try {
              String result = template.getForObject(url + operation, String.class);
              if (!"Hello world!".equals(result)) {
                notExpectedFailed.set(true);
              }
            } catch (Exception e) {
              if (!"InvocationException: code=429;msg=CommonExceptionData [message=rate limited.]"
                  .equals(e.getMessage())) {
                notExpectedFailed.set(true);
              }
              expectedFailed.set(true);
            }
            latch.countDown();
          }
        }.start();
      }
      Thread.sleep(100);
    }

    latch.await(20, TimeUnit.SECONDS);
    TestMgr.check(true, expectedFailed.get());
    TestMgr.check(false, notExpectedFailed.get());
  }
}
