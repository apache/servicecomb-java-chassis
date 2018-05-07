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

package org.apache.servicecomb.swagger.invocation.models;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.swagger.invocation.context.InvocationContext;

import io.swagger.annotations.ApiOperation;

public class PojoImpl {
  public int testSimple(int a, int b, int c) {
    return a - b - c;
  }

  public Person testObject(Person user) {
    user.setName("hello " + user.getName());
    return user;
  }

  @ApiOperation(nickname = "testSimpleAndObject", value = "")
  public CompletableFuture<String> testSimpleAndObjectAsync(String prefix, Person user) {
    CompletableFuture<String> future = new CompletableFuture<>();
    future.complete(prefix + " " + user.getName());
    return future;
  }

  public String testContext(InvocationContext context, String name) {
    context.addContext("name", name);
    return name + " sayhi";
  }

  public List<byte[]> testListBytes(List<byte[]> bytes) {
    return bytes;
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
