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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

import org.apache.servicecomb.config.InMemoryDynamicPropertiesSource;
import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.provider.consumer.InvokerUtils;
import org.apache.servicecomb.demo.CategorizedTestCaseRunner;
import org.apache.servicecomb.demo.DemoConst;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.server.Test;
import org.apache.servicecomb.demo.server.User;
import org.apache.servicecomb.demo.smartcare.Application;
import org.apache.servicecomb.demo.smartcare.Group;
import org.apache.servicecomb.demo.smartcare.SmartCare;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClients;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource(value = "classpath*:META-INF/spring/*.bean.xml")
public class PojoClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(PojoClient.class);

  // reference a not exist a microservice, and never use it
  // this should not cause problems
  @RpcReference(microserviceName = "notExist")
  public static Test notExist;

  @RpcReference(microserviceName = "pojo", schemaId = "server")
  public static Test test;

  public static Test testFromXml;

  private static SmartCare smartcare;

  public static void setTestFromXml(Test testFromXml) {
    PojoClient.testFromXml = testFromXml;
  }

  public static void main(String[] args) throws Exception {
    new SpringApplicationBuilder(PojoClient.class).web(WebApplicationType.NONE).run(args);

    try {
      run();
    } catch (Throwable e) {
      TestMgr.check("success", "failed");
      LOGGER.error("-------------- test failed -------------");
      LOGGER.error("", e);
      LOGGER.error("-------------- test failed -------------");
    }
    TestMgr.summary();
    LOGGER.info("-------------- last time updated checks(maybe more/less): 785 -------------");
  }

  private static void testContextClassLoaderIsNull() throws Exception {
    ForkJoinPool pool = new ForkJoinPool(4);
    pool.submit(() ->
        IntStream.range(0, 20).parallel().forEach(item -> {
          if (Thread.currentThread().getName().equals("main")) {
            return;
          }
          // in web environment, this could be null, here we just mock a null class loader.
          Thread.currentThread().setContextClassLoader(null);
          TestMgr.check(null, test.postTestStatic(2));
        })).get();
  }

  public static void run() throws Exception {
    testHttpClientsIsOk();
    CategorizedTestCaseRunner.runCategorizedTestCase("pojo");

    smartcare = BeanUtils.getBean("smartcare");
    String microserviceName = "pojo";

    for (String transport : DemoConst.transports) {
      InMemoryDynamicPropertiesSource.update("servicecomb.references.transport." + microserviceName, transport);
      TestMgr.setMsg(microserviceName, transport);
      LOGGER.info("test {}, transport {}", microserviceName, transport);

      testContextClassLoaderIsNull();
      testNull(testFromXml);

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

      testSmartCare(smartcare);

      testCommonInvoke(transport);

      if ("rest".equals(transport)) {
        testTraceIdOnNotSetBefore();
      }
      testTraceIdOnContextContainsTraceId();
    }
  }

  private static void testHttpClientsIsOk() {
    TestMgr.check(HttpClients.getClient("config-center") != null, false);
    TestMgr.check(HttpClients.getClient("http-transport-client") != null, false);
    TestMgr.check(HttpClients.getClient("http2-transport-client") != null, true);

    TestMgr.check(HttpClients.getClient("config-center", false) != null, false);
    TestMgr.check(HttpClients.getClient("http-transport-client", false) != null, false);
    TestMgr.check(HttpClients.getClient("http2-transport-client", false) != null, true);
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
    context.addContext(CoreConst.TRACE_ID_NAME, String.valueOf(Long.MIN_VALUE));
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

  @SuppressWarnings("rawtypes")
  private static void testCommonInvoke(String transport) {
    Map<String, Object> arguments = new HashMap<>();
    arguments.put("index", 2);
    arguments.put("user", new User());
    Map<String, Object> warpArguments = new HashMap<>();
    warpArguments.put("splitParamBody", arguments);
    User result = InvokerUtils.syncInvoke("pojo", "server",
        "splitParam", warpArguments, User.class);
    TestMgr.check("User [name=nameA,  users count:0, age=100, index=2]", result);

    arguments = new HashMap<>();
    arguments.put("index", 3);
    arguments.put("user", new User());
    warpArguments = new HashMap<>();
    warpArguments.put("splitParamBody", arguments);
    result =
        InvokerUtils.syncInvoke("pojo",
            transport,
            "server",
            "splitParam",
            warpArguments, User.class);
    TestMgr.check("User [name=nameA,  users count:0, age=100, index=3]", result);
  }

  private static void testNull(Test test) {
    TestMgr.check("code is 'null'", test.getTestString(null));
    TestMgr.check(null, test.postTestStatic(2));
    TestMgr.check(null, test.patchTestStatic(2));
  }
}
