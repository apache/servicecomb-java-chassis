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
package org.apache.servicecomb.demo.filter.tests;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.springframework.stereotype.Component;

@Component
public class TestGovernanceSchemaFromEdge implements CategorizedTestCase {
  interface GovernanceEdgeSchemaInf {
    boolean edgeFlowControl();

    boolean providerFlowControl();
  }

  @RpcReference(microserviceName = "filterEdge", schemaId = "GovernanceConsumerSchema")
  private GovernanceEdgeSchemaInf retrySchemaInf;

  @Override
  public String getMicroserviceName() {
    return "filterEdge";
  }

  @Override
  public void testRestTransport() throws Exception {
    testEdgeFlowControl();
    testConsumerFlowControl();
  }

  private void testConsumerFlowControl() throws Exception {
    CountDownLatch latch = new CountDownLatch(100);
    AtomicBoolean expectedFailed = new AtomicBoolean(false);
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);

    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        String name = "t-" + i + "-" + j;
        new Thread(name) {
          public void run() {
            try {
              boolean result = retrySchemaInf.providerFlowControl();
              if (!result) {
                notExpectedFailed.set(true);
              }
            } catch (Exception e) {
              if (!e.getMessage().contains("rate limited")) {
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
    TestMgr.check(expectedFailed.get(), true);
    TestMgr.check(notExpectedFailed.get(), false);
  }

  private void testEdgeFlowControl() throws Exception {
    CountDownLatch latch = new CountDownLatch(100);
    AtomicBoolean expectedFailed = new AtomicBoolean(false);
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);

    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        String name = "t-" + i + "-" + j;
        new Thread(name) {
          public void run() {
            try {
              boolean result = retrySchemaInf.edgeFlowControl();
              if (!result) {
                notExpectedFailed.set(true);
              }
            } catch (Exception e) {
              if (!e.getMessage().contains("rate limited")) {
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
    TestMgr.check(expectedFailed.get(), true);
    TestMgr.check(notExpectedFailed.get(), false);
  }
}
