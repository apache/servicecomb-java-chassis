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

package org.apache.servicecomb.demo.pojo.test.endpoints;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.demo.CodeFirstPojoIntf;
import org.apache.servicecomb.demo.compute.Person;
import org.apache.servicecomb.demo.server.User;
import org.apache.servicecomb.provider.pojo.RpcSchema;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;

@RpcSchema(schemaId = "codeFirst")
public class CodeFirstPojo implements CodeFirstPojoIntf {

  @Override
  public Map<String, User> testUserMap(Map<String, User> userMap) {
    return userMap;
  }

  @Override
  public List<User> testUserArray(List<User> users) {
    return users;
  }

  public String[] testStrings(String[] input) {
    input[0] += input[0] + "0";
    return input;
  }

  public byte[] testBytes(byte[] input) {
    input[0] = (byte) (input[0] + 1);
    return input;
  }

  public int reduce(int a, int b) {
    return a - b;
  }

  public Date addDate(Date date, long second) {
    return new Date(date.getTime() + second * 1000);
  }

  public Person sayHello(Person user) {
    user.setName("hello " + user.getName());
    return user;
  }

  public String saySomething(String prefix, Person user) {
    return prefix + " " + user.getName();
  }

  public String sayHi(String name) {
    ContextUtils.getInvocationContext().setStatus(202);
    return name + " sayhi";
  }

  public String sayHi2(String name) {
    return name + " sayhi 2";
  }

  public boolean isTrue() {
    return true;
  }

  public String addString(List<String> s) {
    String result = "";
    for (String x : s) {
      result += x;
    }
    return result;
  }
}
