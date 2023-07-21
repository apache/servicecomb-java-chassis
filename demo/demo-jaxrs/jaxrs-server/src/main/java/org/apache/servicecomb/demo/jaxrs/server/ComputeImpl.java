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

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.demo.compute.Person;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

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
  @Parameters(value = {@Parameter(in = ParameterIn.QUERY, name = "a", schema = @Schema(implementation = int.class)),
      @Parameter(in = ParameterIn.QUERY, name = "b", schema = @Schema(implementation = int.class))})
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
      person = RestObjectMapperFactory.getRestObjectMapper()
          .readValue(jsonInput.getBytes(StandardCharsets.UTF_8), Map.class);
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
    StringBuilder result = new StringBuilder();
    for (String x : s) {
      result.append(x);
    }
    return result.toString();
  }
}
