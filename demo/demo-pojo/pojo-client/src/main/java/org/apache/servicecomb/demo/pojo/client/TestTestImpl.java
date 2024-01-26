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

import java.util.Arrays;

import org.apache.servicecomb.config.InMemoryDynamicPropertiesSource;
import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.server.Test;
import org.apache.servicecomb.demo.server.TestRequest;
import org.apache.servicecomb.demo.server.User;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TestTestImpl implements CategorizedTestCase {
  private static final Logger LOGGER = LoggerFactory.getLogger(TestTestImpl.class);

  private static final byte[] buffer = new byte[1024];

  static {
    Arrays.fill(buffer, (byte) 1);
  }

  @RpcReference(microserviceName = "pojo", schemaId = "server")
  private Test test;

  @Override
  public void testAllTransport() throws Exception {
    testNull(test);

    // This test case shows destroy of WeightedResponseTimeRule timer task. after test finished will not print:
    // "Weight adjusting job started" and thread "NFLoadBalancer-serverWeightTimer-unknown" destroyed.
    InMemoryDynamicPropertiesSource.update("servicecomb.loadbalance.strategy.name", "WeightedResponse");
    testStringArray(test);
    InMemoryDynamicPropertiesSource.update("servicecomb.loadbalance.strategy.name", "RoundRobin");
    testStringArray(test);

    testChinese(test);
    testStringHaveSpace(test);
    testWrapParam(test);

    testSplitParam(test);
    testInputArray(test);

    testException(test);

    testIntArray(test);
  }

  @Override
  public void testRestTransport() throws Exception {
    testNullRest(test);
    testExceptionRest(test);
    testEmptyRest(test);
  }

  @Override
  public void testHighwayTransport() throws Exception {
    testNullHighway(test);
    testEmptyHighway(test);
  }

  private static void testIntArray(Test test) {
    int[] request = new int[] {5, 11, 4};
    int[] result = test.testIntArray(request);
    TestMgr.check(request.length, result.length);
    TestMgr.check(request[1], result[1]);
  }

  private static void testEmptyHighway(Test test) {
    TestMgr.check("code is ''", test.getTestString(""));
  }

  private static void testEmptyRest(Test test) {
    TestMgr.check("code is ''", test.getTestString(""));
  }

  private static void testNullRest(Test test) {
    TestMgr.check(null, test.wrapParam(null));
  }

  private static void testNullHighway(Test test) {
    TestMgr.check("nameA", test.wrapParam(null).getName());
  }

  private static void testNull(Test test) {
    TestMgr.check("code is 'null'", test.getTestString(null));
    TestMgr.check(null, test.postTestStatic(2));
    TestMgr.check(null, test.patchTestStatic(2));
  }

  private static void testChinese(Test test) {
    TestMgr.check("code is '测试'", test.getTestString("测试"));

    User user = new User();
    user.setName("名字");
    User result = test.splitParam(1, user);
    TestMgr.check("名字,  users count:0", result.getName());
  }

  private static void testStringHaveSpace(Test test) {
    TestMgr.check("code is 'a b'", test.getTestString("a b"));
  }

  private static void testStringArray(Test test) {
    //        TestMgr.check("arr is '[a, , b]'", test.testStringArray(new String[] {"a", null, "b"}));
    TestMgr.check("arr is '[a, b]'", test.testStringArray(new String[] {"a", "b"}));
  }

  private static void testWrapParam(Test test) {
    User user = new User();

    TestRequest request = new TestRequest();
    request.setUser(user);
    request.setIndex(0);
    request.setData(buffer);
    request.getUsers().add(user);

    User result = test.wrapParam(request);
    LOGGER.info("wrap param result:{}", result);

    TestMgr.check("User [name=nameA,  users count:1, age=100, index=0]", result);
  }


  private static void testExceptionRest(Test test) {
    try {
      test.testException(456);
    } catch (InvocationException e) {
      TestMgr.check("456 error", e.getErrorData());
    }

    try {
      test.testException(556);
    } catch (InvocationException e) {
      TestMgr.check("[556 error]", e.getErrorData());
    }

    try {
      test.testException(557);
    } catch (InvocationException e) {
      TestMgr.check("[[557 error]]", e.getErrorData());
    }
  }

  private static void testException(Test test) {
    try {
      test.testException(456);
    } catch (InvocationException e) {
      TestMgr.check("456 error", e.getErrorData());
    }

    try {
      test.testException(556);
    } catch (InvocationException e) {
      TestMgr.check("[556 error]", e.getErrorData());
    }

    try {
      test.testException(557);
    } catch (InvocationException e) {
      TestMgr.check("[[557 error]]", e.getErrorData());
    }
  }

  private static void testInputArray(Test test) {
    String result = test.addString(new String[] {"a", "b"});
    LOGGER.info("input array result:{}", result);
    TestMgr.check("[a, b]", result);
  }

  private static void testSplitParam(Test test) {
    User result = test.splitParam(1, new User());
    LOGGER.info("split param result:{}", result);
    TestMgr.check("User [name=nameA,  users count:0, age=100, index=1]", result);
  }
}
