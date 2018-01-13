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

package org.apache.servicecomb.demo.jaxrs.tests.endpoints;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import java.util.Date;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.common.rest.codec.RestObjectMapper;
import org.apache.servicecomb.demo.compute.Person;
import org.apache.servicecomb.demo.server.User;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;

@Produces(MediaType.APPLICATION_JSON)
public class SomeAbstractJaxrsRestEndpoint {

  @Path("/testUserMap")
  @POST
  public Map<String, User> testUserMap(Map<String, User> userMap) {
    return userMap;
  }

  @Path("/textPlain")
  @POST
  @Consumes(TEXT_PLAIN)
  public String textPlain(String body) {
    return body;
  }

  @Path("/bytes")
  @POST
  public byte[] bytes(byte[] input) {
    input[0] = (byte) (input[0] + 1);
    return input;
  }

  @Path("/addDate")
  @POST
  public Date addDate(@FormParam("date") Date date, @QueryParam("seconds") long seconds) {
    return new Date(date.getTime() + seconds * 1000);
  }

  @GET
  public int defaultPath() {
    return 100;
  }

  @Path("/add")
  @POST
  public int add(@FormParam("a") int a, @FormParam("b") int b) {
    return a + b;
  }

  @Path("/sayhello")
  @POST
  public Person sayHello(Person user) {
    user.setName("hello " + user.getName());
    return user;
  }

  @SuppressWarnings("unchecked")
  @Path("/testrawjson")
  @POST
  public String testRawJsonString(String jsonInput) {
    Map<String, String> person;
    try {
      person = RestObjectMapper.INSTANCE.readValue(jsonInput.getBytes(), Map.class);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    return "hello " + person.get("name");
  }

  @Path("/saysomething")
  @POST
  public String saySomething(@HeaderParam("prefix") String prefix, Person user) {
    return prefix + " " + user.getName();
  }

  @Path("/saysomething1")
  @POST
  public String saySomething1(@HeaderParam("prefix-test") String prefix_test, Person user) {
    return prefix_test + " " + user.getName();
  }

  @Path("/sayhi/{name}")
  @PUT
  public String sayHi(@PathParam("name") String name) {
    ContextUtils.getInvocationContext().setStatus(202);
    return name + " sayhi";
  }

  @Path("/sayhi/{name}/v2")
  @PUT
  public String sayHi2(@PathParam("name") String name) {
    return name + " sayhi 2";
  }

  @Path("/istrue")
  @GET
  public boolean isTrue() {
    return true;
  }
}
