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

package org.apache.servicecomb.demo.jaxrs.server.multiErrorCode;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.demo.multiErrorCode.MultiRequest;
import org.apache.servicecomb.demo.multiErrorCode.MultiResponse200;
import org.apache.servicecomb.demo.multiErrorCode.MultiResponse400;
import org.apache.servicecomb.demo.multiErrorCode.MultiResponse500;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response.Status;

@RestSchema(schemaId = "MultiErrorCodeService")
@Path("MultiErrorCodeService")
public class MultiErrorCodeService {
  @Path("/errorCode")
  @POST
  @ApiResponses({
      @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = MultiResponse200.class)), description = ""),
      @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = MultiResponse400.class)), description = ""),
      @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = MultiResponse500.class)), description = "")})
  public MultiResponse200 errorCode(MultiRequest request) {
    if (request.getCode() == 400) {
      MultiResponse400 r = new MultiResponse400();
      r.setCode(400);
      r.setMessage("bad request");
      throw new InvocationException(jakarta.ws.rs.core.Response.Status.BAD_REQUEST, r);
    } else if (request.getCode() == 500) {
      MultiResponse500 r = new MultiResponse500();
      r.setCode(500);
      r.setMessage("internal error");
      throw new InvocationException(jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR, r);
    } else {
      MultiResponse200 r = new MultiResponse200();
      r.setCode(200);
      r.setMessage("success result");
      return r;
    }
  }

  @Path("/errorCodeWithHeader")
  @POST
  @ApiResponses({
      @ApiResponse(responseCode = "200", headers = {
          @Header(name = "x-code", schema = @Schema(implementation = String.class))},
          content = @Content(schema = @Schema(implementation = MultiResponse200.class)), description = ""),
      @ApiResponse(responseCode = "400", headers = {
          @Header(name = "x-code", schema = @Schema(implementation = String.class))},
          content = @Content(schema = @Schema(implementation = MultiResponse400.class)), description = ""),
      @ApiResponse(responseCode = "500", headers = {
          @Header(name = "x-code", schema = @Schema(implementation = String.class))},
          content = @Content(schema = @Schema(implementation = MultiResponse500.class)), description = "")})
  public Response errorCodeWithHeader(MultiRequest request) {
    Response response = new Response();
    if (request.getCode() == 400) {
      MultiResponse400 r = new MultiResponse400();
      r.setCode(400);
      r.setMessage("bad request");
      response.setStatus(Status.BAD_REQUEST);
      // If got many types for different status code, we can only using InvocationException for failed error code like 400-500.
      // The result for Failed Family(e.g. 400-500), can not set return value as target type directly or will give exception.
      response.setResult(new InvocationException(Status.BAD_REQUEST, r));
      response.setHeader("x-code", "400");
    } else if (request.getCode() == 500) {
      MultiResponse500 r = new MultiResponse500();
      r.setCode(500);
      r.setMessage("internal error");
      response.setStatus(Status.INTERNAL_SERVER_ERROR);
      response.setResult(new InvocationException(Status.INTERNAL_SERVER_ERROR, r));
      response.setHeader("x-code", "500");
    } else {
      MultiResponse200 r = new MultiResponse200();
      r.setCode(200);
      r.setMessage("success result");
      response.setStatus(Status.OK);
      // If error code is OK family(like 200), we can use the target type.
      response.setResult(r);
      response.setHeader("x-code", "200");
    }
    return response;
  }

  // using JAX-RS providers, users need to add dependencies for implementations, see pom for an example.
  @Path("/errorCodeWithHeaderJAXRS")
  @POST
  @ApiResponses({
      @ApiResponse(responseCode = "200", headers = {
          @Header(name = "x-code", schema = @Schema(implementation = String.class))},
          content = @Content(schema = @Schema(implementation = MultiResponse200.class)), description = ""),
      @ApiResponse(responseCode = "400", headers = {
          @Header(name = "x-code", schema = @Schema(implementation = String.class))},
          content = @Content(schema = @Schema(implementation = MultiResponse400.class)), description = ""),
      @ApiResponse(responseCode = "500", headers = {
          @Header(name = "x-code", schema = @Schema(implementation = String.class))},
          content = @Content(schema = @Schema(implementation = MultiResponse500.class)), description = "")})
  public jakarta.ws.rs.core.Response errorCodeWithHeaderJAXRS(MultiRequest request) {
    jakarta.ws.rs.core.Response response;
    if (request.getCode() == 400) {
      MultiResponse400 r = new MultiResponse400();
      r.setCode(request.getCode());
      r.setMessage(request.getMessage());
      // If got many types for different status code, we can only using InvocationException for failed error code like 400-500.
      // The result for Failed Family(e.g. 400-500), can not set return value as target type directly or will give exception.
      response = jakarta.ws.rs.core.Response.status(Status.BAD_REQUEST)
          .entity(new InvocationException(Status.BAD_REQUEST, r))
          .header("x-code", "400")
          .build();
    } else if (request.getCode() == 500) {
      MultiResponse500 r = new MultiResponse500();
      r.setCode(request.getCode());
      r.setMessage(request.getMessage());
      response = jakarta.ws.rs.core.Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(new InvocationException(Status.INTERNAL_SERVER_ERROR, r))
          .header("x-code", "500")
          .build();
    } else {
      MultiResponse200 r = new MultiResponse200();
      r.setCode(request.getCode());
      r.setMessage(request.getMessage());
      // If error code is OK family(like 200), we can use the target type.
      response = jakarta.ws.rs.core.Response.status(Status.OK)
          .entity(r)
          .header("x-code", "200")
          .build();
    }
    return response;
  }

  @Path("/noClientErrorCode")
  @POST
  @ApiResponses({
      @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = NoClientErrorCode400.class)), description = "")})
  public List<NoClientErrorCode200> noClientErrorCode(MultiRequest request) {
    if (request.getCode() == 400) {
      NoClientErrorCode400 r = new NoClientErrorCode400();
      r.setCode(request.getCode());
      r.setMessage(request.getMessage());
      r.setT400(400);
      throw new InvocationException(Status.BAD_REQUEST, r);
    } else {
      NoClientErrorCode200 r = new NoClientErrorCode200();
      r.setCode(request.getCode());
      r.setMessage(request.getMessage());
      r.setT200(200);
      List<NoClientErrorCode200> result = new ArrayList<>();
      result.add(r);
      return result;
    }
  }
}
