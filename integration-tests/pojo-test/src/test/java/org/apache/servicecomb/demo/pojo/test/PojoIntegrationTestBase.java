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

package org.apache.servicecomb.demo.pojo.test;

import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.demo.compute.Person;
import org.apache.servicecomb.demo.server.TestRequest;
import org.apache.servicecomb.demo.server.User;
import org.apache.servicecomb.demo.smartcare.Application;
import org.apache.servicecomb.demo.smartcare.Group;
import org.apache.servicecomb.demo.smartcare.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

@Disabled
public class PojoIntegrationTestBase {

  @Test
  public void remoteHelloPojo_sayHello() {
    String result = PojoService.hello.SayHello("whatever");
    Assertions.assertEquals("Hello Message fast", result);
  }

  @Test
  public void remoteHelloPojo_sayHelloAgain() {
    long startTime = System.currentTimeMillis();
    String result = PojoService.hello.SayHelloAgain("whatever");
    long stopTime = System.currentTimeMillis();
    long elapsedTime = stopTime - startTime;

    Assertions.assertEquals("Hello Message slow", result);
    MatcherAssert.assertThat(elapsedTime,
        Matchers.is(both(greaterThan(4000L)).and(lessThan(7000L))));
  }

  @Test
  public void remoteSmartCarePojo_addApplication() {
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

    Response result = PojoService.smartCare.addApplication(application);
    Assertions.assertEquals(0, result.getResultCode());
    Assertions.assertEquals("add application app0 success", result.getResultMessage());
  }

  @Test
  public void remoteSmartCarePojo_delApplication() {
    Response result = PojoService.smartCare.delApplication("app0");

    Assertions.assertEquals(1, result.getResultCode());
    Assertions.assertEquals("delete application app0 failed", result.getResultMessage());
  }

  @Test
  public void remoteTestPojo_testStringArray() {
    String result = PojoService.test.testStringArray(new String[] {"a", "b"});
    Assertions.assertEquals("arr is '[a, b]'", result);
  }

  @Test
  public void remoteTestPojo_getTestString() {
    // test empty string
    String result = PojoService.test.getTestString("");
    Assertions.assertEquals("code is ''", result);

    // test null
    result = PojoService.test.getTestString(null);
    Assertions.assertEquals("code is 'null'", result);

    // test Chinese
    result = PojoService.test.getTestString("测试");
    Assertions.assertEquals("code is '测试'", result);

    // test String with space
    result = PojoService.test.getTestString("a b");
    Assertions.assertEquals("code is 'a b'", result);
  }

  @Test
  public void remoteTestPojo_postTestStatic() {
    String result = PojoService.test.postTestStatic(1);
    Assertions.assertNull(result);
    result = PojoService.test.patchTestStatic(1);
    Assertions.assertNull(result);
  }

  @Test
  public void remoteTestPojo_testException() {
    // when code is 200
    String result = PojoService.test.testException(200);
    Assertions.assertEquals("200", result);

    // when code is 456
    try {
      PojoService.test.testException(456);
      Assertions.fail("Exception expected, but threw nothing");
    } catch (InvocationException e) {
      Assertions.assertEquals("456 error", e.getErrorData());
    } catch (Exception e) {
      Assertions.fail("InvocationException expected, but threw " + e);
    }

    // when code is 556
    try {
      PojoService.test.testException(556);
      Assertions.fail("InvocationException expected, but threw nothing");
    } catch (InvocationException e) {
      Assertions.assertEquals(556, e.getStatusCode());
      Assertions.assertEquals("[556 error]", e.getErrorData().toString());
    } catch (Exception e) {
      Assertions.fail("InvocationException expected, but threw " + e);
    }

    // when code is 557
    try {
      PojoService.test.testException(557);
      Assertions.fail("InvocationException expected, but threw nothing");
    } catch (InvocationException e) {
      Assertions.assertEquals(557, e.getStatusCode());
      Assertions.assertEquals("[[557 error]]", e.getErrorData().toString());
    } catch (Exception e) {
      Assertions.fail("InvocationException expected, but threw " + e);
    }

    // when code is 123(other number, the default case)
    result = PojoService.test.testException(123);
    Assertions.assertEquals("not expected", result);
  }

  @Test
  public void remoteTestPojo_splitParam() {
    User result = PojoService.test.splitParam(1, new User());
    Assertions.assertEquals("User [name=nameA,  users count:0, age=100, index=1]", result.toString());
  }

