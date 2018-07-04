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

import static org.apache.servicecomb.serviceregistry.client.LocalServiceRegistryClientImpl.LOCAL_REGISTRY_FILE_KEY;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class PojoIntegrationTestBase {

  protected static void setUpLocalRegistry() {
    System.setProperty(LOCAL_REGISTRY_FILE_KEY, "notExistJustForceLocal");
  }

  @Test
  public void remoteHelloPojo_sayHello() {
    String result = PojoService.hello.SayHello("whatever");
    assertThat(result, is("Hello Message fast"));
  }

  @Test
  public void remoteHelloPojo_sayHelloAgain() {
    long startTime = System.currentTimeMillis();
    String result = PojoService.hello.SayHelloAgain("whatever");
    long stopTime = System.currentTimeMillis();
    long elapsedTime = stopTime - startTime;

    assertThat(result, is("Hello Message slow"));
    assertThat(elapsedTime,
        is(both(greaterThan(4000L)).and(lessThan(7000L))));
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
    assertThat(result.getResultCode(), is(0));
    assertThat(result.getResultMessage(), is("add application app0 success"));
  }

  @Test
  public void remoteSmartCarePojo_delApplication() {
    Response result = PojoService.smartCare.delApplication("app0");

    assertThat(result.getResultCode(), is(1));
    assertThat(result.getResultMessage(), is("delete application app0 failed"));
  }

  @Test
  public void remoteTestPojo_testStringArray() {
    String result = PojoService.test.testStringArray(new String[] {"a", "b"});
    assertThat(result, is("arr is '[a, b]'"));
  }

  @Test
  public void remoteTestPojo_getTestString() {
    // test empty string
    String result = PojoService.test.getTestString("");
    assertThat(result, is("code is ''"));

    // test null
    result = PojoService.test.getTestString(null);
    assertThat(result, is("code is 'null'"));

    // test Chinese
    result = PojoService.test.getTestString("测试");
    assertThat(result, is("code is '测试'"));

    // test String with space
    result = PojoService.test.getTestString("a b");
    assertThat(result, is("code is 'a b'"));
  }

  @Test
  public void remoteTestPojo_postTestStatic() {
    String result = PojoService.test.postTestStatic(1);
    assertThat(result, is(nullValue()));
    result = PojoService.test.patchTestStatic(1);
    assertThat(result, is(nullValue()));
  }

  @Test
  public void remoteTestPojo_testException() {
    // when code is 200
    String result = PojoService.test.testException(200);
    assertThat(result, is("200"));

    // when code is 456
    try {
      PojoService.test.testException(456);
      fail("Exception expected, but threw nothing");
    } catch (InvocationException e) {
      assertThat(e.getErrorData(), is("456 error"));
    } catch (Exception e) {
      fail("InvocationException expected, but threw " + e);
    }

    // when code is 556
    try {
      PojoService.test.testException(556);
      fail("InvocationException expected, but threw nothing");
    } catch (InvocationException e) {
      assertThat(e.getStatusCode(), is(556));
      assertThat(e.getErrorData().toString(), is("[556 error]"));
    } catch (Exception e) {
      fail("InvocationException expected, but threw " + e);
    }

    // when code is 557
    try {
      PojoService.test.testException(557);
      fail("InvocationException expected, but threw nothing");
    } catch (InvocationException e) {
      assertThat(e.getStatusCode(), is(557));
      assertThat(e.getErrorData().toString(), is("[[557 error]]"));
    } catch (Exception e) {
      fail("InvocationException expected, but threw " + e);
    }

    // when code is 123(other number, the default case)
    result = PojoService.test.testException(123);
    assertThat(result, is("not expected"));
  }

  @Test
  public void remoteTestPojo_splitParam() {
    User result = PojoService.test.splitParam(1, new User());
    assertThat(result.toString(),
        is("User [name=nameA,  users count:0, age=100, index=1]"));
  }

  @Test
  public void remoteTestPojo_wrapParam() {
    // when request is null
    User result = PojoService.test.wrapParam(null);
    assertThat(result, is(nullValue()));

    // when request is not null
    User user = new User();
    byte[] buffer = new byte[1024];

    TestRequest request = new TestRequest();
    request.setUser(user);
    request.setIndex(0);
    request.setData(buffer);
    request.getUsers().add(user);

    result = PojoService.test.wrapParam(request);
    assertThat(result.toString(),
        is("User [name=nameA,  users count:1, age=100, index=0]"));
  }

  @Test
  public void remoteTestPojo_addString() {
    String result = PojoService.test.addString(new String[] {"a", "b"});
    assertThat(result, is("[a, b]"));
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

    assertThat(result.get("u1").getNames()[0], is("u1"));
    assertThat(result.get("u1").getNames()[1], is("u2"));
    assertThat(result.get("u2").getNames()[0], is("u3"));
    assertThat(result.get("u2").getNames()[1], is("u4"));
  }

  @Test
  public void remoteCodeFirstPojo_testUserArray() {
    User user1 = new User();
    user1.setNames(new String[] {"u1", "u2"});

    User user2 = new User();
    user2.setNames(new String[] {"u3", "u4"});

    User[] users = new User[] {user1, user2};
    List<User> result = PojoService.codeFirst.testUserArray(Arrays.asList(users));

    assertThat(result.get(0).getNames()[0], is("u1"));
    assertThat(result.get(0).getNames()[1], is("u2"));
    assertThat(result.get(1).getNames()[0], is("u3"));
    assertThat(result.get(1).getNames()[1], is("u4"));
  }

  @Test
  public void remoteCodeFirstPojo_testStrings() {
    String[] result = PojoService.codeFirst.testStrings(new String[] {"a", "b"});
    assertThat(Arrays.asList(result), contains("aa0", "b"));
  }

  @Test
  public void remoteCodeFirstPojo_testBytes() {
    byte[] input = new byte[] {0, 1, 2};
    byte[] result = PojoService.codeFirst.testBytes(input);

    assertEquals(3, result.length);
    assertEquals(1, result[0]);
    assertEquals(1, result[1]);
    assertEquals(2, result[2]);
  }

  @Test
  public void remoteCodeFirstPojo_reduce() {
    int result = PojoService.codeFirst.reduce(5, 3);
    assertThat(result, is(2));
  }

  @Test
  public void remoteCodeFirstPojo_addDate() {
    Date date = new Date();
    int seconds = 1;
    Date result = PojoService.codeFirst.addDate(date, seconds);

    assertThat(result, equalTo(new Date(date.getTime() + seconds * 1000)));
  }

  @Test
  public void remoteCodeFirstPojo_sayHello() {
    Person input = new Person();
    input.setName("person name");

    Person result = PojoService.codeFirst.sayHello(input);
    assertThat(result.getName(), is("hello person name"));
  }

  @Test
  public void remoteCodeFirstPojo_saySomething() {
    Person person = new Person();
    person.setName("person name");

    String result = PojoService.codeFirst.saySomething("prefix  prefix", person);
    assertThat(result, is("prefix  prefix person name"));
  }

  @Test
  public void remoteCodeFirstPojo_sayHi() {
    String result = PojoService.codeFirst.sayHi("world");
    assertThat(result, is("world sayhi"));
  }

  @Test
  public void remoteCodeFirstPojo_isTrue() {
    boolean result = PojoService.codeFirst.isTrue();
    assertThat(result, is(true));
  }

  @Test
  public void remoteCodeFirstPojo_addString() {
    String result = PojoService.codeFirst.addString(Arrays.asList("a", "b"));
    assertThat(result, is("ab"));
  }
}
