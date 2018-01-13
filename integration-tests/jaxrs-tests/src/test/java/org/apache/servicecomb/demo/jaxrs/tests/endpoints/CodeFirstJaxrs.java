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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.demo.server.User;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.swagger.extend.annotations.ResponseHeaders;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.apache.servicecomb.swagger.invocation.response.Headers;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ResponseHeader;

@RestSchema(schemaId = "codeFirst")
@Path("/codeFirstJaxrs")
public class CodeFirstJaxrs extends SomeAbstractJaxrsRestEndpoint {

  @ApiResponse(code = 200, response = User.class, message = "")
  @ResponseHeaders({@ResponseHeader(name = "h1", response = String.class),
      @ResponseHeader(name = "h2", response = String.class)})
  @Path("/response")
  @GET
  public Response response(InvocationContext c1) {
    Response response = Response.createSuccess(Status.ACCEPTED, new User());
    Headers headers = response.getHeaders();
    headers.addHeader("h1", "h1v " + c1.getContext().toString());

    InvocationContext c2 = ContextUtils.getInvocationContext();
    headers.addHeader("h2", "h2v " + c2.getContext().toString());

    return response;
  }

  @Path("/reduce")
  @GET
  @ApiImplicitParams({
      @ApiImplicitParam(name = "a", dataType = "integer", format = "int32", paramType = "query")})
  public int reduce(HttpServletRequest request, @CookieParam("b") int b) {
    int a = Integer.parseInt(request.getParameter("a"));
    return a - b;
  }

  @Path("/addstring")
  @DELETE
  @Produces(TEXT_PLAIN)
  public String addString(@QueryParam("s") List<String> s) {
    String result = "";
    for (String x : s) {
      result += x;
    }
    return result;
  }
}
