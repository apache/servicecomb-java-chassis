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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.CodeFirstPojoIntf;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.compute.Person;
import org.apache.servicecomb.demo.mapnull.ParseRequest;
import org.apache.servicecomb.demo.mapnull.ParseResponse;
import org.apache.servicecomb.demo.server.MapModel;
import org.apache.servicecomb.demo.server.User;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.springframework.stereotype.Component;

import io.vertx.core.Vertx;

@Component
public class CodeFirstPojoClient implements CategorizedTestCase {
  @RpcReference(microserviceName = "pojo", schemaId = "org.apache.servicecomb.demo.CodeFirstPojoIntf")
  public CodeFirstPojoClientIntf codeFirstAnnotation;

  @RpcReference(microserviceName = "pojo")
  public CodeFirstPojoIntf codeFirstAnnotationEmptySchemaId;

  @Inject
  private CodeFirstPojoIntf codeFirstFromXml;

  @Override
  public void testRestTransport() throws Exception {
    testOnlyRest(codeFirstAnnotation);
  }

  @Override
  public void testAllTransport() throws Exception {
    testAll(codeFirstAnnotation);
    testAll(codeFirstAnnotationEmptySchemaId);
    testAll(codeFirstFromXml);
  }

  private void testOnlyRest(CodeFirstPojoIntf codeFirst) {
    testCodeFirstStrings(codeFirst);
  }

  private void testAll(CodeFirstPojoIntf codeFirst) {
    remoteCodeFirstPojo_testParseResponse(codeFirst);
    remoteCodeFirstPojo_testMapModel(codeFirst);
    remoteCodeFirstPojo_testMap(codeFirst);
    testCodeFirstUserMap(codeFirst);
    testCodeFirstUserArray(codeFirst);
    testCodeFirstStrings(codeFirst);
    testCodeFirstBytes(codeFirst);
    testCodeFirstAddDate(codeFirst);
    testCodeFirstAddString(codeFirst);
    testCodeFirstIsTrue(codeFirst);
    testCodeFirstSayHi2(codeFirst);
    testCodeFirstSayHi(codeFirst);
    testCodeFirstSaySomething(codeFirst);
    //            testCodeFirstRawJsonString(template, cseUrlPrefix);
    testCodeFirstSayHello(codeFirst);
    testCodeFirstReduce(codeFirst);
    testCodeFirstCompletableFuture(codeFirst);
  }

