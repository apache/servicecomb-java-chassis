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

package org.apache.servicecomb.demo.jaxrs.server;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.common.rest.codec.RestObjectMapper;
import org.apache.servicecomb.demo.compute.Person;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestSchema(schemaId = "compute")
@Path("/compute")
@Produces(MediaType.APPLICATION_JSON)
public class ComputeImpl {
  private static final Logger LOGGER = LoggerFactory.getLogger(ComputeImpl.class);

  @Path("/add")
  @POST
  public int add(@FormParam("a") int a, @FormParam("b") int b) {
    return a + b;
  }

  @Path("/reduce")
  @GET
  public int reduce(@Context HttpServletRequest request) {
    int a = Integer.parseInt(request.getParameter("a"));
    int b = Integer.parseInt(request.getParameter("b"));
    return a - b;
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

  @Path("/sayhi/{name}")
  @PUT
  public void sayHi(@PathParam("name") String name) {
    LOGGER.info(name + " sayhi");
    ContextUtils.getInvocationContext().setStatus(202);
  }

  @Path("/sayhi/{name}/v2")
  @PUT
  public void sayHi2(@PathParam("name") String name) {
    LOGGER.info(name + " sayhi 2");
  }

  @Path("/sayhei")
  @DELETE
  public void sayHei(@QueryParam("name") String name) {
    LOGGER.info(name + " sayhei");
  }

  @Path("/istrue")
  @GET
  public boolean isTrue() {
    return true;
  }

  @Path("/addstring")
  @DELETE
  @Produces(MediaType.TEXT_PLAIN)
  public String addString(@QueryParam("s") String[] s) {
    String result = "";
    for (String x : s) {
      result += x;
    }
    return result;
  }
}
