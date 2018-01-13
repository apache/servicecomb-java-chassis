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

package org.apache.servicecomb.demo.springmvc.tests.endpoints;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.apache.servicecomb.demo.controller.Person;

public class ControllerBase {
  public int add(int a, int b) {
    return a + b;
  }

  public String sayHello(String name) {
    return "hello " + name;
  }

  public String saySomething(String prefix, Person user) {
    return prefix + " " + user.getName();
  }

  public String sayHi(HttpServletRequest request) {
    String[] values = request.getParameterValues("name");
    if (values != null && values.length > 0 && values[0].equals("throwexception")) {
      throw new RuntimeException();
    }
    return "hi " + request.getParameter("name") + " " + Arrays.toString(values);
  }

  public String sayHei(String name) {
    return "hei " + name;
  }
}
