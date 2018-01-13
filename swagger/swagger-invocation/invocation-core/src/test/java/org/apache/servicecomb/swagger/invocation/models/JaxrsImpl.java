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

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.swagger.invocation.context.InvocationContext;

@Path("/JaxrsImpl")
@Produces(MediaType.APPLICATION_JSON)
public class JaxrsImpl {
  @Path("/testTwoSimple")
  @GET
  public int testSimple(@PathParam("a") int a, @QueryParam("b") int b, @HeaderParam("c") int c) {
    return a - b - c;
  }

  @Path("/testObject")
  @POST
  public Person testObject(Person user) {
    user.setName("hello " + user.getName());
    return user;
  }

  @Path("/testSimpleAndObject")
  @POST
  public String testSimpleAndObject(@CookieParam("prefix") String prefix, Person user) {
    return prefix + " " + user.getName();
  }

  @Path("/testContext")
  @POST
  public String testContext(InvocationContext context, @FormParam("form") String name) {
    context.addContext("name", name);
    return name + " sayhi";
  }

  @Path("/bytes")
  @POST
  public byte[] testBytes(byte[] input) {
    return input;
  }

  @Path("/testArrayArray")
  @POST
  public String[] testArrayArray(String[] s) {
    return s;
  }

  @Path("/testArrayList")
  @POST
  public List<String> testArrayList(String[] s) {
    return Arrays.asList(s);
  }

  @Path("/testListArray")
  @POST
  public String[] testListArray(List<String> s) {
    return s.toArray(new String[s.size()]);
  }

  @Path("/testListList")
  @POST
  public List<String> testListList(List<String> s) {
    return s;
  }

  @Path("/testObjectArrayArray")
  @POST
  public Person[] testObjectArrayArray(Person[] s) {
    return s;
  }

  @Path("/testObjectArrayList")
  @POST
  public List<Person> testObjectArrayList(Person[] s) {
    return Arrays.asList(s);
  }

  @Path("/testObjectListArray")
  @POST
  public Person[] testObjectListArray(List<Person> s) {
    return s.toArray(new Person[s.size()]);
  }

  @Path("/testObjectListList")
  @POST
  public List<Person> testObjectListList(List<Person> s) {
    return s;
  }
}
