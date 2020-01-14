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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.provider.consumer.InvokerUtils;
import org.apache.servicecomb.demo.CategorizedTestCaseRunner;
import org.apache.servicecomb.demo.DemoConst;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.server.Test;
import org.apache.servicecomb.demo.server.TestRequest;
import org.apache.servicecomb.demo.server.User;
import org.apache.servicecomb.demo.smartcare.Application;
import org.apache.servicecomb.demo.smartcare.Group;
import org.apache.servicecomb.demo.smartcare.SmartCare;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.Log4jUtils;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PojoClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(PojoClient.class);

  public static final byte buffer[] = new byte[1024];

  public static CodeFirstPojoClient codeFirstPojoClient;

  // reference a not exist a microservice, and never use it
  // this should not cause problems
  @RpcReference(microserviceName = "notExist")
  public static Test notExist;

  @RpcReference(microserviceName = "pojo")
  public static Test test;

  public static Test testFromXml;

  private static SmartCare smartcare;

  static {
    Arrays.fill(buffer, (byte) 1);
  }

  public static void setTestFromXml(Test testFromXml) {
    PojoClient.testFromXml = testFromXml;
  }

  public static void main(String[] args) throws Exception {
    Log4jUtils.init();
    BeanUtils.init();

    try {
      run();
    } catch (Exception e) {
      TestMgr.check("success", "failed");
      LOGGER.error("-------------- test failed -------------");
      LOGGER.error("", e);
      System.err.println("-------------- test failed -------------");
    }

    TestMgr.summary();
  }

  private static void testContextClassLoaderIsNull() {
    // TODO: WEAK protostuff many classes use ContextClassLoader to load classes, if it is null,
    // Will cause many components not work.
//    IntStream.range(0, 100).parallel().forEach(item -> {
//      if (Thread.currentThread().getName().equals("main")) {
//        return;
//      }
//      // in web environment, this could be null, here we just mock a null class loader.
//      Thread.currentThread().setContextClassLoader(null);
//      TestMgr.check(null, test.postTestStatic(2));
//    });
  }

  public static void run() throws Exception {
    CategorizedTestCaseRunner.runCategorizedTestCase("pojo");

    testContextClassLoaderIsNull();

    smartcare = BeanUtils.getBean("smartcare");

    String microserviceName = "pojo";
    codeFirstPojoClient.testCodeFirst(microserviceName);

    for (String transport : DemoConst.transports) {
      ArchaiusUtils.setProperty("servicecomb.references.transport." + microserviceName, transport);
      TestMgr.setMsg(microserviceName, transport);
      LOGGER.info("test {}, transport {}", microserviceName, transport);

      testNull(testFromXml);
      testNull(test);

      // This test case shows destroy of WeightedResponseTimeRule timer task. after test finished will not print:
      // "Weight adjusting job started" and thread "NFLoadBalancer-serverWeightTimer-unknown" destroyed.
      ArchaiusUtils.setProperty("servicecomb.loadbalance.strategy.name", "WeightedResponse");
      testStringArray(test);
      ArchaiusUtils.setProperty("servicecomb.loadbalance.strategy.name", "RoundRobin");
      testStringArray(test);

      boolean checkerDestroyed = true;
      // Timer cancel may not destroy thread very fast so check for 3 times.
      for (int i = 0; i < 3; i++) {
        checkerDestroyed = true;
        Set<Thread> allThreads = Thread.getAllStackTraces().keySet();
        for (Thread t : allThreads) {
          if (t.getName().equals("NFLoadBalancer-serverWeightTimer-unknown")) {
            checkerDestroyed = false;
            Thread.sleep(1000);
          }
        }
      }
      TestMgr.check(checkerDestroyed, true);

      testChinese(test);
      testStringHaveSpace(test);
      testWrapParam(test);
      testSplitParam(test);
      testInputArray(test);

      testException(test);

      testSmartCare(smartcare);

      testCommonInvoke(transport);

      if ("rest".equals(transport)) {
        testTraceIdOnNotSetBefore();
        testNullRest(test);
        testExceptionRest(test);
        testEmptyRest(test);
      } else if ("highway".equals(transport)) {
        testNullHighway(test);
        testEmptyHighway(test);
      }

      testTraceIdOnContextContainsTraceId();
    }
  }

  /**
   * Only in http transport, traceId will be set to invocation if null.
   * But in highway, nothing done.
   */
  private static void testTraceIdOnNotSetBefore() {
    String traceId = test.testTraceId();
    TestMgr.checkNotEmpty(traceId);
  }

  private static void testTraceIdOnContextContainsTraceId() {
    InvocationContext context = new InvocationContext();
    context.addContext(Const.TRACE_ID_NAME, String.valueOf(Long.MIN_VALUE));
    ContextUtils.setInvocationContext(context);
    String traceId = test.testTraceId();
    TestMgr.check(String.valueOf(Long.MIN_VALUE), traceId);
    ContextUtils.removeInvocationContext();
  }

  private static void testSmartCare(SmartCare smartCare) {
    Group group = new Group();
    group.setName("group0");

    Application application = new Application();
    application.setName("app0");
    application.setDefaultGroup("group0");
    application.setVersion("v1");
    application.setDynamicFlag(true);
    List<Group> groups = new ArrayList<>();
    groups.add(group);
    application.setGroups(groups);

    TestMgr.check("resultCode: 0\nresultMessage: add application app0 success",
        smartCare.addApplication(application));
    TestMgr.check("resultCode: 1\nresultMessage: delete application app0 failed",
        smartCare.delApplication("app0"));
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
    // TODO : WEAK highway support error code
//    try {
//      test.testException(456);
//    } catch (InvocationException e) {
//      TestMgr.check("456 error", e.getErrorData());
//    }
//
//    try {
//      test.testException(556);
//    } catch (InvocationException e) {
//      TestMgr.check("[556 error]", e.getErrorData());
//    }

//    try {
//      test.testException(557);
//    } catch (InvocationException e) {
//      TestMgr.check("[[557 error]]", e.getErrorData());
//    }
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

  private static void testCommonInvoke(String transport) {
    Map<String, Object> arguments = new HashMap<>();
    arguments.put("index", 2);
    arguments.put("user", new User());
    Object result = InvokerUtils.syncInvoke("pojo", "server", "splitParam", arguments);
    TestMgr.check("User [name=nameA,  users count:0, age=100, index=2]", result);

    arguments = new HashMap<>();
    arguments.put("index", 3);
    arguments.put("user", new User());
    result =
        InvokerUtils.syncInvoke("pojo",
            "0.0.4",
            transport,
            "server",
            "splitParam",
            arguments);
    TestMgr.check("User [name=nameA,  users count:0, age=100, index=3]", result);
  }

  private static void testEmptyHighway(Test test) {
    // TODO : WEAK highway will never encoding empty string
    TestMgr.check("code is 'null'", test.getTestString(""));
  }

  private static void testEmptyRest(Test test) {
    TestMgr.check("code is ''", test.getTestString(""));
  }

  private static void testNullRest(Test test) {
    TestMgr.check(null, test.wrapParam(null));
  }

  private static void testNullHighway(Test test) {
    // TODO: WEAK highway will never have request with null. When new User, the default name is nameA
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

  @Inject
  public void setCodeFirstPojoClient(CodeFirstPojoClient codeFirstPojoClient) {
    PojoClient.codeFirstPojoClient = codeFirstPojoClient;
  }
}