  private void testCodeFirstCompletableFuture(CodeFirstPojoIntf codeFirst) {
    if (!CodeFirstPojoClientIntf.class.isInstance(codeFirst)) {
      return;
    }

    Vertx vertx = VertxUtils.getOrCreateVertxByName("transport", null);
    CountDownLatch latch = new CountDownLatch(1);
    // vertx.runOnContext in normal thread is not a good practice
    // here just a test, not care for this.
    vertx.runOnContext(V -> {
      InvocationContext context = new InvocationContext();
      context.addContext("k", "v");
      ContextUtils.setInvocationContext(context);

      ((CodeFirstPojoClientIntf) codeFirst).sayHiAsync("someone")
          .thenCompose(result -> {
            TestMgr.check("someone sayhi, context k: v", result);

            TestMgr.check(true, context == ContextUtils.getInvocationContext());

            return ((CodeFirstPojoClientIntf) codeFirst).sayHiAsync("someone 1");
          })
          .whenComplete((r, e) -> {
            TestMgr.check("someone 1 sayhi, context k: v", r);
            latch.countDown();
          });

      ContextUtils.removeInvocationContext();
    });

    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  private void remoteCodeFirstPojo_testParseResponse(CodeFirstPojoIntf codeFirst) {
    ParseResponse r = codeFirst.parse(new ParseRequest());
    TestMgr.check("", r.getMsgHeader().get("K16"));
    TestMgr.check("CMT", r.getMsgHeader().get("K14"));
  }

  private void remoteCodeFirstPojo_testMapModel(CodeFirstPojoIntf codeFirst) {
    MapModel model = new MapModel();
    model.setName("hello");
    Map<String, String> userMap = new HashMap<>();
    userMap.put("u1", "u1");
    userMap.put("u2", null);
    model.setNames(userMap);
    MapModel result = codeFirst.testMapModel(model);

    TestMgr.check(result.getName(), "hello");
    TestMgr.check(result.getNames().get("u1"), "u1");
    TestMgr.check(result.getNames().get("u2"), null);

    model = new MapModel();
    model.setName(null);
    userMap = new HashMap<>();
    userMap.put("u1", "u1");
    userMap.put("u2", null);
    model.setNames(userMap);
    result = codeFirst.testMapModel(model);

    TestMgr.check(result.getName(), null);
    TestMgr.check(result.getNames().get("u1"), "u1");
    TestMgr.check(result.getNames().get("u2"), null);

    model = new MapModel();
    model.setName(null);
    userMap = new HashMap<>();
    userMap.put("u1", "u1");
    userMap.put("u2", "");
    model.setNames(userMap);
    result = codeFirst.testMapModel(model);

    TestMgr.check(result.getName(), null);
    TestMgr.check(result.getNames().get("u1"), "u1");
    TestMgr.check(result.getNames().get("u2"), "");
  }

  private void remoteCodeFirstPojo_testMap(CodeFirstPojoIntf codeFirst) {
    Map<String, String> userMap = new HashMap<>();
    userMap.put("u1", "u1");
    userMap.put("u2", null);
    Map<String, String> result = codeFirst.testMap(userMap);

    TestMgr.check(result.get("u1"), "u1");
    TestMgr.check(result.get("u2"), null);

    userMap = new HashMap<>();
    userMap.put("u1", "u1");
    userMap.put("u2", "u2");
    result = codeFirst.testMap(userMap);

    TestMgr.check(result.get("u1"), "u1");
    TestMgr.check(result.get("u2"), "u2");

    // test large data more than 20M
    // can not run the test case in CI , because will cause heap size limit
//    char[] data = new char[30 * 1024 * 1024];
//    Arrays.fill(data, 'h');
//    userMap = new HashMap<>();
//    userMap.put("u1", "u1");
//    userMap.put("u2", "u2");
//    userMap.put("u3", new String(data));
//    result = codeFirst.testMap(userMap);
//
//    TestMgr.check(result.get("u1"), "u1");
//    TestMgr.check(result.get("u2"), "u2");
//    TestMgr.check(result.get("u3"), new String(data));
  }

  private void testCodeFirstUserMap(CodeFirstPojoIntf codeFirst) {
    User user1 = new User();
    user1.setNames(new String[] {"u1", "u2"});

    User user2 = new User();
    user2.setNames(new String[] {"u3", "u4"});

    Map<String, User> userMap = new HashMap<>();
    userMap.put("u1", user1);
    userMap.put("u2", user2);
    Map<String, User> result = codeFirst.testUserMap(userMap);

    TestMgr.check("u1", result.get("u1").getNames()[0]);
    TestMgr.check("u2", result.get("u1").getNames()[1]);
    TestMgr.check("u3", result.get("u2").getNames()[0]);
    TestMgr.check("u4", result.get("u2").getNames()[1]);

    userMap = new HashMap<>();
    userMap.put("u1", user1);
    userMap.put("u2", null);
    result = codeFirst.testUserMap(userMap);

    TestMgr.check(result.get("u1").getNames()[0], "u1");
    TestMgr.check(result.get("u1").getNames()[1], "u2");
    TestMgr.check(result.get("u2"), null);
  }

  private void testCodeFirstUserArray(CodeFirstPojoIntf codeFirst) {
    User user1 = new User();
    user1.setNames(new String[] {"u1", "u2"});

    User user2 = new User();
    user2.setNames(new String[] {"u3", "u4"});

    User[] users = new User[] {user1, user2};
    List<User> result = codeFirst.testUserArray(Arrays.asList(users));
    TestMgr.check("u1", result.get(0).getNames()[0]);
    TestMgr.check("u2", result.get(0).getNames()[1]);
    TestMgr.check("u3", result.get(1).getNames()[0]);
    TestMgr.check("u4", result.get(1).getNames()[1]);
  }

  private void testCodeFirstStrings(CodeFirstPojoIntf codeFirst) {
    String[] result = codeFirst.testStrings(new String[] {"a", "b"});
    TestMgr.check("aa0", result[0]);
    TestMgr.check("b", result[1]);

    result = codeFirst.testStrings(new String[] {"a", ""});
    TestMgr.check("aa0", result[0]);
    TestMgr.check("", result[1]);
  }

  private void testCodeFirstBytes(CodeFirstPojoIntf codeFirst) {
    byte[] input = new byte[] {0, 1, 2};
    byte[] result = codeFirst.testBytes(input);
    TestMgr.check(3, result.length);
    TestMgr.check(1, result[0]);
    TestMgr.check(1, result[1]);
    TestMgr.check(2, result[2]);
  }

  private void testCodeFirstAddDate(CodeFirstPojoIntf codeFirst) {
    Date date = new Date();
    int seconds = 1;
    Date result = codeFirst.addDate(date, seconds);
    TestMgr.check(new Date(date.getTime() + seconds * 1000), result);
  }

  private void testCodeFirstAddString(CodeFirstPojoIntf codeFirst) {
    String result = codeFirst.addString(Arrays.asList("a", "b"));
    TestMgr.check("ab", result);
  }

  private void testCodeFirstIsTrue(CodeFirstPojoIntf codeFirst) {
    boolean result = codeFirst.isTrue();
    TestMgr.check(true, result);
  }

  private void testCodeFirstSayHi2(CodeFirstPojoIntf codeFirst) {
    if (!CodeFirstPojoClientIntf.class.isInstance(codeFirst)) {
      return;
    }

    String result = ((CodeFirstPojoClientIntf) codeFirst).sayHi2("world");
    TestMgr.check("world sayhi 2", result);
  }

  private void testCodeFirstSayHi(CodeFirstPojoIntf codeFirst) {
    String result = codeFirst.sayHi("world");
    TestMgr.check("world sayhi, context k: null", result);
    //        TestMgr.check(202, responseEntity.getStatusCode());
  }

  private void testCodeFirstSaySomething(CodeFirstPojoIntf codeFirst) {
    Person person = new Person();
    person.setName("person name");

    String result = codeFirst.saySomething("prefix  prefix", person);
    TestMgr.check("prefix  prefix person name", result);
  }

  private void testCodeFirstSayHello(CodeFirstPojoIntf codeFirst) {
    Person input = new Person();
    input.setName("person name");

    Person result = codeFirst.sayHello(input);
    TestMgr.check("hello person name", result.getName());

    input.setName("");

    result = codeFirst.sayHello(input);
    TestMgr.check("hello ", result.getName());
  }

  private void testCodeFirstReduce(CodeFirstPojoIntf codeFirst) {
    int result = codeFirst.reduce(5, 3);
    TestMgr.check(2, result);
  }
}
