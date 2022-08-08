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

package org.apache.servicecomb.demo.jaxrs.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.springframework.stereotype.Component;

@Component
public class TestCodeFirstJaxrsReactive implements CategorizedTestCase {
  interface AddOperation {
    CompletableFuture<Integer> add(int a, int b);
  }

  @RpcReference(microserviceName = "jaxrs", schemaId = "codeFirst")
  AddOperation addOperation;

  @Override
  public void testAllTransport() throws Exception {
    final int count = 10;
    CountDownLatch latch = new CountDownLatch(count);
    AtomicInteger result = new AtomicInteger(0);

    for (int i = 0; i < count; i++) {
      new Thread(() -> addOperation.add(1, 2)
          .whenComplete((r, e) -> addOperation.add(r, r).whenComplete((r1, e1) -> {
            result.addAndGet(r1);
            latch.countDown();
          }))).start();
    }

    latch.await(3, TimeUnit.SECONDS);
    TestMgr.check(count * 6, result.get());
  }
}
