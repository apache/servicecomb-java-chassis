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

package io.servicecomb.demo.pojo.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.servicecomb.core.CseContext;
import io.servicecomb.core.provider.consumer.InvokerUtils;
import io.servicecomb.demo.DemoConst;
import io.servicecomb.demo.TestMgr;
import io.servicecomb.demo.server.Test;
import io.servicecomb.demo.server.TestRequest;
import io.servicecomb.demo.server.User;
import io.servicecomb.demo.smartcare.Application;
import io.servicecomb.demo.smartcare.Group;
import io.servicecomb.demo.smartcare.SmartCare;
import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.foundation.common.utils.Log4jUtils;
import io.servicecomb.provider.pojo.RpcReference;
import io.servicecomb.swagger.invocation.exception.InvocationException;

@Component
public class PojoClient {
  private static Logger LOGGER = LoggerFactory.getLogger(PojoClient.class);

  public static CodeFirstPojoClient codeFirstPojoClient;

  // reference a not exist a microservice, and never use it
  // this should not cause problems
  @RpcReference(microserviceName = "notExist")
  public static Test notExist;

  @RpcReference(microserviceName = "pojo")
  public static Test test;

  public static Test testFromXml;

  private static SmartCare smartcare;

  public static final byte buffer[] = new byte[1024];

  static {
    Arrays.fill(buffer, (byte) 1);
  }

  public static void setTestFromXml(Test testFromXml) {
    PojoClient.testFromXml = testFromXml;
  }

  @Inject
  public void setCodeFirstPojoClient(CodeFirstPojoClient codeFirstPojoClient) {
    PojoClient.codeFirstPojoClient = codeFirstPojoClient;
  }

  public static void main(String[] args) throws Exception {
    Log4jUtils.init();
    BeanUtils.init();

    run();

    TestMgr.summary();
  }

  public static void run() throws Exception {
    smartcare = BeanUtils.getBean("smartcare");

    String microserviceName = "pojo";
    codeFirstPojoClient.testCodeFirst(microserviceName);

    for (String transport : DemoConst.transports) {
      CseContext.getInstance().getConsumerProviderManager().setTransport(microserviceName, transport);
      TestMgr.setMsg(microserviceName, transport);
      LOGGER.info("test {}, transport {}", microserviceName, transport);

      testNull(testFromXml);
      testNull(test);
      testEmpty(test);
      testStringArray(test);
      testChinese(test);
      testStringHaveSpace(test);
      testWrapParam(test);
      testSplitParam(test);
      testInputArray(test);

      testException(test);

      testSmartCare(smartcare);

      testCommonInvoke(transport);
    }
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

  private static void testCommonInvoke(String transport) {
    Object result = InvokerUtils.syncInvoke("pojo", "server", "splitParam", new Object[] {2, new User()});
    TestMgr.check("User [name=nameA,  users count:0, age=100, index=2]", result);

    result =
        InvokerUtils.syncInvoke("pojo",
            "0.0.4",
            transport,
            "server",
            "splitParam",
            new Object[] {3, new User()});
    TestMgr.check("User [name=nameA,  users count:0, age=100, index=3]", result);
  }

  private static void testEmpty(Test test) {
    TestMgr.check("code is ''", test.getTestString(""));
  }

  private static void testNull(Test test) {
    TestMgr.check("code is 'null'", test.getTestString(null));
    TestMgr.check(null, test.wrapParam(null));
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
}
