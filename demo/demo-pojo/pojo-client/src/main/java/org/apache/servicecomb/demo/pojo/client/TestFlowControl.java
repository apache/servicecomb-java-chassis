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

package org.apache.servicecomb.demo.pojo.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.stereotype.Component;

@Component("TestFlowControl")
public class TestFlowControl {
  interface Client {
    int foo(int num);

    int bar(int num);
  }

  @RpcReference(microserviceName = "pojo", schemaId = "FlowControlSchema")
  Client client1;

  @RpcReference(microserviceName = "pojo", schemaId = "FlowControlClientSchema")
  Client client2;

  public void testAllTransport() throws Exception {
    // 1.3.2 未统一。 2.1.5 统一了。
    String serverMsg = "InvocationException: code=429;msg={message=provider request rejected by qps flowcontrol}";
    String clientMsg = "InvocationException: code=429;msg=CommonExceptionData [message=consumer request rejected by qps flowcontrol]";

    testFlowControl((num) -> client1.foo(num), true, serverMsg);
    testFlowControl((num) -> client1.bar(num), false, serverMsg);
    testFlowControl((num) -> client2.foo(num), true, clientMsg);
    testFlowControl((num) -> client2.bar(num), false, clientMsg);
  }

  private void testFlowControl(Function<Integer, Integer> function, boolean expected, String message)
      throws InterruptedException {
    AtomicBoolean failed = new AtomicBoolean(false);
    CountDownLatch countDownLatch = new CountDownLatch(10);
    for (int i = 0; i < 10; i++) {
      new Thread() {
        public void run() {
          for (int i = 0; i < 10; i++) {
            try {
              int result = function.apply(10);
              if (result != 10) {
                TestMgr.failed("", new Exception("not expected"));
              }
            } catch (InvocationException e) {
              TestMgr.check(e.getStatusCode(), 429);
              TestMgr.check(e.getMessage(), message);
              failed.set(true);
              break;
            }
          }
          countDownLatch.countDown();
        }
      }.start();
    }
    countDownLatch.await(10, TimeUnit.SECONDS);
    TestMgr.check(expected, failed.get());
  }
}
