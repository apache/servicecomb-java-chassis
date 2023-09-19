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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.demo.compute.Person;
import org.apache.servicecomb.demo.ignore.InputModelForTestIgnore;
import org.apache.servicecomb.demo.ignore.OutputModelForTestIgnore;
import org.apache.servicecomb.demo.server.User;
import org.apache.servicecomb.foundation.common.part.FilePart;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.swagger.extend.annotations.RawJsonRequestBody;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.vertx.core.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

@RestSchema(schemaId = "codeFirst")
@Path("/codeFirstJaxrs")
@Produces(MediaType.APPLICATION_JSON)
public class CodeFirstJaxrs {
  @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)), description = "",
      headers = {@Header(name = "h1", schema = @Schema(implementation = String.class)),
          @Header(name = "h2", schema = @Schema(implementation = String.class))})
  @Path("/cseResponse")
  @GET
  public Response cseResponse(InvocationContext c1) {
    Response response = Response.createSuccess(Status.ACCEPTED, new User());
    response.setHeader("h1", "h1v " + c1.getContext().get(CoreConst.SRC_MICROSERVICE));

    InvocationContext c2 = ContextUtils.getInvocationContext();
    response.setHeader("h2", "h2v " + c2.getContext().get(CoreConst.SRC_MICROSERVICE));

    return response;
  }

  @Path("/testUserMap")
  @POST
  public Map<String, User> testUserMap(Map<String, User> userMap) {
    return userMap;
  }

  @Path("/textPlain")
  @POST
  @Consumes(MediaType.TEXT_PLAIN)
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

  @Path("/reduce")
  @GET
  @Parameters({@Parameter(name = "a", schema = @Schema(implementation = int.class), in = ParameterIn.QUERY)})
  public int reduce(HttpServletRequest request, @CookieParam("b") int b) {
    int a = Integer.parseInt(request.getParameter("a"));
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

  @Path("/addstring")
  @DELETE
  @Produces(MediaType.TEXT_PLAIN)
  public String addString(@QueryParam("s") List<String> s) {
    StringBuilder result = new StringBuilder();
    for (String x : s) {
      result.append(x);
    }
    return result.toString();
  }

  @Path("/ignore")
  @POST
  public OutputModelForTestIgnore testModelWithIgnoreField(InputModelForTestIgnore input) {
    return new OutputModelForTestIgnore("output_id", input.getInputId(), input.getContent(), input.getInputObject(),
        input.getInputJsonObject(), input.getInputIgnoreInterface(),
        new Person("outputSomeone"), new JsonObject("{\"OutputJsonKey\" : \"OutputJsonValue\"}"), () -> {
    });
  }

  @SuppressWarnings("unchecked")
  @Path("/rawJsonAnnotation")
  @POST
  public String testRawJsonAnnotation(@RawJsonRequestBody String jsonInput) {
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

  @Path("/traceId")
  @GET
  public String getTraceId() {
    return ContextUtils.getInvocationContext().getContext(CoreConst.TRACE_ID_NAME);
  }

  @GET
  @Path("/responseLong")
  @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Object.class)), description = "")
  public Response responseLong() {
    return Response.createSuccess(Long.MAX_VALUE);
  }

  @Path("/download/testDeleteAfterFinished")
  @GET
  public Part testDeleteAfterFinished(@QueryParam("name") String name, @QueryParam("content") String content)
      throws IOException {
    File file = createTempFile(name, content);

    return new FilePart(null, file)
        .setDeleteAfterFinished(true)
        .setSubmittedFileName(name);
  }

  @Path("/instanceIsolationTest")
  @GET
  public String instanceIsolationTest() {
    throw new InvocationException(503, "", "business");
  }

  private File createTempFile(String name, String content) throws IOException {
    File systemTempFile = new File(System.getProperty("java.io.tmpdir"));
    File file = new File(systemTempFile, name);
    FileUtils.write(file, content, StandardCharsets.UTF_8, false);
    return file;
  }
}