  @Test
  public void remoteTestPojo_wrapParam() {
    // when request is null
    User result = PojoService.test.wrapParam(null);
    Assertions.assertNull(result);

    // when request is not null
    User user = new User();
    byte[] buffer = new byte[1024];

    TestRequest request = new TestRequest();
    request.setUser(user);
    request.setIndex(0);
    request.setData(buffer);
    request.getUsers().add(user);

    result = PojoService.test.wrapParam(request);
    Assertions.assertEquals("User [name=nameA,  users count:1, age=100, index=0]", result.toString());
  }

  @Test
  public void remoteTestPojo_addString() {
    String result = PojoService.test.addString(new String[] {"a", "b"});
    Assertions.assertEquals("[a, b]", result);
  }

  @Test
  public void remoteCodeFirstPojo_testUserMap() {
    User user1 = new User();
    user1.setNames(new String[] {"u1", "u2"});

    User user2 = new User();
    user2.setNames(new String[] {"u3", "u4"});

    Map<String, User> userMap = new HashMap<>();
    userMap.put("u1", user1);
    userMap.put("u2", user2);
    Map<String, User> result = PojoService.codeFirst.testUserMap(userMap);

    Assertions.assertEquals("u1", result.get("u1").getNames()[0]);
    Assertions.assertEquals("u2", result.get("u1").getNames()[1]);
    Assertions.assertEquals("u3", result.get("u2").getNames()[0]);
    Assertions.assertEquals("u4", result.get("u2").getNames()[1]);
  }

  @Test
  public void remoteCodeFirstPojo_testUserArray() {
    User user1 = new User();
    user1.setNames(new String[] {"u1", "u2"});

    User user2 = new User();
    user2.setNames(new String[] {"u3", "u4"});

    User[] users = new User[] {user1, user2};
    List<User> result = PojoService.codeFirst.testUserArray(Arrays.asList(users));

    Assertions.assertEquals("u1", result.get(0).getNames()[0]);
    Assertions.assertEquals("u2", result.get(0).getNames()[1]);
    Assertions.assertEquals("u3", result.get(1).getNames()[0]);
    Assertions.assertEquals("u4", result.get(1).getNames()[1]);
  }

  @Test
  public void remoteCodeFirstPojo_testStrings() {
    String[] result = PojoService.codeFirst.testStrings(new String[] {"a", "b"});
    MatcherAssert.assertThat(Arrays.asList(result), contains("aa0", "b"));
  }

  @Test
  public void remoteCodeFirstPojo_testBytes() {
    byte[] input = new byte[] {0, 1, 2};
    byte[] result = PojoService.codeFirst.testBytes(input);

    Assertions.assertEquals(3, result.length);
    Assertions.assertEquals(1, result[0]);
    Assertions.assertEquals(1, result[1]);
    Assertions.assertEquals(2, result[2]);
  }

  @Test
  public void remoteCodeFirstPojo_reduce() {
    int result = PojoService.codeFirst.reduce(5, 3);
    Assertions.assertEquals(2, result);
  }

  @Test
  public void remoteCodeFirstPojo_addDate() {
    Date date = new Date();
    int seconds = 1;
    Date result = PojoService.codeFirst.addDate(date, seconds);

    MatcherAssert.assertThat(result, Matchers.equalTo(new Date(date.getTime() + seconds * 1000)));
  }

  @Test
  public void remoteCodeFirstPojo_sayHello() {
    Person input = new Person();
    input.setName("person name");

    Person result = PojoService.codeFirst.sayHello(input);
    Assertions.assertEquals("hello person name", result.getName());
  }

  @Test
  public void remoteCodeFirstPojo_saySomething() {
    Person person = new Person();
    person.setName("person name");

    String result = PojoService.codeFirst.saySomething("prefix  prefix", person);
    Assertions.assertEquals("prefix  prefix person name", result);
  }

  @Test
  public void remoteCodeFirstPojo_sayHi() {
    String result = PojoService.codeFirst.sayHi("world");
    Assertions.assertEquals("world sayhi", result);
  }

  @Test
  public void remoteCodeFirstPojo_isTrue() {
    boolean result = PojoService.codeFirst.isTrue();
    Assertions.assertTrue(result);
  }

  @Test
  public void remoteCodeFirstPojo_addString() {
    String result = PojoService.codeFirst.addString(Arrays.asList("a", "b"));
    Assertions.assertEquals("ab", result);
  }
}
