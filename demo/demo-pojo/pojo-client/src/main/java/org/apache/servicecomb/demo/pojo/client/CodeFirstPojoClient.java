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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.demo.CodeFirstPojoIntf;
import org.apache.servicecomb.demo.DemoConst;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.compute.Person;
import org.apache.servicecomb.demo.server.User;
import org.apache.servicecomb.provider.pojo.RpcReference;

public class CodeFirstPojoClient {
  @RpcReference(microserviceName = "pojo", schemaId = "org.apache.servicecomb.demo.CodeFirstPojoIntf")
  public CodeFirstPojoClientIntf codeFirstAnnotation;

  @RpcReference(microserviceName = "pojo")
  public CodeFirstPojoIntf codeFirstAnnotationEmptySchemaId;

  @Inject
  private CodeFirstPojoIntf codeFirstFromXml;

  public void testCodeFirst(String microserviceName) {
    for (String transport : DemoConst.transports) {
      CseContext.getInstance().getConsumerProviderManager().setTransport(microserviceName, transport);
      TestMgr.setMsg(microserviceName, transport);

      testAll(codeFirstAnnotation, transport);
      testAll(codeFirstAnnotationEmptySchemaId, transport);
      testAll(codeFirstFromXml, transport);
    }
  }

  protected void testAll(CodeFirstPojoIntf codeFirst, String transport) {
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

    CompletableFuture<String> future = ((CodeFirstPojoClientIntf) codeFirst).sayHiAsync("someone");
    try {
      TestMgr.check("someone sayhi", future.get());
    } catch (InterruptedException | ExecutionException e) {
      throw new IllegalStateException(e);
    }
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

  protected void testCodeFirstAddString(CodeFirstPojoIntf codeFirst) {
    String result = codeFirst.addString(Arrays.asList("a", "b"));
    TestMgr.check("ab", result);
  }

  protected void testCodeFirstIsTrue(CodeFirstPojoIntf codeFirst) {
    boolean result = codeFirst.isTrue();
    TestMgr.check(true, result);
  }

  protected void testCodeFirstSayHi2(CodeFirstPojoIntf codeFirst) {
    if (!CodeFirstPojoClientIntf.class.isInstance(codeFirst)) {
      return;
    }

    String result = ((CodeFirstPojoClientIntf) codeFirst).sayHi2("world");
    TestMgr.check("world sayhi 2", result);
  }

  protected void testCodeFirstSayHi(CodeFirstPojoIntf codeFirst) {
    String result = codeFirst.sayHi("world");
    TestMgr.check("world sayhi", result);
    //        TestMgr.check(202, responseEntity.getStatusCode());
  }

  protected void testCodeFirstSaySomething(CodeFirstPojoIntf codeFirst) {
    Person person = new Person();
    person.setName("person name");

    String result = codeFirst.saySomething("prefix  prefix", person);
    TestMgr.check("prefix  prefix person name", result);
  }

  protected void testCodeFirstSayHello(CodeFirstPojoIntf codeFirst) {
    Person input = new Person();
    input.setName("person name");

    Person result = codeFirst.sayHello(input);
    TestMgr.check("hello person name", result.getName());
  }

  protected void testCodeFirstReduce(CodeFirstPojoIntf codeFirst) {
    int result = codeFirst.reduce(5, 3);
    TestMgr.check(2, result);
  }
}
