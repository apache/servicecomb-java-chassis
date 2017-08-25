/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.swagger.invocation.models;

import java.util.Arrays;
import java.util.List;

import io.servicecomb.swagger.invocation.context.InvocationContext;

public class PojoImpl {
  public int testSimple(int a, int b, int c) {
    return a - b - c;
  }

  public Person testObject(Person user) {
    user.setName("hello " + user.getName());
    return user;
  }

  public String testSimpleAndObject(String prefix, Person user) {
    return prefix + " " + user.getName();
  }

  public String testContext(InvocationContext context, String name) {
    context.addContext("name", name);
    return name + " sayhi";
  }

  public byte[] testBytes(byte[] bytes) {
    return bytes;
  }

  public String[] testArrayArray(String[] s) {
    return s;
  }

  public List<String> testArrayList(String[] s) {
    return Arrays.asList(s);
  }

  public String[] testListArray(List<String> s) {
    return s.toArray(new String[s.size()]);
  }

  public List<String> testListList(List<String> s) {
    return s;
  }

  public Person[] testObjectArrayArray(Person[] s) {
    return s;
  }

  public List<Person> testObjectArrayList(Person[] s) {
    return Arrays.asList(s);
  }

  public Person[] testObjectListArray(List<Person> s) {
    return s.toArray(new Person[s.size()]);
  }

  public List<Person> testObjectListList(List<Person> s) {
    return s;
  }
}
