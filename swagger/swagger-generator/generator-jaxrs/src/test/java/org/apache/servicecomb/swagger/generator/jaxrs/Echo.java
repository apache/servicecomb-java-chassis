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

package org.apache.servicecomb.swagger.generator.jaxrs;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.servicecomb.swagger.extend.annotations.RawJsonRequestBody;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;

@Path(value = "Echo")
public class Echo {
  @POST
  @ApiResponse(response = int.class, code = 200, message = "")
  public Response response() {
    return null;
  }

  @GET
  public Response invalidResponse() {
    return null;
  }

  @POST
  @Produces("")
  @Consumes("")
  @ApiOperation(value = "")
  public void emptyPath() {

  }

  @Path(value = "echo/{targetName}")
  @Consumes(value = {"json", "xml"})
  @Produces(value = {"json", "xml"})
  @POST
  public String echo(User srcUser, @HeaderParam(value = "header") String header,
      @PathParam(value = "targetName") String targetName,
      @QueryParam(value = "word") String word) {
    return String.format("%s %s %s %s", srcUser.name, header, targetName, word);
  }

  @Path(value = "cookie")
  @POST
  public String cookie(@CookieParam(value = "cookie") String cookie) {
    return String.format("%s", cookie);
  }

  @Path(value = "form")
  @POST
  public String form(@FormParam(value = "form") String form) {
    return String.format("%s", form);
  }

  @Path(value = "query")
  @GET
  public String query(@QueryParam(value = "query") String query) {
    return String.format("%s", query);
  }

  @Path(value = "query")
  @GET
  public String queryComplex(@QueryParam(value = "querys") List<User> querys) {
    return String.format("%s", querys);
  }

  @ApiOperation(value = "")
  public void ignoredNonRestful() {

  }

  @Path(value = "testRawJson")
  @POST
  public void rawJsonStringMethod(@RawJsonRequestBody String jsonInput) {
  }
}
