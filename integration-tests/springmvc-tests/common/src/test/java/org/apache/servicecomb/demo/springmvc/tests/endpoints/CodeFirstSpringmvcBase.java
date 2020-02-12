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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.demo.controller.Person;
import org.apache.servicecomb.demo.server.User;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.apache.servicecomb.swagger.invocation.response.Headers;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public class CodeFirstSpringmvcBase {
  public ResponseEntity<Date> responseEntity(InvocationContext c1, Date date) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("h1", "h1v " + c1.getContext().toString());

    InvocationContext c2 = ContextUtils.getInvocationContext();
    headers.add("h2", "h2v " + c2.getContext().toString());

    return new ResponseEntity<Date>(date, headers, HttpStatus.ACCEPTED);
  }

  public Response cseResponse(InvocationContext c1) {
    Response response = Response.createSuccess(Status.ACCEPTED, new User());
    Headers headers = response.getHeaders();
    headers.addHeader("h1", "h1v " + c1.getContext().toString());

    InvocationContext c2 = ContextUtils.getInvocationContext();
    headers.addHeader("h2", "h2v " + c2.getContext().toString());

    return response;
  }

  public Map<String, User> testUserMap(Map<String, User> userMap) {
    return userMap;
  }

  public String textPlain(String body) {
    return body;
  }

  public byte[] bytes(byte[] input) {
    input[0] = (byte) (input[0] + 1);
    return input;
  }

  public String fileUpload(MultipartFile file1, MultipartFile file2, String name) {
    try {
      return IOUtils.toString(file1.getBytes(), StandardCharsets.UTF_8.name())
          + IOUtils.toString(file2.getBytes(), StandardCharsets.UTF_8.name())
          + name;
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public Date addDate(Date date, long seconds) {
    return new Date(date.getTime() + seconds * 1000);
  }

  public int add(int a) {
    return a;
  }

  public int add(int a, int b) {
    return a + b;
  }

  public int reduce(HttpServletRequest request, int b) {
    int a = Integer.parseInt(request.getParameter("a"));
    return a - b;
  }

  public Person sayHello(Person user) {
    user.setName("hello " + user.getName());
    return user;
  }

  @SuppressWarnings("unchecked")
  public String testRawJsonString(String jsonInput) {
    Map<String, String> person;
    try {
      person = RestObjectMapperFactory.getRestObjectMapper().readValue(jsonInput.getBytes(), Map.class);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    return "hello " + person.get("name");
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
